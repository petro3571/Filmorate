package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage {

    private static final String INSERT_QUERY = "INSERT INTO reviews (user_id, film_id, positive, content)" +
            " VALUES (?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM reviews r WHERE r.id = ?";
    private static final String SELECT_QUERY = "SELECT r.*, (SELECT COUNT(*) FROM review_likes WHERE review_id = r.id) - " +
            " (SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.id) AS useful " +
            "FROM reviews r " +
            "WHERE r.id = ? ";
    private static final String SELECT_ALL_QUERY = "SELECT r.*, (SELECT COUNT(*) FROM review_likes WHERE review_id = r.id) - " +
            " (SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.id) AS useful " +
            "FROM reviews r " +
            "GROUP BY r.id " +
            "ORDER BY useful DESC " +
            "LIMIT ?";
    private static final String SELECT_ALL_BY_FILM_ID_QUERY = "SELECT r.*, (SELECT COUNT(*) FROM review_likes WHERE review_id = r.id) - " +
            " (SELECT COUNT(*) FROM review_dislikes WHERE review_id = r.id) AS useful " +
            "FROM reviews r " +
            "WHERE r.film_id = ? " +
            "GROUP BY r.id " +
            "ORDER BY useful DESC " +
            "LIMIT ?";
    private static final String UPDATE_QUERY = "UPDATE reviews r SET user_id = ?, film_id = ?, positive = ?, " +
            " content = ? WHERE r.id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)";
    private static final String ADD_DISLIKE_QUERY = "INSERT INTO review_dislikes (review_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM review_likes r WHERE r.review_id = ? AND r.user_id = ?";
    private static final String DELETE_DISLIKE_QUERY = "DELETE FROM review_dislikes r WHERE r.review_id = ? AND r.user_id = ?";
    private static final String EXISTS_LIKE_QUERY = "SELECT 1 FROM review_likes r WHERE r.user_id = ? AND r.review_id = ?";
    private static final String EXISTS_DISLIKE_QUERY = "SELECT 1 FROM review_dislikes r WHERE r.user_id = ? AND r.review_id = ?";
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final ReviewRowMapper reviewRowMapper;

    // Создание нового отзыва + запись в ленту событий
    public Review createReview(Review review) {

        long id = insert(INSERT_QUERY,
                review.getUserId(),
                review.getFilmId(),
                review.isPositive(),
                review.getContent());
        review.setId(id);

        String feedQuery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) " +
                "VALUES (?,?,?,?,?)";
        update(feedQuery, review.getUserId(), Instant.now().toEpochMilli(), review.getId(), 2, 2);

        return review;
    }

    public void addLike(Long reviewId, Long userId) {
        jdbcTemplate.update(ADD_LIKE_QUERY, reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        jdbcTemplate.update(ADD_DISLIKE_QUERY, reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        jdbcTemplate.update(DELETE_LIKE_QUERY, reviewId, userId);
    }

    public void deleteDislike(Long reviewId, Long userId) {
        jdbcTemplate.update(DELETE_DISLIKE_QUERY, reviewId, userId);
    }

    // Проверка лайка
    public boolean existsLike(Long reviewId, Long userId) {
        try {
            Integer result = jdbcTemplate.queryForObject(
                    EXISTS_LIKE_QUERY,
                    Integer.class,
                    userId,
                    reviewId
            );
            return result != 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    // Проверка дизлайка
    public boolean existsDislike(Long reviewId, Long userId) {
        try {
            Integer result = jdbcTemplate.queryForObject(
                    EXISTS_DISLIKE_QUERY,
                    Integer.class,
                    userId,
                    reviewId
            );
            return result != 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    // Обновление отзыва + запись в ленту событий
    public Review updateReview(Review review) {
        update(UPDATE_QUERY, review.getUserId(), review.getFilmId(), review.isPositive(), review.getContent(),
                review.getId());

        String feedQuery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) " +
                "VALUES (?,?,?,?,?)";
        update(feedQuery, review.getUserId(), Instant.now().toEpochMilli(), review.getId(), 2, 3);

        return review;
    }

    // Удаление отзыва
    public void delete(Long reviewId) {
        Optional<Review> review = getReview(reviewId);

        if (review.isPresent()) {
            Review findReview = review.get();
            String feedQuery = "INSERT INTO feeds (user_id, timestamp, entity_id, event_type_id, event_operation_id) " +
                    "VALUES (?,?,?,?,?)";
            update(feedQuery, findReview.getUserId(), Instant.now().toEpochMilli(), findReview.getId(), 2, 1);
        }

        delete(DELETE_QUERY, reviewId);
    }


    // Получение одного отзыва
    public Optional<Review> getReview(Long reviewId) {
        try {
            Review review = jdbcTemplate.queryForObject(SELECT_QUERY, reviewRowMapper, reviewId);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Все отзывы
    public Collection<Review> getAllReviews(int count) {
        return jdbcTemplate.query(SELECT_ALL_QUERY, reviewRowMapper, count);
    }

    // Отзывы по фильму
    public Collection<Review> getAllReviewsByFilmId(Long filmId, int count) {
        return jdbcTemplate.query(SELECT_ALL_BY_FILM_ID_QUERY, reviewRowMapper, filmId, count);
    }

    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    private void update(String query, Object... params) {
        int rowsUpdated = jdbcTemplate.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    private void delete(String query, long id) {
        int rowsDeleted = jdbcTemplate.update(query, id);
    }
}