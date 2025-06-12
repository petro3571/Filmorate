package ru.yandex.practicum.filmorate.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
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
public class UpdateFilmRequest {
    @Id
    @NotNull
    private Long id;

    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String description;

    @RealeaseDateMin
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной.")
    private Long duration;

    private Mpa mpa;

    private Set<Genre> genres = new HashSet<>();

    private Set<Director> directors = new HashSet<>();

    public boolean hasId() {
        return !(id == null);
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(releaseDate == null);
    }

    public boolean hasDuration() {
        return !(duration == null);
    }

    public boolean hasMpa() {
        return !(mpa == null);
    }

    public boolean hasGenres() {
        return genres != null && !genres.isEmpty();
    }
}