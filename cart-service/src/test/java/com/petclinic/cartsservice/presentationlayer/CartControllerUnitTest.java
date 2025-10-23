package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.AddProductRequestModel;

import com.petclinic.cartsservice.domainclientlayer.UpdateProductQuantityRequestModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = CartController.class)
class CartControllerUnitTest {

    @MockBean
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    // Use valid UUIDs for cart IDs
    private final String VALID_CART_ID = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
    private final String NOT_FOUND_ID = "98f7b33a-d62a-420a-a84a-05a27c85fc92";

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
                .cartId(NOT_FOUND_ID)
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
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
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
    void whenGetAllCarts_streamsAndSupportsBackpressure() {
        // Arrange: create a Flux that emits 3 items with some delay
        CartResponseModel cart1 = CartResponseModel.builder()
                .cartId("stream-cart-1")
                .customerId("c1")
                .products(new ArrayList<>())
                .build();

        CartResponseModel cart2 = CartResponseModel.builder()
                .cartId("stream-cart-2")
                .customerId("c2")
                .products(new ArrayList<>())
                .build();

        CartResponseModel cart3 = CartResponseModel.builder()
                .cartId("stream-cart-3")
                .customerId("c3")
                .products(new ArrayList<>())
                .build();

        List<CartResponseModel> items = List.of(cart1, cart2, cart3);
        Flux<CartResponseModel> delayed = Flux.fromIterable(items).delayElements(Duration.ofMillis(50));

        when(cartService.getAllCarts()).thenReturn(delayed);

        // Act: request the endpoint and capture responseBody Flux
        Flux<CartResponseModel> responseFlux = webTestClient.get()
                .uri("/api/v1/carts")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(CartResponseModel.class)
                .getResponseBody();

        // Assert: verify backpressure - subscribe without initial request, then request 1, then 2 more.
        StepVerifier.create(responseFlux, StepVerifierOptions.create().initialRequest(0))
                .thenRequest(1)
                .expectNextMatches(r -> r.getCartId().equals(items.get(0).getCartId()))
                .thenRequest(2)
                .expectNextCount(2)
                .thenCancel()
                .verify();
    }

