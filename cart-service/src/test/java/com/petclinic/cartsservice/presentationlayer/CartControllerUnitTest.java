package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.AddProductRequestModel;

import com.petclinic.cartsservice.domainclientlayer.UpdateProductQuantityRequestModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = CartController.class)
class CartControllerUnitTest {

    @MockBean
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    // Use valid UUIDs for cart IDs
    private final String VALID_CART_ID = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
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

    CartProduct wishListProduct1 = CartProduct.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .productName("Dog Food")
            .productDescription("Premium dry food for adult dogs")
            .productSalePrice(45.99)
            .quantityInCart(2)
            .averageRating(5.0)
            .build();

    CartProduct wishlistProduct2 = CartProduct.builder()
            .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .productName("Cat Litter")
            .productDescription("Clumping cat litter with odor control")
            .productSalePrice(12.99)
            .quantityInCart(1)
            .averageRating(3.0)
            .build();


    @Test
    public void whenGetCartByCartId_thenReturnCartResponseModel() {
        // Arrange
        Cart cart = Cart.builder()
                .cartId(VALID_CART_ID)
                .products(products)
                .customerId("1")
                .build();

        CartResponseModel cartResponseModel = CartResponseModel.builder()
                .cartId(cart.getCartId())
                .customerId("1")
                .products(products)
                .build();

        when(cartService.getCartByCartId(cart.getCartId())).thenReturn(Mono.just(cartResponseModel));

        // Act & Assert
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
                .cartId(VALID_CART_ID)
                .products(products)
                .customerId("1")
                .build();

        Cart cart2 = Cart.builder()
                .cartId(NOT_FOUND_CART_ID)
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
                .uri("/api/v1/carts")
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
        String invalidCartId = "incorrect-cart-id";
        when(cartService.getCartByCartId(invalidCartId))
                .thenReturn(Mono.error(new IllegalArgumentException("Provided cart id is invalid: " + invalidCartId)));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/carts/" + invalidCartId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    public void getCartItemCount_Success() {
        // Use a valid cart ID
        when(cartService.getCartItemCount(VALID_CART_ID)).thenReturn(Mono.just(3));

        webTestClient.get().uri("/api/v1/carts/" + VALID_CART_ID + "/count")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.itemCount").isEqualTo(3);
    }

