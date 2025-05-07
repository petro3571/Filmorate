package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilm(Long filmId) {
        return filmStorage.getFilm(filmId);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film deleteFilm(Long filmId) {
        return filmStorage.deleteFilm(filmId);
    }

    public Set<Long> addLike(Long filmId, Long userId) {
        if (!userStorage.existsUserById(userId)) {
            throw new NotFoundException("Такого пользователя нет");
        }

        filmStorage.addLike(filmId, userId);
        Film film = filmStorage.getFilm(filmId);
        Set<Long> likes = film.getLike();
        return likes;
    }

    public Set<Long> deleteLike(Long filmId, Long userId) {
        if (!userStorage.existsUserById(userId)) {
            throw new NotFoundException("Такого пользователя нет");
        }

        filmStorage.deleteLike(filmId, userId);
        Film film = filmStorage.getFilm(filmId);
        Set<Long> likes = film.getLike();
        return likes;
    }

    public Collection<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }
}