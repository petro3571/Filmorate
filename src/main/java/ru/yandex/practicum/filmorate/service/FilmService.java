package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DateFilmValidationException;
import ru.yandex.practicum.filmorate.exception.IdValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    private static final LocalDate START_FILM_DATA = LocalDate.of(1895,12,28);


    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilm(Long filmId) {
        if (filmId == null) {
            log.warn("Нет id");
            throw new IdValidationException("Id должен быть указан");
        }
        return filmStorage.getFilm(filmId);
    }

    public Film create(Film film) {
        validate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            log.warn("Нет id");
            throw new IdValidationException("Id должен быть указан");
        }

        validate(film);
        return filmStorage.update(film);
    }

    public Film deleteFilm(Long filmId) {
        return filmStorage.deleteFilm(filmId);
    }

    public Set<Long> addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
        Film film = filmStorage.getFilm(filmId);
        Set<Long> likes = film.getLike();
        return likes;
    }

    public Set<Long> deleteLike(Long filmId, Long userId) {
        filmStorage.deleteLike(filmId, userId);
        Film film = filmStorage.getFilm(filmId);
        Set<Long> likes = film.getLike();
        return likes;
    }

    public Collection<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    private void validate(Film film) {
        if (film.getReleaseDate().isBefore(START_FILM_DATA)) {
//            log.warn("Дата релиза раньше 1895-12-28");
            throw new DateFilmValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }
}