package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreDbService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
    private final GenreDbService genreDbService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GenreDto> getAll() {
        return genreDbService.getAll();
    }

    @GetMapping("/{genreId}")
    public GenreDto getGenre(@PathVariable("genreId") int genreId) {
        return genreDbService.getGenre(genreId);
    }
}