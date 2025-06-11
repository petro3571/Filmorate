package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Feed {
    @Id
    private Long id;

    @NotNull
    private Long userId;

    @PastOrPresent
    private LocalDate eventDate;

    @NotNull
    private Integer entityId;

    @NotNull
    private String eventType;

    @NotNull
    private String eventOperation;
}