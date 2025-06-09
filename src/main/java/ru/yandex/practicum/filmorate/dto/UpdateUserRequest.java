package ru.yandex.practicum.filmorate.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    @Id
    @NotNull
    private Long id;
    private String name;
    @Email
    private String email;

    private String login;
    @PastOrPresent
    private LocalDate birthday;

    public boolean hasId() {
        return !(id == null);
    }

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasEmail() {
        return ! (email == null || email.isBlank());
    }

    public boolean hasLogin() {
        return ! (login == null || login.isBlank());
    }

    public boolean hasBirthday() {
        return ! (birthday == null);
    }
}