package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Cart.*;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class CartServiceClientTest {

    @MockBean
    CartServiceClient mockCartServiceClient; // Mocking the CartServiceClient
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

        Mono<CartResponseDTO> cartResponseDTO = mockCartServiceClient.createCart((cartRequestDTO));

        StepVerifier.create(cartResponseDTO)
                .assertNext(result -> {
                    assertEquals("98f7b33a-d62a-420a-a84a-05a27c85fc91", result.getCartId());
                })
                .verifyComplete();
    }

    @Test
    void testClearCart_Success() {
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
        );

        Mono<Void> clearCartResponse = mockCartServiceClient.clearCart("98f7b33a-d62a-420a-a84a-05a27c85fc91");

        StepVerifier.create(clearCartResponse)
                .verifyComplete();
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
                .setBody("[]"));

        Flux<CartResponseDTO> result = mockCartServiceClient.getAllCarts();

        List<CartResponseDTO> carts = result.collectList().block();
        assertEquals(0, carts.size());
    }

    @Test
    void testAddProductToCart_Success() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        AddProductRequestDTO requestDTO = new AddProductRequestDTO("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 3);
        String responseBody = """
                {
                    "cartId": "98f7b33a-d62a-420a-a84a-05a27c85fc91",
                    "products": [
                        {
                            "productId": "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223",
                            "quantityInCart": 3
                        }
                    ]
                }
                """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.addProductToCart(cartId, requestDTO);

        StepVerifier.create(result)
                .assertNext(cartResponseDTO -> {
                    assertEquals(cartId, cartResponseDTO.getCartId());
                    assertEquals(1, cartResponseDTO.getProducts().size());
                    assertEquals("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", cartResponseDTO.getProducts().get(0).getProductId());
                    assertEquals(3, cartResponseDTO.getProducts().get(0).getQuantityInCart());
                })
                .verifyComplete();
    }



    @Test
    void testUpdateProductQuantityInCart_Success() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";
        UpdateProductQuantityRequestDTO requestDTO = new UpdateProductQuantityRequestDTO(5);
        String responseBody = """
                {
                    "cartId": "98f7b33a-d62a-420a-a84a-05a27c85fc91",
                    "products": [
                        {
                            "productId": "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223",
                            "quantityInCart": 5
                        }
                    ]
                }
                """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO);

        StepVerifier.create(result)
                .assertNext(cartResponseDTO -> {
                    assertEquals(cartId, cartResponseDTO.getCartId());
                    assertEquals(1, cartResponseDTO.getProducts().size());
                    assertEquals("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", cartResponseDTO.getProducts().get(0).getProductId());
                    assertEquals(5, cartResponseDTO.getProducts().get(0).getQuantityInCart());
                })
                .verifyComplete();
    }

    @Test
    void testUpdateProductQuantityInCart_OutOfStock_ThrowsException() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";
        UpdateProductQuantityRequestDTO requestDTO = new UpdateProductQuantityRequestDTO(15);
        String responseBody = """
            {
                "message": "You cannot set quantity more than 10 items. Only 10 items left in stock."
            }
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400) // Simulating a Bad Request response
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof WebClientResponseException;
                    WebClientResponseException exception = (WebClientResponseException) throwable;
                    assertEquals(400, exception.getStatusCode().value()); // Assert that we got a 400 status
                    assert exception.getResponseBodyAsString().contains("Only 10 items left in stock");
                })
                .verify();
    }

    @Test
    void testGetCartByCustomerId_Success() {
        String customerId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String responseBody = """
            {
                "cartId": "98f7b33a-d62a-420a-a84a-05a27c85fc91",
                "customerId": "customer1",
                "products": [
                    {
                        "productId": "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223",
                        "quantityInCart": 1
                    }
                ]
            }
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.getCartByCustomerId(customerId);

        StepVerifier.create(result)
                .assertNext(cartResponseDTO -> {
                    assertEquals("98f7b33a-d62a-420a-a84a-05a27c85fc91", cartResponseDTO.getCartId());
                    assertEquals("customer1", cartResponseDTO.getCustomerId());
                    assertEquals(1, cartResponseDTO.getProducts().size());
                    assertEquals("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", cartResponseDTO.getProducts().get(0).getProductId());
                    assertEquals(1, cartResponseDTO.getProducts().get(0).getQuantityInCart());
                })
                .verifyComplete();
    }

    @Test
    void testGetCartByCustomerId_NotFound() {
        String customerId = "non-existent-cart-id";
        String responseBody = """
            {
                "message": "Cart for customer id was not found: non-existent-cart-id"
            }
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.getCartByCustomerId(customerId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof WebClientResponseException;
                    WebClientResponseException exception = (WebClientResponseException) throwable;
                    assertEquals(404, exception.getStatusCode().value());
                    assert exception.getResponseBodyAsString().contains("Cart for customer id was not found");
                })
                .verify();
    }

    /*
    --------------------------------------------------------------------------------------------------------------------
        from here it's the tests related to the wishlist
    */

    @Test
    void MoveProductFromCartToWishlist_Success() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";
        String responseBody = """
                {
                    "cartId": "98f7b33a-d62a-420a-a84a-05a27c85fc91",
                    "products": [
                        {
                            "productId": "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223",
                            "quantityInCart": 1
                        }
                    ]
                }
                """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.moveProductFromCartToWishlist(cartId, productId);

        StepVerifier.create(result)
                .assertNext(cartResponseDTO -> {
                    assertEquals(cartId, cartResponseDTO.getCartId());
                    assertEquals(1, cartResponseDTO.getProducts().size());
                    assertEquals("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", cartResponseDTO.getProducts().get(0).getProductId());
                    assertEquals(1, cartResponseDTO.getProducts().get(0).getQuantityInCart());
                })
                .verifyComplete();
    }

    @Test
    void MoveProductFromWishListToCart_Success() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";
        String responseBody = """
                {
                    "cartId": "98f7b33a-d62a-420a-a84a-05a27c85fc91",
                    "products": [
                        {
                            "productId": "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223",
                            "quantityInCart": 1
                        }
                    ]
                }
                """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.moveProductFromWishListToCart(cartId, productId);

        StepVerifier.create(result)
                .assertNext(cartResponseDTO -> {
                    assertEquals(cartId, cartResponseDTO.getCartId());
                    assertEquals(1, cartResponseDTO.getProducts().size());
                    assertEquals("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", cartResponseDTO.getProducts().get(0).getProductId());
                    assertEquals(1, cartResponseDTO.getProducts().get(0).getQuantityInCart());
                })
                .verifyComplete();
    }

    /*
    --------------------------------------------------------------------------------------------------------------------
        from here it's the tests related to remove product from cart
    */

    @Test
    void testRemoveProductFromCart_Success() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";
        String responseBody = """
            {
                "cartId": "98f7b33a-d62a-420a-a84a-05a27c85fc91",
                "products": []
            }
            """;

        // Simulate a successful removal of a product
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.removeProductFromCart(cartId, productId);

        StepVerifier.create(result)
                .assertNext(cartResponseDTO -> {
                    assertEquals(cartId, cartResponseDTO.getCartId());
                    assertEquals(0, cartResponseDTO.getProducts().size()); // No products in the cart after removal
                })
                .verifyComplete();
    }

    @Test
    void testRemoveProductFromCart_NotFound() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String productId = "non-existent-product-id";
        String responseBody = """
            {
                "message": "Cart or product not found."
            }
            """;

        // Simulate a 404 Not Found response when trying to remove a product from a non-existent cart
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.removeProductFromCart(cartId, productId);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof NotFoundException;
                    NotFoundException exception = (NotFoundException) throwable;
                    assertEquals("Cart or product not found for cartId: " + cartId + " and productId: " + productId, exception.getMessage()); // Assert the 404 Not Found status
                })
                .verify();
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

    @Test
    void testGetActivePromos() {
        String responseBody = """
        [
            {
                "id": "promo1",
                "name": "Active Promo 1",
                "code": "ACTIVEPROMO1",
                "discount": 10.0,
                "expirationDate": "2024-12-31T23:59:59",
                "active": true
            },
            {
                "id": "promo2",
                "name": "Active Promo 2",
                "code": "ACTIVEPROMO2",
                "discount": 20.0,
                "expirationDate": "2024-12-31T23:59:59",
                "active": true
            }
        ]
        """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody));

        Flux<PromoCodeResponseDTO> result = mockCartServiceClient.getAllPromoCodes();  // Assuming active promos are fetched using this method

        List<PromoCodeResponseDTO> promos = result.collectList().block();

        assertEquals(2, promos.size());
        assertEquals("Active Promo 1", promos.get(0).getName());
        assertEquals("ACTIVEPROMO1", promos.get(0).getCode());
        assertEquals(true, promos.get(0).isActive());

        assertEquals("Active Promo 2", promos.get(1).getName());
        assertEquals("ACTIVEPROMO2", promos.get(1).getCode());
        assertEquals(true, promos.get(1).isActive());
    }

    @Test
    void testValidatePromoCode_Success() {
        String promoCode = "SUMMER2024";
        String responseBody = """
            {
                "id": "promo1",
                "name": "Summer Sale",
                "code": "SUMMER2024",
                "discount": 15.0,
                "expirationDate": "2024-12-31T23:59:59"
            }
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody)
                .setResponseCode(200));

        Mono<PromoCodeResponseDTO> result = mockCartServiceClient.validatePromoCode(promoCode);

        StepVerifier.create(result)
                .assertNext(promo -> {
                    assertEquals("promo1", promo.getId());
                    assertEquals("Summer Sale", promo.getName());
                    assertEquals("SUMMER2024", promo.getCode());
                    assertEquals(15.0, promo.getDiscount());
                    assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), promo.getExpirationDate());
                })
                .verifyComplete();
    }


    @Test
    void testValidatePromoCode_InvalidInput() {
        String promoCode = "INVALIDCODE";
        String responseBody = """
        {
            "message": "Invalid promo code provided."
        }
        """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody)
                .setResponseCode(400));

        Mono<PromoCodeResponseDTO> result = mockCartServiceClient.validatePromoCode(promoCode);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidInputException &&
                                throwable.getMessage().contains("Promo code is not valid: INVALIDCODE")
                )
                .verify();
    }

    @Test
    void testAddProductToCart_InvalidInput_ErrorFromResponse() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        AddProductRequestDTO requestDTO = new AddProductRequestDTO("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 3);
        String responseBody = """
            {
                "message": "Invalid product quantity."
            }
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400) // Simulate a 400 Bad Request
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.addProductToCart(cartId, requestDTO);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(InvalidInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Invalid product quantity.");
                })
                .verify();
    }

    @Test
    void testAddProductToCart_GenericErrorHandling() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        AddProductRequestDTO requestDTO = new AddProductRequestDTO("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 3);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500) // Simulate a 500 Internal Server Error
        );

        Mono<CartResponseDTO> result = mockCartServiceClient.addProductToCart(cartId, requestDTO);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(Exception.class);
                    assertThat(throwable.getMessage()).isEqualTo("An error occurred while adding product to cart");
                })
                .verify();
    }

    @Test
    void testClearCart_InvalidCartId() {
        String cartId = "invalid-cart-id";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(422) // Simulate a 422 Unprocessable Entity
                .setBody("""
                {
                    "message": "Cart id is invalid: invalid-cart-id"
                }
                """)
        );

        Mono<Void> clearCartResponse = mockCartServiceClient.clearCart(cartId);

        StepVerifier.create(clearCartResponse)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(InvalidInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Cart id is invalid: " + cartId);
                })
                .verify();
    }

    @Test
    void testClearCart_GenericClientError() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400) // Simulate a generic 400 Bad Request
        );

        Mono<Void> clearCartResponse = mockCartServiceClient.clearCart(cartId);

        StepVerifier.create(clearCartResponse)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Client error");
                })
                .verify();
    }
    @Test
    void testGetCartByCartId_Success() {
        String cartId = "c-200";
        String body = """
      {
        "cartId":"c-200",
        "customerId":"cust-xyz",
        "products":[]
      }
    """;

        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(body)
        );

        StepVerifier.create(mockCartServiceClient.getCartByCartId(cartId))
                .assertNext(resp -> {
                    // keep assertions generic to avoid DTO getter name mismatches
                    assert resp != null;
                })
                .verifyComplete();
    }

    @Test
    void testGetCartByCartId_NotFound404() {
        String cartId = "missing-1";
        String body = "{\"message\":\"Cart not found\"}";

        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody(body)
        );

        StepVerifier.create(mockCartServiceClient.getCartByCartId(cartId))
                .expectErrorSatisfies(ex ->
                        // tolerant assertion: your client throws org.webjars.NotFoundException
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("notfound"))
                .verify();
    }

    @Test
    void testGetCartByCartId_ServerError500() {
        String cartId = "c-err";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
        );

        StepVerifier.create(mockCartServiceClient.getCartByCartId(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument"))
                .verify();
    }



    @Test
    void testAddProductToCart_Created201() {
        String cartId = "c-100";
        String body = """
      {"cartId":"c-100","customerId":"u-1","products":[{"productId":"p-1","quantityInCart":2}]}
    """;

        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201)
                .setBody(body)
        );

        AddProductRequestDTO req = new AddProductRequestDTO();
        req.setProductId("p-1");
        req.setQuantity(2);

        StepVerifier.create(mockCartServiceClient.addProductToCart(cartId, req))
                .assertNext(resp -> { assert resp != null; })
                .verifyComplete();
    }




    @Test
    void testRemoveProductFromCart_ClientError400() {
        String cartId = "c-1";
        String productId = "p-x";

        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\":\"Bad request for remove\"}")
        );

        StepVerifier.create(mockCartServiceClient.removeProductFromCart(cartId, productId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .containsAnyOf("invalid","illegal"))
                .verify();
    }

    @Test
    void testClearCart_NoContent204() {
        String cartId = "c-2";

        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(204)
        );

        StepVerifier.create(mockCartServiceClient.clearCart(cartId))
                .verifyComplete();
    }



    @Test
    void testGetCartByCartId_Unprocessable422() {
        String cartId = "bad-id";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(422)
                .setBody("{\"message\":\"Cart id is invalid\"}")
        );

        StepVerifier.create(mockCartServiceClient.getCartByCartId(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("invalid"))
                .verify();
    }


    @Test
    void testRemoveProductFromCart_NotFound404() {
        String cartId = "c-1";
        String productId = "missing";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\":\"Cart or product not found\"}")
        );

        StepVerifier.create(mockCartServiceClient.removeProductFromCart(cartId, productId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("notfound"))
                .verify();
    }

    @Test
    void testRemoveProductFromCart_Unprocessable422() {
        String cartId = "c-1";
        String productId = "p-1";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(422)
                .setBody("{\"message\":\"Invalid input for cartId or productId\"}")
        );

        StepVerifier.create(mockCartServiceClient.removeProductFromCart(cartId, productId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("invalid"))
                .verify();
    }


    @Test
    void testDeleteCartByCartId_Unprocessable422() {
        String cartId = "bad-uuid";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(422)
                .setBody("{\"message\":\"Cart is invalid\"}")
        );

        StepVerifier.create(mockCartServiceClient.deleteCartByCartId(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("invalid"))
                .verify();
    }


    @Test
    void testRemoveProductFromWishlist_NotFound404() {
        String cartId = "c-99";
        String productId = "p-x";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\":\"Wishlist item not found\"}")
        );

        StepVerifier.create(mockCartServiceClient.removeProductFromWishlist(cartId, productId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("notfound"))
                .verify();
    }

    @Test
    void testRemoveProductFromWishlist_Unprocessable422() {
        String cartId = "c-99";
        String productId = "p-1";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(422)
                .setBody("{\"message\":\"Invalid input for cartId or productId\"}")
        );

        StepVerifier.create(mockCartServiceClient.removeProductFromWishlist(cartId, productId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("invalid"))
                .verify();
    }


    @Test
    void testClearCart_Unprocessable422() {
        String cartId = "bad";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(422)
                .setBody("{\"message\":\"Cart id is invalid\"}")
        );

        StepVerifier.create(mockCartServiceClient.clearCart(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("invalid"))
                .verify();
    }


    @Test
    void testGetActivePromos_NotFound404() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\":\"No active promos found\"}")
        );

        StepVerifier.create(mockCartServiceClient.getActivePromos().collectList())
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("notfound"))
                .verify();
    }

    @Test
    void testGetActivePromos_ServerError500() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\":\"oops\"}")
        );

        StepVerifier.create(mockCartServiceClient.getActivePromos().collectList())
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument"))
                .verify();
    }


    @Test
    void testAssignCartToUser_NotFound404() {
        String customerId = "missing-user";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\":\"Customer not found\"}")
        );

        StepVerifier.create(mockCartServiceClient.assignCartToUser(customerId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("notfound"))
                .verify();
    }

    @Test
    void testAssignCartToUser_Unprocessable422() {
        String customerId = "bad-user";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(422)
                .setBody("{\"message\":\"Invalid input for customerId\"}")
        );

        StepVerifier.create(mockCartServiceClient.assignCartToUser(customerId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("invalid"))
                .verify();
    }


    @Test
    void testAddProductToCart_BadRequest400_WithMessage() {
        String cartId = "c-100";
        // This hits: status=400 -> bodyToMono(CartResponseDTO) -> message != null -> InvalidInputException(message)
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\":\"Only 10 items left in stock\"}")
        );

        AddProductRequestDTO req = new AddProductRequestDTO();
        req.setProductId("p-1");
        req.setQuantity(999);

        StepVerifier.create(mockCartServiceClient.addProductToCart(cartId, req))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                                .contains("Only 10 items"))
                .verify();
    }

    @Test
    void testAddProductToCart_BadRequest400_NoMessageInBody() {
        String cartId = "c-100";
        // This hits the else branch: message == null -> InvalidInputException("Invalid input")
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{}")
        );

        AddProductRequestDTO req = new AddProductRequestDTO();
        req.setProductId("p-1");
        req.setQuantity(999);

        StepVerifier.create(mockCartServiceClient.addProductToCart(cartId, req))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                                .contains("Invalid input"))
                .verify();
    }


    @Test
    void testAddProductToCartFromProducts_BadRequest400_WithMessage() {
        String cartId = "c-7";
        String productId = "p-77";

        // Mirrors code: 400 -> bodyToMono(CartResponseDTO) -> if message != null throw InvalidInput(message)
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\":\"Cannot add from products page\"}")
        );

        StepVerifier.create(mockCartServiceClient.addProductToCartFromProducts(cartId, productId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                                .contains("Cannot add from products page"))
                .verify();
    }


    @Test
    void testValidatePromoCode_BadRequest400() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\":\"Promo code is not valid\"}")
        );

        StepVerifier.create(mockCartServiceClient.validatePromoCode("BADCODE"))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("invalid"))
                .verify();
    }


    @Test
    void testCheckoutCart_Success200() {
        String cartId = "c-55";
        String body = """
      {"cartId":"c-55","customerId":"u-22","products":[]}
    """;

        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(body)
        );

        StepVerifier.create(mockCartServiceClient.checkoutCart(cartId))
                .assertNext(resp -> { assert resp != null; })
                .verifyComplete();
    }
    @Test
    void testGetCartByCartId_ClientErrorElse_400() {
        String cartId = "c-else";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400) // BAD_REQUEST -> hits the 'else' in your 4xx handler
                .setBody("{\"message\":\"some client error\"}")
        );

        StepVerifier.create(mockCartServiceClient.getCartByCartId(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument")) // "Client error"
                .verify();
    }

    @Test
    void testDeleteCartByCartId_ClientErrorElse_401() {
        String cartId = "c-bad";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401) // neither 404 nor 422
                .setBody("{\"message\":\"unauthorized\"}")
        );

        StepVerifier.create(mockCartServiceClient.deleteCartByCartId(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument"))
                .verify();
    }

    @Test
    void testClearCart_ClientErrorElse_401() {
        String cartId = "c-err";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401)
                .setBody("{\"message\":\"unauthorized\"}")
        );

        StepVerifier.create(mockCartServiceClient.clearCart(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument"))
                .verify();
    }

    @Test
    void testGetActivePromos_ClientErrorElse_401() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401)
                .setBody("{\"message\":\"unauthorized\"}")
        );

        StepVerifier.create(mockCartServiceClient.getActivePromos().collectList())
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument"))
                .verify();
    }


    @Test
    void testValidatePromoCode_ClientErrorElse_401() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(401)
                .setBody("{\"message\":\"unauthorized\"}")
        );

        StepVerifier.create(mockCartServiceClient.validatePromoCode("SAVE10"))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument"))
                .verify();
    }


    @Test
    void testAddProductToCartFromProducts_BadRequest400_NoMessage() {
        String cartId = "c-7";
        String productId = "p-77";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{}")
        );

        StepVerifier.create(mockCartServiceClient.addProductToCartFromProducts(cartId, productId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
                                .contains("Invalid input"))
                .verify();
    }

    @Test
    void testAddProductToWishList_DoOnError_5xx() {
        String cartId = "c-1";
        String productId = "p-1";
        int quantity = 3;

        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500) // retrieve() -> error -> doOnError path executed
                .setBody("{\"message\":\"server\"}")
        );

        StepVerifier.create(mockCartServiceClient.addProductToWishList(cartId, productId, quantity))
                .expectError() // we just need the error to flow to trigger doOnError
                .verify();
    }

    @Test
    void testMoveProductFromCartToWishlist_DoOnError_5xx() {
        String cartId = "c-err";
        String productId = "p-err";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\":\"server\"}")
        );

        StepVerifier.create(mockCartServiceClient.moveProductFromCartToWishlist(cartId, productId))
                .expectError()
                .verify();
    }

    @Test
    void testCreateCart_BadRequest400() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\"message\":\"Invalid\"}")
        );

        StepVerifier.create(mockCartServiceClient.createCart(CartRequestDTO.builder().customerId("u-1").build()))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex)
                                .isInstanceOf(com.petclinic.bffapigateway.exceptions.InvalidInputException.class))
                .verify();
    }

    @Test
    void testCreateCart_NotFound404() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\":\"missing related\"}")
        );

        StepVerifier.create(mockCartServiceClient.createCart(CartRequestDTO.builder().customerId("u-2").build()))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("notfound"))
                .verify();
    }

    @Test
    void testCreateCart_ServerError500() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
        );

        StepVerifier.create(mockCartServiceClient.createCart(CartRequestDTO.builder().customerId("u-3").build()))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName())
                                .containsIgnoringCase("IllegalArgument"))
                .verify();
    }

    @Test
    void testMoveProductFromWishListToCart_DoOnError_5xx() {
        String cartId = "c-err";
        String productId = "p-err";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(500)
                .setBody("{\"message\":\"server\"}")
        );

        StepVerifier.create(mockCartServiceClient.moveProductFromWishListToCart(cartId, productId))
                .expectError()
                .verify();
    }

    @Test
    void testMoveAllWishlistToCart_Success() {
        String body = """
      {"cartId":"c-x","customerId":"u-x","products":[]}
    """;
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(body)
        );

        StepVerifier.create(mockCartServiceClient.moveAllWishlistToCart("c-x"))
                .assertNext(resp -> { assertThat(resp).isNotNull(); })
                .verifyComplete();
    }

    @Test
    void testMoveAllWishlistToCart_NotFound404() {
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\":\"wishlist empty\"}")
        );

        StepVerifier.create(mockCartServiceClient.moveAllWishlistToCart("missing"))
                .expectErrorSatisfies(ex -> {
                    org.assertj.core.api.Assertions.assertThat(ex)
                            .isInstanceOf(org.springframework.web.reactive.function.client.WebClientResponseException.class);
                    org.assertj.core.api.Assertions.assertThat(
                            ((org.springframework.web.reactive.function.client.WebClientResponseException) ex)
                                    .getStatusCode()
                                    .value())
                            .isEqualTo(404);
                })
                .verify();
    }

    @Test
    void testAddProductToWishList_Success() {
        String cartId = "c-3";
        String productId = "p-3";
        int qty = 1;
        String body = """
      {"cartId":"c-3","customerId":"u","products":[{"productId":"p-3","quantityInCart":1}]}
    """;
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(body)
        );

        StepVerifier.create(mockCartServiceClient.addProductToWishList(cartId, productId, qty))
                .assertNext(resp -> { assertThat(resp).isNotNull(); })
                .verifyComplete();
    }

    @Test
    void testRemoveProductFromWishlist_Success() {
        String cartId = "c-4";
        String productId = "p-4";
        String body = """
      {"cartId":"c-4","customerId":"u","products":[]}
    """;
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(body)
        );

        StepVerifier.create(mockCartServiceClient.removeProductFromWishlist(cartId, productId))
                .assertNext(resp -> { assertThat(resp).isNotNull(); })
                .verifyComplete();
    }

    @Test
    void testDeleteCartByCartId_NotFound404() {
        String cartId = "c-404";
        prepareResponse(r -> r
                .setHeader("Content-Type", "application/json")
                .setResponseCode(404)
                .setBody("{\"message\":\"not found\"}")
        );

        StepVerifier.create(mockCartServiceClient.deleteCartByCartId(cartId))
                .expectErrorSatisfies(ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getClass().getSimpleName().toLowerCase())
                                .contains("notfound"))
                .verify();
    }
    @Test
    void testGetRecentPurchases_Success() throws IOException {
        // Arrange
        String cartId = "test-cart-id";
        String responseBody = "[{\"productId\":\"prod1\",\"productName\":\"Product 1\",\"productSalePrice\":10.0,\"quantityInCart\":2,\"productQuantity\":5}," +
                "{\"productId\":\"prod2\",\"productName\":\"Product 2\",\"productSalePrice\":20.0,\"quantityInCart\":1,\"productQuantity\":3}]";

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        CartServiceClient client = new CartServiceClient(
                WebClient.builder(),
                mockWebServer.getHostName(),
                String.valueOf(mockWebServer.getPort())
        );

        // Act
        Mono<List<CartProductResponseDTO>> resultMono = client.getRecentPurchases(cartId);

        // Assert
        StepVerifier.create(resultMono)
                .expectNextMatches(products -> {
                    assertThat(products).hasSize(2);
                    assertThat(products.get(0).getProductId()).isEqualTo("prod1");
                    assertThat(products.get(1).getProductId()).isEqualTo("prod2");
                    return true;
                })
                .verifyComplete();
    }
    @Test
    void testGetRecommendationPurchases_ReturnsProducts() {
        String host = "localhost";
        String port = String.valueOf(mockWebServer.getPort());
        WebClient.Builder webClientBuilder = WebClient.builder();

        String responseBody = "[{\"productId\":\"prod1\",\"imageId\":null,\"productName\":null,\"productDescription\":null,\"productSalePrice\":null,\"averageRating\":null,\"quantityInCart\":null,\"productQuantity\":null}]";
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        CartServiceClient client = new CartServiceClient(webClientBuilder, host, port);

        Mono<List<CartProductResponseDTO>> result = client.getRecommendationPurchases("test-cart-id");

        StepVerifier.create(result)
                .expectNextMatches(list -> list.size() == 1 && "prod1".equals(list.get(0).getProductId()))
                .verifyComplete();
    }
    @Test
    void testGetRecommendationPurchases_ReturnsEmpty() {
        String host = "localhost";
        String port = String.valueOf(mockWebServer.getPort());
        WebClient.Builder webClientBuilder = WebClient.builder();

        String responseBody = "[]";
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        CartServiceClient client = new CartServiceClient(webClientBuilder, host, port);

        Mono<List<CartProductResponseDTO>> result = client.getRecommendationPurchases("empty-cart-id");

        StepVerifier.create(result)
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

}
