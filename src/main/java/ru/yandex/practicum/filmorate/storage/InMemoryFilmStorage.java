package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DataAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(Long filmId) {
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден!");
        }
        return films.get(filmId);
    }

    @Override
    public Film create(Film film) {
        if (films.containsKey(film.getId())) {
            throw new DataAlreadyExistException("Фильм уже создан.");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (films.containsKey(film.getId())) {
            Film oldFilm = films.get(film.getId());

            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());

            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден!");
    }

    @Override
    public Film deleteFilm(Long filmId) {
        if (films.containsKey(filmId)) {
            return films.remove(filmId);
        }
        throw new NotFoundException("Фильм с id = " + filmId + " не найден!");
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = getFilm(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден!");
        }

        if (!film.getLike().contains(userId)) {
            film.getLike().add(userId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = getFilm(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден!");
        }

            film.getLike().remove(userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return films.values().stream().sorted((f1, f2) -> f2.getLike().size() - f1.getLike().size())
        .limit(count).collect(Collectors.toList());
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