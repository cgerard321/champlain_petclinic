package com.petclinic.visits.visitsservicenew.BusinessLayer.Review;

import com.petclinic.visits.visitsservicenew.DataLayer.Review.Review;
import com.petclinic.visits.visitsservicenew.DataLayer.Review.ReviewRepository;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Flux<ReviewResponseDTO> GetAllReviews() {
        return reviewRepository.findAll().map(EntityDtoUtil::toReviewResponseDTO);
    }




    @Override
    public Mono<ReviewResponseDTO> AddReview(Mono<ReviewRequestDTO> reviewRequestDTOMono) {

        return reviewRequestDTOMono
                .map(EntityDtoUtil::toReviewEntity)
                //.doOnNext(e-> e.setReviewId(EntityDtoUtil.generateReviewIdString()))
                .flatMap(reviewRepository::save)
                .map(EntityDtoUtil::toReviewResponseDTO);
    }

    @Override
    public Mono<ReviewResponseDTO> UpdateReview(Mono<ReviewRequestDTO> reviewRequestDTOMono, String reviewId) {
        return reviewRepository.findReviewByReviewId(reviewId)
                .switchIfEmpty(Mono.defer(()-> Mono.error(new NotFoundException("review id is not found: "+ reviewId))))
                .flatMap(found->reviewRequestDTOMono
                        .map(EntityDtoUtil::toReviewEntity)
                        .doOnNext(e->e.setReviewId(found.getReviewId()))
                        .doOnNext(e->e.setId(found.getId())))
                .flatMap(reviewRepository::save)
                .map(EntityDtoUtil::toReviewResponseDTO);
    }

    @Override
    public Mono<ReviewResponseDTO> DeleteReview(String reviewId) {
        return reviewRepository.findReviewByReviewId(reviewId)
                .switchIfEmpty(Mono.defer(()-> Mono.error(new NotFoundException("review id is not found: "+ reviewId))))
                .flatMap(found ->reviewRepository.delete(found)
                        .then(Mono.just(found)))
                .map(EntityDtoUtil::toReviewResponseDTO);
    }

    @Override
    public Mono<ReviewResponseDTO> GetReviewByReviewId(String reviewId) {
        return reviewRepository.findReviewByReviewId(reviewId)
                .switchIfEmpty(Mono.defer(()-> Mono.error(new NotFoundException("review id is not found: "+ reviewId))))
                .doOnNext(c-> log.debug("the review entity is: " + c.toString()))
                .map(EntityDtoUtil::toReviewResponseDTO);
    }

//    @Override
//    public Flux<ReviewResponseDTO> GetAllReviewsByOwnerId(String ownerId) {
//        return reviewRepository.findAllByOwnerId(ownerId)
//                .map(EntityDtoUtil::toReviewResponseDTO);
//    }

//    @Override
//    public Mono<ReviewResponseDTO> addReview(String ownerId, Mono<ReviewRequestDTO> reviewRequestDTOMono) {
//        return reviewRequestDTOMono.map(dto -> {
//                    Review review = new Review();
//                    review.setOwnerId(ownerId);
//                    review.setRating(dto.getRating());
//                    review.setReviewerName(dto.getReviewerName());
//                    review.setReview(dto.getReview());
//                    review.setDateSubmitted(dto.getDateSubmitted());
//
//                    review.setReviewId(UUID.randomUUID().toString());
//
//                    return review;
//                })
//                .flatMap(reviewRepository::save)
//                .map(EntityDtoUtil::toReviewResponseDTO);
//    }

//    @Override
//    public Mono<ReviewResponseDTO> deleteReview(String ownerId, String reviewId) {
//        return reviewRepository.findReviewByOwnerIdAndReviewId(ownerId, reviewId)
//                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Review not found for owner ID: " + ownerId + " and review ID: " + reviewId))))
//                .flatMap(found -> reviewRepository.delete(found).then(Mono.just(found)))
//                .map(EntityDtoUtil::toReviewResponseDTO);
//    }


}
