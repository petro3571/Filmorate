package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации.Проверьте почту");
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }

        if (user.getLogin().isEmpty() || user.getLogin().isBlank()) {
            log.warn("Ошибка валидации. Проверьте логин");
            throw new ValidationException("логин не может быть пустым и содержать пробелы;");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации. Проверьте дату");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;

    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }

        if (users.containsKey(user.getId())) {
            User oldUser = users.get(user.getId());
            if (user.getEmail() == null || !user.getEmail().contains("@")) {
                log.warn("Ошибка валидации. Проверьте почту");
                throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
            }

            if (user.getLogin().isEmpty() || user.getLogin().isBlank()) {
                log.warn("Ошибка валидации.Проверь логин");
                throw new ValidationException("логин не может быть пустым и содержать пробелы;");
            }

            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Ошибка валидации. Проверь дату");
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
            }

            if (user.getName() == null) {
                user.setName(user.getLogin());
            }

            oldUser.setName(user.getName());
            oldUser.setEmail(user.getEmail());
            oldUser.setBirthday(user.getBirthday());
            oldUser.setLogin(user.getLogin());

            return oldUser;
        }
        log.warn("Обновление пользователя не удалось");
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден!");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}