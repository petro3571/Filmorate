package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserStorage {
    Collection<User> getAll();

    User create(User user);

    User update(User user);

    User deleteUser(Long userId);

    Set<Long> addFriend(Long userId, Long friendId);

    User deleteFriend(Long userId, Long friendId);

    User getUser(Long userId);

    Set<Long> getUserFriends(Long userId);

    List<User> getSameFriends(Long userId, Long otherId);

    boolean existsUserById(Long userId);
}