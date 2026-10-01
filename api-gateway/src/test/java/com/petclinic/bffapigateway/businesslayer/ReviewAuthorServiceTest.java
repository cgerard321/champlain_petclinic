package com.petclinic.bffapigateway.businesslayer;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.PublicUserProfile;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewAuthorServiceTest {
    @Mock AuthServiceClient auth;
    @Mock CustomersServiceClient customers;
    @InjectMocks ReviewAuthorService service;

    private RatingResponseModel rating() {
        return RatingResponseModel.builder().customerId("author-id").rating((byte) 5).review("Great!").build();
    }

    private void username(String name) {
        when(auth.getPublicUserProfile("author-id"))
                .thenReturn(Mono.just(new PublicUserProfile(name)));
    }

    @Test
    void returnsCurrentUsernameAndPhotoAndReflectsUpdatesOnNextFetch() {
        when(auth.getPublicUserProfile("author-id"))
                .thenReturn(Mono.just(new PublicUserProfile("before")),
                        Mono.just(new PublicUserProfile("after")));
        when(customers.getCustomer("author-id", true)).thenReturn(
                Mono.just(CustomerResponseDTO.builder().photo(FileDetails.builder()
                        .fileType("image/png").fileData(new byte[]{1, 2, 3}).build()).build()),
                Mono.just(CustomerResponseDTO.builder().photo(FileDetails.builder()
                        .fileType("image/jpeg").fileData(new byte[]{4, 5, 6}).build()).build()));
        StepVerifier.create(service.enrich(rating())).assertNext(result -> {
            assertEquals("before", result.getReviewerUsername());
            assertEquals("data:image/png;base64,AQID", result.getReviewerPhoto());
            assertEquals("Great!", result.getReview());
            assertEquals("author-id", result.getCustomerId());
        }).verifyComplete();
        StepVerifier.create(service.enrich(rating())).assertNext(result -> {
            assertEquals("after", result.getReviewerUsername());
            assertEquals("data:image/jpeg;base64,BAUG", result.getReviewerPhoto());
        }).verifyComplete();
    }

    @Test
    void keepsUsernameWhenNoPhotoExists() {
        username("reviewer");
        when(customers.getCustomer("author-id", true)).thenReturn(Mono.just(new CustomerResponseDTO()));
        StepVerifier.create(service.enrich(rating())).assertNext(result -> {
            assertEquals("reviewer", result.getReviewerUsername());
            assertNull(result.getReviewerPhoto());
        }).verifyComplete();
    }

    @Test
    void keepsReviewWhenUserLookupFails() {
        when(auth.getPublicUserProfile("author-id")).thenReturn(Mono.error(new RuntimeException("unavailable")));
        StepVerifier.create(service.enrich(rating())).assertNext(result -> {
            assertEquals("Customer", result.getReviewerUsername());
            assertEquals("Great!", result.getReview());
            assertNull(result.getReviewerPhoto());
        }).verifyComplete();
        verifyNoInteractions(customers);
    }

    @Test
    void keepsUsernameWhenPhotoLookupFails() {
        username("reviewer");
        when(customers.getCustomer("author-id", true)).thenReturn(Mono.error(new RuntimeException("unavailable")));
        StepVerifier.create(service.enrich(rating())).assertNext(result -> {
            assertEquals("reviewer", result.getReviewerUsername());
            assertNull(result.getReviewerPhoto());
        }).verifyComplete();
    }

    @Test
    void doesNotEmbedNonImageContent() {
        username("reviewer");
        when(customers.getCustomer("author-id", true)).thenReturn(Mono.just(CustomerResponseDTO.builder()
                .photo(FileDetails.builder().fileType("text/html").fileData(new byte[]{1}).build()).build()));
        StepVerifier.create(service.enrich(rating()))
                .assertNext(result -> assertNull(result.getReviewerPhoto())).verifyComplete();
    }

    @Test
    void supportsLegacyReviewsWithoutCustomerId() {
        var review = rating();
        review.setCustomerId(null);
        StepVerifier.create(service.enrich(review)).assertNext(result -> {
            assertEquals("Customer", result.getReviewerUsername());
            assertEquals("Great!", result.getReview());
        }).verifyComplete();
        verifyNoInteractions(auth, customers);
    }
}
