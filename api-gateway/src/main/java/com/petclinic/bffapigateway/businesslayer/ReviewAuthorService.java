package com.petclinic.bffapigateway.businesslayer;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAuthorService {
    private final AuthServiceClient authServiceClient;
    private final CustomersServiceClient customersServiceClient;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    public Mono<RatingResponseModel> enrich(RatingResponseModel rating) {
        rating.setReviewerUsername("Customer");
        rating.setReviewerPhoto(null);
        String customerId = rating.getCustomerId();
        if (customerId == null || customerId.isBlank()) {
            return Mono.just(rating);
        }
        // Resolve current profile data on every review fetch, rather than storing a stale snapshot.
        return authServiceClient.getPublicUserProfile(customerId)
                .timeout(Duration.ofSeconds(3))
                .flatMap(user -> {
                    if (user.username() != null && !user.username().isBlank()) {
                        rating.setReviewerUsername(user.username());
                    }
                    return customersServiceClient.getCustomer(customerId, true)
                            .timeout(Duration.ofSeconds(3))
                            .map(owner -> {
                                var photo = owner.getPhoto();
                                if (photo != null && photo.getFileData() != null
                                        && photo.getFileData().length > 0 && IMAGE_TYPES.contains(photo.getFileType() == null ? "" : photo.getFileType())) {
                                    rating.setReviewerPhoto("data:" + photo.getFileType() + ";base64,"
                                            + Base64.getEncoder().encodeToString(photo.getFileData()));
                                }
                                return rating;
                            })
                            .defaultIfEmpty(rating);
                })
                .defaultIfEmpty(rating)
                .onErrorResume(error -> {
                    log.warn("Could not load reviewer profile ({})", error.getClass().getSimpleName());
                    return Mono.just(rating);
                });
    }
}
