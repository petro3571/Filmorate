package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.service.FilmDbService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class, FilmDbService.class})
class FilmorateDbApplicationTests {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final FilmDbService filmDbService;

    private User testUser;
    private Film testFilm;

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
    @Sql(scripts = "/testdata.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGetCorrectPopularFilms() {
        FilmDto film1 = filmDbService.getFilm(1L);
        FilmDto film2 = filmDbService.getFilm(2L);
        FilmDto film3 = filmDbService.getFilm(3L);
        FilmDto film4 = filmDbService.getFilm(4L);
        FilmDto film5 = filmDbService.getFilm(5L);
        FilmDto film6 = filmDbService.getFilm(6L);
        FilmDto film7 = filmDbService.getFilm(7L);
        FilmDto film8 = filmDbService.getFilm(8L);
        FilmDto film9 = filmDbService.getFilm(9L);
        FilmDto film10 = filmDbService.getFilm(10L);
        assertArrayEquals(filmDbService.getPopularFilms(10, null, null).toArray(),
                new FilmDto[] {film1, film2, film3, film4, film5, film6, film7, film9, film8, film10});
        assertArrayEquals(filmDbService.getPopularFilms(10, 1, null).toArray(),
                new FilmDto[] {film1, film3, film6, film8});
        assertArrayEquals(filmDbService.getPopularFilms(10, null, 2022).toArray(),
                new FilmDto[] {film2, film6, film10});
        assertArrayEquals(filmDbService.getPopularFilms(10, 1, 2023).toArray(),
                new FilmDto[] {film1, film3});
    }
}