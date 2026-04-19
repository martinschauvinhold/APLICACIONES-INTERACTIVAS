package com.uade.tpo.demo.controllers;

import java.net.URI;
import java.util.ArrayList;
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
import com.uade.tpo.demo.service.ReviewService;

@RestController
@RequestMapping("reviews")
public class ReviewsController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ArrayList<Review>> getReviews() {
        return ResponseEntity.ok(reviewService.getReviews());
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReviewById(@PathVariable int reviewId) {
        Optional<Review> result = reviewService.getReviewById(reviewId);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable int productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('buyer')")
    public ResponseEntity<Object> createReview(@RequestBody ReviewRequest reviewRequest) {
        Review result = reviewService.createReview(reviewRequest);
        return ResponseEntity.created(URI.create("/reviews/" + result.getId())).body(result);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('buyer', 'admin')")
    public ResponseEntity<Object> updateReview(@PathVariable int reviewId, @RequestBody ReviewRequest reviewRequest) {
        Optional<Review> result = reviewService.getReviewById(reviewId);
        if (result.isPresent()) {
            Review updated = reviewService.updateReview(reviewId, reviewRequest);
            return ResponseEntity.ok(updated);
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
