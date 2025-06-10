package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;

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
    private final DirectorStorage directorStorage;

    public Collection<FilmDto> getAll() {
        Collection<Film> films = filmDbStorage.getAll();

        if (films.isEmpty()) {
            throw new NotFoundException("Фильмы не найдены.");
        }

        List<Long> listFilmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, Set<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(listFilmIds);
        Map<Long, Set<Director>> directorsForFilms = directorStorage.getDirectorsForFilms(listFilmIds);

        films.forEach(film -> {
            film.setGenres(genresForFilms.getOrDefault(film.getId(), new HashSet<>()));
            film.setDirectors(directorsForFilms.getOrDefault(film.getId(), new HashSet<>()));
        });

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilm(Long filmId) {
        FilmDto filmDto = filmDbStorage.getFilm(filmId).map(FilmMapper::mapToFilmDto).orElseThrow(() -> new NotFoundException("Фильм с ID " +
                filmId + " не найден."));

        filmDto.setGenres(genreDbStorage.getFilmGenres(filmId));
        filmDto.setDirectors(directorStorage.getFilmDirectors(filmId));

        return filmDto;
    }

    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);

        if (mpaDbStorage.getMpa(film.getMpa().getId()).isEmpty()) {
            throw new NotFoundException("Рейтинга с id " + film.getMpa().getId() + " нет.");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (genreDbStorage.getGenre(genre.getId()).isEmpty()) {
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
        if (filmDbStorage.getFilm(updateFilm.getId()).isEmpty()) {
            throw new NotFoundException("Фильма с id " + updateFilm.getId() + "не найден.");
        }

        if (mpaDbStorage.getMpa(updateFilm.getMpa().getId()).isEmpty()) {
            throw new NotFoundException("Рейтинга с id " + updateFilm.getMpa().getId() + " нет.");
        }

        if (updateFilm.getGenres() != null && !updateFilm.getGenres().isEmpty()) {
            for (Genre genre : updateFilm.getGenres()) {
                if (genreDbStorage.getGenre(genre.getId()).isEmpty()) {
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
        log.info("Пользователи с id {} поставил лайк фильму с id {} .", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userDbStorage.existsUserById(userId);
        filmDbStorage.deleteLike(filmId, userId);
        log.info("Пользователи с id {} удалил лайк фильму с id {} .", userId, filmId);

    }

    public Collection<Film> getPopularFilms(Integer count) {
        Collection<Film> popularfilms = filmDbStorage.getPopularFilms(count);

        if (popularfilms.isEmpty()) {
            throw new NotFoundException("Фильмы не найдены.");
        }

        List<Long> listFilmIds = popularfilms.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, Set<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(listFilmIds);

        popularfilms.forEach(film -> film.setGenres(genresForFilms.getOrDefault(film.getId(), new HashSet<>())));

        return popularfilms;
    }

    public Collection<FilmDto> searchFilms(String query, String searchBy) {
        if (query == null || query.isBlank()) {
            throw new NotFoundException("Поисковый запрос не может быть пустым.");
        }

        String searchParam = "%" + query.toLowerCase() + "%";
        String[] searchCriteria = searchBy.split(",");

        Collection<Film> films;
        if (searchCriteria.length == 1) {
            if (searchCriteria[0].equals("title")) {
                films = filmDbStorage.searchByTitle(searchParam);
            } else if (searchCriteria[0].equals("director")) {
                films = filmDbStorage.searchByDirector(searchParam);
            } else {
                throw new NotFoundException("Неправильный параметр поиска.");
            }
        } else {
            films = filmDbStorage.searchByTitleAndDirector(searchParam);
        }

        if (films.isEmpty()) {
            throw new NotFoundException("Фильмы не не найдены.");
        }

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<Long, Set<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(filmIds);
        Map<Long, Set<Director>> directorsForFilms = directorStorage.getDirectorsForFilms(filmIds);

        films.forEach(film -> {
            film.setGenres(genresForFilms.getOrDefault(film.getId(), new HashSet<>()));
            Set<Director> directors = directorsForFilms.getOrDefault(film.getId(), new HashSet<>());
            film.setDirectors(directors);
        });

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    private void validateMpaAndGenres(Film film) {
        if (mpaDbStorage.getMpa(film.getMpa().getId()).isEmpty()) {
            throw new NotFoundException("Рейтинга с id " + film.getMpa().getId() + " нет.");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (genreDbStorage.getGenre(genre.getId()).isEmpty()) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " нет.");
                }
            }
        }
    }

}