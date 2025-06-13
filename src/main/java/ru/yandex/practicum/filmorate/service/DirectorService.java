package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director getById(Long id) {
        return directorStorage.getDirector(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + id + " не найден."));
    }

    public Director create(Director director) {
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя режиссёра не может быть пустым или состоять только из пробелов");
        }
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        getById(director.getId()); // проверяем существование
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя режиссёра не может быть пустым или состоять только из пробелов");
        }
        return directorStorage.update(director);
    }

    public void delete(Long id) {
        getById(id); // проверяем существование
        directorStorage.delete(id);
    }
}