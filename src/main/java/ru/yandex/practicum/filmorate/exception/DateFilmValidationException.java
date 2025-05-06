package ru.yandex.practicum.filmorate.exception;

public class DateFilmValidationException extends RuntimeException {
    public DateFilmValidationException(String message) {
        super(message);
    }
}