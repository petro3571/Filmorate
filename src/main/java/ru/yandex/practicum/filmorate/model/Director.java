package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Director {
    @Id
    private Long id;
    private String name;
}