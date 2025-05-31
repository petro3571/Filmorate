package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

/**
 * User.
 */
@Data
public class User {

    @NotNull(groups = UpdateGroup.class)
    @Id
    private Long id;

    @NotBlank(groups = CreateGroup.class)
    @Email(groups = {CreateGroup.class, UpdateGroup.class})
    private String email;

    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    private String login;

    private String name;

    @PastOrPresent(groups = {CreateGroup.class, UpdateGroup.class})
    private LocalDate birthday;
}