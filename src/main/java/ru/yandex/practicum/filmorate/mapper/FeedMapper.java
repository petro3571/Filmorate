package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FeedDto;
import ru.yandex.practicum.filmorate.model.Feed;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedMapper {
    public static FeedDto mapToFeedDto(Feed feed) {
        FeedDto feedDto = new FeedDto();
        feedDto.setEventId(feed.getEventId());
        feedDto.setUserId(feed.getUserId());
        feedDto.setUserId(feed.getUserId());
        feedDto.setTimestamp(feed.getTimestamp());
        feedDto.setEntityId(feed.getEntityId());
        feedDto.setEventType(feed.getEventType());
        feedDto.setOperation(feed.getOperation());
        return feedDto;
    }
}