package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private String content;
    private boolean isPositive;
    private Long userId;
    private Long filmId;
    private int useful;
}
