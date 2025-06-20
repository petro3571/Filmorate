package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.RealeaseDateMin;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class NewFilmRequest {
    @NotBlank
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String description;

    @RealeaseDateMin
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной.")
    private long duration;

    private Mpa mpa;

    private Set<Genre> genres = new HashSet<>();

    private Set<Director> directors = new HashSet<>();
}