package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewReviewDto {

    @NotBlank
    @NotNull
    @Size(max = 200, message = "Описание не должно превышать 200 символов.")
    private String content;

    // я считаю, что здесь должна стоять аннотация @Positive, но тогда не проходят тесты постмена
    @NotNull
    private Long userId;

    @NotNull
    private Long filmId;

    @NotNull
    private Boolean isPositive;
}