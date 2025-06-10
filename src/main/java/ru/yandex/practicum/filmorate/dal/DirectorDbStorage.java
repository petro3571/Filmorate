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
import java.util.Collection;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE director_id = ?";

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
}
