package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.tpo.demo.entity.Review;
import com.uade.tpo.demo.entity.dto.ReviewRequest;
import com.uade.tpo.demo.entity.dto.ReviewResponse;
import com.uade.tpo.demo.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("reviews")
public class ReviewsController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews() {
        List<ReviewResponse> result = reviewService.getReviews().stream()
                .map(ReviewResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable int reviewId) {
        Optional<Review> result = reviewService.getReviewById(reviewId);
        return result.map(e -> ResponseEntity.ok(ReviewResponse.from(e)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(@PathVariable int productId) {
        List<ReviewResponse> result = reviewService.getReviewsByProduct(productId).stream()
                .map(ReviewResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<Object> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        Review result = reviewService.createReview(reviewRequest);
        return ResponseEntity.created(URI.create("/reviews/" + result.getId())).body(ReviewResponse.from(result));
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Object> updateReview(@PathVariable int reviewId, @Valid @RequestBody ReviewRequest reviewRequest) {
        Optional<Review> result = reviewService.getReviewById(reviewId);
        if (result.isPresent()) {
            Review updated = reviewService.updateReview(reviewId, reviewRequest);
            return ResponseEntity.ok(ReviewResponse.from(updated));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> deleteReview(@PathVariable int reviewId) {
        Optional<Review> result = reviewService.getReviewById(reviewId);
        if (result.isPresent()) {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
