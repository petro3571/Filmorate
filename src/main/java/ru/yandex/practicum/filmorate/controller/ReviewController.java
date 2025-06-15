package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewDto;
import ru.yandex.practicum.filmorate.service.ReviewDbService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reviews")
@Validated
public class ReviewController {

    private final ReviewDbService reviewDbService;

    @GetMapping
    public Collection<ReviewDto> getReviews(@RequestParam(required = false) Long filmId,
                                            @RequestParam(defaultValue = "10") int count) {
        return reviewDbService.getReviews(filmId, count);
    }

    @GetMapping(path = "/{id}")
    public ReviewDto getReview(@PathVariable Long id) {
        return reviewDbService.getReview(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto createReview(@RequestBody @Valid NewReviewDto reviewDto) {
        return reviewDbService.createReview(reviewDto);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewDbService.deleteReview(id);
    }

    @PutMapping
    public ReviewDto updateReview(@RequestBody @Valid UpdateReviewDto reviewDto) {
        return reviewDbService.updateReview(reviewDto);
    }

    @PutMapping(path = "{id}/like/{userId}")
    public void likeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewDbService.addLike(id, userId);
    }

    @PutMapping(path = "{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable Long id, @PathVariable Long userId) {
        reviewDbService.addDislike(id, userId);
    }

    @DeleteMapping(path = "{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        reviewDbService.removeLike(id, userId);
    }

    @DeleteMapping(path = "{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Long id, @PathVariable Long userId) {
        reviewDbService.removeDislike(id, userId);
    }
}
