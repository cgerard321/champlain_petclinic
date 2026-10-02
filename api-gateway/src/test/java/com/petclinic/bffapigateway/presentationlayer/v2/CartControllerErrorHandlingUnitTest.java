package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.WishlistItemRequestDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CartControllerErrorHandlingUnitTest {

    private static final String CART_URL = "/api/v2/gateway/carts/{cartId}";

    @Mock
    private CartServiceClient cartServiceClient;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CartController controller = new CartController(cartServiceClient);
        client = WebTestClient.bindToController(controller).build();
    }

    private CartResponseDTO sampleCart(String cartId) {
        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cartId);
        response.setCustomerId("cust-1");
        return response;
    }

    private WebClientResponseException webClientError(int status, String statusText) {
        return WebClientResponseException.create(status, statusText, HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
    }

    // deleteAllItemsInCart

    @Test
    void deleteAllItemsInCart_success_returns204() {
        when(cartServiceClient.deleteAllItemsInCart("c-1")).thenReturn(Mono.empty());

        client.delete().uri(CART_URL + "/products", "c-1")
                .exchange()
                .expectStatus().isNoContent();

        verify(cartServiceClient).deleteAllItemsInCart("c-1");
    }

    @Test
    void deleteAllItemsInCart_invalidInput_returns422() {
        when(cartServiceClient.deleteAllItemsInCart("c-1")).thenReturn(Mono.error(new InvalidInputException("bad")));

        client.delete().uri(CART_URL + "/products", "c-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void deleteAllItemsInCart_notFound_returns404() {
        when(cartServiceClient.deleteAllItemsInCart("c-1")).thenReturn(Mono.error(new NotFoundException("missing")));

        client.delete().uri(CART_URL + "/products", "c-1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteAllItemsInCart_webClientError_returnsSameStatus() {
        when(cartServiceClient.deleteAllItemsInCart("c-1")).thenReturn(Mono.error(webClientError(409, "Conflict")));

        client.delete().uri(CART_URL + "/products", "c-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    // addProductToCart

    @Test
    void addProductToCart_withoutProductId_returnsCreatedWithoutLocation() {
        when(cartServiceClient.addProductToCart(eq("c-1"), any())).thenReturn(Mono.just(sampleCart("c-1")));

        client.post().uri(CART_URL + "/products", "c-1")
                .bodyValue(new CartItemRequestDTO())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().doesNotExist("Location");
    }

    @Test
    void addProductToCart_webClientBadRequest_returns400() {
        when(cartServiceClient.addProductToCart(eq("c-1"), any())).thenReturn(Mono.error(webClientError(400, "Bad Request")));

        client.post().uri(CART_URL + "/products", "c-1")
                .bodyValue(new CartItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void addProductToCart_otherWebClientError_returnsSameStatus() {
        when(cartServiceClient.addProductToCart(eq("c-1"), any())).thenReturn(Mono.error(webClientError(409, "Conflict")));

        client.post().uri(CART_URL + "/products", "c-1")
                .bodyValue(new CartItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void addProductToCart_unexpectedError_returns500() {
        when(cartServiceClient.addProductToCart(eq("c-1"), any())).thenReturn(Mono.error(new RuntimeException("boom")));

        client.post().uri(CART_URL + "/products", "c-1")
                .bodyValue(new CartItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // updateProductQuantityInCart (PUT and PATCH share the same error handling)

    @Test
    void updateProductQuantity_put_success() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.just(sampleCart("c-1")));

        client.put().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateProductQuantity_invalidInput_returns422() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(new InvalidInputException("bad")));

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void updateProductQuantity_notFoundException_returns404() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(new NotFoundException("missing")));

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateProductQuantity_webClientSubclasses_returnMatchingStatus() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(webClientError(422, "Unprocessable Entity")))
                .thenReturn(Mono.error(webClientError(404, "Not Found")));

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateProductQuantity_otherWebClientError_returnsSameStatus() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(webClientError(409, "Conflict")));

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateProductQuantity_responseStatusException_returnsMatchingStatus() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY)))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isNotFound();

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void updateProductQuantity_unexpectedError_returns500() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.patch().uri(CART_URL + "/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // getCartByCustomerId

    @Test
    void getCartByCustomerId_returnsCart() {
        when(cartServiceClient.getCartByCustomerId("cust-1")).thenReturn(Mono.just(sampleCart("c-1")));

        client.get().uri("/api/v2/gateway/carts/customer/{customerId}", "cust-1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getCartByCustomerId_missing_returns404() {
        when(cartServiceClient.getCartByCustomerId("missing")).thenReturn(Mono.empty());

        client.get().uri("/api/v2/gateway/carts/customer/{customerId}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    // wishlist

    @Test
    void addProductToWishlist_invalidInput_returns400() {
        when(cartServiceClient.addProductToWishlist(eq("c-1"), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(new InvalidInputException("bad")));

        client.post().uri(CART_URL + "/wishlist", "c-1")
                .bodyValue(new WishlistItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void addProductToWishlist_webClientUnprocessable_returns422() {
        when(cartServiceClient.addProductToWishlist(eq("c-1"), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(webClientError(422, "Unprocessable Entity")));

        client.post().uri(CART_URL + "/wishlist", "c-1")
                .bodyValue(new WishlistItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void addProductToWishlist_otherWebClientError_returnsSameStatus() {
        when(cartServiceClient.addProductToWishlist(eq("c-1"), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(webClientError(409, "Conflict")));

        client.post().uri(CART_URL + "/wishlist", "c-1")
                .bodyValue(new WishlistItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void addProductToWishlist_unexpectedError_returns500() {
        when(cartServiceClient.addProductToWishlist(eq("c-1"), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.post().uri(CART_URL + "/wishlist", "c-1")
                .bodyValue(new WishlistItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void removeProductFromWishlist_invalidInput_returns422() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-1")).thenReturn(Mono.error(new InvalidInputException("bad")));

        client.delete().uri(CART_URL + "/wishlist/{productId}", "c-1", "p-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void removeProductFromWishlist_webClientError_returnsSameStatus() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-1")).thenReturn(Mono.error(webClientError(409, "Conflict")));

        client.delete().uri(CART_URL + "/wishlist/{productId}", "c-1", "p-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void removeProductFromWishlist_illegalArgument_returns500() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-1")).thenReturn(Mono.error(new IllegalArgumentException("bad")));

        client.delete().uri(CART_URL + "/wishlist/{productId}", "c-1", "p-1")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // wishlist transfers

    @Test
    void createWishlistTransfer_success_returns200() {
        when(cartServiceClient.createWishlistTransfer(eq("c-1"), any(), any())).thenReturn(Mono.just(sampleCart("c-1")));

        client.post().uri(CART_URL + "/wishlist-transfers", "c-1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void createWishlistTransfer_notFound_returns404() {
        when(cartServiceClient.createWishlistTransfer(eq("c-1"), any(), any())).thenReturn(Mono.error(webClientError(404, "Not Found")));

        client.post().uri(CART_URL + "/wishlist-transfers", "c-1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createWishlistTransfer_unprocessableOrInvalidInput_returns422() {
        when(cartServiceClient.createWishlistTransfer(eq("c-1"), any(), any()))
                .thenReturn(Mono.error(webClientError(422, "Unprocessable Entity")))
                .thenReturn(Mono.error(new InvalidInputException("bad")));

        client.post().uri(CART_URL + "/wishlist-transfers", "c-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        client.post().uri(CART_URL + "/wishlist-transfers", "c-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void createWishlistTransfer_otherWebClientError_returnsSameStatus() {
        when(cartServiceClient.createWishlistTransfer(eq("c-1"), any(), any())).thenReturn(Mono.error(webClientError(409, "Conflict")));

        client.post().uri(CART_URL + "/wishlist-transfers", "c-1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createWishlistTransfer_unexpectedError_returns500() {
        when(cartServiceClient.createWishlistTransfer(eq("c-1"), any(), any())).thenReturn(Mono.error(new RuntimeException("boom")));

        client.post().uri(CART_URL + "/wishlist-transfers", "c-1")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // promos exposed through the cart controller

    @Test
    void getAllPromos_returnsPromos() {
        when(cartServiceClient.getAllPromoCodes()).thenReturn(Flux.just(new PromoCodeResponseDTO()));

        client.get().uri("/api/v2/gateway/carts/promos")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PromoCodeResponseDTO.class)
                .hasSize(1);
    }

    @Test
    void getPromoCodeById_returnsPromoOrNotFound() {
        when(cartServiceClient.getPromoCodeById("p-1")).thenReturn(Mono.just(new PromoCodeResponseDTO()));
        when(cartServiceClient.getPromoCodeById("missing")).thenReturn(Mono.empty());

        client.get().uri("/api/v2/gateway/carts/promos/{promoCodeId}", "p-1")
                .exchange()
                .expectStatus().isOk();

        client.get().uri("/api/v2/gateway/carts/promos/{promoCodeId}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }
}
