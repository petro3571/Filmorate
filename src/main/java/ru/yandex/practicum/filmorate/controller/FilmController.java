package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (film.getName().isEmpty()) {
            log.warn("Пустое название");
            throw new ValidationException("Название не может быть пустым.");
        }

        if (film.getDescription().length() > 200) {
            log.warn("Длина превысила 200 знаков");
            throw new ValidationException("Максимальная длина описания 200.");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза раньше 1895-12-28");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() < 0) {
            log.warn("Продолжительность нулевая или отрицательная");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;

    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Нет id");
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());
            if (film.getName() == null) {
                log.warn("Пустое название фильма");
                throw new ValidationException("Название не может быть пустым.");
            }

            if (film.getDescription().length() > 200) {
                log.warn("Превышена длина описания");
                throw new ValidationException("Максимальная длина описания 200.");
            }

            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.warn("Дата релиза некорректна");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }

            if (film.getDuration() < 0) {
                log.warn("продолжительность фильма отрицательная или равна нулю");
                throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
            }

            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());

            return oldFilm;
        }
        log.warn("Такого фильма нет");
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден!");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}