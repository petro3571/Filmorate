package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Collection<Director> getAll();

    Director getDirector(Long id);

    Director create(Director director);

    Director update(Director director);

    void delete(Long id);
}