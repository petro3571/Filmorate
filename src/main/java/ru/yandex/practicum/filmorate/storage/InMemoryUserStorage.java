package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User getUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден!");
        }
        return users.get(userId);
    }

    @Override
    public Set<Long> getUserFriends(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден!");
        }
        User user = getUser(userId);
        Set<Long> friends = user.getFriends();

        if(friends == null) {
            throw new NotFoundException("Список друзей пуст у пользователя" + userId);
        }
        return friends;
    }

    @Override
    public List<User> getSameFriends(Long userId, Long otherId) {
        User firstUser = getUser(userId);
        User secondUser = getUser(otherId);

        if (firstUser.getFriends() == null || secondUser.getFriends() == null) {
            List<User> newList = new ArrayList<>();
            return newList;
        }

        Set<Long> commonFriendIds = firstUser.getFriends().stream()
                .filter(friendId -> secondUser.getFriends().contains(friendId))
                .collect(Collectors.toSet());

        List<User> sameFriends = new ArrayList<>();
        for (Long id : commonFriendIds) {
            sameFriends.add(getUser(id));
        }
        return sameFriends;
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Обновление пользователя не удалось");
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден!");
        }
        log.info("Начинается обновление.");
        User oldUser = users.get(user.getId());
        if (user.getName() != null) oldUser.setName(user.getName());
        if (user.getEmail() != null) oldUser.setEmail(user.getEmail());
        if (user.getLogin() != null) oldUser.setLogin(user.getLogin());
        if (user.getBirthday() != null) oldUser.setBirthday(user.getBirthday());
        if (user.getFriends() != null) oldUser.setFriends(user.getFriends());
        log.info("Обноление прошло успешно.");
        return oldUser;
    }

    @Override
    public User deleteUser(Long userId) {
        if (users.containsKey(userId)) {
            log.info("Удаление пользователя");
            return users.remove(userId);
        }
        log.warn("Удаление пользователя не удалось");
        throw new NotFoundException("Пользователь с id = " + userId + " не найден!");
    }

    @Override
    public Set<Long> addFriend(Long userId, Long friendId) {
        if (users.containsKey(userId)) {
            if (!users.containsKey(friendId)) {
                throw new NotFoundException("Пользователь с id = " + friendId + " не найден, чтобы добавить в друзья!");
            }
            User firstUser = users.get(userId);
            User secondUser = users.get(friendId);

            if (firstUser.getFriends() == null) {
                firstUser.setFriends(new HashSet<>());
            }
            if (secondUser.getFriends() == null) {
                secondUser.setFriends(new HashSet<>());
            }
            firstUser.getFriends().add(friendId);
            secondUser.getFriends().add(userId);
            return firstUser.getFriends();
        }
        log.warn("Добавление друга не удалось");
        throw new NotFoundException("Пользователь с id = " + userId + " не найден!");
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) {
        if (users.containsKey(userId)) {
            if (!users.containsKey(friendId)) {
                throw new NotFoundException("Пользователь с id = " + friendId + " не найден, чтобы удалить из друзей!");
            }
            User firstUser = users.get(userId);
            User secondUser = users.get(friendId);

            firstUser.getFriends().remove(friendId);
            secondUser.getFriends().remove(userId);

            return firstUser;
        }
        log.warn("Удаление друга не удалось");
        throw new NotFoundException("Пользователь с id = " + userId + " не найден!");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public boolean existsUserById(Long userId) {
        return users.containsKey(userId);
    }

}