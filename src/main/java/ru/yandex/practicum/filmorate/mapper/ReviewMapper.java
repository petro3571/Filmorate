package ru.yandex.practicum.filmorate.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.NewReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

@AllArgsConstructor
@Repository
public class ReviewMapper {

    public Review mapFromCreateDto(NewReviewDto newReviewDto) {
        Review review = new Review();
        review.setContent(newReviewDto.getContent());
        review.setPositive(newReviewDto.getIsPositive());
        review.setUserId(newReviewDto.getUserId());
        review.setFilmId(newReviewDto.getFilmId());
        return review;
    }

    public ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setContent(review.getContent());
        dto.setIsPositive(review.isPositive());
        dto.setReviewId(review.getId());
        dto.setUseful(review.getUseful());
        dto.setFilmId(review.getFilmId());
        dto.setUserId(review.getUserId());
        return dto;
    }
}