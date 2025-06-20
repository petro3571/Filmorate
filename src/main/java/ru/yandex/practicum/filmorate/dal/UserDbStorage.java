package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private static final String INSERT_QUERY = "INSERT INTO users(name, email, login, birthday) " +
            "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT user_id AS id, name AS username, email, login, birthday " +
            "FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT user_id AS id, name AS username, email, login, birthday " +
            "FROM users WHERE user_id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT user_id AS id, name AS username, email, login, birthday " +
            "FROM users WHERE email = ?";

    private static final String EXISTS_QUERY = "SELECT 1 FROM users u WHERE u.user_id = ?";

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    // Получение всех пользователей
    @Override
    public List<User> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    // Поиск по ID
    @Override
    public Optional<User> getUser(Long userId) {
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Сохранение с генерацией ID
    @Override
    public User saveUser(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    // Обновление данных
    @Override
    public User updateUser(User user) {
        update(
                UPDATE_QUERY,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    // Удаление пользователя
    @Override
    public User deleteUser(User user) {
        delete(DELETE_QUERY,
                user.getId());
        return user;
    }

    // Добавление друга + запись в ленту событий
    @Override
    public void addFriend(Long userId, Long friendId) {
        existsUserById(userId);
        existsUserById(friendId);

        String query = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        update(query, userId, friendId);

        String feedquery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) VALUES (?,?,?,?,?)";
        update(feedquery, userId, Instant.now().toEpochMilli(), friendId, 3, 2);
    }

    // Подтверждение дружбы
    @Override
    public void confirmFriend(Long userId, Long friendId) {
        existsUserById(userId);
        existsUserById(friendId);
        String query = "UPDATE friends SET friend_confirm = true WHERE user_id = ? AND friend_id = ?";
        update(query, userId, friendId);

        String feedQuery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) VALUES (?,?,?,?,?)";
        update(feedQuery, userId, Instant.now().toEpochMilli(), friendId, 3, 3);
    }

    // Список друзей
    @Override
    public List<Optional<User>> getUserFriends(Long userId) {
        existsUserById(userId);
        String query = "SELECT friend_id FROM friends WHERE user_id = ?";
        return jdbc.queryForList(query, Long.class, userId).stream().map(this::getUser).collect(Collectors.toList());
    }

    // Удаление из друзей
    @Override
    public void deleteFriend(Long userId, Long friendId) {
        existsUserById(userId);
        existsUserById(friendId);

        String feedQuery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) VALUES (?,?,?,?,?)";
        update(feedQuery, userId, Instant.now().toEpochMilli(), friendId, 3, 1);

        String query = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbc.update(query, userId, friendId);

    }

    // Общие друзья
    @Override
    public List<Optional<User>> getSameFriends(Long userId, Long otherId) {
        existsUserById(userId);
        existsUserById(otherId);
        String query = "SELECT friend_id FROM friends WHERE user_id = ? AND friend_id IN (SELECT friend_id FROM friends WHERE user_id = ?)";
        return jdbc.queryForList(query, Long.class, userId, otherId).stream().map(friendId -> getUser(friendId)).collect(Collectors.toList());
    }

    @Override
    public void existsUserById(Long userId) {
        User user = getUser(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " +
                userId + " не найден."));
    }

    public boolean exists(Long userId) {
        try {
            Integer result = jdbc.queryForObject(
                    EXISTS_QUERY,
                    Integer.class,
                    userId
            );
            return result != 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    // Поиск по email
    @Override
    public Optional<User> findByEmail(String email) {
        try {
            User user = jdbc.queryForObject(FIND_BY_EMAIL_QUERY, mapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    private void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    private void delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
    }
}