    @Test
    void whenGetCartByCartId_withNonExistingCartId_thenReturnNotFound() {
        // Arrange
        when(cartService.getCartByCartId(NOT_FOUND_ID))
                .thenReturn(Mono.error(new NotFoundException("Enrollment id not found: " + NOT_FOUND_ID)));

        // Act & Assert
        webTestClient
                .get()
                .uri("/api/v1/carts/" + NOT_FOUND_ID)
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

        CartResponseModel updatedCartResponse = CartResponseModel.builder()
                .cartId(cartId)
                .customerId("1")
                .products(updatedProducts)
                .build();

        when(cartService.removeProductFromCart(cartId, productIdToRemove)).thenReturn(Mono.just(updatedCartResponse));

        // Act & Assert
        webTestClient
                .delete()
                .uri("/api/v1/carts/" + cartId + "/products/" + productIdToRemove)
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
                .uri("/api/v1/carts/" + nonExistentCartId + "/products/" + productIdToRemove)
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
                .uri("/api/v1/carts/" + cartId + "/products/" + nonExistentProductId)
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
                .uri("/api/v1/carts/" + invalidCartId + "/products/" + productIdToRemove)
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

    @Test
    void whenAddProductToWishList_thenProductIsNotAlreadyInWishList_thenSuccess() {
        Cart updatedCart = Cart.builder()
                .cartId(VALID_CART_ID)
                .wishListProducts(Arrays.asList(wishListProduct1))
                .products(new ArrayList<>())
                .build();

        when(cartService.addProductToWishList(VALID_CART_ID, wishListProduct1.getProductId(), 2))
                .thenReturn(Mono.just(EntityModelUtil.toCartResponseModel(updatedCart, updatedCart.getProducts())));

        webTestClient.post()
                .uri("/api/v1/carts/" + VALID_CART_ID + "/products/" + wishListProduct1.getProductId() + "/quantity/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cart.class)
                .value(responseCart -> {
                    assertThat(responseCart.getWishListProducts()).hasSize(1);
                    CartProduct addedProduct = responseCart.getWishListProducts().get(0);
                    assertEquals(wishListProduct1.getProductId(), addedProduct.getProductId());
                    assertEquals(wishListProduct1.getProductName(), addedProduct.getProductName());
                    assertEquals(2, addedProduct.getQuantityInCart());
                });

        verify(cartService, times(1)).addProductToWishList(VALID_CART_ID, wishListProduct1.getProductId(), 2);
    }
    @Test
    void addProductToCartFromProducts_ok_returns200() {
        // given
        String cartId = "01234567-0123-0123-0123-012345678901";
        String productId = "11111111-1111-1111-1111-111111111111";

        CartResponseModel resp = new CartResponseModel();
        resp.setCartId(cartId);

        when(cartService.addProductToCartFromProducts(cartId, productId))
                .thenReturn(Mono.just(resp));

        // when + then
        webTestClient.post()
                .uri("/api/v1/carts/{cartId}/{productId}", cartId, productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseModel.class)
                .value(r -> assertEquals(cartId, r.getCartId()));

        verify(cartService).addProductToCartFromProducts(cartId, productId);
    }


    @Test
    void whenAddProductToWishList_thenProductIsAlreadyInWishList_thenSuccess(){
        // Arrange
        Cart updatedCart = Cart.builder()
                .cartId(VALID_CART_ID)
                .wishListProducts(Arrays.asList(wishListProduct1))
                .products(new ArrayList<>())
                .build();

        when(cartService.addProductToWishList(VALID_CART_ID, wishListProduct1.getProductId(), 2))
                .thenReturn(Mono.just(EntityModelUtil.toCartResponseModel(updatedCart, updatedCart.getProducts())));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + VALID_CART_ID + "/products/" + wishListProduct1.getProductId() + "/quantity/2")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Cart.class)
                .value(responseCart -> {
                    assertThat(responseCart.getWishListProducts()).hasSize(1);
                    CartProduct addedProduct = responseCart.getWishListProducts().get(0);
                    assertEquals(wishListProduct1.getProductId(), addedProduct.getProductId());
                    assertEquals(wishListProduct1.getProductName(), addedProduct.getProductName());
                    assertEquals(2, addedProduct.getQuantityInCart());
                });

        verify(cartService, times(1)).addProductToWishList(VALID_CART_ID, wishListProduct1.getProductId(), 2);
    }

