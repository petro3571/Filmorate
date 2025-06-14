package ru.yandex.practicum.filmorate.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedDto {
    @Id
    private Long eventId;

    @NotNull
    private Long userId;

    @NotNull
    private Long timestamp;

    @NotNull
    private Integer entityId;

    @NotNull
    private String eventType;

    @NotNull
    private String operation;
}
