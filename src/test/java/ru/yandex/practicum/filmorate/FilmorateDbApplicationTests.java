package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class})
class FilmorateDbApplicationTests {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    private User testUser;
    private Film testFilm;

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testUserFields() {
        User testUser = createTestUser();
        User createdUser = userDbStorage.saveUser(testUser);

        assertNotNull(createdUser.getId(), "Поле 'id' должно быть заполнено");
        assertNotNull(createdUser.getEmail(), "Поле 'email' должно быть заполнено");
        assertNotNull(createdUser.getName(), "Поле 'name' должно быть заполнено");
        assertNotNull(createdUser.getLogin(), "Поле 'login' должно быть заполнено");
        assertNotNull(createdUser.getBirthday(), "Поле 'birthday' должно быть заполнено");
    }

    @Test
    void shouldRunWorkWithUsers() {
        testUser = new User();
        testUser.setEmail("test@ya.ru");
        testUser.setLogin("testLogin");
        testUser.setName("Test Name");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userDbStorage.saveUser(testUser);

        Optional<User> getUser = userDbStorage.getUser(createdUser.getId());

        Collection<User> users = userDbStorage.getAll();

        assertEquals(getUser.get().getName(), "Test Name");

        assertFalse(users.isEmpty());

        createdUser.setName("New Test Name");

        User updateUser = userDbStorage.updateUser(createdUser);

        assertEquals(updateUser.getName(), "New Test Name");

        User newTestUser = new User();

        newTestUser.setEmail("test1@ya.ru");
        newTestUser.setLogin("testLogin1");
        newTestUser.setName("Test Name1");
        newTestUser.setBirthday(LocalDate.of(1990, 1, 2));

        userDbStorage.saveUser(newTestUser);

        userDbStorage.addFriend(updateUser.getId(), newTestUser.getId());
        List<Optional<User>> newListFriends = userDbStorage.getUserFriends(updateUser.getId());

        assertEquals(1, newListFriends.size());

        userDbStorage.deleteFriend(updateUser.getId(), newTestUser.getId());
        newListFriends = userDbStorage.getUserFriends(updateUser.getId());

        assertEquals(0, newListFriends.size());
    }

    @Test
    void shouldRunWorkWithFilms() {
        testUser = new User();
        testUser.setEmail("test@ya.ru");
        testUser.setLogin("testLogin");
        testUser.setName("Test Name");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(new Mpa(1, null));

        User createdUser = userDbStorage.saveUser(testUser);

        Film createdFilm = filmDbStorage.create(testFilm);

        Optional<Film> getFilm = filmDbStorage.getFilm(createdFilm.getId());

        Collection<Film> films = filmDbStorage.getAll();

        assertFalse(films.isEmpty());

        assertEquals(getFilm.get().getName(), "Test Film");

        createdFilm.setName("Test Film Name");

        Film updatedFilm = filmDbStorage.update(createdFilm);

        assertEquals(updatedFilm.getName(), "Test Film Name");

        assertEquals(films.size(), 1);

        Film testFilm2 = new Film();
        testFilm2.setName("Test Film2");
        testFilm2.setDescription("Test Description2");
        testFilm2.setReleaseDate(LocalDate.of(2001, 1, 1));
        testFilm2.setDuration(150);
        testFilm2.setMpa(new Mpa(2, null));


        Film newCreatedFilm = filmDbStorage.create(testFilm2);

        filmDbStorage.addLike(updatedFilm.getId(), createdUser.getId());

        Collection<Film> popularFilm = filmDbStorage.getPopularFilms(1);

        assertEquals(popularFilm.size(), 1);

        Film newFilm = popularFilm.stream().findFirst().get();

        assertEquals(newFilm.getName(), "Test Film Name");
    }

    @Test
    void shouldSearchFilms() {
        Film film1 = new Film();
        film1.setName("Test Film One");
        film1.setDescription("Description One");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new Mpa(1, null));

        Film createdFilm1 = filmDbStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Another Film");
        film2.setDescription("Description Two");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(150);
        film2.setMpa(new Mpa(2, null));

        Film createdFilm2 = filmDbStorage.create(film2);

        Collection<Film> searchResult = filmDbStorage.searchFilms("one", List.of("title"));
        assertEquals(1, searchResult.size());
        assertTrue(searchResult.stream().anyMatch(f -> f.getName().equals("Test Film One")));

    }
}