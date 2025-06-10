package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE director_id = ?";
    private static final String FIND_FILM_DIRECTORS = "SELECT d.director_id, d.name " +
            "FROM film_director fd " +
            "JOIN directors d ON fd.director_id = d.director_id " +
            "WHERE fd.film_id = ?";
    private static final String FIND_DIRECTORS_FOR_FILMS = "SELECT fd.film_id, d.director_id, d.name " +
            "FROM film_director fd " +
            "JOIN directors d ON fd.director_id = d.director_id " +
            "WHERE fd.film_id IN (%s)";

    private final JdbcTemplate jdbcTemplate;
    private final DirectorRowMapper directorRowMapper;


    @Override
    public Collection<Director> getAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, directorRowMapper);
    }

    @Override
    public Director getDirector(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, directorRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден.");
        }
    }

    @Override
    public Director create(Director director) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        director.setId(generatedId);
        return director;
    }

    @Override
    public Director update(Director director) {
        int updatedRows = jdbcTemplate.update(UPDATE_QUERY,
                director.getName(),
                director.getId());

        if (updatedRows == 0) {
            throw new NotFoundException("Режиссер с ID " + director.getId() + " не найден.");
        }

        return director;
    }

    @Override
    public void delete(Long id) {
        int deletedRows = jdbcTemplate.update(DELETE_QUERY, id);

        if (deletedRows == 0) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден.");
        }
    }

    @Override
    public Set<Director> getFilmDirectors(Long filmId) {
        return new HashSet<>(jdbcTemplate.query(FIND_FILM_DIRECTORS, directorRowMapper, filmId));
    }

    @Override
    public Map<Long, Set<Director>> getDirectorsForFilms(Collection<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(FIND_DIRECTORS_FOR_FILMS, inClause);

        return jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            Map<Long, Set<Director>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Director director = new Director();
                director.setId(rs.getLong("director_id"));
                director.setName(rs.getString("name"));
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            }
            return result;
        });
    }

    @Override
    public void updateFilmDirectors(Long filmId, Set<Director> directors) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", filmId);

        if (directors != null && !directors.isEmpty()) {
            List<Object[]> batchArgs = directors.stream()
                    .map(director -> new Object[]{filmId, director.getId()})
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate("INSERT INTO film_director (film_id, director_id) VALUES (?, ?)", batchArgs);
        }
    }
}
