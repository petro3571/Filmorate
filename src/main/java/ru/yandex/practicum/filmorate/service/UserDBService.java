package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.DataAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDBService {
    private final UserStorage userDbStorage;
    private final FeedStorage feedDbStorage;

    public List<UserDto> getUsers() {
        return userDbStorage.getAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return userDbStorage.getUser(id).map(UserMapper::mapToUserDto).orElseThrow(() -> new NotFoundException("Пользователь с ID " +
                id + " не найден."));
    }

    public UserDto createUser(NewUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new NotFoundException("Имейл должен быть указан");
        }

        Optional<User> alreadyExistUser = userDbStorage.findByEmail(request.getEmail());
        if (alreadyExistUser.isPresent()) {
            throw new DataAlreadyExistException("Данный имейл уже используется");
        }

        User user = UserMapper.mapToUser(request);

        user = userDbStorage.saveUser(user);

        return UserMapper.mapToUserDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request) {
        User newUser = UserMapper.updateUserFields(new User(), request);

        if (!(userDbStorage.getUser(newUser.getId()).isPresent())) {
            throw new NotFoundException("Пользователь не найден");
        }
        newUser = userDbStorage.updateUser(newUser);
        return UserMapper.mapToUserDto(newUser);
    }

    public UserDto deleteUser(long userId) {
        User userForDelete = userDbStorage.getUser(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " +
                userId + " не найден."));
        userForDelete = userDbStorage.deleteUser(userForDelete);
        return UserMapper.mapToUserDto(userForDelete);
    }

    public void addFriend(long userId, long friendId) {
        userDbStorage.addFriend(userId, friendId);
        log.info("Пользователи теперь друзья.");
    }

    public List<Optional<User>> getFriends(Long userId) {
        return userDbStorage.getUserFriends(userId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userDbStorage.deleteFriend(userId, friendId);
        log.info("Пользователи теперь не друзья.");
    }

    public List<Optional<User>> getSameFriends(Long userId, Long otherId) {
        return userDbStorage.getSameFriends(userId, otherId);
    }

    public void confirmFriend(Long userId, Long friendId) {
        userDbStorage.confirmFriend(userId, friendId);
    }

    public Collection<Feed> getFeedUser(Long userId) {
        return feedDbStorage.getFeedUser(userId);
    }
}