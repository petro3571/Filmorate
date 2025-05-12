package ru.yandex.practicum.filmorate.exception;

public class IdValidationException extends RuntimeException {
    public IdValidationException(String message) {
        super(message);
    }
}