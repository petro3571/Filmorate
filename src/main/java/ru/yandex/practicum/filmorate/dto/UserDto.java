package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private long id;
    private String name;
    private String email;
    private String login;
    private LocalDate birthday;
}