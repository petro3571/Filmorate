package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    List<Genre> getAll();

    Optional<Genre> getGenre(Integer genreId);

    Set<Genre> getFilmGenres(Long filmId);

    Map<Long, Set<Genre>> getGenresForFilms(List<Long> filmsId);
}