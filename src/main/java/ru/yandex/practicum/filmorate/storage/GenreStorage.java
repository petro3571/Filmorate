package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {
    List<Genre> getAll();

    Optional<Genre> getGenre(Integer genreId);

    Set<Genre> getFilmGenres(Long filmId);

    Map<Long, TreeSet<Genre>> getGenresForFilms(List<Long> filmsId);
}