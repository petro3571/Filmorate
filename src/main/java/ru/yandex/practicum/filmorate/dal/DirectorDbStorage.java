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

    @Override
    public List<Director> getAll() {
        String sql = "SELECT director_id AS id, name FROM directors";
        return jdbc.query(sql, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            return director;
        });
    }

    @Override
    public Optional<Director> getDirector(Long id) {
        String sql = "SELECT director_id AS id, name FROM directors WHERE director_id = ?";
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

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        jdbc.update(sql, director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbc.update(sql, id);
    }

    @Override
    public Set<Director> getFilmDirectors(Long filmId) {
        String sql = "SELECT d.director_id AS id, d.name FROM film_director fd " +
                "JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id = ?";
        return new HashSet<>(jdbc.query(sql, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            return director;
        }, filmId));
    }

    @Override
    public Map<Long, Set<Director>> getDirectorsForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fd.film_id, d.director_id AS id, d.name FROM film_director fd " +
                "JOIN directors d ON fd.director_id = d.director_id " +
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
