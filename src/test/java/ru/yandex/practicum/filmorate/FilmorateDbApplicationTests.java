package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.mapper.*;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.DirectorServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        UserDbStorage.class,
        FilmDbStorage.class,
        GenreDbStorage.class,
        MpaDbStorage.class,
        DirectorDbStorage.class,
        DirectorServiceImpl.class,
        UserRowMapper.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        MpaRowMapper.class,
        DirectorRowMapper.class
})
class FilmorateDbApplicationTests {

    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final DirectorService directorService;
    private final GenreDbStorage genreDbStorage;
    private final MpaDbStorage mpaDbStorage;
    private JdbcTemplate jdbc;

    @BeforeEach
    void clearDatabase() {
        filmDbStorage.getAll().forEach(film -> filmDbStorage.deleteFilm(film.getId()));
        userDbStorage.getAll().forEach(userDbStorage::deleteUser);
    }

    @Test
    void userCreateSuccess() {
        User user = createTestUser("user@mail.com", "login", "name", LocalDate.of(1990, 1, 1));
        User createdUser = userDbStorage.saveUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("user@mail.com", createdUser.getEmail());
        assertEquals("login", createdUser.getLogin());
        assertEquals("name", createdUser.getName());
        assertEquals(LocalDate.of(1990, 1, 1), createdUser.getBirthday());
    }

    @Test
    void userCreateFailLogin() {
        User user = createTestUser("user@mail.com", "", "name", LocalDate.of(1990, 1, 1));

        assertThrows(InternalServerException.class, () -> userDbStorage.saveUser(user));
    }

    @Test
    void userCreateFailEmail() {
        User user = createTestUser("invalid-email", "login", "name", LocalDate.of(1990, 1, 1));

        assertThrows(InternalServerException.class, () -> userDbStorage.saveUser(user));
    }

    @Test
    void userCreateFailBirthday() {
        User user = createTestUser("user@mail.com", "login", "name", LocalDate.now().plusDays(1));

        assertThrows(InternalServerException.class, () -> userDbStorage.saveUser(user));
    }

    @Test
    void userUpdateUnknown() {
        User user = createTestUser("user@mail.com", "login", "name", LocalDate.of(1990, 1, 1));
        user.setId(9999L);

        assertThrows(InternalServerException.class, () -> userDbStorage.updateUser(user));
    }

