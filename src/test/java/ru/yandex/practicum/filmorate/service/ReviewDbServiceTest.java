package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.NewReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewDbService.class, ReviewDto.class, ReviewMapper.class, NewReviewDto.class, UpdateReviewDto.class,
        ReviewDbStorage.class, ReviewRowMapper.class, UserDbStorage.class, FilmDbStorage.class, UserRowMapper.class, FilmRowMapper.class})
@Sql(scripts = "/testdata.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ReviewDbServiceTest {

    private final ReviewDbService service;

    @Test
    void getReviews() {
        ReviewDto dto1 = service.getReview(1L);
        ReviewDto dto2 = service.getReview(2L);
        ReviewDto dto3 = service.getReview(3L);
        ReviewDto dto4 = service.getReview(4L);
        ReviewDto dto5 = service.getReview(5L);
        assertArrayEquals(new ReviewDto[]{dto5, dto2, dto1, dto4, dto3}, service.getReviews(null, 10).toArray());
        assertArrayEquals(new ReviewDto[]{dto2, dto1}, service.getReviews(1L, 10).toArray());
        assertTrue(service.getReviews(10L, 10).isEmpty()); // фильм, на который нет отзывов
        assertThrows(NotFoundException.class, () -> service.getReviews(12L, 10)); // фильм не существует
    }

    @Test
    void shouldCalculateCorrectUseful() {
        assertEquals(1, service.getReview(1L).getUseful());
        assertEquals(3, service.getReview(2L).getUseful());
        assertEquals(-1, service.getReview(3L).getUseful());
        assertEquals(0, service.getReview(4L).getUseful());
        assertEquals(4, service.getReview(5L).getUseful());
    }

    @Test
    void createAndGetReview() {
        NewReviewDto newReviewDto = new NewReviewDto();
        newReviewDto.setUserId(1L);
        newReviewDto.setFilmId(10L);
        newReviewDto.setContent("content");
        newReviewDto.setIsPositive(true);

        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setUserId(1L);
        reviewDto.setFilmId(10L);
        reviewDto.setContent("content");
        reviewDto.setIsPositive(true);

        ReviewDto addedReview = service.createReview(newReviewDto);
        assertNotNull(addedReview);
        assertEquals(addedReview.getFilmId(), reviewDto.getFilmId());
        assertEquals(service.getReview(addedReview.getReviewId()), addedReview);

        assertThrows(NotFoundException.class, () -> service.getReview(11L)); // поиск по не сущ. id

        NewReviewDto incorrectReviewDto = new NewReviewDto();
        incorrectReviewDto.setUserId(20L); // ставим id, который не существует
        incorrectReviewDto.setFilmId(10L);
        incorrectReviewDto.setContent("content");
        incorrectReviewDto.setIsPositive(false);

        assertThrows(NotFoundException.class, () -> service.createReview(incorrectReviewDto));

        NewReviewDto secondReviewDto = new NewReviewDto();
        secondReviewDto.setUserId(1L);
        secondReviewDto.setFilmId(10L);
        secondReviewDto.setContent("new content");
        secondReviewDto.setIsPositive(false);
        assertThrows(NotFoundException.class, () -> service.createReview(secondReviewDto));

        NewReviewDto reviewWithoutContent = new NewReviewDto();
        reviewWithoutContent.setUserId(3L);
        reviewWithoutContent.setFilmId(10L);
        reviewWithoutContent.setIsPositive(false);
        assertThrows(NotFoundException.class, () -> service.createReview(reviewWithoutContent));
    }

    @Test
    void updateReview() {
        ReviewDto dto = service.getReview(1L);
        UpdateReviewDto updateDto = new UpdateReviewDto();
        updateDto.setReviewId(dto.getReviewId());
        updateDto.setUserId(2L);
        updateDto.setFilmId(10L);
        updateDto.setContent("updated content");
        updateDto.setIsPositive(true);
        service.updateReview(updateDto);
        ReviewDto updatedReview = service.getReview(1L);
        assertNotEquals(updatedReview, dto);
        assertEquals(updatedReview.getFilmId(), updateDto.getFilmId());
        assertEquals(updatedReview.getContent(), updateDto.getContent());
        assertEquals(updatedReview.getUserId(), updateDto.getUserId());

    }

    @Test
    void deleteReview() {
        service.deleteReview(1L);
        assertEquals(4, service.getReviews(null, 10).size());
        assertThrows(NotFoundException.class, () -> service.getReview(1L));
    }

    @Test
    void shouldWorkWithLikesAndDislikes() {
        service.addLike(2L, 5L);
        assertEquals(4, service.getReview(2L).getUseful());

        service.addDislike(1L, 5L);
        assertEquals(0, service.getReview(1L).getUseful());

        service.removeLike(5L, 1L);
        assertEquals(3, service.getReview(5L).getUseful());

        service.removeDislike(3L, 2L);
        assertEquals(0, service.getReview(3L).getUseful());

        //если пользователь уже ставил лайк или дизайк этому фильму:
        assertThrows(IllegalStateException.class, () -> service.addDislike(1L, 5L));
    }
}