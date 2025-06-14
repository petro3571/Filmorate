package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Qualifier("genreDbStorage")
public class GenreDbStorage implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT id, name FROM genre";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM genre WHERE id = ?";
    private static final String FIND_FILM_GENRES = "SELECT g.id AS id, g.name AS name " +
            "FROM film_genre fg JOIN genre g ON fg.genre_id = g.id " +
            "WHERE fg.film_id = ? GROUP BY id ORDER BY id";
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    @Override
    public List<Genre> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Optional<Genre> getGenre(Integer genreId) {
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, genreId);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public TreeSet<Genre> getFilmGenres(Long filmId) {
        return jdbc.query(FIND_FILM_GENRES, mapper, filmId).stream().sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Map<Long, TreeSet<Genre>> getGenresForFilms(List<Long> filmIds) {
        String sql = "SELECT fg.film_id, g.id, g.name FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + String.join(",", Collections.nCopies(filmIds.size(), "?")) + ")";

        return jdbc.query(sql, filmIds.toArray(), rs -> {
            Map<Long, TreeSet<Genre>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = new Genre();
                genre.setId(rs.getInt("id"));
                genre.setName(rs.getString("name"));
                result.computeIfAbsent(filmId, k -> new TreeSet<>()).add(genre);
            }
            return result;
        });
    }
}