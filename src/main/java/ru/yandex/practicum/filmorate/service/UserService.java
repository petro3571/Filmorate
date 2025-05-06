package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IdValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getUser(Long userId) {
        validate(userId);
        return userStorage.getUser(userId);
    }

    public List<User> getSameFriends(Long userId, Long otherId) {
        validate(userId);
        validate(otherId);
        return userStorage.getSameFriends(userId, otherId);
    }

    public User create(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user.getId());
        User updatedUser = userStorage.update(user);
        return updatedUser;
    }

    public User delete(Long userId) {
        validate(userId);
        return userStorage.deleteUser(userId);
    }

    public Set<Long> addFriend(Long userId, Long friendId) {
        validate(userId);
        validate(friendId);
        return userStorage.addFriend(userId, friendId);
    }

    public User deleteFriend(Long userId, Long friendId) {
        validate(userId);
        validate(friendId);
        return userStorage.deleteFriend(userId, friendId);
    }

    public Set<Long> getUserFriends(Long userId) {
        return userStorage.getUserFriends(userId);
    }

    private void validate(Long id) {
        if (id == null) {
            throw new IdValidationException("Id должен быть указан");
        }
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }
}