package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewReviewDto {

    @NotBlank
    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String content;

    @Positive
    private Long userId;

    @Positive
    private Long filmId;

    private boolean isPositive;
}
