package com.petclinic.visits.visitsservicenew.BusinessLayer.Review;

import com.petclinic.visits.visitsservicenew.DataLayer.Review.Review;
import com.petclinic.visits.visitsservicenew.DataLayer.Review.ReviewRepository;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceUnitTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    Review review1 = Review.builder()
            .id(UUID.randomUUID().toString())
            .reviewId(UUID.randomUUID().toString())
            .rating(5)
            .reviewerName("John Doe")
            .review("Excellent service")
            .dateSubmitted(LocalDateTime.now())
            .build();

    Review review2 = Review.builder()
            .id(UUID.randomUUID().toString())
            .reviewId(UUID.randomUUID().toString())
            .rating(4)
            .reviewerName("Jane Doe")
            .review("Very good experience")
            .dateSubmitted(LocalDateTime.now())
            .build();

    Review review3 = Review.builder()
            .id(UUID.randomUUID().toString())
            .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .reviewId(UUID.randomUUID().toString())
            .rating(4)
            .reviewerName("Jane Doe")
            .review("Very good experience")
            .dateSubmitted(LocalDateTime.now())
            .build();

    Review review4 = Review.builder()
            .id(UUID.randomUUID().toString())
            .ownerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .reviewId(UUID.randomUUID().toString())
            .rating(4)
            .reviewerName("Jane Doe")
            .review("Very good experience")
            .dateSubmitted(LocalDateTime.now())
            .build();

    @Test
    public void whenGetAllReviews_thenReturnReviews() {
        // Arrange
        when(reviewRepository.findAll()).thenReturn(Flux.just(review1, review2));

        // Act
        Flux<ReviewResponseDTO> result = reviewService.GetAllReviews();

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(reviewResponseDTO -> reviewResponseDTO.getReviewId().equals(review1.getReviewId()))
                .expectNextMatches(reviewResponseDTO -> reviewResponseDTO.getReviewId().equals(review2.getReviewId()))
                .verifyComplete();
    }

    @Test
    public void whenGetReviewByReviewId_thenReturnReview() {
        // Arrange
        when(reviewRepository.findReviewByReviewId(review1.getReviewId())).thenReturn(Mono.just(review1));

        // Act
        Mono<ReviewResponseDTO> result = reviewService.GetReviewByReviewId(review1.getReviewId());

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(reviewResponseDTO -> reviewResponseDTO.getReviewId().equals(review1.getReviewId()))
                .verifyComplete();
    }

  /*  @Test
    public void whenReviewIdDoesNotExistOnGetById_returnNotFound() {
        // Arrange
        String nonExistentReviewId = UUID.randomUUID().toString();
        when(reviewRepository.findReviewByReviewId(nonExistentReviewId)).thenReturn(Mono.empty());

        // Act
        Mono<ReviewResponseDTO> result = reviewService.GetReviewByReviewId(nonExistentReviewId);

        // Assert
        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Review id not found: " + nonExistentReviewId))
                .verify();
    }*/

    @Test
    public void whenAddReview_thenReturnReviewResponseDTO() {
        // Arrange
        when(reviewRepository.save(any(Review.class))).thenReturn(Mono.just(review1));

        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setRating(review1.getRating());
        reviewRequestDTO.setReviewerName(review1.getReviewerName());
        reviewRequestDTO.setReview(review1.getReview());
        reviewRequestDTO.setDateSubmitted(review1.getDateSubmitted());

        // Act
        Mono<ReviewResponseDTO> result = reviewService.AddReview(Mono.just(reviewRequestDTO));

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(reviewResponseDTO -> {
                    assertNotNull(reviewResponseDTO);
                    assertEquals(reviewResponseDTO.getReviewId(), review1.getReviewId());
                    assertEquals(reviewResponseDTO.getRating(), review1.getRating());
                    assertEquals(reviewResponseDTO.getReviewerName(), review1.getReviewerName());
                    assertEquals(reviewResponseDTO.getReview(), review1.getReview());
                    assertEquals(reviewResponseDTO.getDateSubmitted(), review1.getDateSubmitted());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenUpdateReview_thenReturnUpdatedReviewResponseDTO() {
        // Arrange
        String existingReviewId = review1.getReviewId();
        Review updatedReview = Review.builder()
                .id(review1.getId())
                .reviewId(existingReviewId)
                .rating(3) // Updated rating
                .reviewerName("John Smith") // Updated reviewer name
                .review("Good service") // Updated review text
                .dateSubmitted(review1.getDateSubmitted())
                .build();

        ReviewRequestDTO updatedReviewRequestDTO = new ReviewRequestDTO();
        updatedReviewRequestDTO.setRating(updatedReview.getRating());
        updatedReviewRequestDTO.setReviewerName(updatedReview.getReviewerName());
        updatedReviewRequestDTO.setReview(updatedReview.getReview());
        updatedReviewRequestDTO.setDateSubmitted(updatedReview.getDateSubmitted());

        when(reviewRepository.findReviewByReviewId(existingReviewId)).thenReturn(Mono.just(review1));
        when(reviewRepository.save(any(Review.class))).thenReturn(Mono.just(updatedReview));

        // Act
        Mono<ReviewResponseDTO> result = reviewService.UpdateReview(Mono.just(updatedReviewRequestDTO), existingReviewId);

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(reviewResponseDTO -> {
                    assertNotNull(reviewResponseDTO);
                    assertEquals(updatedReview.getReviewId(), reviewResponseDTO.getReviewId());
                    assertEquals(updatedReview.getRating(), reviewResponseDTO.getRating());
                    assertEquals(updatedReview.getReviewerName(), reviewResponseDTO.getReviewerName());
                    assertEquals(updatedReview.getReview(), reviewResponseDTO.getReview());
                    assertEquals(updatedReview.getDateSubmitted(), reviewResponseDTO.getDateSubmitted());
                    return true;
                })
                .verifyComplete();
    }

 /*   @Test
    public void whenReviewIdDoesNotExistOnUpdate_thenReturnNotFoundException() {
        // Arrange
        String nonExistentReviewId = UUID.randomUUID().toString();
        ReviewRequestDTO updatedReviewRequestDTO = new ReviewRequestDTO();
        // Populate updatedReviewRequestDTO with necessary data if needed.

        when(reviewRepository.findReviewByReviewId(nonExistentReviewId)).thenReturn(Mono.empty());

        // Act
        Mono<ReviewResponseDTO> result = reviewService.UpdateReview(Mono.just(updatedReviewRequestDTO), nonExistentReviewId);

        // Assert
        StepVerifier
                .create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("Review id not found: " + nonExistentReviewId)
                )
                .verify();
    }*/

    @Test
    public void whenDeleteReviewByReviewId_thenDeleteReview() {
        // Arrange
        when(reviewRepository.findReviewByReviewId(review1.getReviewId())).thenReturn(Mono.just(review1));
        // Mock the correct `delete` method, ensuring it returns a valid Mono
        when(reviewRepository.delete(review1)).thenReturn(Mono.empty());

        // Act
        Mono<ReviewResponseDTO> result = reviewService.DeleteReview(review1.getReviewId());

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(reviewResponseDTO -> reviewResponseDTO.getReviewId().equals(review1.getReviewId()))
                .verifyComplete();
    }


    @Test
    public void whenReviewIdDoesNotExistOnDelete_thenReturnNotFound() {
        // Arrange
        String nonExistentReviewId = UUID.randomUUID().toString();
        when(reviewRepository.findReviewByReviewId(nonExistentReviewId)).thenReturn(Mono.empty());

        // Act
        Mono<ReviewResponseDTO> result = reviewService.DeleteReview(nonExistentReviewId);

        // Assert
        StepVerifier
                .create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("review id is not found: " + nonExistentReviewId)  // Ensure the message matches the service
                )
                .verify();
    }

//    @Test
//    void getAllReviewsByOwnerId_shouldReturnEmptyFlux_whenNoReviewsFound() {
//        String ownerId = "nonExistentOwnerId";
//        when(reviewRepository.findAllByOwnerId(ownerId)).thenReturn(Flux.empty());
//
//        Flux<ReviewResponseDTO> result = reviewService.GetAllReviewsByOwnerId(ownerId);
//
//        StepVerifier.create(result)
//                .verifyComplete();
//    }

//    @Test
//    void getAllReviewsByOwnerId_shouldReturnError_whenRepositoryFails() {
//        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
//        when(reviewRepository.findAllByOwnerId(ownerId)).thenReturn(Flux.error(new RuntimeException("Database error")));
//
//        Flux<ReviewResponseDTO> result = reviewService.GetAllReviewsByOwnerId(ownerId);
//
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Database error"))
//                .verify();
//    }

//    @Test
//    void addReview_shouldReturnError_whenRepositoryFails() {
//        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
//        ReviewRequestDTO reviewRequestDTO = ReviewRequestDTO.builder()
//                .review("Great service!")
//                .rating(5)
//                .build();
//
//        when(reviewRepository.save(any(Review.class))).thenReturn(Mono.error(new RuntimeException("Database error")));
//
//        Mono<ReviewResponseDTO> result = reviewService.addReview(ownerId, Mono.just(reviewRequestDTO));
//
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Database error"))
//                .verify();
//    }

//    @Test
//    void deleteReview_shouldDeleteReviewSuccessfully() {
//        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
//        String reviewId = "reviewId123";
//        Review review = new Review();
//        review.setReviewId(reviewId);
//        review.setOwnerId(ownerId);
//
//        ReviewResponseDTO reviewResponseDTO = ReviewResponseDTO.builder()
//                .reviewId(reviewId)
//                .ownerId(ownerId)
//                .build();
//
//        when(reviewRepository.findReviewByOwnerIdAndReviewId(ownerId, reviewId)).thenReturn(Mono.just(review));
//        when(reviewRepository.delete(review)).thenReturn(Mono.empty());
//
//        Mono<ReviewResponseDTO> result = reviewService.deleteReview(ownerId, reviewId);
//
//        StepVerifier.create(result)
//                .expectNextMatches(response -> response.getReviewId().equals(reviewId) && response.getOwnerId().equals(ownerId))
//                .verifyComplete();
//    }

//    @Test
//    void deleteReview_shouldReturnError_whenRepositoryFails() {
//        String ownerId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";
//        String reviewId = "reviewId123";
//        Review review = new Review();
//        review.setReviewId(reviewId);
//        review.setOwnerId(ownerId);
//
//        when(reviewRepository.findReviewByOwnerIdAndReviewId(ownerId, reviewId)).thenReturn(Mono.just(review));
//        when(reviewRepository.delete(review)).thenReturn(Mono.error(new RuntimeException("Database error")));
//
//        Mono<ReviewResponseDTO> result = reviewService.deleteReview(ownerId, reviewId);
//
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Database error"))
//                .verify();
//    }



}