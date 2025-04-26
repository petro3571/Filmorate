package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import org.apache.catalina.startup.CertificateCreateRule;

import java.time.LocalDate;

/**
 * User.
 */
@Data
public class User {
    private Long id;

    @NotBlank(groups = CreateGroup.class)
    @Email(groups = CreateGroup.class)
    private String email;

    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    private String login;

    private String name;

    @PastOrPresent(groups = {CreateGroup.class, UpdateGroup.class})
    private LocalDate birthday;
}