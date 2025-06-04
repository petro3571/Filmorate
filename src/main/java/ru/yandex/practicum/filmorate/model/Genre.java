package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Genre {
    @Id
    private int id;
    private String name;
}