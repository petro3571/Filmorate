package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmDbService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Validated
public class FilmController {

    private final FilmDbService filmService;

    @GetMapping
    public Collection<FilmDto> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{filmId}")
    public FilmDto getFilm(@PathVariable("filmId") Long filmId) {
        return filmService.getFilm(filmId);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest film) {
        return filmService.create(film);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest film) {
        return filmService.update(film);
    }

    @DeleteMapping("/{filmId}")
    public FilmDto delete(@PathVariable("filmId") Long filmId) {
        return filmService.deleteFilm(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") Integer count,
                                               @RequestParam(required = false) @Positive Integer genreId,
                                               @RequestParam(required = false) @Positive Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }
}