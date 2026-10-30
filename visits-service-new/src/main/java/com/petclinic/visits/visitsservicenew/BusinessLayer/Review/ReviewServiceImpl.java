package com.petclinic.visits.visitsservicenew.BusinessLayer.Review;

import com.petclinic.visits.visitsservicenew.DataLayer.Review.ReviewRepository;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Customers.CustomerServiceClient;
import com.petclinic.visits.visitsservicenew.Exceptions.BadRequestException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import com.petclinic.visits.visitsservicenew.Utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final CustomerServiceClient customerServiceClient;

    public ReviewServiceImpl(ReviewRepository reviewRepository, CustomerServiceClient customerServiceClient) {
        this.reviewRepository = reviewRepository;
        this.customerServiceClient = customerServiceClient;
    }

    @Override
    public Flux<ReviewResponseDTO> GetAllReviews() {
        return reviewRepository.findAll().flatMap(this::toReviewResponseDTO);
    }

    @Override
    public Mono<ReviewResponseDTO> AddReview(Mono<ReviewRequestDTO> reviewRequestDTOMono) {

        return reviewRequestDTOMono
                // The reviewer is the customer leaving the review, so fall back to the owner id when no reviewer id is sent
                .doOnNext(request -> request.setReviewerId(firstNonBlank(request.getReviewerId(), request.getOwnerId())))
                .flatMap(this::validateReviewer)
                .map(EntityDtoUtil::toReviewEntity)
                // Generate new ID with current date and incremented sequence
                .flatMap(review -> {
                    review.setReviewId(IdGenerator.generateReviewId());
                    return reviewRepository.save(review);
                })
                .flatMap(this::toReviewResponseDTO);
    }

    @Override
    public Mono<ReviewResponseDTO> UpdateReview(Mono<ReviewRequestDTO> reviewRequestDTOMono, String reviewId) {
        return reviewRepository.findReviewByReviewId(reviewId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("review id is not found: " + reviewId))))
                .flatMap(found -> reviewRequestDTOMono
                        .doOnNext(request -> request.setReviewerId(
                                firstNonBlank(request.getReviewerId(), found.getReviewerId(), found.getOwnerId())))
                        .flatMap(this::validateReviewer)
                        .map(request -> {
                            var review = EntityDtoUtil.toReviewEntity(request);
                            review.setReviewId(found.getReviewId());
                            review.setId(found.getId());
                            review.setOwnerId(found.getOwnerId());
                            review.setReviewerId(request.getReviewerId());
                            return review;
                        }))
                .flatMap(reviewRepository::save)
                .flatMap(this::toReviewResponseDTO);
    }

    @Override
    public Mono<ReviewResponseDTO> DeleteReview(String reviewId) {
        return reviewRepository.findReviewByReviewId(reviewId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("review id is not found: " + reviewId))))
                .flatMap(found -> toReviewResponseDTO(found)
                        .flatMap(response -> reviewRepository.delete(found).thenReturn(response)));
    }

    @Override
    public Mono<ReviewResponseDTO> GetReviewByReviewId(String reviewId) {
        return reviewRepository.findReviewByReviewId(reviewId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("review id is not found: " + reviewId))))
                .doOnNext(c -> log.debug("the review entity is: " + c.toString()))
                .flatMap(this::toReviewResponseDTO);
    }

    private Mono<ReviewRequestDTO> validateReviewer(ReviewRequestDTO request) {
        if (isBlank(request.getReviewerId())) {
            return Mono.error(new BadRequestException("reviewer id is required"));
        }
        return customerServiceClient.getCustomerByCustomerId(request.getReviewerId())
                .thenReturn(request);
    }

    private Mono<ReviewResponseDTO> toReviewResponseDTO(
            com.petclinic.visits.visitsservicenew.DataLayer.Review.Review review) {
        ReviewResponseDTO response = EntityDtoUtil.toReviewResponseDTO(review);
        // Older reviews have no reviewer id, and a deleted customer should not break the whole review list
        if (isBlank(review.getReviewerId())) {
            return Mono.just(response);
        }
        return customerServiceClient.getCustomerByCustomerId(review.getReviewerId())
                .map(customer -> {
                    response.setReviewerName(
                            ((customer.getFirstName() == null ? "" : customer.getFirstName()) + " "
                                    + (customer.getLastName() == null ? "" : customer.getLastName())).trim());
                    return response;
                })
                .onErrorResume(e -> {
                    log.warn("Could not load reviewer {} for review {}: {}",
                            review.getReviewerId(), review.getReviewId(), e.getMessage());
                    return Mono.just(response);
                })
                .defaultIfEmpty(response);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
