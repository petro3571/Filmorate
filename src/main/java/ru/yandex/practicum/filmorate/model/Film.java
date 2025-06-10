package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import lombok.Data;
import jakarta.validation.constraints.*;
import ru.yandex.practicum.filmorate.annotation.RealeaseDateMin;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
public class Film {
    @NotNull(groups = UpdateGroup.class)
    @Id
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым.")
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