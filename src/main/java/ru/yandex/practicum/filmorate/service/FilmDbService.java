package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmDbService {

    private final FilmStorage filmDbStorage;
    private final GenreStorage genreDbStorage;
    private final MpaStorage mpaDbStorage;
    private final UserStorage userDbStorage;

    public Collection<FilmDto> getAll() {
        return filmDbStorage.getAll()
                .stream()
                .map(f -> this.getFilm(f.getId()))
                .collect(Collectors.toList());
    }

    public FilmDto getFilm(Long filmId) {
        FilmDto filmDto =  filmDbStorage.getFilm(filmId).map(FilmMapper::mapToFilmDto).orElseThrow(() -> new NotFoundException("Фильм с ID " +
                filmId + " не найден."));

        filmDto.setGenres(genreDbStorage.getFilmGenres(filmId));

        return filmDto;
    }

    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);

        if (!(mpaDbStorage.getMpa(film.getMpa().getId()).isPresent())) {
            throw new NotFoundException("Рейтинга с id " + film.getMpa().getId() + " нет.");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreDbStorage.getGenre(genre.getId()).isPresent()) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " нет.");
                }
            }
        }

        film = filmDbStorage.create(film);

        film.setGenres(genreDbStorage.getFilmGenres(film.getId()));

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Film updateFilm = FilmMapper.updateFilmFields(new Film(), request);
        if (!filmDbStorage.getFilm(updateFilm.getId()).isPresent()) {
            throw new NotFoundException("Фильма с id " + updateFilm.getId() + "не найден.");
        }

        if (!(mpaDbStorage.getMpa(updateFilm.getMpa().getId()).isPresent())) {
            throw new NotFoundException("Рейтинга с id " + updateFilm.getMpa().getId() + " нет.");
        }

        if (updateFilm.getGenres() != null && !updateFilm.getGenres().isEmpty()) {
            for (Genre genre : updateFilm.getGenres()) {
                if (!genreDbStorage.getGenre(genre.getId()).isPresent()) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " нет.");
                }
            }
        }

        updateFilm = filmDbStorage.update(updateFilm);

        updateFilm.setGenres(genreDbStorage.getFilmGenres(updateFilm.getId()));

        return FilmMapper.mapToFilmDto(updateFilm);
    }

    public FilmDto deleteFilm(Long filmId) {
        Film film = filmDbStorage.getFilm(filmId).orElseThrow(() -> new NotFoundException("Фильма с id " + filmId + " нет."));
        film.setGenres(genreDbStorage.getFilmGenres(film.getId()));
        filmDbStorage.deleteFilm(filmId);
        return FilmMapper.mapToFilmDto(film);
    }

    public void addLike(Long filmId, Long userId) {
        userDbStorage.existsUserById(userId);
        filmDbStorage.addLike(filmId, userId);
        log.info("Пользователи с id " + userId + " поставил лайк фильму с id " + filmId + " .");
    }

    public void deleteLike(Long filmId, Long userId) {
        userDbStorage.existsUserById(userId);
        filmDbStorage.deleteLike(filmId, userId);
        log.info("Пользователи с id " + userId + " удалил лайк фильму с id " + filmId + " .");

    }

    public Collection<Film> getPopularFilms(Integer count) {
        return filmDbStorage.getPopularFilms(count);
    }
}