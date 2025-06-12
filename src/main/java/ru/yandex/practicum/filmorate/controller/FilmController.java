package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmDbService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
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
    public FilmDto update(@RequestBody(required = false) UpdateFilmRequest film) {
        if (film == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Тело запроса не может быть пустым");
        }
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
    public Collection<Film> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") Integer count) {
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title,director") String by) {

        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр query не может быть пустым");
        }

        List<String> validByValues = List.of("title", "director");
        List<String> searchBy = Arrays.stream(by.split(","))
                .map(String::trim)
                .filter(validByValues::contains)
                .distinct()
                .collect(Collectors.toList());

        if (searchBy.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Параметр by должен содержать title или director");
        }

        return filmService.searchFilms(query, searchBy);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(@PathVariable Long directorId,
                                               @RequestParam(defaultValue = "likes") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}