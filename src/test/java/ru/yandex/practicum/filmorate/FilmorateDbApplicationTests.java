package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.*;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmDbService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, DirectorDbStorage.class, MpaDbStorage.class, UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class, FilmDbService.class, DirectorService.class})
class FilmorateDbApplicationTests {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final DirectorDbStorage directorDbStorage;
    private final FilmDbService filmDbService;

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

        Director director = new Director();
        director.setName("Test Director");
        Director createdDirector = directorDbStorage.create(director);

        testFilm.setDirectors(Set.of(createdDirector));

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
        FilmDto film11 = filmDbService.getFilm(11L);
        assertArrayEquals(filmDbService.getPopularFilms(10, null, null).toArray(),
                new FilmDto[] {film11, film1, film2, film3, film4, film5, film6, film7, film9, film8});
        assertArrayEquals(filmDbService.getPopularFilms(10, 1, null).toArray(),
                new FilmDto[] {film11, film1, film3, film6, film8});
        assertArrayEquals(filmDbService.getPopularFilms(10, null, 2022).toArray(),
                new FilmDto[] {film2, film6, film10});
        assertArrayEquals(filmDbService.getPopularFilms(10, 1, 2023).toArray(),
                new FilmDto[] {film11,film1, film3});

    }

    @Test
    @Sql(scripts = "/testdata.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGetGenres() {
        System.out.println(filmDbService.getAll());
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
        film1.setGenres(new HashSet<>());
        film1.setDirectors(new HashSet<>());

        film2.setGenres(new HashSet<>());
        film2.setDirectors(new HashSet<>());

        Film createdFilm2 = filmDbStorage.create(film2);

        Collection<Film> searchResult = filmDbStorage.searchFilms("one", List.of("title"));
        assertEquals(1, searchResult.size());
        assertTrue(searchResult.stream().anyMatch(f -> f.getName().equals("Test Film One")));

    }
}