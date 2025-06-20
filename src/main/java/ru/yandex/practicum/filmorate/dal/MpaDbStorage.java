package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private static final String FIND_ALL_QUERY = "SELECT id, name FROM mpa";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM mpa WHERE id = ?";
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    // Получает все рейтинги из таблицы mpa
    @Override
    public List<Mpa> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    // Получает рейтинг по его ID
    @Override
    public Optional<Mpa> getMpa(Integer mpaId) {
        try {
            Mpa mpa = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, mpaId);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}