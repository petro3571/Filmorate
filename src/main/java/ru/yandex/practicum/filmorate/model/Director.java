package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {
    @Id
    private Long id;

    @NotBlank(message = "Имя режиссёра не может быть пустым или состоять только из пробелов")
    private String name;
}