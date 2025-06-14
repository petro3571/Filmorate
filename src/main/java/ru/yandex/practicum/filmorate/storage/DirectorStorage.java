package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getAll();

    Optional<Director> getDirector(Long id);

    Director create(Director director);

    Director update(Director director);

    void delete(Long id);

    Set<Director> getFilmDirectors(Long filmId);

    Map<Long, Set<Director>> getDirectorsForFilms(List<Long> filmIds);
}
