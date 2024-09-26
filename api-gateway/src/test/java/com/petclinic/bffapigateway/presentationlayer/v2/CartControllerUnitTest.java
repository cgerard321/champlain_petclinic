package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CartController.class, CartServiceClient.class, GlobalExceptionHandler.class })
@WebFluxTest(controllers = CartController.class)
@AutoConfigureWebTestClient
public class CartControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CartServiceClient cartServiceClient;

    private final String NON_EXISTING_CART_ID = "pe29fff7-564a-4cc9-8fe1-36f6ca9bc223";

    ProductResponseDTO product1 = ProductResponseDTO.builder()
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .productName("Web Services")
            .productDescription("Learn how to create web services")
            .productSalePrice(100.00)
            .build();

    List<ProductResponseDTO> products = List.of(product1);

    CartResponseDTO cartResponseModel = CartResponseDTO.builder()
            .cartId("cartId123")
            .customerId("1")
            .products(products)
            .build();


    @Test
    void testClearCart_Success() {
        // Arrange
        when(cartServiceClient.clearCart("cartId123")).thenReturn(Mono.empty()); // Simulate a successful cart clear action

        // Act
        client.delete()
                .uri("/api/v2/gateway/carts/cartId123/clear")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Cart successfully cleared");

        // Assert
        verify(cartServiceClient, times(1)).clearCart("cartId123");
    }

    @Test
    void whenGetCartById_thenReturnCartResponseModel() {
        // arrange
        List<String> productIds = List.of("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223");

        when(cartServiceClient.getCartByCartId("cartId123")).thenReturn(Mono.just(cartResponseModel));

        // Act
        client.get()
                .uri("/api/v2/gateway/carts/cartId123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .value(result -> {
                    assertEquals(cartResponseModel.getCartId(), result.getCartId());
                    assertEquals(cartResponseModel.getCustomerId(), result.getCustomerId());
                    assertEquals(cartResponseModel.getProducts(), result.getProducts());
                });

        // Assert
        verify(cartServiceClient, times(1)).getCartByCartId("cartId123");
    }


}
