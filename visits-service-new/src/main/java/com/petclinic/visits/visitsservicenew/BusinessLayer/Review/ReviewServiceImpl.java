package com.petclinic.visits.visitsservicenew.BusinessLayer.Review;

import com.petclinic.visits.visitsservicenew.DataLayer.Review.ReviewRepository;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Customers.CustomerServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Customers.CustomerResponseDTO;
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
                .flatMap(request -> customerServiceClient.getCustomerByCustomerId(request.getReviewerId())
                        .thenReturn(request))
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
                        .flatMap(request -> customerServiceClient.getCustomerByCustomerId(request.getReviewerId())
                                .thenReturn(request))
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

    private Mono<ReviewResponseDTO> toReviewResponseDTO(
            com.petclinic.visits.visitsservicenew.DataLayer.Review.Review review) {
        return customerServiceClient.getCustomerByCustomerId(review.getReviewerId())
                .map(customer -> {
                    ReviewResponseDTO response = EntityDtoUtil.toReviewResponseDTO(review);
                    response.setReviewerName(
                            ((customer.getFirstName() == null ? "" : customer.getFirstName()) + " "
                                    + (customer.getLastName() == null ? "" : customer.getLastName())).trim());
                    return response;
                });
    }
}
