package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Qualifier("genreDbStorage")
public class GenreDbStorage {
    private static final String FIND_ALL_QUERY = "SELECT id, name FROM genre";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM genre WHERE id = ?";
    private static final String FIND_FILM_GENRES = "SELECT genre_id from film_genre where film_id = ?";
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    public List<Genre> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public Optional<Genre> getGenre(Integer genreId) {
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, genreId);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Genre> getFilmGenres(Long filmId) {
        List<Optional<Genre>> newlist = jdbc.queryForList(FIND_FILM_GENRES, Integer.class, filmId).stream().map(g -> getGenre(g)).collect(Collectors.toList());
        List<Genre> listGenres = new ArrayList<>();
        for (Optional<Genre> g : newlist) {
            if (g.isPresent()) {
                listGenres.add(g.get());
            }
        }
        return listGenres;
    }
}