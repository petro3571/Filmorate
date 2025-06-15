package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbc;

    // Получение всех режиссеров из БД
    @Override
    public List<Director> getAll() {
        String sql = "SELECT id, name FROM directors";
        return jdbc.query(sql, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            return director;
        });
    }

    // Получение режиссера по ID (возвращает Optional)
    @Override
    public Optional<Director> getDirector(Long id) {
        String sql = "SELECT id, name FROM directors WHERE id = ?";
        try {
            Director director = jdbc.queryForObject(sql, (rs, rowNum) -> {
                Director d = new Director();
                d.setId(rs.getLong("id"));
                d.setName(rs.getString("name"));
                return d;
            }, id);
            return Optional.ofNullable(director);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Создание нового режиссера с генерацией ID
    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return director;
    }

    // Обновление данных режиссера
    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        jdbc.update(sql, director.getName(), director.getId());
        return director;
    }

    // Удаление режиссера по ID
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbc.update(sql, id);
    }

    // Получение режиссеров для конкретного фильма
    @Override
    public Set<Director> getFilmDirectors(Long filmId) {
        String sql = "SELECT d.id AS id, d.name as name FROM film_director fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id = ?";
        return new HashSet<>(jdbc.query(sql, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            return director;
        }, filmId));
    }

    // Получение режиссеров для списка фильмов (оптимизированный запрос)
    @Override
    public Map<Long, Set<Director>> getDirectorsForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fd.film_id, d.id, d.name FROM film_director fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id IN (" + inSql + ")";

        return jdbc.query(sql, filmIds.toArray(), rs -> {
            Map<Long, Set<Director>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Director director = new Director();
                director.setId(rs.getLong("id"));
                director.setName(rs.getString("name"));
                result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            }
            return result;
        });
    }
}