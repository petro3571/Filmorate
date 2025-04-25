package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    private static final LocalDate START_FILM_DATA = LocalDate.of(1895,12,28);

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(START_FILM_DATA)) {
            log.warn("Дата релиза раньше 1895-12-28");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Нет id");
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());

            if (film.getReleaseDate().isBefore(START_FILM_DATA)) {
                log.warn("Дата релиза некорректна");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
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