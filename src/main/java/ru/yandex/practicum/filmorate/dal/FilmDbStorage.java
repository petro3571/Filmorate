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
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private static final String INSERT_QUERY = "INSERT INTO films(title, description, release_date, duration, MPA_id, director) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String FIND_ALL_QUERY = "SELECT f.film_id, f.title, f.description, f.release_date, " +
            "f.duration, f.mpa_id, m.name, f.director FROM films AS f left JOIN mpa AS m ON f.MPA_id = m.id";

    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ?, director = ? WHERE film_id = ?";

    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";

    private static final String NEWFIND = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, " +
            "f.mpa_id, m.name, f.director FROM films AS f left JOIN mpa AS m ON f.MPA_id = m.id WHERE f.film_id = ?";

    private static final String LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES(?, ?)";

    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String POPULAR_QUERY = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
            "FROM films f LEFT JOIN likes l ON f.film_id = l.film_id JOIN mpa m ON f.mpa_id = m.id " +
            "GROUP BY f.film_id ORDER BY likes_count DESC LIMIT ?";

    private static final String SEARCH_QUERY = "SELECT f.*, m.name AS mpa_name FROM films f " +
            "LEFT JOIN mpa m ON f.mpa_id = m.id " +
            "WHERE LOWER(f.title) LIKE LOWER(?) OR LOWER(f.director) LIKE LOWER(?) " +
            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC";

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Override
    public Collection<Film> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Film create(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getDirector()
        );
        film.setId(id);
        saveGenres(film);
        return getFilm(id).orElseThrow(() -> new IllegalStateException("Не сохранен фильм с Id " + id));
    }

    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getDirector(),
                film.getId()
        );

        updateGenres(film);
        return getFilm(film.getId()).orElseThrow(() -> new IllegalStateException("Не удалось обновить фильм с Id " + film.getId()));
    }

    @Override
    public void deleteFilm(Long filmId) {
        delete(DELETE_QUERY, filmId);
    }

    @Override
    public Optional<Film> getFilm(Long filmId) {
        try {
            Film film = jdbc.queryForObject(NEWFIND, mapper, filmId);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        existFilmById(filmId);
        jdbc.update(LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        existFilmById(filmId);
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return jdbc.query(POPULAR_QUERY, mapper, count);
    }

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

    private void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    private boolean delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    private void existFilmById(Long filmId) {
        Film film = getFilm(filmId).orElseThrow(() -> new NotFoundException("Фильм с ID " +
                filmId + " не найден."));
    }

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

    private void updateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbc.update(deleteSql, film.getId());

        String sql = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        jdbc.batchUpdate(sql, batchArgs);
    }

    public Collection<Film> searchFilms(String query, String searchBy) {
        String searchParam = "%" + query.toLowerCase() + "%";

        if (searchBy.equals("title")) {
            return jdbc.query(
                    "SELECT f.*, m.name FROM films f " +
                            "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                            "WHERE LOWER(f.title) LIKE ? " +
                            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC",
                    mapper, searchParam);
        } else if (searchBy.equals("director")) {
            return jdbc.query(
                    "SELECT f.*, m.name FROM films f " +
                            "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                            "WHERE LOWER(f.director) LIKE ? " +
                            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC",
                    mapper, searchParam);
        } else {
            return jdbc.query(
                    "SELECT f.*, m.name FROM films f " +
                            "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                            "WHERE LOWER(f.title) LIKE ? OR LOWER(f.director) LIKE ? " +
                            "ORDER BY (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id) DESC",
                    mapper, searchParam, searchParam);
        }

    }
}