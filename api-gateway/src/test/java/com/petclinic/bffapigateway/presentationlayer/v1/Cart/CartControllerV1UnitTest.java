package com.petclinic.bffapigateway.presentationlayer.v1.Cart;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.*;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.presentationlayer.v1.CartControllerV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        CartControllerV1.class,
        CartServiceClient.class
})
@WebFluxTest(controllers = CartControllerV1.class)
@AutoConfigureWebTestClient
public class CartControllerV1UnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartServiceClient cartServiceClient;

    private final String baseCartURL = "/api/gateway/carts";

    // Test Data Builders
    private CartResponseDTO buildCartResponseDTO() {
        return CartResponseDTO.builder()
                .cartId("cart-123")
                .customerId("customer-456")
                .products(Collections.singletonList(buildCartProductResponseDTO()))
                .wishListProducts(Collections.emptyList())
                .subtotal(100.0)
                .tvq(14.98)
                .tvc(9.98)
                .total(124.96)
                .paymentStatus("PENDING")
                .build();
    }

    private CartProductResponseDTO buildCartProductResponseDTO() {
        return CartProductResponseDTO.builder()
                .productId("product-789")
                .imageId("image-001")
                .productName("Test Product")
                .productDescription("Test Description")
                .productSalePrice(25.99)
                .averageRating(4.5)
                .quantityInCart(2)
                .productQuantity(10)
                .build();
    }

    private AddProductRequestDTO buildAddProductRequestDTO() {
        return AddProductRequestDTO.builder()
                .productId("product-789")
                .quantity(2)
                .build();
    }

    private UpdateProductQuantityRequestDTO buildUpdateQuantityRequestDTO() {
        return UpdateProductQuantityRequestDTO.builder()
                .quantity(5)
                .build();
    }

    // Tests for getAllCarts endpoint
    @Test
    @DisplayName("GET /api/gateway/carts - Should return all carts successfully")
    void getAllCarts_shouldReturnAllCarts() {
        // Arrange
        when(cartServiceClient.getAllCarts())
                .thenReturn(Flux.just(buildCartResponseDTO()));

        // Act & Assert
        webTestClient.get()
                .uri(baseCartURL)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartResponseDTO.class)
                .hasSize(1)
                .contains(buildCartResponseDTO());

        verify(cartServiceClient, times(1)).getAllCarts();
    }

    @Test
    @DisplayName("GET /api/gateway/carts - Should return empty list when no carts exist")
    void getAllCarts_shouldReturnEmptyList() {
        // Arrange
        when(cartServiceClient.getAllCarts())
                .thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get()
                .uri(baseCartURL)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartResponseDTO.class)
                .hasSize(0);

        verify(cartServiceClient, times(1)).getAllCarts();
    }

    // Tests for getCartById endpoint
    @Test
    @DisplayName("GET /api/gateway/carts/{cartId} - Should return cart by valid ID")
    void getCartById_withValidId_shouldReturnCart() {
        // Arrange
        String cartId = "cart-123";
        CartResponseDTO cart = buildCartResponseDTO();
        when(cartServiceClient.getCartByCartId(cartId))
                .thenReturn(Mono.just(cart));

        // Act & Assert
        webTestClient.get()
                .uri(baseCartURL + "/" + cartId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(cart);

        verify(cartServiceClient, times(1)).getCartByCartId(cartId);
    }

    @Test
    @DisplayName("GET /api/gateway/carts/{cartId} - Should return 404 for non-existent cart")
    void getCartById_withNonExistingId_shouldReturnNotFound() {
        // Arrange
        String cartId = "non-existent-cart";
        when(cartServiceClient.getCartByCartId(cartId))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get()
                .uri(baseCartURL + "/" + cartId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).getCartByCartId(cartId);
    }

    // Tests for deleteCartByCartId endpoint
    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId} - Should delete cart successfully")
    void deleteCartByCartId_withValidId_shouldDeleteCart() {
        // Arrange
        String cartId = "cart-123";
        CartResponseDTO deletedCart = buildCartResponseDTO();
        when(cartServiceClient.deleteCartByCartId(cartId))
                .thenReturn(Mono.just(deletedCart));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(deletedCart);

        verify(cartServiceClient, times(1)).deleteCartByCartId(cartId);
    }

    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId} - Should return 404 for non-existent cart")
    void deleteCartByCartId_withNonExistingId_shouldReturnNotFound() {
        // Arrange
        String cartId = "non-existent-cart";
        when(cartServiceClient.deleteCartByCartId(cartId))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).deleteCartByCartId(cartId);
    }

    // Tests for clearCart endpoint
    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId}/clear - Should clear cart successfully")
    void clearCart_withValidId_shouldClearCart() {
        // Arrange
        String cartId = "cart-123";
        when(cartServiceClient.clearCart(cartId))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/clear")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Cart successfully cleared");

        verify(cartServiceClient, times(1)).clearCart(cartId);
    }

    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId}/clear - Should return 404 when cart not found")
    void clearCart_withNonExistingId_shouldReturnNotFound() {
        // Arrange
        String cartId = "non-existent-cart";
        when(cartServiceClient.clearCart(cartId))
                .thenReturn(Mono.error(new NotFoundException("Cart not found")));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/clear")
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).clearCart(cartId);
    }

    // Tests for removeProductFromCart endpoint
    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId}/{productId} - Should remove product successfully")
    void removeProductFromCart_withValidIds_shouldRemoveProduct() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).removeProductFromCart(cartId, productId);
    }

    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId}/{productId} - Should return 404 when cart or product not found")
    void removeProductFromCart_withNonExistingIds_shouldReturnNotFound() {
        // Arrange
        String cartId = "invalid-cart";
        String productId = "invalid-product";
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.error(new NotFoundException("Cart or product not found")));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).removeProductFromCart(cartId, productId);
    }

    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId}/{productId} - Should return 422 for invalid input")
    void removeProductFromCart_withInvalidInput_shouldReturnUnprocessableEntity() {
        // Arrange
        String cartId = "cart-123";
        String productId = "invalid-format";
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.error(WebClientResponseException.create(422, "Unprocessable Entity", null, null, null)));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        verify(cartServiceClient, times(1)).removeProductFromCart(cartId, productId);
    }

    // Tests for addProductToCart endpoint
    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/products - Should add product successfully")
    void addProductToCart_withValidRequest_shouldAddProduct() {
        // Arrange
        String cartId = "cart-123";
        AddProductRequestDTO requestDTO = buildAddProductRequestDTO();
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.addProductToCart(cartId, requestDTO))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).addProductToCart(cartId, requestDTO);
    }

    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/products - Should return 400 for invalid input")
    void addProductToCart_withInvalidInput_shouldReturnBadRequest() {
        // Arrange
        String cartId = "cart-123";
        AddProductRequestDTO requestDTO = buildAddProductRequestDTO();
        when(cartServiceClient.addProductToCart(cartId, requestDTO))
                .thenReturn(Mono.error(new InvalidInputException("Invalid product quantity")));

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cartServiceClient, times(1)).addProductToCart(cartId, requestDTO);
    }

    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/products - Should return 404 when cart not found")
    void addProductToCart_withNonExistingCartId_shouldReturnNotFound() {
        // Arrange
        String cartId = "invalid-cart";
        AddProductRequestDTO requestDTO = buildAddProductRequestDTO();
        when(cartServiceClient.addProductToCart(cartId, requestDTO))
                .thenReturn(Mono.error(new NotFoundException("Cart not found")));

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).addProductToCart(cartId, requestDTO);
    }

    // Tests for updateProductQuantityInCart endpoint
    @Test
    @DisplayName("PUT /api/gateway/carts/{cartId}/products/{productId} - Should update quantity successfully")
    void updateProductQuantityInCart_withValidRequest_shouldUpdateQuantity() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        UpdateProductQuantityRequestDTO requestDTO = buildUpdateQuantityRequestDTO();
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.put()
                .uri(baseCartURL + "/" + cartId + "/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).updateProductQuantityInCart(cartId, productId, requestDTO);
    }

    @Test
    @DisplayName("PUT /api/gateway/carts/{cartId}/products/{productId} - Should return 404 when cart or product not found")
    void updateProductQuantityInCart_withNonExistingIds_shouldReturnNotFound() {
        // Arrange
        String cartId = "invalid-cart";
        String productId = "invalid-product";
        UpdateProductQuantityRequestDTO requestDTO = buildUpdateQuantityRequestDTO();
        when(cartServiceClient.updateProductQuantityInCart(cartId, productId, requestDTO))
                .thenReturn(Mono.error(new NotFoundException("Cart or product not found")));

        // Act & Assert
        webTestClient.put()
                .uri(baseCartURL + "/" + cartId + "/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).updateProductQuantityInCart(cartId, productId, requestDTO);
    }

    // Tests for checkoutCart endpoint
    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/checkout - Should checkout cart successfully")
    void checkoutCart_withValidId_shouldCheckoutCart() {
        // Arrange
        String cartId = "cart-123";
        CartResponseDTO checkedOutCart = buildCartResponseDTO();
        when(cartServiceClient.checkoutCart(cartId))
                .thenReturn(Mono.just(checkedOutCart));

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/checkout")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(checkedOutCart);

        verify(cartServiceClient, times(1)).checkoutCart(cartId);
    }

    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/checkout - Should return 404 when cart not found")
    void checkoutCart_withNonExistingId_shouldReturnNotFound() {
        // Arrange
        String cartId = "non-existant-cart";
        when(cartServiceClient.checkoutCart(cartId))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/checkout")
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).checkoutCart(cartId);
    }

    // Tests for getCartByCustomerId endpoint
    @Test
    @DisplayName("GET /api/gateway/carts/customer/{customerId} - Should return cart by customer ID")
    void getCartByCustomerId_withValidId_shouldReturnCart() {
        // Arrange
        String customerId = "customer-456";
        CartResponseDTO cart = buildCartResponseDTO();
        when(cartServiceClient.getCartByCustomerId(customerId))
                .thenReturn(Mono.just(cart));

        // Act & Assert
        webTestClient.get()
                .uri(baseCartURL + "/customer/" + customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(cart);

        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }

    @Test
    @DisplayName("GET /api/gateway/carts/customer/{customerId} - Should return 404 when customer has no cart")
    void getCartByCustomerId_withNonExistingId_shouldReturnNotFound() {
        // Arrange
        String customerId = "non-existant-customer";
        when(cartServiceClient.getCartByCustomerId(customerId))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get()
                .uri(baseCartURL + "/customer/" + customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }

    // Tests for wishlist operations
    @Test
    @DisplayName("PUT /api/gateway/carts/{cartId}/wishlist/{productId}/toCart - Should move product from wishlist to cart")
    void moveProductFromWishListToCart_withValidIds_shouldMoveProduct() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.moveProductFromWishListToCart(cartId, productId))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.put()
                .uri(baseCartURL + "/" + cartId + "/wishlist/" + productId + "/toCart")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).moveProductFromWishListToCart(cartId, productId);
    }

    @Test
    @DisplayName("PUT /api/gateway/carts/{cartId}/wishlist/{productId}/toWishList - Should move product from cart to wishlist")
    void moveProductFromCartToWishlist_withValidIds_shouldMoveProduct() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.moveProductFromCartToWishlist(cartId, productId))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.put()
                .uri(baseCartURL + "/" + cartId + "/wishlist/" + productId + "/toWishList")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).moveProductFromCartToWishlist(cartId, productId);
    }

    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/products/{productId}/quantity/{quantity} - Should add product to wishlist")
    void addProductToWishList_withValidIds_shouldAddToWishlist() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        int quantity = 2;
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/products/" + productId + "/quantity/" + quantity)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).addProductToWishList(cartId, productId, quantity);
    }

    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId}/wishlist/{productId} - Should remove product from wishlist")
    void removeProductFromWishlist_withValidIds_shouldRemoveFromWishlist() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.removeProductFromWishlist(cartId, productId))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/wishlist/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).removeProductFromWishlist(cartId, productId);
    }

    @Test
    @DisplayName("DELETE /api/gateway/carts/{cartId}/wishlist/{productId} - Should return 404 when wishlist item not found")
    void removeProductFromWishlist_withNonExistantProductId_shouldReturnNotFound() {
        // Arrange
        String cartId = "cart-123";
        String productId = "non-existant-product";
        when(cartServiceClient.removeProductFromWishlist(cartId, productId))
                .thenReturn(Mono.error(new NotFoundException("Wishlist item not found")));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/wishlist/" + productId)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).removeProductFromWishlist(cartId, productId);
    }

    // Tests for addProductToCartFromProducts endpoint
    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/{productId} - Should add product from products view")
    void addProductToCartFromProducts_withValidIds_shouldAddProduct() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        CartResponseDTO updatedCart = buildCartResponseDTO();
        when(cartServiceClient.addProductToCartFromProducts(cartId, productId))
                .thenReturn(Mono.just(updatedCart));

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(updatedCart);

        verify(cartServiceClient, times(1)).addProductToCartFromProducts(cartId, productId);
    }

    @Test
    @DisplayName("POST /api/gateway/carts/{cartId}/{productId} - Should return 400 for invalid input")
    void addProductToCartFromProducts_withInvalidInput_shouldReturnBadRequest() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        when(cartServiceClient.addProductToCartFromProducts(cartId, productId))
                .thenReturn(Mono.error(new InvalidInputException("Product out of stock")));

        // Act & Assert
        webTestClient.post()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cartServiceClient, times(1)).addProductToCartFromProducts(cartId, productId);
    }

    // Additional error handling tests
    @Test
    @DisplayName("Should handle WebClientResponseException.BadRequest")
    void handleWebClientBadRequestException() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.error(WebClientResponseException.create(400, "Bad Request", null, null, null)));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .exchange()
                .expectStatus().isBadRequest();

        verify(cartServiceClient, times(1)).removeProductFromCart(cartId, productId);
    }

    @Test
    @DisplayName("Should handle WebClientResponseException with custom status code")
    void handleWebClientCustomStatusException() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.error(WebClientResponseException.create(409, "Conflict", null, null, null)));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        verify(cartServiceClient, times(1)).removeProductFromCart(cartId, productId);
    }

    @Test
    @DisplayName("Should handle unexpected errors with 500 status")
    void handleUnexpectedError() {
        // Arrange
        String cartId = "cart-123";
        String productId = "product-789";
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

        // Act & Assert
        webTestClient.delete()
                .uri(baseCartURL + "/" + cartId + "/" + productId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(cartServiceClient, times(1)).removeProductFromCart(cartId, productId);
    }
}
