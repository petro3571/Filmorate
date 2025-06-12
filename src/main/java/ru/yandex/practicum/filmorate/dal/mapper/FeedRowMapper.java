package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEventId(rs.getLong("event_id"));
        feed.setUserId(rs.getLong("user_id"));
        feed.setTimestamp(rs.getLong("timestamp"));
        feed.setEntityId(rs.getInt("entity_id"));
        feed.setEventType(rs.getString("event_type"));
        feed.setOperation(rs.getString("event_operation"));

        return feed;
    }
}
