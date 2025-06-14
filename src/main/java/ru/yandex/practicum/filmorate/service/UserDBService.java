package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FeedDto;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.DataAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.IdValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FeedMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDBService {
    private final UserStorage userDbStorage;
    private final FeedStorage feedDbStorage;
    private final FilmStorage filmDbStorage;
    private final GenreStorage genreDbStorage;

    public List<UserDto> getUsers() {
        return userDbStorage.getAll()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userDbStorage.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
        return UserMapper.mapToUserDto(user);
    }

    public UserDto createUser(NewUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IdValidationException("Email должен быть указан");
        }

        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new IdValidationException("Login должен быть указан");
        }

        if (request.getBirthday() == null) {
            throw new IdValidationException("Дата рождения должна быть указана");
        }

        Optional<User> alreadyExistUser = userDbStorage.findByEmail(request.getEmail());
        if (alreadyExistUser.isPresent()) {
            throw new DataAlreadyExistException("Данный email уже используется");
        }

        User user = UserMapper.mapToUser(request);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

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

    public Collection<FeedDto> getFeedUser(Long userId) {
        boolean existsUser = userDbStorage.exists(userId);
        if (!existsUser) {
            throw new NotFoundException("Пользователя с таким id не существует");
        }
        return feedDbStorage.getFeedUser(userId).stream().map(FeedMapper::mapToFeedDto).collect(Collectors.toList());
    }

    public Collection<Film> getRecommendations(Long userId) {
        userDbStorage.existsUserById(userId);
        Collection<Film> recFilms = filmDbStorage.getRecommendations(userId);

        if (recFilms.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> listFilmIds = recFilms.stream().map(Film::getId).collect(Collectors.toList());

        Map<Long, TreeSet<Genre>> genresForFilms = genreDbStorage.getGenresForFilms(listFilmIds);

        recFilms.forEach(film -> film.setGenres(genresForFilms.getOrDefault(film.getId(), new TreeSet<>())));

        return recFilms;
    }
}