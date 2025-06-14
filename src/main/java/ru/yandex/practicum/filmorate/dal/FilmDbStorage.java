package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private static final String INSERT_QUERY = "INSERT INTO films(title, description, release_date, duration, MPA_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT f.film_id, f.title, f.description, f.release_date, " +
            "f.duration, f.mpa_id, m.name FROM films AS f LEFT JOIN mpa AS m ON f.MPA_id = m.id ";
    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String NEWFIND = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
            "f.mpa_id, m.name FROM films AS f LEFT JOIN mpa AS m ON f.MPA_id = m.id WHERE f.film_id = ?";
    private static final String LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES(?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String SEARCH_BY_TITLE_AND_DIRECTOR = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
            "f.mpa_id, m.name FROM films f " +
            "LEFT JOIN likes l ON f.film_id = l.film_id " +
            "JOIN mpa m ON f.mpa_id = m.id " +
            "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "WHERE (LOWER(f.title) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?)) " +
            "GROUP BY f.film_id, m.name " +
            "ORDER BY COUNT(l.user_id) DESC";

    private static final String SEARCH_BY_TITLE = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
            "f.mpa_id, m.name FROM films f " +
            "LEFT JOIN likes l ON f.film_id = l.film_id " +
            "JOIN mpa m ON f.mpa_id = m.id " +
            "WHERE LOWER(f.title) LIKE LOWER(?) " +
            "GROUP BY f.film_id, m.name " +
            "ORDER BY COUNT(l.user_id) DESC";

    private static final String SEARCH_BY_DIRECTOR = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
            "f.mpa_id, m.name FROM films f " +
            "LEFT JOIN likes l ON f.film_id = l.film_id " +
            "JOIN mpa m ON f.mpa_id = m.id " +
            "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
            "LEFT JOIN directors d ON fd.director_id = d.id " +
            "WHERE LOWER(d.name) LIKE LOWER(?) " +
            "GROUP BY f.film_id, m.name " +
            "ORDER BY COUNT(l.user_id) DESC";

    private static final String EXISTS_QUERY = "SELECT 1 FROM films f WHERE f.film_id = ?";

    private static final String RECOMMENDATION_QUERY = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa m ON " +
            "f.mpa_id = m.id WHERE f.film_id in (SELECT film_id FROM likes WHERE user_id in (SELECT user_id FROM likes" +
            " WHERE film_id in (SELECT film_id FROM likes WHERE user_id = ?) and user_id not in (?) GROUP BY user_id " +
            "limit 1)) and f.film_id not in (SELECT film_id FROM likes WHERE user_id = ?)";

    private static final String POPULAR_QUERY = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
            "FROM films f LEFT JOIN likes l ON f.film_id = l.film_id JOIN mpa m ON f.mpa_id = m.id " +
            "GROUP BY f.film_id ORDER BY likes_count DESC, f.film_id  LIMIT ?";

    private static final String POPULAR_QUERY_BY_YEAR = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
            "FROM films f LEFT JOIN likes l ON f.film_id = l.film_id JOIN mpa m ON f.mpa_id = m.id " +
            "WHERE extract(YEAR from CAST(release_date AS date)) = ?" +
            "GROUP BY f.film_id ORDER BY likes_count DESC, film_id LIMIT ?";

    private static final String POPULAR_QUERY_BY_GENRE = "SELECT f.*, m.name AS mpa_name," +
            " COUNT(l.user_id) AS likes_count FROM films f " +
            "LEFT JOIN likes l ON f.film_id = l.film_id JOIN mpa m ON f.mpa_id = m.id " +
            "LEFT JOIN film_genre fg on f.film_id = fg.film_id " +
            "WHERE fg.genre_id = ?" +
            "GROUP BY f.film_id ORDER BY likes_count DESC, f.film_id LIMIT ?";

    private static final String POPULAR_QUERY_BY_GENRE_AND_YEAR = "SELECT f.*, m.name AS mpa_name," +
            " COUNT(l.user_id) AS likes_count FROM films f " +
            "LEFT JOIN likes l ON f.film_id = l.film_id JOIN mpa m ON f.mpa_id = m.id " +
            "LEFT JOIN film_genre fg on f.film_id = fg.film_id " +
            "WHERE fg.genre_id = ? AND EXTRACT(YEAR from CAST(release_date AS date)) = ?" +
            "GROUP BY f.film_id ORDER BY likes_count DESC, f.film_id LIMIT ?";

    private static final String COMMON_FILMS = "SELECT f.*, m.name AS mpa_name, count(l.user_id) AS likes_count FROM films f " +
            "LEFT JOIN likes l on f.film_id = l.film_id JOIN mpa m ON f.mpa_id = m.id " +
            "WHERE EXISTS (SELECT 1 FROM likes ul WHERE f.film_id = ul.film_id AND ul.user_id = ?) " +
            "AND EXISTS (SELECT 1 FROM likes ul1 WHERE f.film_id = ul1.film_id AND ul1.user_id = ?) " +
            "GROUP BY f.film_id " +
            "ORDER BY likes_count DESC, f.film_id";

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    // Получение всех фильмов из БД
    @Override
    public Collection<Film> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    // Создание нового фильма с сохранением в БД
    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        saveGenres(film);
        saveDirectors(film);
        return getFilm(id).orElseThrow(() -> new IllegalStateException("Не сохранен фильм с Id " + id));
    }

    // Обновление данных фильма
    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        updateGenres(film);
        updateDirectors(film);
        return getFilm(film.getId()).orElseThrow(() -> new IllegalStateException("Не удалось обновить фильм с Id " + film.getId()));
    }

    // Удаление фильма по ID
    @Override
    public void deleteFilm(Long filmId) {
        delete(DELETE_QUERY, filmId);
    }

    // Получение фильма по ID
    @Override
    public Optional<Film> getFilm(Long filmId) {
        try {
            Film film = jdbc.queryForObject(NEWFIND, mapper, filmId);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Добавление лайка фильму от пользователя
    @Override
    public void addLike(Long filmId, Long userId) {
        existFilmById(filmId);
        jdbc.update(LIKE_QUERY, filmId, userId);

        String feedquery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) VALUES (?,?,?,?,?)";
        update(feedquery, userId, Instant.now().toEpochMilli(), filmId, 1, 2);
    }

    // Удаление лайка у фильма
    @Override
    public void deleteLike(Long filmId, Long userId) {
        existFilmById(filmId);

        String feedQuery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) VALUES (?,?,?,?,?)";
        update(feedQuery, userId, Instant.now().toEpochMilli(), filmId, 1, 1);

        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    // Получение списка популярных фильмов
    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return jdbc.query(POPULAR_QUERY, mapper, count);
    }

    // Проверка существования фильма
    public boolean exists(Long filmId) {
        try {
            Integer result = jdbc.queryForObject(
                    EXISTS_QUERY,
                    Integer.class,
                    filmId
            );
            return result != 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    // Поиск фильмов по названию и/или режиссеру
    @Override
    public Collection<Film> searchFilms(String query, List<String> by) {
        String searchPattern = "%" + query.toLowerCase() + "%";

        if (by == null || by.isEmpty()) {
            by = List.of("title");
        }

        if (by.contains("title") && by.contains("director")) {
            return jdbc.query(SEARCH_BY_TITLE_AND_DIRECTOR, mapper, searchPattern, searchPattern);
        } else if (by.contains("title")) {
            return jdbc.query(SEARCH_BY_TITLE, mapper, searchPattern);
        } else if (by.contains("director")) {
            return jdbc.query(SEARCH_BY_DIRECTOR, mapper, searchPattern);
        }

        return Collections.emptyList();
    }

    // Получение фильмов режиссера с сортировкой по году или популярности
    @Override
    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String sql;
        if ("year".equals(sortBy)) {
            sql = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
                    "f.mpa_id, m.name FROM films f " +
                    "JOIN film_director fd ON f.film_id = fd.film_id " +
                    "JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
        } else {
            sql = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
                    "f.mpa_id, m.name FROM films f " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "JOIN film_director fd ON f.film_id = fd.film_id " +
                    "JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id, m.name " +
                    "ORDER BY COUNT(l.user_id) DESC";
        }

        return jdbc.query(sql, mapper, directorId);
    }

    @Override
    public Collection<Film> getPopularFilmsByYear(Integer count, Integer year) {
        return jdbc.query(POPULAR_QUERY_BY_YEAR, mapper, year, count);
    }

    @Override
    public Collection<Film> getPopularFilmsByGenre(Integer count, Integer genre) {
        return jdbc.query(POPULAR_QUERY_BY_GENRE, mapper, genre, count);
    }

    @Override
    public Collection<Film> getPopularFilmsByGenreAndYear(Integer count, Integer genre, Integer year) {
        return jdbc.query(POPULAR_QUERY_BY_GENRE_AND_YEAR, mapper, genre, year, count);
    }

    // Получение общих фильмов двух пользователей
    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbc.query(COMMON_FILMS, mapper, userId, friendId);
    }

    // Вставка данных с возвратом сгенерированного ID
    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    // Обновление данных
    private void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    // Удаление данных
    private void delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
    }

    // Проверка существования фильма по ID
    private void existFilmById(Long filmId) {
        Film film = getFilm(filmId).orElseThrow(() -> new NotFoundException("Фильм с ID " +
                filmId + " не найден."));
    }

    // Сохранение жанров фильма
    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());
        jdbc.batchUpdate(sql, batchArgs);
    }

    // Обновление жанров фильма
    private void updateGenres(Film film) {
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbc.update(deleteSql, film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());
        jdbc.batchUpdate(sql, batchArgs);
    }

    // Сохранение режиссеров фильма
    private void saveDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_director(film_id, director_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getDirectors().stream()
                .map(director -> new Object[]{film.getId(), director.getId()})
                .collect(Collectors.toList());
        jdbc.batchUpdate(sql, batchArgs);
    }

    // Обновление режиссеров фильма
    private void updateDirectors(Film film) {
        String deleteSql = "DELETE FROM film_director WHERE film_id = ?";
        jdbc.update(deleteSql, film.getId());
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_director(film_id, director_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getDirectors().stream()
                .map(director -> new Object[]{film.getId(), director.getId()})
                .collect(Collectors.toList());
        jdbc.batchUpdate(sql, batchArgs);
    }

    // Получение рекомендаций фильмов для пользователя
    @Override
    public Collection<Film> getRecommendations(Long userId) {
        return jdbc.query(RECOMMENDATION_QUERY, mapper, userId, userId, userId);
    }
}