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
    private LocalDate event_date;

    @NotNull
    private Integer entity_id;

    @NotNull
    private String event_type;

    @NotNull
    private String event_operation;
}