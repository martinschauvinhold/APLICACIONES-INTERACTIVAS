package com.uade.tpo.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.tpo.demo.entity.Product;
import com.uade.tpo.demo.entity.Review;
import com.uade.tpo.demo.entity.User;
import com.uade.tpo.demo.entity.dto.ReviewRequest;
import com.uade.tpo.demo.exceptions.NotFoundException;
import com.uade.tpo.demo.repository.ProductRepository;
import com.uade.tpo.demo.repository.ReviewRepository;
import com.uade.tpo.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void getReviews_deberiaRetornarListaCompleta() {
        // Arrange
        var reviews = List.of(
                Review.builder().id(1).rating(5).comment("Excelente").build(),
                Review.builder().id(2).rating(3).comment("Regular").build());
        when(reviewRepository.findAll()).thenReturn(reviews);

        // Act
        var result = reviewService.getReviews();

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getReviewById_deberiaRetornarResena_cuandoIdExiste() {
        // Arrange
        var review = Review.builder().id(1).rating(5).comment("Excelente").build();
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        // Act
        var result = reviewService.getReviewById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getRating()).isEqualTo(5);
    }

    @Test
    void getReviewById_deberiaRetornarVacio_cuandoIdNoExiste() {
        // Arrange
        when(reviewRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        var result = reviewService.getReviewById(99);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getReviewsByProduct_deberiaRetornarResenasFiltradas() {
        // Arrange
        var product = Product.builder().id(1).build();
        var reviews = List.of(
                Review.builder().id(1).product(product).rating(5).build(),
                Review.builder().id(2).product(product).rating(4).build());
        when(productRepository.existsById(1)).thenReturn(true);
        when(reviewRepository.findByProductId(1)).thenReturn(reviews);

        // Act
        var result = reviewService.getReviewsByProduct(1);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getReviewsByProduct_deberiaLanzarNotFoundException_cuandoProductoNoExiste() {
        // Arrange
        when(productRepository.existsById(99)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> reviewService.getReviewsByProduct(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createReview_deberiaGuardarYRetornarResena_cuandoUserYProductoExisten() {
        // Arrange
        var user = User.builder().id(1).build();
        var product = Product.builder().id(2).build();
        var request = new ReviewRequest();
        request.setUserId(1);
        request.setProductId(2);
        request.setRating(5);
        request.setComment("Muy bueno");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(productRepository.findById(2)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = reviewService.createReview(request);

        // Assert
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Muy bueno");
        verify(reviewRepository).save(any());
    }

    @Test
    void createReview_deberiaLanzarNotFoundException_cuandoUserNoExiste() {
        // Arrange
        var request = new ReviewRequest();
        request.setUserId(99);
        request.setProductId(1);
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createReview_deberiaLanzarNotFoundException_cuandoProductoNoExiste() {
        // Arrange
        var user = User.builder().id(1).build();
        var request = new ReviewRequest();
        request.setUserId(1);
        request.setProductId(99);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(productRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateReview_deberiaActualizarYRetornarResena_cuandoIdExiste() {
        // Arrange
        var review = Review.builder().id(1).rating(3).comment("Regular").build();
        var request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Mejoró mucho");
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = reviewService.updateReview(1, request);

        // Assert
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Mejoró mucho");
    }

    @Test
    void updateReview_deberiaLanzarNotFoundException_cuandoIdNoExiste() {
        // Arrange
        when(reviewRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.updateReview(99, new ReviewRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteReview_deberiaEliminar() {
        // Act
        reviewService.deleteReview(1);

        // Assert
        verify(reviewRepository).deleteById(1);
    }
}