    @Test
    void userGetAll() {
        User user1 = createTestUser("user1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1));
        User user2 = createTestUser("user2@mail.com", "login2", "name2", LocalDate.of(1990, 1, 2));

        userDbStorage.saveUser(user1);
        userDbStorage.saveUser(user2);

        Collection<User> users = userDbStorage.getAll();
        assertEquals(2, users.size());
    }

    @Test
    void friendAddSuccess() {
        User user1 = userDbStorage.saveUser(createTestUser("useruser1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1)));
        User user2 = userDbStorage.saveUser(createTestUser("user2@mail.com", "login2", "name2", LocalDate.of(1990, 1, 2)));

        userDbStorage.addFriend(user1.getId(), user2.getId());

        List<Optional<User>> friends = userDbStorage.getUserFriends(user1.getId());
        assertEquals(1, friends.size());
        assertTrue(friends.get(0).isPresent());
        assertEquals(user2.getId(), friends.get(0).get().getId());
    }

    @Test
    void friendAddUnknownId() {
        User user1 = userDbStorage.saveUser(createTestUser("user1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1)));

        assertThrows(NotFoundException.class, () -> userDbStorage.addFriend(user1.getId(), 9999L));
    }

    @Test
    void friendGetSuccess() {
        User user1 = userDbStorage.saveUser(createTestUser("user1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1)));
        User user2 = userDbStorage.saveUser(createTestUser("user2@mail.com", "login2", "name2", LocalDate.of(1990, 1, 2)));

        userDbStorage.addFriend(user1.getId(), user2.getId());

        List<Optional<User>> friends = userDbStorage.getUserFriends(user1.getId());
        assertEquals(1, friends.size());
    }

    @Test
    void friendGetUnknownId() {
        assertThrows(NotFoundException.class, () -> userDbStorage.getUserFriends(9999L));
    }

    @Test
    void friendRemoveSuccess() {
        User user1 = userDbStorage.saveUser(createTestUser("user1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1)));
        User user2 = userDbStorage.saveUser(createTestUser("user2@mail.com", "login2", "name2", LocalDate.of(1990, 1, 2)));

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.deleteFriend(user1.getId(), user2.getId());

        List<Optional<User>> friends = userDbStorage.getUserFriends(user1.getId());
        assertEquals(0, friends.size());
    }

    @Test
    void friendRemoveNotFriend() {
        User user1 = userDbStorage.saveUser(createTestUser("user1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1)));
        User user2 = userDbStorage.saveUser(createTestUser("user2@mail.com", "login2", "name2", LocalDate.of(1990, 1, 2)));

        // Убедимся, что дружбы нет
        List<Optional<User>> friendsBefore = userDbStorage.getUserFriends(user1.getId());
        assertEquals(0, friendsBefore.size());

        assertThrows(InternalServerException.class, () -> userDbStorage.deleteFriend(user1.getId(), user2.getId()));
    }

    @Test
    void friendRemoveUnknownId() {
        User user1 = userDbStorage.saveUser(createTestUser("user1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1)));

        assertThrows(NotFoundException.class, () -> userDbStorage.deleteFriend(user1.getId(), 9999L));
    }

    @Test
    void getCommonFriends() {
        User user1 = userDbStorage.saveUser(createTestUser("user1@mail.com", "login1", "name1", LocalDate.of(1990, 1, 1)));
        User user2 = userDbStorage.saveUser(createTestUser("user2@mail.com", "login2", "name2", LocalDate.of(1990, 1, 2)));
        User commonFriend = userDbStorage.saveUser(createTestUser("common@mail.com", "common", "common", LocalDate.of(1990, 1, 3)));

        userDbStorage.addFriend(user1.getId(), commonFriend.getId());
        userDbStorage.addFriend(user2.getId(), commonFriend.getId());

        List<Optional<User>> commonFriends = userDbStorage.getSameFriends(user1.getId(), user2.getId());
        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.get(0).isPresent());
        assertEquals(commonFriend.getId(), commonFriends.get(0).get().getId());
    }

    // Film tests
    @Test
    void filmCreateSuccess() {
        Film film = createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));
        Film createdFilm = filmDbStorage.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Film", createdFilm.getName());
        assertEquals("Description", createdFilm.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
        assertEquals(120, createdFilm.getDuration());
        assertEquals(1, createdFilm.getMpa().getId());
    }

    @Test
    void filmCreateSeveralGenres() {
        Film film = createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));
        Set<Genre> genres = new HashSet<>();

        Genre comedy = new Genre();
        comedy.setId(1);
        comedy.setName("Комедия");
        genres.add(comedy);

        Genre drama = new Genre();
        drama.setId(2);
        drama.setName("Драма");
        genres.add(drama);

        film.setGenres(genres);

        Film createdFilm = filmDbStorage.create(film);
        Optional<Film> retrievedFilm = filmDbStorage.getFilm(createdFilm.getId());

        assertTrue(retrievedFilm.isPresent());
        assertEquals(2, retrievedFilm.get().getGenres().size());
    }

    @Test
    void filmCreateFailName() {
        Film film = createTestFilm("", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));

        assertThrows(InternalServerException.class, () -> filmDbStorage.create(film));
    }

    @Test
    void filmCreateFailDescription() {
        String longDescription = "a".repeat(201);
        Film film = createTestFilm("Film", longDescription, LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));

        assertThrows(InternalServerException.class, () -> filmDbStorage.create(film));
    }

    @Test
    void filmCreateFailReleaseDate() {
        Film film = createTestFilm("Film", "Description", LocalDate.of(1800, 1, 1), 120, new Mpa(1, "G"));

        assertThrows((InternalServerException.class), () -> filmDbStorage.create(film));
    }

    @Test
    void filmCreateFailDuration() {
        Film film = createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), -120, new Mpa(1, "G"));

        assertThrows(InternalServerException.class, () -> filmDbStorage.create(film));
    }

    @Test
    void filmCreateFailMPA() {
        Film film = createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(999, "Unknown"));

        assertThrows(NotFoundException.class, () -> filmDbStorage.create(film));
    }

    @Test
    void filmCreateFailGenre() {
        Film film = createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));
        Set<Genre> genres = new HashSet<>();

        Genre unknown = new Genre();
        unknown.setId(999);
        unknown.setName("Unknown");
        genres.add(unknown);

        film.setGenres(genres);

        assertThrows(NotFoundException.class, () -> filmDbStorage.create(film));
    }

    @Test
    void filmUpdateUnknown() {
        Film film = createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G"));
        film.setId(9999L);

        assertThrows(InternalServerException.class, () -> filmDbStorage.update(film));
    }

    @Test
    void filmGetAll() {
        Film film1 = filmDbStorage.create(createTestFilm("Film1", "Description1", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G")));
        Film film2 = filmDbStorage.create(createTestFilm("Film2", "Description2", LocalDate.of(2001, 1, 1), 150, new Mpa(2, "PG")));

        Collection<Film> films = filmDbStorage.getAll();
        assertEquals(2, films.size());
    }

    @Test
    void filmGetPopular() {
        User user = userDbStorage.saveUser(createTestUser("user@mail.com", "login", "name", LocalDate.of(1990, 1, 1)));
        Film film1 = filmDbStorage.create(createTestFilm("Film1", "Description1", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G")));
        Film film2 = filmDbStorage.create(createTestFilm("Film2", "Description2", LocalDate.of(2001, 1, 1), 150, new Mpa(2, "PG")));

        filmDbStorage.addLike(film1.getId(), user.getId());

        Collection<Film> popularFilms = filmDbStorage.getPopularFilms(1);
        assertEquals(1, popularFilms.size());
        assertEquals(film1.getId(), popularFilms.iterator().next().getId());
    }

    @Test
    void addLikeSuccess() {
        User user = userDbStorage.saveUser(createTestUser("user@mail.com", "login", "name", LocalDate.of(1990, 1, 1)));
        Film film = filmDbStorage.create(createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G")));

        filmDbStorage.addLike(film.getId(), user.getId());

        Collection<Film> popularFilms = filmDbStorage.getPopularFilms(1);
        assertEquals(1, popularFilms.size());
        assertEquals(film.getId(), popularFilms.iterator().next().getId());
    }

    @Test
    void removeLikeSuccess() {
        User user = userDbStorage.saveUser(createTestUser("user@mail.com", "login", "name", LocalDate.of(1990, 1, 1)));
        Film film = filmDbStorage.create(createTestFilm("Film", "Description", LocalDate.of(2000, 1, 1), 120, new Mpa(1, "G")));

        filmDbStorage.addLike(film.getId(), user.getId());
        filmDbStorage.deleteLike(film.getId(), user.getId());

        Collection<Film> popularFilms = filmDbStorage.getPopularFilms(1);
        assertEquals(1, popularFilms.size());
    }

    // MPA tests
    @Test
    void getMPANameById() {
        Optional<Mpa> mpa = mpaDbStorage.getMpa(1);
        assertTrue(mpa.isPresent());
        assertEquals("G", mpa.get().getName());
    }

    @Test
    void getMPANameByIdNotFound() {
        assertThrows(NotFoundException.class, () -> mpaDbStorage.getMpa(999));
    }

    @Test
    void mpaGetAll() {
        List<Mpa> mpaList = mpaDbStorage.getAll();
        assertEquals(5, mpaList.size());
    }

    // Helper methods
    private User createTestUser(String email, String login, String name, LocalDate birthday) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday);
        return user;
    }

    private Film createTestFilm(String name, String description, LocalDate releaseDate, int duration, Mpa mpa) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        film.setMpa(mpa);
        return film;
    }
}