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
    private final DirectorService directorService;

    public Collection<FilmDto> getAll() {
        Collection<Film> films = filmDbStorage.getAll();

        if (films.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> listFilmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, TreeSet<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(listFilmIds);
        Map<Long, Set<Director>> directorsForFilms = directorStorage.getDirectorsForFilms(listFilmIds);

        films.forEach(film -> {
            film.setGenres(genresForFilms.getOrDefault(film.getId(), new TreeSet<>()));
            film.setDirectors(directorsForFilms.getOrDefault(film.getId(), new HashSet<>()));
        });

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilm(Long filmId) {
        FilmDto filmDto = filmDbStorage.getFilm(filmId).map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден."));

        filmDto.setGenres(genreDbStorage.getFilmGenres(filmId));
        filmDto.setDirectors(directorStorage.getFilmDirectors(filmId));
        return filmDto;
    }

    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);

        if (film.getMpa() == null || !(mpaDbStorage.getMpa(film.getMpa().getId()).isPresent())) {
            throw new NotFoundException("Рейтинга с id " + (film.getMpa() != null ? film.getMpa().getId() : "null") + " нет.");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreDbStorage.getGenre(genre.getId()).isPresent()) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " нет.");
                }
            }
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                if (!directorStorage.getDirector(director.getId()).isPresent()) {
                    throw new NotFoundException("Режиссёра с id " + director.getId() + " нет.");
                }
            }
        }

        film = filmDbStorage.create(film);

        film.setGenres(genreDbStorage.getFilmGenres(film.getId()));
        film.setDirectors(directorStorage.getFilmDirectors(film.getId()));

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Film updateFilm = FilmMapper.updateFilmFields(new Film(), request);

        if (!filmDbStorage.getFilm(updateFilm.getId()).isPresent()) {
            throw new NotFoundException("Фильма с id " + updateFilm.getId() + " не найден.");
        }

        if (updateFilm.getMpa() == null || !(mpaDbStorage.getMpa(updateFilm.getMpa().getId()).isPresent())) {
            throw new NotFoundException("Рейтинга с id " + (updateFilm.getMpa() != null ? updateFilm.getMpa().getId() : "null") + " нет.");
        }

        if (updateFilm.getGenres() != null && !updateFilm.getGenres().isEmpty()) {
            for (Genre genre : updateFilm.getGenres()) {
                if (!genreDbStorage.getGenre(genre.getId()).isPresent()) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " нет.");
                }
            }
        }

        if (updateFilm.getDirectors() != null && !updateFilm.getDirectors().isEmpty()) {
            for (Director director : updateFilm.getDirectors()) {
                if (!directorStorage.getDirector(director.getId()).isPresent()) {
                    throw new NotFoundException("Режиссёра с id " + director.getId() + " нет.");
                }
            }
        }

        updateFilm = filmDbStorage.update(updateFilm);

        updateFilm.setGenres(genreDbStorage.getFilmGenres(updateFilm.getId()));
        updateFilm.setDirectors(directorStorage.getFilmDirectors(updateFilm.getId()));

        return FilmMapper.mapToFilmDto(updateFilm);
    }

    public FilmDto deleteFilm(Long filmId) {
        Film film = filmDbStorage.getFilm(filmId).orElseThrow(() -> new NotFoundException("Фильма с id " + filmId + " нет."));
        film.setGenres(genreDbStorage.getFilmGenres(film.getId()));
        film.setDirectors(directorStorage.getFilmDirectors(film.getId()));
        filmDbStorage.deleteFilm(filmId);
        return FilmMapper.mapToFilmDto(film);
    }

    public void addLike(Long filmId, Long userId) {
        userDbStorage.existsUserById(userId);
        filmDbStorage.addLike(filmId, userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {} .", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        userDbStorage.existsUserById(userId);
        filmDbStorage.deleteLike(filmId, userId);
        log.info("Пользователь с id {} удалил лайк фильму с id {} .", userId, filmId);
    }

    public Collection<FilmDto> getPopularFilms(Integer count, Integer genreId, Integer year) {
        Collection<Film> popularFilms;
        if (genreId != null && year != null) {
            popularFilms = filmDbStorage.getPopularFilmsByGenreAndYear(count, genreId, year);
        } else if (genreId != null) {
            popularFilms = filmDbStorage.getPopularFilmsByGenre(count, genreId);
        } else if (year != null) {
            popularFilms = filmDbStorage.getPopularFilmsByYear(count, year);
        } else {
            popularFilms = filmDbStorage.getPopularFilms(count);
        }

        if (popularFilms.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> listFilmIds = popularFilms.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, TreeSet<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(listFilmIds);
        Map<Long, Set<Director>> directorsForFilms = directorStorage.getDirectorsForFilms(listFilmIds);

        popularFilms.forEach(film -> {
            film.setGenres(genresForFilms.getOrDefault(film.getId(), new TreeSet<>()));
            film.setDirectors(directorsForFilms.getOrDefault(film.getId(), new HashSet<>()));
        });

        popularFilms.forEach(film -> film.setGenres(genresForFilms.getOrDefault(film.getId(), new TreeSet<>())));

        return popularFilms.stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toList());
    }

    public Collection<Film> searchFilms(String query, List<String> by) {
        Collection<Film> films = filmDbStorage.searchFilms(query, by);

        if (!films.isEmpty()) {
            List<Long> listFilmIds = films.stream().map(Film::getId).collect(Collectors.toList());
            Map<Long, TreeSet<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(listFilmIds);
            Map<Long, Set<Director>> directorsForFilms = directorStorage.getDirectorsForFilms(listFilmIds);

            films.forEach(film -> {
                film.setGenres(genresForFilms.getOrDefault(film.getId(), new TreeSet<>()));
                film.setDirectors(directorsForFilms.getOrDefault(film.getId(), new HashSet<>()));
            });
        }

        return films;
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorService.getById(directorId);

        Collection<Film> films = filmDbStorage.getFilmsByDirector(directorId, sortBy);

        if (!films.isEmpty()) {
            List<Long> listFilmIds = films.stream().map(Film::getId).collect(Collectors.toList());
            Map<Long, TreeSet<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(listFilmIds);
            Map<Long, Set<Director>> directorsForFilms = directorStorage.getDirectorsForFilms(listFilmIds);
            films.forEach(film -> {
                film.setGenres(genresForFilms.getOrDefault(film.getId(), new TreeSet<>()));
                film.setDirectors(directorsForFilms.getOrDefault(film.getId(), new HashSet<>()));
            });
        }
        return films;
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        Collection<Film> commonFilms = filmDbStorage.getCommonFilms(userId, friendId);
        if (commonFilms.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> listFilmIds = commonFilms.stream().map(Film::getId).toList();
        Map<Long, TreeSet<Genre>> genres = genreDbStorage.getGenresForFilms(listFilmIds);
        commonFilms.forEach(film -> film.setGenres(genres.getOrDefault(film.getId(), new TreeSet<>())));
        return commonFilms;
    }
}