package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorServiceImpl implements DirectorService {
    private final DirectorStorage directorStorage;

    @Override
    public Collection<Director> getAll() {
        return directorStorage.getAll();
    }

    @Override
    public Director getDirector(Long id) {
        return directorStorage.getDirector(id);
    }

    @Override
    public Director create(Director director) {
        return directorStorage.create(director);
    }

    @Override
    public Director update(Director director) {
        return directorStorage.update(director);
    }

    @Override
    public void delete(Long id) {
        directorStorage.delete(id);
    }
}