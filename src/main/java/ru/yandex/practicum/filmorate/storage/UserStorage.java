package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getAll();

    User saveUser(User user);

    User updateUser(User user);

    User deleteUser(User user);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Optional<User> getUser(Long userId);

    List<Optional<User>> getUserFriends(Long userId);

    List<Optional<User>> getSameFriends(Long userId, Long otherId);

    Optional<User> findByEmail(String email);

    void confirmFriend(Long userId, Long otherId);

    void existsUserById(Long userId);

    boolean exists(Long userId);
}