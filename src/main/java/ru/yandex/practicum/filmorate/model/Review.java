package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Review {

    @Id
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String content;

    @NotNull
    private boolean isPositive;

    private Long userId;
    private Long filmId;
    private int useful;
}
