package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartServiceClientTest {

    @MockBean
    CartServiceClient mockCartServiceClient;

    MockWebServer mockWebServer;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        mockCartServiceClient = new CartServiceClient(
                WebClient.builder(),
                mockWebServer.getHostName(),
                String.valueOf(mockWebServer.getPort())
        );
    }

    @AfterEach
    void shutdown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void testCreateCart() {
        String body = """
                  {
                    "cartId":"98f7b33a-d62a-420a-a84a-05a27c85fc91"
                  }
                """;

        CartRequestDTO cartRequestDTO = new CartRequestDTO();
        cartRequestDTO.setCustomerId("12345");

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final CartResponseDTO cartResponseDTO = mockCartServiceClient.createCart(cartRequestDTO);
        assertEquals(cartResponseDTO.getCartId(), "98f7b33a-d62a-420a-a84a-05a27c85fc91");
    }

    @Test
    void testClearCart_Success() {
        // Arrange the server response for clear cart action
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
        );

        // Act: Call the clearCart method
        Mono<Void> clearCartResponse = mockCartServiceClient.clearCart("98f7b33a-d62a-420a-a84a-05a27c85fc91");

        // Assert: Verify that the action completed successfully
        StepVerifier.create(clearCartResponse)
                .verifyComplete(); // Verify that no errors occurred
    }

    @Test
    void testGetAllCarts() {
        String responseBody = """
                [
                    {
                        "cartId": "cart1",
                        "customerId": "customer1"
                    },
                    {
                        "cartId": "cart2",
                        "customerId": "customer2"
                    }
                ]
                """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Flux<CartResponseDTO> result = mockCartServiceClient.getAllCarts();

        List<CartResponseDTO> carts = result.collectList().block();
        assertEquals(2, carts.size());
        assertEquals("cart1", carts.get(0).getCartId());
        assertEquals("cart2", carts.get(1).getCartId());
    }

    @Test
    void testGetAllCartsEmptyResponse() {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody("[]")); // Simulate an empty response

        Flux<CartResponseDTO> result = mockCartServiceClient.getAllCarts();

        List<CartResponseDTO> carts = result.collectList().block(); // Collect the results into a list
        assertEquals(0, carts.size()); // Assert that the list is empty
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.mockWebServer.enqueue(response);
    }

    @Test
    void testGetAllPromoCodes() {
        String responseBody = """
            [
                {
                    "id": "promo1",
                    "name": "Promo 1",
                    "code": "PROMO1",
                    "discount": 10.0,
                    "expirationDate": "2024-12-31T23:59:59"
                },
                {
                    "id": "promo2",
                    "name": "Promo 2",
                    "code": "PROMO2",
                    "discount": 20.0,
                    "expirationDate": "2024-12-31T23:59:59"
                }
            ]
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Flux<PromoCodeResponseDTO> result = mockCartServiceClient.getAllPromoCodes();

        List<PromoCodeResponseDTO> promos = result.collectList().block();

        assertEquals(2, promos.size());

        assertEquals("Promo 1", promos.get(0).getName());
        assertEquals("PROMO1", promos.get(0).getCode());
        assertEquals(10.0, promos.get(0).getDiscount());
        assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), promos.get(0).getExpirationDate());

        assertEquals("Promo 2", promos.get(1).getName());
        assertEquals("PROMO2", promos.get(1).getCode());
        assertEquals(20.0, promos.get(1).getDiscount());
        assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), promos.get(1).getExpirationDate());
    }
    @Test
    void testGetPromoCodeById() {
        String responseBody = """
            {
                "id": "promo1",
                "name": "Promo 1",
                "code": "PROMO1",
                "discount": 10.0,
                "expirationDate": "2024-12-31T23:59:59"
            }
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<PromoCodeResponseDTO> result = mockCartServiceClient.getPromoCodeById("promo1");

        StepVerifier.create(result)
                .expectNextMatches(promo ->
                        promo.getId().equals("promo1") &&
                                promo.getCode().equals("PROMO1") &&
                                promo.getExpirationDate().equals(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                )
                .verifyComplete();
    }

    @Test
    void testCreatePromoCode() {
        String responseBody = """
            {
                "id": "promo1",
                "name": "Promo 1",
                "code": "PROMO1",
                "discount": 10.0,
                "expirationDate": "2024-12-31T23:59:59"
            }
            """;

        PromoCodeRequestDTO promoCodeRequestDTO = new PromoCodeRequestDTO("Promo 1", "PROMO1", 10.0, "2024-12-31T23:59:59");

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<PromoCodeResponseDTO> result = mockCartServiceClient.createPromoCode(promoCodeRequestDTO);

        StepVerifier.create(result)
                .expectNextMatches(promo ->
                        promo.getId().equals("promo1") &&
                                promo.getCode().equals("PROMO1") &&
                                promo.getExpirationDate().equals(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                )
                .verifyComplete();
    }

    @Test
    void testUpdatePromoCode() {
        String responseBody = """
            {
                "id": "promo1",
                "name": "Updated Promo 1",
                "code": "PROMO1",
                "discount": 15.0,
                "expirationDate": "2024-12-31T23:59:59"
            }
            """;

        PromoCodeRequestDTO promoCodeRequestDTO = new PromoCodeRequestDTO("Updated Promo 1", "PROMO1", 15.0, "2024-12-31T23:59:59");

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<PromoCodeResponseDTO> result = mockCartServiceClient.updatePromoCode("promo1", promoCodeRequestDTO);

        StepVerifier.create(result)
                .expectNextMatches(promo ->
                        promo.getId().equals("promo1") &&
                                promo.getDiscount() == 15.0 &&
                                promo.getExpirationDate().equals(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                )
                .verifyComplete();
    }
    @Test
    void testDeletePromoCode() {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200));

        Mono<PromoCodeResponseDTO> result = mockCartServiceClient.deletePromoCode("12345");

        StepVerifier.create(result)
                .verifyComplete();
    }








}
