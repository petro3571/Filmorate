package ru.yandex.practicum.filmorate.dto;

import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReviewDto {
    @Id
    @NotNull
    private Long id;

    @Nullable
    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String content;

    @Positive
    @Nullable
    private Long userId;

    @Positive
    @Nullable
    private Long filmId;

    @Nullable
    private Boolean isPositive;
}
