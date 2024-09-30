package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = CartController.class)
class CartControllerUnitTest {

    @MockBean
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    private final String NOT_FOUND_CART_ID = "98f7b33a-d62a-420a-a84a-05a27c85fc92";

    private final CartProduct product1 = CartProduct.builder()
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .productName("Product1")
            .productDescription("Description1")
            .productSalePrice(100.0)
            .quantityInCart(1)
            .averageRating(4.5)
            .build();

    private final CartProduct product2 = CartProduct.builder()
            .productId("d819e4f4-25af-4d33-91e9-2c45f0071606")
            .productName("Product2")
            .productDescription("Description2")
            .productSalePrice(200.0)
            .quantityInCart(1)
            .averageRating(4.0)
            .build();

    private final CartProduct product3 = CartProduct.builder()
            .productId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")
            .productName("Product3")
            .productDescription("Description3")
            .productSalePrice(300.0)
            .quantityInCart(1)
            .averageRating(3.5)
            .build();

    private final List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));

    @Test
    public void whenGetCartByCartId_thenReturnCartResponseModel() {
        // arrange
          Cart cart = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .products(products)
                .customerId("1")
                .build();

        CartResponseModel cartResponseModel = CartResponseModel.builder()
                .cartId(cart.getCartId())
                .customerId("1")
                .products(products)
                .build();

        when(cartService.getCartByCartId(cart.getCartId())).thenReturn(Mono.just(cartResponseModel));

        // act & assert
        webTestClient
                .get()
                .uri("/api/v1/carts/" + cart.getCartId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .value(result -> {
                    assertEquals(cart.getCartId(), result.getCartId());
                    assertEquals(cart.getCustomerId(), result.getCustomerId());
                    assertEquals(cart.getProducts(), result.getProducts());
                });
    }

    @Test
    void whenGetAllCarts_thenReturnAllCartResponseModels() {
        // Arrange
        Cart cart1 = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .products(products)
                .customerId("1")
                .build();

        Cart cart2 = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc92")
                .products(products)
                .customerId("2")
                .build();

        CartResponseModel cartResponseModel1 = CartResponseModel.builder()
                .cartId(cart1.getCartId())
                .customerId(cart1.getCustomerId())
                .products(cart1.getProducts())
                .build();

        CartResponseModel cartResponseModel2 = CartResponseModel.builder()
                .cartId(cart2.getCartId())
                .customerId(cart2.getCustomerId())
                .products(cart2.getProducts())
                .build();

        when(cartService.getAllCarts()).thenReturn(Flux.just(cartResponseModel1, cartResponseModel2));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v2/carts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(CartResponseModel.class)
                .value(result -> {
                    assertEquals(2, result.size());
                    assertEquals(cartResponseModel1.getCartId(), result.get(0).getCartId());
                    assertEquals(cartResponseModel1.getCustomerId(), result.get(0).getCustomerId());
                    assertEquals(cartResponseModel1.getProducts(), result.get(0).getProducts());
                    assertEquals(cartResponseModel2.getCartId(), result.get(1).getCartId());
                    assertEquals(cartResponseModel2.getCustomerId(), result.get(1).getCustomerId());
                    assertEquals(cartResponseModel2.getProducts(), result.get(1).getProducts());
                });
    }

    @Test
    void whenGetCartByCartId_withNonExistingCartId_thenReturnNotFound() {
        // Arrange
        when(cartService.getCartByCartId(NOT_FOUND_CART_ID))
                .thenReturn(Mono.error(new NotFoundException("Enrollment id not found: " + NOT_FOUND_CART_ID)));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/carts/" + NOT_FOUND_CART_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetCartByCartId_withIncorrectCartId_thenThrowIllegalArgumentException() {
        when(cartService.getCartByCartId("incorrect-cart-id"))
                .thenReturn(Mono.error(new IllegalArgumentException("Provided cart id is invalid: " + "incorrect-cart-id")));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/carts/" + "incorrect-cart-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);    }

//    @Test
    public void getCartItemCount_Success() {
        when(cartService.getCartItemCount("cart1")).thenReturn(Mono.just(3));

        webTestClient.get().uri("/api/v1/carts/cart1/count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.itemCount").isEqualTo(3);
    }

 //   @Test
    public void getCartItemCount_CartNotFound() {
        when(cartService.getCartItemCount("cart1")).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/v1/carts/cart1/count")
                .exchange()
                .expectStatus().isNotFound();
    }


}