package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
class FilmorateApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void ShouldBeFailWithOver200LenghtDescritpion() throws ValidationException {
		FilmController filmController = new FilmController();
		Film film = new Film();
		film.setName("test");
		film.setDescription("test".repeat(100));
		film.setDuration(Duration.ofMinutes(90));
		film.setReleaseDate(LocalDate.of(2000,1,1));
		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	void ShouldBeFailWithNoAInMail() throws ValidationException {
		UserController userController = new UserController();
		User user = new User();
		user.setName("test");
		user.setLogin("testLogin");
		user.setEmail("dimapetro357gmail.com");
		user.setBirthday(LocalDate.of(1996,4,19));
		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	void ShouldBeNameSameLogin() {
		UserController userController = new UserController();
		User user = new User();
		user.setLogin("testLogin");
		user.setEmail("dimapetro357@gmail.com");
		user.setBirthday(LocalDate.of(1996,4,19));
		userController.create(user);
		assertEquals("?", "testLogin", user.getName());
	}

	@Test
	public void testUserUpdateNotFound() {
		UserController userController = new UserController();
		User user = new User();
		user.setEmail("test@example.com");
		user.setLogin("login");
		user.setName("name");
		user.setBirthday(LocalDate.of(2000, 1, 1));

		assertThrows(ValidationException.class, () -> userController.update(user));
	}

	@Test
	public void testFilmUpdateNotFound() {
		FilmController filmController = new FilmController();
		Film film = new Film();
		film.setId(1L);
		film.setDescription("Description");
		film.setDuration(Duration.ofMinutes(1));
		film.setName("name");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));

		assertThrows(NotFoundException.class, () -> filmController.update(film));
	}
}