package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmDbService {

    private final FilmDbStorage filmDbStorage;

    public Collection<FilmDto> getAll() {
        return filmDbStorage.getAll()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilm(Long filmId) {
        return filmDbStorage.getFilm(filmId).map(FilmMapper::mapToFilmDto).orElseThrow(() -> new NotFoundException("Фильм с ID " +
                filmId + " не найден."));
    }

    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);

        film = filmDbStorage.create(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Film updateFilm = FilmMapper.updateFilmFields(new Film(), request);
        if (!filmDbStorage.getFilm(updateFilm.getId()).isPresent()) {
            throw new NotFoundException("Фильма с id " + updateFilm.getId() + "не найден.");
        }
        updateFilm = filmDbStorage.update(updateFilm);
        return FilmMapper.mapToFilmDto(updateFilm);
    }

    public FilmDto deleteFilm(Long filmId) {
        Film film = filmDbStorage.getFilm(filmId).orElseThrow(() -> new NotFoundException("Фильма с id " + filmId + " нет."));
        filmDbStorage.deleteFilm(filmId);
        return FilmMapper.mapToFilmDto(film);
    }

    public void addLike(Long filmId, Long userId) {
        filmDbStorage.addLike(filmId, userId);
        log.info("Пользователи с id " + userId + " поставил лайк фильму с id " + filmId + " .");
    }

    public void deleteLike(Long filmId, Long userId) {
        filmDbStorage.deleteLike(filmId, userId);
        log.info("Пользователи с id " + userId + " удалил лайк фильму с id " + filmId + " .");

    }
    public Collection<Film> getPopularFilms(Integer count) {
        return filmDbStorage.getPopularFilms(count);
    }
}