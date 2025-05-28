package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserDBService;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserDBService userDBService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers() {
        return userDBService.getUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable("userId") Long userId) {
        return userDBService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest userRequest) {
        return userDBService.createUser(userRequest);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UpdateUserRequest userRequest) {
        return userDBService.updateUser(userRequest);
    }

    @DeleteMapping("/{userId}")
    public UserDto deleteUser(@PathVariable("userId") Long userId) {
        return userDBService.deleteUser(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        userDBService.addFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<Optional<User>> getFriends(@PathVariable("userId") Long userId) {
        return userDBService.getFriends(userId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        userDBService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public List<Optional<User>> getSameFriends(@PathVariable("userId") Long userId, @PathVariable("otherId") Long otherId) {
        return userDBService.getSameFriends(userId, otherId);
    }

    @PutMapping("/{userId}/friends/{friendId}/confirm")
    public void confirmFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        userDBService.confirmFriend(userId, friendId);
    }
}