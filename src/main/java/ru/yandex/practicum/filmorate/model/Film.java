package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.RealeaseDateMin;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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

    private Set<Genre> genres = new TreeSet<>();

    private Set<Director> directors = new HashSet<>();
}