    @Test
    void whenAddProductToWishlist_thenProductNotFound() {
        // Arrange
        String cartId = VALID_CART_ID;
        String productId = NOT_FOUND_ID;
        int quantity = 2;

        when(cartService.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.error(new NotFoundException("Product not found: " + productId)));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products/" + productId + "/quantity/" + quantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getMessage()).isEqualTo("Product not found: " + productId);
                });

        verify(cartService, times(1)).addProductToWishList(cartId, productId, quantity);
    }

    @Test
    void whenAddProductToWishlist_thenCartNotFound(){
        // Arrange
        String cartId = NOT_FOUND_ID;
        String productId = wishListProduct1.getProductId();
        int quantity = 2;

        when(cartService.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.error(new NotFoundException("Cart not found: " + cartId)));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products/" + productId + "/quantity/" + quantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getMessage()).isEqualTo("Cart not found: " + cartId);
                });

        verify(cartService, times(1)).addProductToWishList(cartId, productId, quantity);
    }

    @Test
    void whenAddProductToWishlist_thenInvalidProductId(){
        // Arrange
        String cartId = VALID_CART_ID;
        String invalidProductId = "invalidProductId";
        int quantity = 2;

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products/" + invalidProductId + "/quantity/" + quantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " + invalidProductId);

        verify(cartService, never()).addProductToWishList(cartId, invalidProductId, quantity);
    }

    @Test
    void whenAddProductToWishlist_thenInvalidCartId(){
        // Arrange
        String invalidCartId = "invalidCartId";
        String productId = wishListProduct1.getProductId();
        int quantity = 2;

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + invalidCartId + "/products/" + productId + "/quantity/" + quantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided cart id is invalid: " + invalidCartId);

        verify(cartService, never()).addProductToWishList(invalidCartId, productId, quantity);
    }

    @Test
    void whenAddProductToWishlist_thenOutOfStock(){
        // Arrange
        String cartId = VALID_CART_ID;
        String productId = wishListProduct1.getProductId();
        int quantity = 2;

        when(cartService.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.error(new OutOfStockException("Only 5 items left in stock.")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products/" + productId + "/quantity/" + quantity)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Only 5 items left in stock.");

        verify(cartService, times(1)).addProductToWishList(cartId, productId, quantity);
    }



    //NEGATIF

    @Test
    void removeProductFromWishlist_invalidIds_returns422() {
        String badCartId = "short";
        String badProductId = "short";

        webTestClient.delete()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}", badCartId, badProductId)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(CartResponseModel.class)
                .value(r -> assertEquals("Provided cart id is invalid: " + badCartId, r.getMessage()));

        verify(cartService, never()).removeProductFromWishlist(anyString(), anyString());
    }

    @Test
    void removeProductFromWishlist_notFound_returns404() {
        String cartId = "01234567-0123-0123-0123-012345678901";
        String productId = "11111111-1111-1111-1111-111111111111";

        when(cartService.removeProductFromWishlist(cartId, productId))
                .thenReturn(Mono.error(new NotFoundException("Product not found in wishlist: " + productId)));

        webTestClient.delete()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}", cartId, productId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)
                .value(r -> assertEquals("Product not found in wishlist: " + productId, r.getMessage()));
    }


    @Test
    void moveProductFromCartToWishlist_invalidIds_returns422() {
        String badCartId = "bad";
        String badProductId = "bad";

        webTestClient.put()
                .uri("/api/v1/carts/{cartId}/wishlist/{productId}/toWishList", badCartId, badProductId)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(CartResponseModel.class)
                .value(r -> assertEquals("Provided cart id is invalid: " + badCartId, r.getMessage()));

        verify(cartService, never()).moveProductFromCartToWishlist(anyString(), anyString());
    }

    @Test
    void testGetRecentPurchases_Found() {
        // Arrange
        CartService cartService = Mockito.mock(CartService.class);
        CartController controller = new CartController(cartService);

        String cartId = "cart123";
        List<CartProduct> products = List.of(
                CartProduct.builder().productId("prod1").build()
        );
        Mockito.when(cartService.getRecentPurchases(cartId)).thenReturn(Mono.just(products));

        Mono<ResponseEntity<List<CartProduct>>> result = controller.getRecentPurchases(cartId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful()
                        && response.getBody() != null
                        && response.getBody().size() == 1
                        && "prod1".equals(response.getBody().get(0).getProductId()))
                .verifyComplete();
    }
    @Test
    void testGetRecentPurchases_NotFound() {
        CartService cartService = Mockito.mock(CartService.class);
        CartController controller = new CartController(cartService);

        String cartId = "missing-cart";
        Mockito.when(cartService.getRecentPurchases(cartId)).thenReturn(Mono.empty());

        Mono<ResponseEntity<List<CartProduct>>> result = controller.getRecentPurchases(cartId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().is4xxClientError()
                        && response.getBody() == null)
                .verifyComplete();
    }
    @Test
    void addProductToCartFromProducts_success() {
        String validCartId = "cart123456789012345678901234567890123456";
        CartResponseModel response = CartResponseModel.builder().cartId(validCartId).build();
        Mockito.when(cartService.addProductToCartFromProducts(validCartId, "prod456"))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/prod456")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseModel.class)
                .value(r -> r.getCartId().equals(validCartId));
    }

    @Test
    void addProductToCartFromProducts_outOfStockOrInvalidInput() {
        String validCartId = "cart123456789012345678901234567890123456";
        Mockito.when(cartService.addProductToCartFromProducts(validCartId, "prod456"))
                .thenReturn(Mono.error(new OutOfStockException("Out of stock")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/prod456")
                .exchange()
                .expectStatus().isBadRequest();
    }
    @Test
    void addProductToCartFromProducts_notFound() {
        String validCartId = "cart123456789012345678901234567890123456";
        Mockito.when(cartService.addProductToCartFromProducts(validCartId, "prod456"))
                .thenReturn(Mono.error(new NotFoundException("Not found")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/prod456")
                .exchange()
                .expectStatus().isNotFound();
    }

    // --- moveAllWishlistToCart endpoint ---

    @Test
    void moveAllWishlistToCart_success() {
        String validCartId = "123456789012345678901234567890123456"; // 36 chars
        CartResponseModel response = CartResponseModel.builder().cartId(validCartId).build();
        Mockito.when(cartService.moveAllWishlistToCart(validCartId))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist/moveAll")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseModel.class)
                .value(r -> r.getCartId().equals(validCartId));
    }

    @Test
    void moveAllWishlistToCart_invalidInput() {
        String validCartId = "cart123456789012345678901234567890123456";
        Mockito.when(cartService.moveAllWishlistToCart(validCartId))
                .thenReturn(Mono.error(new InvalidInputException("Invalid input")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist/moveAll")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void moveAllWishlistToCart_outOfStock() {
        String validCartId = "123456789012345678901234567890123456"; // 36 chars
        Mockito.when(cartService.moveAllWishlistToCart(validCartId))
                .thenReturn(Mono.error(new OutOfStockException("Out of stock")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist/moveAll")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void moveAllWishlistToCart_unexpectedError() {
        String validCartId = "123456789012345678901234567890123456"; // 36 chars
        Mockito.when(cartService.moveAllWishlistToCart(validCartId))
                .thenReturn(Mono.error(new RuntimeException("Unexpected")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist/moveAll")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void moveAllWishlistToCart_notFound() {
        String validCartId = "123456789012345678901234567890123456";
        Mockito.when(cartService.moveAllWishlistToCart(validCartId))
                .thenReturn(Mono.error(new NotFoundException("Not found")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist/moveAll")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void moveAllWishlistToCart_invalidCartId() {
        String invalidCartId = "short-id";
        webTestClient.post()
                .uri("/api/v1/carts/" + invalidCartId + "/wishlist/moveAll")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @Test
    void deleteAllItemsInCart_success() {
        String validCartId = "123456789012345678901234567890123456"; // 36 chars
        Mockito.when(cartService.deleteAllItemsInCart(validCartId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/carts/" + validCartId + "/items")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void deleteAllItemsInCart_invalidCartId() {
        String invalidCartId = "short-id"; // Not 36 chars
        webTestClient.delete()
                .uri("/api/v1/carts/" + invalidCartId + "/items")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void assignCartToCustomer_success() {
        String customerId = "customer123";
        List<CartProduct> products = List.of(
                CartProduct.builder().productId("prod1").build(),
                CartProduct.builder().productId("prod2").build()
        );
        CartResponseModel response = CartResponseModel.builder().customerId(customerId).products(products).build();
        Mockito.when(cartService.assignCartToCustomer(customerId, products))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/carts/" + customerId + "/assign")
                .bodyValue(products)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CartResponseModel.class)
                .value(cart -> assertEquals(customerId, cart.getCustomerId()));
    }

    @Test
    void assignCartToCustomer_emptyResult() {
        String customerId = "customer123";
        List<CartProduct> products = List.of();
        Mockito.when(cartService.assignCartToCustomer(customerId, products))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/carts/" + customerId + "/assign")
                .bodyValue(products)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testGetRecommendationPurchases_ReturnsOk() {
        // Arrange
        CartService cartService = Mockito.mock(CartService.class);
        CartController controller = new CartController(cartService);

        String cartId = "test-cart-id";
        List<CartProduct> recommendations = List.of(CartProduct.builder().productId("prod1").build());

        Mockito.when(cartService.getRecommendationPurchases(cartId)).thenReturn(Mono.just(recommendations));

        // Act
        Mono<ResponseEntity<List<CartProduct>>> result = controller.getRecommendationPurchases(cartId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful()
                        && response.getBody() != null
                        && response.getBody().size() == 1
                        && "prod1".equals(response.getBody().get(0).getProductId()))
                .verifyComplete();
    }

    @Test
    void testGetRecommendationPurchases_ReturnsNotFound() {
        // Arrange
        CartService cartService = Mockito.mock(CartService.class);
        CartController controller = new CartController(cartService);

        String cartId = "missing-cart-id";
        Mockito.when(cartService.getRecommendationPurchases(cartId)).thenReturn(Mono.empty());

        // Act
        Mono<ResponseEntity<List<CartProduct>>> result = controller.getRecommendationPurchases(cartId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().is4xxClientError()
                        && response.getBody() == null)
                .verifyComplete();
    }
}
