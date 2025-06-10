package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface DirectorStorage {
    Collection<Director> getAll();

    Director getDirector(Long id);

    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

    Set<Director> getFilmDirectors(Long filmId);

    Map<Long, Set<Director>> getDirectorsForFilms(Collection<Long> filmIds);

    void updateFilmDirectors(Long filmId, Set<Director> directors);
}