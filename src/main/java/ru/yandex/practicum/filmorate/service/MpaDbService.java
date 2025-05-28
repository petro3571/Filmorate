package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MpaDbService {
    private final MpaDbStorage mpaDbStorage;

    public MpaDbService(MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    public List<MpaDto> getAll() {
        return mpaDbStorage.getAll()
                .stream()
                .map(MpaMapper::mapToMpaDto)
                .collect(Collectors.toList());
    }

    public MpaDto getMpa(Integer mpaId) {
        return mpaDbStorage.getMpa(mpaId).map(MpaMapper::mapToMpaDto).orElseThrow(() -> new NotFoundException("Рейтинг с ID " +
                mpaId + " не найден."));
    }
}