package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mapper.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, DirectorDbStorage.class, MpaDbStorage.class,
        UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class, DirectorRowMapper.class, FilmService.class, DirectorService.class})
class FilmorateDbApplicationTests {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final DirectorDbStorage directorDbStorage;
    private final FilmService filmService;

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

    /**
     * Тест работы с пользователями:
     * 1. Создание пользователя
     * 2. Получение пользователя по ID
     * 3. Получение всех пользователей
     * 4. Обновление пользователя
     * 5. Добавление и удаление друга
     * Проверяет корректность выполнения всех операций
     */
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

    /**
     * Тест работы с фильмами:
     * 1. Создание фильма с режиссером
     * 2. Получение фильма по ID
     * 3. Получение всех фильмов
     * 4. Обновление фильма
     * 5. Добавление лайка и получение популярных фильмов
     * Проверяет корректность выполнения всех операций
     */
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

    /**
     * Тест проверяет корректность получения популярных фильмов с различными параметрами:
     * 1. Без фильтров (топ-10)
     * 2. С фильтром по жанру
     * 3. С фильтром по году выпуска
     * 4. С фильтрами по жанру и году одновременно
     * Использует SQL-скрипты для заполнения тестовых данных перед тестом и очистки после
     */
    @Test
    @Sql(scripts = "/testdata.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGetCorrectPopularFilms() {
        FilmDto film1 = filmService.getFilm(1L);
        FilmDto film2 = filmService.getFilm(2L);
        FilmDto film3 = filmService.getFilm(3L);
        FilmDto film4 = filmService.getFilm(4L);
        FilmDto film5 = filmService.getFilm(5L);
        FilmDto film6 = filmService.getFilm(6L);
        FilmDto film7 = filmService.getFilm(7L);
        FilmDto film8 = filmService.getFilm(8L);
        FilmDto film9 = filmService.getFilm(9L);
        FilmDto film10 = filmService.getFilm(10L);
        FilmDto film11 = filmService.getFilm(11L);
        assertArrayEquals(filmService.getPopularFilms(10, null, null).toArray(),
                new FilmDto[]{film11, film1, film2, film3, film4, film5, film6, film7, film9, film8});
        assertArrayEquals(filmService.getPopularFilms(10, 1, null).toArray(),
                new FilmDto[]{film11, film1, film3, film6, film8});
        assertArrayEquals(filmService.getPopularFilms(10, null, 2022).toArray(),
                new FilmDto[]{film2, film6, film10});
        assertArrayEquals(filmService.getPopularFilms(10, 1, 2023).toArray(),
                new FilmDto[]{film11, film1, film3});

    }

    /**
     * Тест для проверки получения всех жанров
     * Выводит список всех фильмов в консоль
     * Использует SQL-скрипты для заполнения тестовых данных перед тестом и очистки после
     */
    @Test
    @Sql(scripts = "/testdata.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldGetGenres() {
        System.out.println(filmService.getAll());
    }

    /**
     * Тест проверяет функциональность поиска фильмов:
     * 1. Создает два тестовых фильма
     * 2. Выполняет поиск по названию
     * 3. Проверяет, что найден только один филь фильм с искомым словом в названии
     */
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