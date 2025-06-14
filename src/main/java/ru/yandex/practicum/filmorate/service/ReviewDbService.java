package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dto.NewReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewDbService {

    private final ReviewDbStorage reviewDbStorage;
    private final ReviewMapper reviewMapper;
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    public Collection<ReviewDto> getReviews(Long filmId, int count) {
        if (filmId != null) {
            if (!filmDbStorage.exists(filmId)) {
                throw new NotFoundException("Неверно задан id фильма");
            }
            return reviewDbStorage.getAllReviewsByFilmId(filmId, count).stream()
                    .map(reviewMapper::mapToDto)
                    .collect(Collectors.toList());
        }
        return reviewDbStorage.getAllReviews(count).stream()
                .map(reviewMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public ReviewDto getReview(Long reviewId) {
        Review review = reviewDbStorage.getReview(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id: " + reviewId + "не найден"));
        return reviewMapper.mapToDto(review);
    }

    public ReviewDto createReview(NewReviewDto reviewDto) {
        boolean existsFilm = filmDbStorage.exists(reviewDto.getFilmId());
        boolean existsUser = filmDbStorage.exists(reviewDto.getUserId());
        if (!existsFilm || !existsUser) {
            throw new NotFoundException("Неверные id пользователя или фильма");
        }
        try {
            Review review = reviewDbStorage.createReview(reviewMapper.mapFromCreateDto(reviewDto));
            return reviewMapper.mapToDto(review);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Неверные данные");
        }

    }

    public ReviewDto updateReview(UpdateReviewDto reviewDto) {
        Review oldReview = reviewDbStorage.getReview(reviewDto.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id: " + reviewDto.getReviewId() + "не найден"));
        if (reviewDto.getIsPositive() != null && reviewDto.getIsPositive() != oldReview.isPositive()) {
            oldReview.setPositive(reviewDto.getIsPositive());
        }
        if (reviewDto.getContent() != null && !oldReview.getContent().equals(reviewDto.getContent())) {
            oldReview.setContent(reviewDto.getContent());
        }
        return reviewMapper.mapToDto(reviewDbStorage.updateReview(oldReview));
    }

    public void deleteReview(Long reviewId) {
        reviewDbStorage.delete(reviewId);
    }

    public void addLike(Long reviewId, Long userId) {
        if (reviewDbStorage.existsDislike(reviewId, userId)) {
            removeDislike(reviewId, userId);
        }
        try {
            reviewDbStorage.addLike(reviewId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Пользователь уже поставил лайк этому отзыву");
        }

    }

    public void removeLike(Long reviewId, Long userId) {
        reviewDbStorage.deleteLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {

        if (reviewDbStorage.existsLike(reviewId, userId)) {
            removeLike(reviewId, userId);
        }
        try {
            reviewDbStorage.addDislike(reviewId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Пользователь уже поставил дизлайк этому отзыву");
        }

    }

    public void removeDislike(Long reviewId, Long userId) {
        reviewDbStorage.deleteDislike(reviewId, userId);
    }

}
