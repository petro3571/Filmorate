package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenreDbService {
    private final GenreDbStorage genreDbStorage;

    public GenreDbService(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    public List<GenreDto> getAll() {
        return genreDbStorage.getAll()
                .stream()
                .map(GenreMapper::mapToGenreDto)
                .collect(Collectors.toList());
    }

    public GenreDto getGenre(Integer genreId) {
        return genreDbStorage.getGenre(genreId).map(GenreMapper::mapToGenreDto).orElseThrow(() -> new NotFoundException("Жанр с ID " +
                genreId + " не найден."));
    }
}