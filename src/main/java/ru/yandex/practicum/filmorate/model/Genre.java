package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Genre implements Comparable<Genre> {
    @Id
    private int id;
    private String name;

    @Override
    public int compareTo(Genre o) {
        return this.id - o.id;
    }
}