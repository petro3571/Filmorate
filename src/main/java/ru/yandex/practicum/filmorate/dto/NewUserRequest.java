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

    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    @Email(message = "Email должен быть валидным")
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}