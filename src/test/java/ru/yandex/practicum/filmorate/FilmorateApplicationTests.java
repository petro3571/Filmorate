package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {
	UserController userController;
	FilmController filmController;

	@BeforeEach
	public void beaforeEach() {
		userController = new UserController(new UserService(new InMemoryUserStorage()));
		filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()));
	}

	@Test
	void contextLoads() {
	}

	@Test
	void shouldBeNameSameLogin() {
		User user = new User();
		user.setLogin("login");
		user.setEmail("test@example.com");
		user.setBirthday(LocalDate.of(1996,4,19));
		userController.addUser(user);
		assertEquals("login", user.getName());
	}

	@Test
	public void testUserUpdateNotFound() {
		User user = new User();
		user.setEmail("test@example.com");
		user.setLogin("login");
		user.setName("name");
		user.setBirthday(LocalDate.of(2000, 1, 1));

		assertThrows(NotFoundException.class, () -> userController.update(user));
	}

	@Test
	public void testFilmUpdateNotFound() {
		Film film = new Film();
		film.setId(1L);
		film.setDescription("Description");
		film.setDuration(1);
		film.setName("name");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));

		assertThrows(NotFoundException.class, () -> filmController.update(film));
	}

	@Test
	void shouldCreateUserWithEmptyName() {
		User user = new User();
		user.setLogin("testLogin");
		user.setEmail("test@example.com");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User createdUser = userController.addUser(user);
		assertEquals(user.getLogin(), createdUser.getName());
	}

	@Test
	void shouldCreateUserWithName() {
		User user = new User();
		user.setLogin("testLogin");
		user.setName("Test Name");
		user.setEmail("test@example.com");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		User createdUser = userController.addUser(user);
		assertEquals("Test Name", createdUser.getName());
	}

	@Test
	void shouldUpdateUser() {
		User user = new User();
		user.setLogin("testLogin");
		user.setEmail("test@example.com");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		User createdUser = userController.addUser(user);

		createdUser.setName("Updated Name");
		User updatedUser = userController.update(createdUser);

		assertEquals("Updated Name", updatedUser.getName());
	}

	@Test
	void shouldGetAllUsers() {
		User user1 = new User();
		user1.setLogin("user1");
		user1.setEmail("user1@example.com");
		user1.setBirthday(LocalDate.of(1990, 1, 1));
		userController.addUser(user1);

		User user2 = new User();
		user2.setLogin("user2");
		user2.setEmail("user2@example.com");
		user2.setBirthday(LocalDate.of(1995, 1, 1));
		userController.addUser(user2);

		Collection<User> users = userController.getAllUsers();
		assertEquals(2, users.size());
	}

	@Test
	void shouldAddAndDeleteFriend() {
		User user1 = new User();
		user1.setLogin("user1");
		user1.setEmail("user1@example.com");
		user1.setBirthday(LocalDate.of(1990, 1, 1));
		User createdUser1 = userController.addUser(user1);

		User user2 = new User();
		user2.setLogin("user2");
		user2.setEmail("user2@example.com");
		user2.setBirthday(LocalDate.of(1995, 1, 1));
		User createdUser2 = userController.addUser(user2);

		userController.addFriend(createdUser1.getId(), createdUser2.getId());
		Set<User> friends = userController.getUserFriends(createdUser1.getId());
		assertEquals(1, friends.size());

		userController.deleteFriend(createdUser1.getId(), createdUser2.getId());
		friends = userController.getUserFriends(createdUser1.getId());
		assertTrue(friends.isEmpty());
	}

	@Test
	void shouldCreateFilm() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Test Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		Film createdFilm = filmController.create(film);
		assertNotNull(createdFilm.getId());
	}

	@Test
	void shouldUpdateFilm() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Test Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);
		Film createdFilm = filmController.create(film);

		createdFilm.setName("Updated Film");
		Film updatedFilm = filmController.update(createdFilm);

		assertEquals("Updated Film", updatedFilm.getName());
	}

	@Test
	void shouldThrowWhenUserNotFound() {
		assertThrows(NotFoundException.class, () -> userController.getUser(999L));
	}

	@Test
	void shouldThrowWhenFilmNotFound() {
		assertThrows(NotFoundException.class, () -> filmController.getFilm(999L));
	}
}