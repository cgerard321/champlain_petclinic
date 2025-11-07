package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CartItemRequestModel;
import com.petclinic.cartsservice.domainclientlayer.UpdateProductQuantityRequestModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

        when(cartService.getAllCarts(any())).thenReturn(Flux.just(cartResponseModel1, cartResponseModel2));

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
        void whenGetAllCartsAsJson_thenReturnListOfCarts() {
                CartResponseModel cart1 = CartResponseModel.builder()
                                .cartId("json-cart-1")
                                .customerId("json-c1")
                                .products(new ArrayList<>())
                                .build();

                CartResponseModel cart2 = CartResponseModel.builder()
                                .cartId("json-cart-2")
                                .customerId("json-c2")
                                .products(new ArrayList<>())
                                .build();

                when(cartService.getAllCarts(any())).thenReturn(Flux.just(cart1, cart2));

                webTestClient
                                .get()
                                .uri(uriBuilder -> uriBuilder.path("/api/v1/carts").queryParam("page", 0).queryParam("size", 10).build())
                                .accept(MediaType.APPLICATION_JSON)
                                .exchange()
                                .expectStatus().isOk()
                                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                                .expectBodyList(CartResponseModel.class)
                                .value(carts -> {
                                        assertEquals(2, carts.size());
                                        assertEquals("json-cart-1", carts.get(0).getCartId());
                                        assertEquals("json-cart-2", carts.get(1).getCartId());
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

        when(cartService.getAllCarts(any())).thenReturn(delayed);

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
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

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
        CartItemRequestModel requestModel = new CartItemRequestModel("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 2);
        CartResponseModel expectedResponse = new CartResponseModel();
        expectedResponse.setCartId(cartId);
        expectedResponse.setProducts(List.of(product1));

        when(cartService.addProductToCart(anyString(), any(CartItemRequestModel.class))).thenReturn(Mono.just(expectedResponse));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().value("Location", location -> assertEquals("/api/v1/carts/" + cartId + "/products/" + requestModel.getProductId(), location))
                .expectBody(CartResponseModel.class)
                .value(response -> {
                    assertEquals(cartId, response.getCartId());
                    assertEquals(1, response.getProducts().size());
                    assertEquals(product1.getProductId(), response.getProducts().get(0).getProductId());
                });

        verify(cartService, times(1)).addProductToCart(anyString(), any(CartItemRequestModel.class));
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
        CartItemRequestModel requestModel = new CartItemRequestModel("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", 20);

        when(cartService.addProductToCart(anyString(), any(CartItemRequestModel.class)))
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

        verify(cartService, times(1)).addProductToCart(anyString(), any(CartItemRequestModel.class));
    }

    @Test
    void whenAddProductToCart_InvalidQuantity_ThrowsBadRequest() {
        // Arrange
        String cartId = VALID_CART_ID;
        CartItemRequestModel requestModel = new CartItemRequestModel("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", -1);

        when(cartService.addProductToCart(anyString(), any(CartItemRequestModel.class)))
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

        verify(cartService, times(1)).addProductToCart(anyString(), any(CartItemRequestModel.class));
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
        webTestClient.patch()
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
        webTestClient.patch()
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
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

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
                void whenAddProductToWishlist_thenProductIsNotAlreadyInWishlist_thenSuccess() {
        Cart updatedCart = Cart.builder()
                .cartId(VALID_CART_ID)
                .wishListProducts(Arrays.asList(wishListProduct1))
                .products(new ArrayList<>())
                .build();

                WishlistItemRequestModel request = new WishlistItemRequestModel(wishListProduct1.getProductId(), 2);

                when(cartService.addProductToWishlist(eq(VALID_CART_ID), any(WishlistItemRequestModel.class)))
                                .thenReturn(Mono.just(EntityModelUtil.toCartResponseModel(updatedCart, updatedCart.getProducts())));

                webTestClient.post()
                                .uri("/api/v1/carts/" + VALID_CART_ID + "/wishlist")
                                .accept(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody(Cart.class)
                                .value(responseCart -> {
                                        assertThat(responseCart.getWishListProducts()).hasSize(1);
                                        CartProduct addedProduct = responseCart.getWishListProducts().get(0);
                                        assertEquals(wishListProduct1.getProductId(), addedProduct.getProductId());
                                        assertEquals(wishListProduct1.getProductName(), addedProduct.getProductName());
                                        assertEquals(2, addedProduct.getQuantityInCart());
                                });

                ArgumentCaptor<WishlistItemRequestModel> requestCaptor = ArgumentCaptor.forClass(WishlistItemRequestModel.class);
                verify(cartService, times(1)).addProductToWishlist(eq(VALID_CART_ID), requestCaptor.capture());
                assertThat(requestCaptor.getValue().getProductId()).isEqualTo(wishListProduct1.getProductId());
                assertThat(requestCaptor.getValue().getQuantity()).isEqualTo(2);
    }
    @Test
    void whenAddProductToWishlist_thenProductNotFound() {
        // Arrange
        String cartId = VALID_CART_ID;
        String productId = NOT_FOUND_ID;
        int quantity = 2;

        when(cartService.addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class)))
                .thenReturn(Mono.error(new NotFoundException("Product not found: " + productId)));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/wishlist")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new WishlistItemRequestModel(productId, quantity))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getMessage()).isEqualTo("Product not found: " + productId);
                });

        verify(cartService, times(1)).addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class));
    }

    @Test
    void whenAddProductToWishlist_thenCartNotFound(){
        // Arrange
        String cartId = NOT_FOUND_ID;
        String productId = wishListProduct1.getProductId();
        int quantity = 2;

        when(cartService.addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class)))
                .thenReturn(Mono.error(new NotFoundException("Cart not found: " + cartId)));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/wishlist")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new WishlistItemRequestModel(productId, quantity))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CartResponseModel.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getMessage()).isEqualTo("Cart not found: " + cartId);
                });

        verify(cartService, times(1)).addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class));
    }

    @Test
    void whenAddProductToWishlist_thenInvalidProductId(){
        // Arrange
        String cartId = VALID_CART_ID;
        String invalidProductId = "invalidProductId";
        int quantity = 2;

        when(cartService.addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class)))
                .thenReturn(Mono.error(new InvalidInputException("Provided product id is invalid: " + invalidProductId)));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/wishlist")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new WishlistItemRequestModel(invalidProductId, quantity))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided product id is invalid: " + invalidProductId);

        verify(cartService, times(1)).addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class));
    }

    @Test
    void whenAddProductToWishlist_thenInvalidCartId(){
        // Arrange
        String invalidCartId = "invalidCartId";
        String productId = wishListProduct1.getProductId();
        int quantity = 2;

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + invalidCartId + "/wishlist")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new WishlistItemRequestModel(productId, quantity))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided cart id is invalid: " + invalidCartId);

        verify(cartService, never()).addProductToWishlist(anyString(), any());
    }

    @Test
    void whenAddProductToWishlist_thenOutOfStockProductStillAdded(){
        // Arrange
        String cartId = VALID_CART_ID;
        String productId = wishListProduct1.getProductId();
        int quantity = 2;

        CartProduct outOfStockProduct = CartProduct.builder()
                .productId(productId)
                .productName(wishListProduct1.getProductName())
                .quantityInCart(quantity)
                .productQuantity(0)
                .build();

        Cart updatedCart = Cart.builder()
                .cartId(cartId)
                .wishListProducts(List.of(outOfStockProduct))
                .products(new ArrayList<>())
                .build();

        when(cartService.addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class)))
                .thenReturn(Mono.just(EntityModelUtil.toCartResponseModel(updatedCart, updatedCart.getProducts())));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/" + cartId + "/wishlist")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new WishlistItemRequestModel(productId, quantity))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Cart.class)
                .value(responseCart -> {
                    assertThat(responseCart.getWishListProducts()).hasSize(1);
                    CartProduct wishlistEntry = responseCart.getWishListProducts().get(0);
                    assertEquals(productId, wishlistEntry.getProductId());
                    assertEquals(0, wishlistEntry.getProductQuantity());
                    assertEquals(quantity, wishlistEntry.getQuantityInCart());
                });

        verify(cartService, times(1)).addProductToWishlist(eq(cartId), any(WishlistItemRequestModel.class));
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
        void addProductToWishlist_invalidCart_returns422() {
                String badCartId = "bad";

                webTestClient.post()
                                .uri("/api/v1/carts/{cartId}/wishlist", badCartId)
                                .bodyValue(new WishlistItemRequestModel(wishListProduct1.getProductId(), 1))
                                .exchange()
                                .expectStatus().isEqualTo(422)
                                .expectBody(CartResponseModel.class)
                                .value(r -> assertEquals("Provided cart id is invalid: " + badCartId, r.getMessage()));

                verify(cartService, never()).addProductToWishlist(anyString(), any());
    }

    // --- wishlist transfer endpoint ---

    @Test
    void createWishlistTransfer_success() {
        String validCartId = "123456789012345678901234567890123456"; // 36 chars
        CartResponseModel response = CartResponseModel.builder().cartId(validCartId).build();
        Mockito.when(cartService.transferWishlist(Mockito.eq(validCartId), Mockito.anyList(), Mockito.eq(WishlistTransferDirection.TO_CART)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist-transfers")
                .bodyValue(new WishlistTransferRequestModel(null, WishlistTransferDirection.TO_CART))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseModel.class)
                .value(r -> r.getCartId().equals(validCartId));
    }

    @Test
    void createWishlistTransfer_invalidInput() {
        String validCartId = "cart123456789012345678901234567890123456";
        Mockito.when(cartService.transferWishlist(Mockito.eq(validCartId), Mockito.anyList(), Mockito.eq(WishlistTransferDirection.TO_CART)))
                .thenReturn(Mono.error(new InvalidInputException("Invalid input")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist-transfers")
                .bodyValue(new WishlistTransferRequestModel(null, WishlistTransferDirection.TO_CART))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void createWishlistTransfer_unexpectedError() {
        String validCartId = "123456789012345678901234567890123456"; // 36 chars
        Mockito.when(cartService.transferWishlist(Mockito.eq(validCartId), Mockito.anyList(), Mockito.eq(WishlistTransferDirection.TO_CART)))
                .thenReturn(Mono.error(new RuntimeException("Unexpected")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist-transfers")
                .bodyValue(new WishlistTransferRequestModel(null, WishlistTransferDirection.TO_CART))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createWishlistTransfer_notFound() {
        String validCartId = "123456789012345678901234567890123456";
        Mockito.when(cartService.transferWishlist(Mockito.eq(validCartId), Mockito.anyList(), Mockito.eq(WishlistTransferDirection.TO_CART)))
                .thenReturn(Mono.error(new NotFoundException("Not found")));

        webTestClient.post()
                .uri("/api/v1/carts/" + validCartId + "/wishlist-transfers")
                .bodyValue(new WishlistTransferRequestModel(null, WishlistTransferDirection.TO_CART))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createWishlistTransfer_invalidCartId() {
        String invalidCartId = "short-id";
        webTestClient.post()
                .uri("/api/v1/carts/" + invalidCartId + "/wishlist-transfers")
                .bodyValue(new WishlistTransferRequestModel(null, WishlistTransferDirection.TO_CART))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
    @Test
    void deleteAllProductsInCart_success() {
        String validCartId = "123456789012345678901234567890123456"; // 36 chars
        Mockito.when(cartService.deleteAllItemsInCart(validCartId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/carts/" + validCartId + "/products")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void deleteAllProductsInCart_invalidCartId() {
        String invalidCartId = "short-id"; // Not 36 chars
        webTestClient.delete()
                .uri("/api/v1/carts/" + invalidCartId + "/products")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void createCart_success() {
        String customerId = "customer123";
        CartRequestModel request = CartRequestModel.builder().customerId(customerId).build();
        CartResponseModel response = CartResponseModel.builder()
                .customerId(customerId)
                .cartId("generated-cart-id")
                .build();

        Mockito.when(cartService.assignCartToCustomer(customerId))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/carts")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CartResponseModel.class)
                .value(cart -> assertEquals(customerId, cart.getCustomerId()));
    }

    @Test
    void createCart_missingCustomerId_returns422() {
        CartRequestModel request = CartRequestModel.builder().build();

        webTestClient.post()
                .uri("/api/v1/carts")
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void applyPromoToCart_success() {
        String cartId = VALID_CART_ID;
        CartResponseModel response = CartResponseModel.builder()
                .cartId(cartId)
                .promoPercent(15.0)
                .build();

        when(cartService.applyPromoToCart(cartId, 15.0)).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/v1/carts/" + cartId + "/promo")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CartPromoRequestModel(15.0))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseModel.class)
                .value(result -> {
                    assertEquals(cartId, result.getCartId());
                    assertEquals(15.0, result.getPromoPercent());
                });

        verify(cartService).applyPromoToCart(cartId, 15.0);
    }

    @Test
    void applyPromoToCart_missingPercent_returns422() {
        webTestClient.put()
                .uri("/api/v1/carts/" + VALID_CART_ID + "/promo")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CartPromoRequestModel(null))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        verify(cartService, never()).applyPromoToCart(anyString(), any());
    }

    @Test
    void clearPromoFromCart_success() {
        String cartId = VALID_CART_ID;
        when(cartService.applyPromoToCart(cartId, null)).thenReturn(Mono.just(new CartResponseModel()));

        webTestClient.delete()
                .uri("/api/v1/carts/" + cartId + "/promo")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(cartService).applyPromoToCart(cartId, null);
    }

    @Test
    void getAllCartsAsJson_withNegativePage_returns422() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/carts")
                        .queryParam("page", -1)
                        .queryParam("size", 5)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("page must be greater than or equal to 0");

        verify(cartService, never()).getAllCarts(any());
    }

    @Test
    void getAllCartsAsJson_withZeroSize_returns422() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/carts")
                        .queryParam("page", 0)
                        .queryParam("size", 0)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("size must be greater than 0");

        verify(cartService, never()).getAllCarts(any());
    }

}
