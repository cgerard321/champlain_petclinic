package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Cart.AddProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.condition.Not;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        final CartResponseDTO cartResponseDTO = mockCartServiceClient.createCart(cartRequestDTO);
        assertEquals(cartResponseDTO.getCartId(), "98f7b33a-d62a-420a-a84a-05a27c85fc91");
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
                    assertEquals(400, exception.getRawStatusCode()); // Assert that we got a 400 status
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
                    assertEquals(404, exception.getRawStatusCode());
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

}
