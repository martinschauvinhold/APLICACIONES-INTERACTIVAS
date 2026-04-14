package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.uade.tpo.demo.entity.Review;
import com.uade.tpo.demo.entity.dto.ReviewRequest;

public interface ReviewService {
    public ArrayList<Review> getReviews();

    public Optional<Review> getReviewById(int reviewId);

    public List<Review> getReviewsByProduct(int productId);

    public Review createReview(ReviewRequest reviewRequest);

    public Review updateReview(int reviewId, ReviewRequest reviewRequest);

    public void deleteReview(int reviewId);
}
