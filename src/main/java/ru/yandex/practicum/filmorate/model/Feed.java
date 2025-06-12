package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class Feed {
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