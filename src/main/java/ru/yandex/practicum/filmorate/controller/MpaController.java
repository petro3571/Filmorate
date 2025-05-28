package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaDbService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
    private final MpaDbService mpaDbService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MpaDto> getAll() {
        return mpaDbService.getAll();
    }

    @GetMapping("/{mpaId}")
    public MpaDto getMpa(@PathVariable("mpaId") int mpaId) {
        return mpaDbService.getMpa(mpaId);
    }
}