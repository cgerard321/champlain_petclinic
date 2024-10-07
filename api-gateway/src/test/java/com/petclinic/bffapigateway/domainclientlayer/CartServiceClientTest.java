package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Cart.AddProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

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
    void testAddProductToCart_OutOfStock_ThrowsException() {
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        AddProductRequestDTO requestDTO = new AddProductRequestDTO("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 15);
        String responseBody = """
            {
                "message": "You cannot add more than 10 items. Only 10 items left in stock."
            }
            """;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody(responseBody));

        Mono<CartResponseDTO> result = mockCartServiceClient.addProductToCart(cartId, requestDTO);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assert throwable instanceof WebClientResponseException;
                    WebClientResponseException exception = (WebClientResponseException) throwable;
                    assertEquals(400, exception.getRawStatusCode());
                    assert exception.getResponseBodyAsString().contains("Only 10 items left in stock");
                })
                .verify();
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
        String cartId = "invalid-cart-id";
        String productId = "invalid-product-id";
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
                    assert throwable instanceof WebClientResponseException;
                    WebClientResponseException exception = (WebClientResponseException) throwable;
                    assertEquals(404, exception.getRawStatusCode()); // Assert the 404 Not Found status
                    assert exception.getResponseBodyAsString().contains("Cart or product not found");
                })
                .verify();
    }


    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.mockWebServer.enqueue(response);
    }


}
