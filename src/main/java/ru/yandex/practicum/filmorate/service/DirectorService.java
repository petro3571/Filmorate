package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<DirectorDto> getAll() {
        Collection<Director> directors = directorStorage.getAll();
        if (directors.isEmpty()) {
            return new ArrayList<>();
        }

        return directors.stream().map(DirectorMapper::mapToDirectorDto).collect(Collectors.toList());
    }

    public DirectorDto getById(Long id) {
        return directorStorage.getDirector(id).map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException("Режиссер с ID " + id + " не найден."));
    }

    public DirectorDto create(Director director) {
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя режиссёра не может быть пустым или состоять только из пробелов");
        }
        Director createdDirector = directorStorage.create(director);
        return DirectorMapper.mapToDirectorDto(createdDirector);
    }

    public DirectorDto update(Director director) {
        getById(director.getId());
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя режиссёра не может быть пустым или состоять только из пробелов");
        }
        Director updatedDirector = directorStorage.update(director);
        return DirectorMapper.mapToDirectorDto(updatedDirector);
    }

    public void delete(Long id) {
        getById(id); // проверяем существование
        directorStorage.delete(id);
    }
}