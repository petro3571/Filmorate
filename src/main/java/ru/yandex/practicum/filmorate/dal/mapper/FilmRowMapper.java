package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("title"));
        film.setDescription(rs.getString("description"));

        Date releaseDate = rs.getDate("release_date");
        film.setReleaseDate(releaseDate != null ? releaseDate.toLocalDate() : null);

        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("name")));
        film.setGenres(new HashSet<>());
        film.setDirectors(new HashSet<>());
        return film;
    }
}