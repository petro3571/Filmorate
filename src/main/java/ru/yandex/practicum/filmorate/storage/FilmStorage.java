package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getAll();

    Film create(Film film);

    Film update(Film film);

    void deleteFilm(Long filmId);

    Optional<Film> getFilm(Long filmId);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(Integer count);

    Collection<Film> searchFilms(String query, List<String> by);

    Collection<Film> getFilmsByDirector(Long directorId, String sortBy);

    Collection<Film> getRecommendations(Long userId);

    Collection<Film> getPopularFilmsByYear(Integer count, Integer year);

    Collection<Film> getPopularFilmsByGenre(Integer count, Integer genre);

    Collection<Film> getPopularFilmsByGenreAndYear(Integer count, Integer genre, Integer year);

    Collection<Film> getCommonFilms(Long userId, Long friendId);
}