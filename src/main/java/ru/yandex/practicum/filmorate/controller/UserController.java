package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.CreateGroup;
import ru.yandex.practicum.filmorate.model.UpdateGroup;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<User> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable("userId") Long userId) {
        return userService.getUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Validated(CreateGroup.class) @Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Validated(UpdateGroup.class) @Valid @RequestBody User user) {
        log.info("PUT /users: {}", user);  // ← Что пришло?
        User updatedUser = userService.update(user);
        log.info("Updated user: {}", updatedUser);  // ← Что вернулось?
        return updatedUser;
    }

    @DeleteMapping("/{userId}")
    public User deleteUser(@PathVariable("userId") Long userId) {
        return userService.delete(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public Set<Long> addFriend(@PathVariable("userId") Long userID, @PathVariable("friendId") Long friendId) {
        return userService.addFriend(userID, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public User deleteFriend(@PathVariable("userId") Long userID, @PathVariable("friendId") Long friendId) {
        return userService.deleteFriend(userID, friendId);
    }

    @GetMapping("/{userId}/friends")
    public Set<User> getUserFriends(@PathVariable("userId") Long userId) {
        Set<Long> friendsId = userService.getUserFriends(userId);
        Set<User> friends = friendsId.stream()
                .map(userService::getUser)
                .collect(Collectors.toSet());
        return friends;
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public List<User> getSameFriends(@PathVariable("userId") Long userId, @PathVariable("otherId") Long otherId) {
        return userService.getSameFriends(userId, otherId);
    }

    public UserStorage getUserStorage() {
        return userService.getUserStorage();
    }
}