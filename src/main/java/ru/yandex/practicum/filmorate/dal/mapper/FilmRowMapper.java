package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final GenreDbStorage genreDbStorage;

    @Autowired
    public FilmRowMapper(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("title"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("name")));

        List<Genre> genres = new ArrayList<>(genreDbStorage.getFilmGenres(film.getId()));
        Set<Genre> genresWOD = new HashSet<>(genres);
        genres = new ArrayList<>(genresWOD);
        genres.sort(Comparator.comparingInt(Genre::getId));

        film.setGenres(new ArrayList<>(genres));
        return film;
    }
}