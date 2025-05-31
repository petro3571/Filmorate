package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;

@Data
public class NewUserRequest {
    private String name;
    @NotBlank
    private String login;
    @Email
    private String email;
    @PastOrPresent
    private LocalDate birthday;

    private Mpa mpa;
}