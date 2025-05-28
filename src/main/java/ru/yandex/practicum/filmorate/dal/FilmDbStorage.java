package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private static final String INSERT_QUERY = "INSERT INTO films(title, description, release_date, duration, MPA_id)" +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_ALL_QUERY = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, f.mpa_id, m.name FROM films AS f left JOIN mpa AS m ON f.MPA_id = m.id";

    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";

    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";

    private static final String NEWFIND = "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, f.mpa_id, m.name FROM films AS f left JOIN mpa AS m ON f.MPA_id = m.id WHERE f.film_id =  ?";

    private static final String LIKE_QUERY ="INSERT INTO likes(film_id, user_id) VALUES(?, ?)";

    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String POPULAR_QUERY = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count FROM films f LEFT JOIN likes l ON f.film_id = l.film_id JOIN mpa m ON f.mpa_id = m.id GROUP BY f.film_id ORDER BY likes_count DESC LIMIT ?";

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;
    private final UserDbStorage userDbStorage;

    @Override
    public Collection<Film> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Film create(Film film) {
        if (!(mpaDbStorage.getMpa(film.getMpa().getId()).isPresent())) {
            throw new NotFoundException("Рейтинга с id " + film.getMpa().getId() + " нет.");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreDbStorage.getGenre(genre.getId()).isPresent()) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " нет.");
                }
            }
        }

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
        return getFilm(id).orElseThrow(() -> new IllegalStateException("Не сохранен фильм с Id " + id));
    }

    @Override
    public Film update(Film film) {
        if (!(mpaDbStorage.getMpa(film.getMpa().getId()).isPresent())) {
            throw new NotFoundException("Рейтинга с id " + film.getMpa().getId() + " нет.");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreDbStorage.getGenre(genre.getId()).isPresent()) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " нет.");
                }
            }
        }

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
        userDbStorage.existsUserById(userId);
        jdbc.update(LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        existFilmById(filmId);
        userDbStorage.existsUserById(userId);
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
            return ps;}, keyHolder);

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

        List<Genre> newList = film.getGenres();

        for (Genre genre : newList) {
            jdbc.update(sql,film.getId(), genre.getId());
        }
    }

    private void updateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbc.update(deleteSql, film.getId());

        String sql = "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";

        List<Genre> newList = film.getGenres();

        for (Genre genre : newList) {
            jdbc.update(sql,film.getId(), genre.getId());
        }
    }
}