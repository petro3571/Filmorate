package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FeedRowMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private static final String QUERY = "SELECT f.event_id,f.user_id,f.timestamp,f.entity_id, et.name as event_type, " +
            "o.name as event_operation FROM feeds f join event_type as et on f.event_type_id = et.id join operations " +
            "as o on f.event_operation_id = o.id WHERE f.user_id = ?";

    private final JdbcTemplate jdbc;
    private final FeedRowMapper feedRowMapper;

    // Получение ленты событий для конкретного пользователя
    @Override
    public Collection<Feed> getFeedUser(Long userId) {
        return jdbc.query(QUERY, feedRowMapper, userId);
    }
}