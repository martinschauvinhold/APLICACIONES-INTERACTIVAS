package com.uade.tpo.demo.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.Review;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.ReviewRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.ReviewRepository;
import com.uade.tpo.demo.repository.UserRepository;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public ArrayList<Review> getReviews() {
        return new ArrayList<>(reviewRepository.findAll());
    }

    public Optional<Review> getReviewById(int reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public List<Review> getReviewsByProduct(int productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product", productId);
        }
        return reviewRepository.findByProductId(productId);
    }

    @Transactional
    public Review createReview(ReviewRequest reviewRequest) {
        User user = userRepository.findById(reviewRequest.getUserId())
                .orElseThrow(() -> new NotFoundException("User", reviewRequest.getUserId()));
        Product product = productRepository.findById(reviewRequest.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", reviewRequest.getProductId()));
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .createdAt(new Date())
                .build();
        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(int reviewId, ReviewRequest reviewRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review", reviewId));
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(int reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
