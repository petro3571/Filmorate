package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setId(rs.getLong("event_id"));
        feed.setUserId(rs.getLong("user_id"));
        LocalDate newLocalDate = rs.getDate("event_date").toLocalDate();
        feed.setEvent_date(newLocalDate);
        feed.setEntity_id(rs.getInt("entity_id"));
        feed.setEvent_type(rs.getString("event_type"));
        feed.setEvent_operation(rs.getString("event_operation"));

        return feed;
    }
}
