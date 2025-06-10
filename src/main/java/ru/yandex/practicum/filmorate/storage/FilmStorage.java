package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    Collection<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    void deleteFilm(Long filmId);

    Optional<Film> getFilm(Long filmId);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(Integer count);

    Collection<Film> searchFilms(String query, String searchBy);

    Set<Director> getFilmDirectors(Long filmId);

    Collection<Film> searchByTitle(String query);

    Collection<Film> searchByDirector(String query);

    Collection<Film> searchByTitleAndDirector(String query);
}