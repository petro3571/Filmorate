package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.mapper.*;
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

    @BeforeEach
    void clearDatabase() {
        filmDbStorage.getAll().forEach(film -> filmDbStorage.deleteFilm(film.getId()));
        userDbStorage.getAll().forEach(userDbStorage::deleteUser);
    }

    @Test
    void shouldRunWorkWithUsers() {
        User testUser = new User();
        testUser.setEmail("test@ya.ru");
        testUser.setLogin("testLogin");
        testUser.setName("Test Name");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userDbStorage.saveUser(testUser);

        Optional<User> getUser = userDbStorage.getUser(createdUser.getId());

        Collection<User> users = userDbStorage.getAll();

        assertTrue(getUser.isPresent());
        assertEquals("Test Name", getUser.get().getName());

        assertFalse(users.isEmpty());

        createdUser.setName("New Test Name");

        User updateUser = userDbStorage.updateUser(createdUser);

        assertEquals("New Test Name", updateUser.getName());

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
        // Создаем тестового режиссера
        Director director = new Director();
        director.setName("Test Director");
        Director createdDirector = directorService.create(director);

        User testUser = new User();
        testUser.setEmail("test@ya.ru");
        testUser.setLogin("testLogin");
        testUser.setName("Test Name");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userDbStorage.saveUser(testUser);

        Film testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(new Mpa(1, null));
        testFilm.setDirectors(Set.of(createdDirector));

        Film createdFilm = filmDbStorage.create(testFilm);

        Optional<Film> getFilm = filmDbStorage.getFilm(createdFilm.getId());

        Collection<Film> films = filmDbStorage.getAll();

        assertFalse(films.isEmpty());

        assertTrue(getFilm.isPresent());
        assertEquals("Test Film", getFilm.get().getName());

        createdFilm.setName("Test Film Name");

        Film updatedFilm = filmDbStorage.update(createdFilm);

        assertEquals("Test Film Name", updatedFilm.getName());

        // Проверяем, что фильмов ровно 1 (т.к. база очищена)
        assertEquals(1, films.size());

        // Создаем второго режиссера
        Director director2 = new Director();
        director2.setName("Test Director 2");
        Director createdDirector2 = directorService.create(director2);

        Film testFilm2 = new Film();
        testFilm2.setName("Test Film2");
        testFilm2.setDescription("Test Description2");
        testFilm2.setReleaseDate(LocalDate.of(2001, 1, 1));
        testFilm2.setDuration(150);
        testFilm2.setMpa(new Mpa(2, null));
        testFilm2.setDirectors(Set.of(createdDirector2));

        Film newCreatedFilm = filmDbStorage.create(testFilm2);

        filmDbStorage.addLike(updatedFilm.getId(), createdUser.getId());

        Collection<Film> popularFilm = filmDbStorage.getPopularFilms(1);

        assertEquals(1, popularFilm.size());

        Film newFilm = popularFilm.iterator().next();

        assertEquals("Test Film Name", newFilm.getName());
    }

    @Test
    void shouldSearchFilmsByTitle() {
        // Создаем режиссера
        Director director = new Director();
        director.setName("Director One");
        Director createdDirector = directorService.create(director);

        Film film1 = createTestFilm("Test Film 1", "Description 1", Set.of(createdDirector));
        film1.setDirectors(Set.of(createdDirector));
        filmDbStorage.update(film1);

        Film film2 = createTestFilm("Another Film", "Description 2", Set.of(createdDirector));
        filmDbStorage.update(film2);

        Collection<Film> results = filmDbStorage.searchByTitle("test");
        assertEquals(1, results.size());
        assertTrue(results.stream().anyMatch(f -> f.getName().equals("Test Film 1")));
    }

    @Test
    void shouldSearchFilmsByDirector() {
        Director director = new Director();
        director.setName("Director One");
        Director createdDirector = directorService.create(director);

        Film film = createTestFilm("Film 1", "Description 1", Set.of(createdDirector));

        Collection<Film> results = filmDbStorage.searchByDirector("one");
        assertEquals(1, results.size());
        Film resultFilm = results.iterator().next();
        assertTrue(resultFilm.getDirectors().stream()
                .anyMatch(d -> d.getName().equals("Director One")));
    }

    @Test
    void shouldSearchFilmsByTitleAndDirector() {
        Director director = new Director();
        director.setName("Director One");
        Director createdDirector = directorService.create(director);

        Film film1 = createTestFilm("Test Film", "Description 1", Set.of(createdDirector));

        Film film2 = createTestFilm("Another Film", "Description 2", Set.of(createdDirector));

        Collection<Film> results = filmDbStorage.searchByTitleAndDirector("test");
        assertEquals(1, results.size());
        assertTrue(results.stream().anyMatch(f -> f.getName().equals("Test Film")));
    }

    private Film createTestFilm(String name, String description, Set<Director> directors) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));
        film.setDirectors(directors != null ? directors : new HashSet<>());
        film.setGenres(new HashSet<>());
        return filmDbStorage.create(film);
    }
}