    @Test
    public void getCartItemCount_CartNotFound() {
        // Arrange
        when(cartService.getCartItemCount(NOT_FOUND_CART_ID))
                .thenReturn(Mono.error(new NotFoundException("Cart not found: " + NOT_FOUND_CART_ID)));

        // Act & Assert
        webTestClient.get().uri("/api/v1/carts/" + NOT_FOUND_CART_ID + "/count")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void whenDeleteCartByIdWithExistingId_thenReturnCartResponseModel(){
        // Arrange
        Cart cart = Cart.builder()
                .cartId(VALID_CART_ID)
                .products(products)
                .customerId("1")
                .build();

        CartResponseModel cartResponseModel = CartResponseModel.builder()
                .cartId(cart.getCartId())
                .customerId("1")
                .products(products)
                .build();
        when(cartService.deleteCartByCartId(cart.getCartId()))
                .thenReturn(Mono.just(cartResponseModel));

        webTestClient
                .delete()
                .uri("/api/v1/carts/" + cart.getCartId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .isEqualTo(cartResponseModel);

        verify(cartService, times(1)).deleteCartByCartId(cartResponseModel.getCartId());
    }

    @Test
    public void whenDeleteCartByIdWithInvalidId_ThenReturnEmptyMono(){
        String invalidCartId = "98f7b33a-d62a-420a-a84a-05a27c85fc"; // Invalid UUID

        when(cartService.deleteCartByCartId(invalidCartId))
                .thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/api/v1/carts/" + invalidCartId)
                .accept()
                .exchange()
                .expectStatus().isEqualTo(422);

        verify(cartService, times(0)).deleteCartByCartId(invalidCartId);
    }

    @Test
    void whenAddProductToCart_Success() {
        // Arrange
        String cartId = VALID_CART_ID;
        AddProductRequestModel requestModel = new AddProductRequestModel("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 2);
        CartResponseModel expectedResponse = new CartResponseModel();
        expectedResponse.setCartId(cartId);
        expectedResponse.setProducts(List.of(product1));

        when(cartService.addProductToCart(anyString(), anyString(), anyInt())).thenReturn(Mono.just(expectedResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseModel.class)
                .value(response -> {
                    assertEquals(cartId, response.getCartId());
                    assertEquals(1, response.getProducts().size());
                    assertEquals(product1.getProductId(), response.getProducts().get(0).getProductId());
                });

        verify(cartService, times(1)).addProductToCart(anyString(), anyString(), anyInt());
    }

    @Test
    void whenCheckoutCart_thenReturnCartResponseModel(){
        // Arrange
        Cart cart = Cart.builder()
                .cartId(VALID_CART_ID)
                .products(products)
                .customerId("1")
                .build();

        CartResponseModel cartResponseModel = CartResponseModel.builder()
                .cartId(cart.getCartId())
                .customerId("1")
                .products(new ArrayList<>())
                .build();
        when(cartService.checkoutCart(cart.getCartId()))
                .thenReturn(Mono.just(cartResponseModel));

        webTestClient
                .post()
                .uri("/api/v1/carts/" + cart.getCartId() + "/checkout")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .value(response -> {
                    assertEquals(cart.getCartId(), response.getCartId());
                    assertEquals(cart.getCustomerId(), response.getCustomerId());
                    assertEquals(0, response.getProducts().size());
                });

        verify(cartService, times(1)).checkoutCart(cartResponseModel.getCartId());
    }

    @Test
    void whenAddProductToCart_OutOfStock_ThrowsBadRequest() {
        // Arrange
        String cartId = VALID_CART_ID;
        AddProductRequestModel requestModel = new AddProductRequestModel("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 20);

        when(cartService.addProductToCart(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.error(new OutOfStockException("Only 5 items left in stock.")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Only 5 items left in stock.");

        verify(cartService, times(1)).addProductToCart(anyString(), anyString(), anyInt());
    }

    @Test
    void whenAddProductToCart_InvalidQuantity_ThrowsBadRequest() {
        // Arrange
        String cartId = VALID_CART_ID;
        AddProductRequestModel requestModel = new AddProductRequestModel("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", -1);

        when(cartService.addProductToCart(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.error(new InvalidInputException("Quantity must be greater than zero.")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Quantity must be greater than zero.");

        verify(cartService, times(1)).addProductToCart(anyString(), anyString(), anyInt());
    }

    @Test
    void whenUpdateProductQuantityInCart_Success() {
        // Arrange
        String cartId = VALID_CART_ID;
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";
        UpdateProductQuantityRequestModel requestModel = new UpdateProductQuantityRequestModel(3);
        CartResponseModel expectedResponse = new CartResponseModel();
        expectedResponse.setCartId(cartId);
        expectedResponse.setProducts(List.of(product1));

        when(cartService.updateProductQuantityInCart(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(expectedResponse));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/carts/" + cartId + "/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseModel.class)
                .value(response -> assertEquals(cartId, response.getCartId()));

        verify(cartService, times(1)).updateProductQuantityInCart(anyString(), anyString(), anyInt());
    }

    @Test
    void whenUpdateProductQuantityInCart_OutOfStock_ThrowsBadRequest() {
        // Arrange
        String cartId = VALID_CART_ID;
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";
        UpdateProductQuantityRequestModel requestModel = new UpdateProductQuantityRequestModel(15);

        when(cartService.updateProductQuantityInCart(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.error(new OutOfStockException("Only 5 items left in stock.")));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/carts/" + cartId + "/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Only 5 items left in stock.");

        verify(cartService, times(1)).updateProductQuantityInCart(anyString(), anyString(), anyInt());
    }

    @Test
    void whenGetCartByValidCustomerId_thenReturnCartResponseModel() {
        //arrange
        String validCustomerId = "123e4567-e89b-12d3-a456-426614174000";
        CartResponseModel cartResponseModel = CartResponseModel.builder()
                .customerId(validCustomerId)
                .cartId("cart123")
                .build();

        when(cartService.findCartByCustomerId(validCustomerId)).thenReturn(Mono.just(cartResponseModel));

        //act & assert
        webTestClient
                .get()
                .uri("/api/v1/carts/customer/" + validCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .value(result -> {
                    assertEquals(validCustomerId, result.getCustomerId());
                    assertEquals("cart123", result.getCartId());
                });
    }

    @Test
    void whenGetCartByNonExistingCustomerId_thenReturnNotFound() {
        //arrange
        String nonExistingCustomerId = "123e4567-e89b-12d3-a456-426614174999";
        when(cartService.findCartByCustomerId(nonExistingCustomerId))
                .thenReturn(Mono.error(new NotFoundException("Cart not found for customer id: " + nonExistingCustomerId)));

        //act & assert
        webTestClient
                .get()
                .uri("/api/v1/carts/customer/" + nonExistingCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cart not found for customer id: " + nonExistingCustomerId);
    }

    @Test
    public void whenRemoveProductFromCart_withValidCartIdAndProductId_thenReturnUpdatedCart() {
        // Arrange
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String productIdToRemove = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";

        List<CartProduct> updatedProducts = new ArrayList<>(Arrays.asList(product2)); // Product1 removed

        Cart cart = Cart.builder()
                .cartId(cartId)
                .products(updatedProducts)
                .customerId("1")
                .build();

        CartResponseModel updatedCartResponse = CartResponseModel.builder()
                .cartId(cartId)
                .customerId("1")
                .products(updatedProducts)
                .build();

        when(cartService.removeProductFromCart(cartId, productIdToRemove)).thenReturn(Mono.just(updatedCartResponse));

        // Act & Assert
        webTestClient
                .delete()
                .uri("/api/v1/carts/" + cartId + "/" + productIdToRemove)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .value(result -> {
                    assertEquals(cartId, result.getCartId());
                    assertEquals(1, result.getProducts().size()); // Only one product should remain
                    assertEquals(product2.getProductId(), result.getProducts().get(0).getProductId());
                });

        verify(cartService, times(1)).removeProductFromCart(cartId, productIdToRemove);
    }

    @Test
    public void whenRemoveProductFromCart_withNonExistentCart_thenReturnNotFound() {
        // Arrange
        String nonExistentCartId = "98f7b33a-d62a-420a-a84a-05a27c85fc92";
        String productIdToRemove = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";

        when(cartService.removeProductFromCart(nonExistentCartId, productIdToRemove))
                .thenReturn(Mono.error(new NotFoundException("Cart not found: " + nonExistentCartId)));

        // Act & Assert
        webTestClient
                .delete()
                .uri("/api/v1/carts/" + nonExistentCartId + "/" + productIdToRemove)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartService, times(1)).removeProductFromCart(nonExistentCartId, productIdToRemove);
    }

    @Test
    public void whenRemoveProductFromCart_withNonExistentProductId_thenReturnNotFound() {
        // Arrange
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        String nonExistentProductId = "non-existent-product-id";

        when(cartService.removeProductFromCart(cartId, nonExistentProductId))
                .thenReturn(Mono.error(new NotFoundException("Product not found in cart: " + nonExistentProductId)));

        // Act & Assert
        webTestClient
                .delete()
                .uri("/api/v1/carts/" + cartId + "/" + nonExistentProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartService, times(1)).removeProductFromCart(cartId, nonExistentProductId);
    }

    @Test
    public void whenRemoveProductFromCart_withInvalidCartId_thenThrowInvalidInputException() {
        // Arrange
        String invalidCartId = "12345"; // Invalid cartId (not 36 characters)
        String productIdToRemove = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";

        // Act & Assert
        webTestClient
                .delete()
                .uri("/api/v1/carts/" + invalidCartId + "/" + productIdToRemove)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided cart id is invalid: " + invalidCartId); // Assert the error message

        verify(cartService, times(0)).removeProductFromCart(anyString(), anyString()); // cartService should not be called
    }

    @Test
    void whenMoveProductFromCartToWishlist_thenSuccess() {
        // Arrange
        String cartId = VALID_CART_ID; // Use a valid cart ID
        String productId = product1.getProductId(); // Use an existing product ID
        when(cartService.moveProductFromCartToWishlist(cartId, productId)).thenReturn(Mono.empty());

        // Act
        webTestClient.put() // Change to PUT to match the controller's request type
                .uri("/api/v1/carts/" + cartId + "/wishlist/" + productId + "/toWishList") // Updated URI to match the controller
                .exchange()
                .expectStatus().isOk(); // Expect an OK response

        // Assert
        verify(cartService, times(1)).moveProductFromCartToWishlist(cartId, productId); // Verify that the service method was called once
    }

    @Test
    void whenMoveProductFromWishListToCart_thenSuccess() {
        // Arrange
        String cartId = VALID_CART_ID; // Use a valid cart ID
        String productId = wishListProduct1.getProductId(); // Use a valid product ID
        when(cartService.moveProductFromWishListToCart(cartId, productId))
                .thenReturn(Mono.just(new CartResponseModel())); // Mock a successful response

        // Act
        webTestClient.put()
                .uri("/api/v1/carts/" + cartId + "/wishlist/" + productId + "/toCart")
                .exchange()
                .expectStatus().isOk() // Expect 200 OK
                .expectBody(CartResponseModel.class) // Expect a response body
                .consumeWith(response -> {
                    CartResponseModel body = response.getResponseBody();
                    assertThat(body).isNotNull(); // Ensure response body is not null
                });

        // Assert
        verify(cartService, times(1)).moveProductFromWishListToCart(cartId, productId);
    }


    @Test
    void whenMoveProductFromWishListToCart_thenInvalidInput() {
        // Arrange
        String invalidCartId = "invalidCartId"; // An invalid cart ID
        String invalidProductId = "invalidProductId"; // An invalid product ID

        // No need to mock the service method as we're testing the controller's response to invalid inputs.

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/carts/" + invalidCartId + "/wishlist/" + invalidProductId + "/toCart")
                .exchange()
                .expectStatus().isEqualTo(422) // Expect 422 for invalid input
                .expectBody(CartResponseModel.class) // Expect a response body
                .consumeWith(response -> {
                    CartResponseModel body = response.getResponseBody();
                    assertThat(body).isNotNull(); // Ensure response body is not null
                    assertThat(body.getMessage()).isEqualTo("Provided cart id is invalid: " + invalidCartId); // Check error message
                });

        // Verify that the service method was not called since the IDs are invalid
        verify(cartService, never()).moveProductFromWishListToCart(invalidCartId, invalidProductId);
    }

    @Test
    void whenMoveProductFromWishListToCart_thenProductNotFoundInWishlist() {
        // Arrange
        String cartId = VALID_CART_ID; // Use a valid cart ID
        String productId = wishListProduct1.getProductId(); // Use a valid product ID
        when(cartService.moveProductFromWishListToCart(cartId, productId))
                .thenReturn(Mono.error(new NotFoundException("Product not found in wishlist")));

        // Act
        webTestClient.put()
                .uri("/api/v1/carts/" + cartId + "/wishlist/" + productId + "/toCart")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)
                .consumeWith(response -> {
                    CartResponseModel body = response.getResponseBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getMessage()).isEqualTo("Product not found in wishlist");
                });

        // Assert
        verify(cartService, times(1)).moveProductFromWishListToCart(cartId, productId);
    }

}
