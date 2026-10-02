package com.petclinic.bffapigateway.presentationlayer.v1.Products;

import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Products.*;
import com.petclinic.bffapigateway.presentationlayer.v1.*;
import com.petclinic.bffapigateway.utils.Security.Filters.*;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = {ProductControllerV1.class, ImageControllerV1.class, RatingControllerV1.class},
        properties = "frontend.url=http://localhost:3000")
@Import({Utility.class, JwtTokenFilter.class, RoleFilter.class,
        com.petclinic.bffapigateway.businesslayer.ReviewAuthorService.class})
class GuestShopAccessTest {
    @Autowired WebTestClient client;
    @MockBean ProductsServiceClient products;
    @MockBean ImageServiceClient images;
    @MockBean RatingsServiceClient ratings;
    @MockBean AuthServiceClient auth;
    @MockBean CustomersServiceClient customers;
    @MockBean JwtTokenUtil tokens;

    @BeforeEach
    void setUp() {
        when(products.getAllProducts(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(ProductResponseDTO.builder().productId("sample").build()));
        when(products.getProductByProductId("sample"))
                .thenReturn(Mono.just(ProductResponseDTO.builder().productId("sample").build()));
        when(products.getAllProductBundles()).thenReturn(Flux.empty());
        when(products.getProductBundleById("sample"))
                .thenReturn(Mono.just(ProductBundleResponseDTO.builder().bundleId("sample").build()));
        when(images.getImageByImageId("sample"))
                .thenReturn(Mono.just(ImageResponseDTO.builder().imageId("sample").build()));
        when(ratings.getAllRatingsForProductId("sample")).thenReturn(Flux.empty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/products", "/products?minPrice=1&maxPrice=50", "/products/sample",
            "/products/bundles", "/products/bundles/sample", "/images/sample", "/ratings/product/sample"})
    void guestCanReadShopWithoutToken(String path) {
        client.get().uri("/api/gateway" + path).accept(MediaType.ALL).exchange().expectStatus().isOk();
        verifyNoInteractions(auth);
    }

    @Test
    void guestReceivesReviewerProfileWithoutLogin() {
        when(ratings.getAllRatingsForProductId("sample")).thenReturn(Flux.just(
                com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel.builder()
                        .customerId("reviewer").rating((byte) 5).review("Great").build()));
        when(auth.getPublicUserProfile("reviewer")).thenReturn(Mono.just(
                new com.petclinic.bffapigateway.dtos.Auth.PublicUserProfile("ReviewerName")));
        when(customers.getCustomer("reviewer", true)).thenReturn(Mono.just(
                com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO.builder()
                        .photo(com.petclinic.bffapigateway.dtos.Files.FileDetails.builder()
                                .fileType("image/png").fileData(new byte[]{1, 2, 3}).build()).build()));

        client.get().uri("/api/gateway/ratings/product/sample").accept(MediaType.TEXT_EVENT_STREAM).exchange()
                .expectStatus().isOk()
                .expectBodyList(com.petclinic.bffapigateway.dtos.Ratings.RatingResponseModel.class)
                .hasSize(1).value(reviews -> {
                    org.junit.jupiter.api.Assertions.assertEquals("ReviewerName", reviews.get(0).getReviewerUsername());
                    org.junit.jupiter.api.Assertions.assertEquals("data:image/png;base64,AQID", reviews.get(0).getReviewerPhoto());
                });
        verify(auth).getPublicUserProfile("reviewer");
        verifyNoMoreInteractions(auth);
    }

    @Test
    void guestCannotReadPersonalRating() {
        client.get().uri("/api/gateway/ratings/sample").accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isUnauthorized();
        verifyNoInteractions(ratings);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST /products", "PUT /products/sample", "DELETE /products/sample",
            "PATCH /products/sample/quantity", "POST /products/bundles", "PUT /products/bundles/sample",
            "DELETE /products/bundles/sample", "POST /ratings/sample", "PUT /ratings/sample", "DELETE /ratings/sample"})
    void guestCannotModifyShop(String operation) {
        String[] parts = operation.split(" ");
        client.method(HttpMethod.valueOf(parts[0])).uri("/api/gateway" + parts[1])
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}").exchange().expectStatus().isUnauthorized();
        verifyNoInteractions(products, ratings);
    }
}
