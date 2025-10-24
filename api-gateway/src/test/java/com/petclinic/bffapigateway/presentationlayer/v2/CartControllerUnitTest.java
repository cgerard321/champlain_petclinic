package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CartController.class, CartServiceClient.class })
@WebFluxTest(controllers = CartController.class)
@AutoConfigureWebTestClient
public class CartControllerUnitTest {
    private org.springframework.web.reactive.function.client.WebClientResponseException.NotFound mockNotFound() {
        return org.mockito.Mockito.mock(
                org.springframework.web.reactive.function.client.WebClientResponseException.NotFound.class,
                org.mockito.Mockito.withSettings().stubOnly()
        );
    }


    private org.springframework.web.reactive.function.client.WebClientResponseException.UnprocessableEntity mockUnprocessable() {
        return org.mockito.Mockito.mock(
                org.springframework.web.reactive.function.client.WebClientResponseException.UnprocessableEntity.class,
                org.mockito.Mockito.withSettings().stubOnly()
        );
    }


    private org.springframework.web.reactive.function.client.WebClientResponseException mockWithStatus(
            org.springframework.http.HttpStatus status) {

        return org.springframework.web.reactive.function.client.WebClientResponseException.create(
                status.value(),
                status.getReasonPhrase(),
                org.springframework.http.HttpHeaders.EMPTY,
                new byte[0],
                java.nio.charset.StandardCharsets.UTF_8
        );
    }
    @Autowired
    private WebTestClient client;

    @MockBean
    private CartServiceClient cartServiceClient;

        @Test
        void testDeleteAllItemsInCart_Success() {
                // Arrange
                when(cartServiceClient.deleteAllItemsInCart("cartId123")).thenReturn(Mono.empty());

                // Act
                client.delete()
                                .uri("/api/v2/gateway/carts/cartId123/items")
                                .exchange()
                                .expectStatus().isNoContent()
                                .expectBody().isEmpty();

                // Assert
                verify(cartServiceClient, times(1)).deleteAllItemsInCart("cartId123");
        }

    @Test
    void testGetCartByCustomerId_Success() {
        //arrange
        String customerId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        CartResponseDTO mockResponse = new CartResponseDTO();
        mockResponse.setCartId(customerId);
        mockResponse.setCustomerId("customer1");

        when(cartServiceClient.getCartByCustomerId(customerId)).thenReturn(Mono.just(mockResponse));

        //act
        client.get()
                .uri("/api/v2/gateway/carts/customer/" + customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .consumeWith(response -> {
                    CartResponseDTO responseBody = response.getResponseBody();
                    assertEquals(customerId, responseBody.getCartId());
                    assertEquals("customer1", responseBody.getCustomerId());
                });

        //assert
        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }

    @Test
    void testGetCartByCustomerId_NotFound() {
        //arrange
        String customerId = "non-existent-customer-id";
        when(cartServiceClient.getCartByCustomerId(customerId)).thenReturn(Mono.empty());

        //act
        client.get()
                .uri("/api/v2/gateway/carts/customer/" + customerId)
                .exchange()
                .expectStatus().isNotFound();

        //assert
        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }
    @Test
    void testGetCartById_Success() {
        String cartId = "c-1";
        CartResponseDTO dto = new CartResponseDTO();
        dto.setCartId(cartId);
        when(cartServiceClient.getCartByCartId(cartId)).thenReturn(Mono.just(dto));

        client.get()
                .uri("/api/v2/gateway/carts/{cartId}", cartId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .consumeWith(r -> assertEquals(cartId, r.getResponseBody().getCartId()));

        verify(cartServiceClient).getCartByCartId(cartId);
    }

    @Test
    void testGetCartById_NotFound() {
        String cartId = "missing";
        when(cartServiceClient.getCartByCartId(cartId)).thenReturn(Mono.empty());

        client.get()
                .uri("/api/v2/gateway/carts/{cartId}", cartId)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient).getCartByCartId(cartId);
    }

    @Test
    void testGetAllCarts_Success() {
        CartResponseDTO cart1 = new CartResponseDTO();
        cart1.setCartId("c-1");
        CartResponseDTO cart2 = new CartResponseDTO();
        cart2.setCartId("c-2");

        when(cartServiceClient.getAllCarts()).thenReturn(Flux.just(cart1, cart2));

        client.get()
                .uri("/api/v2/gateway/carts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartResponseDTO.class)
                .hasSize(2)
                .contains(cart1, cart2);

        verify(cartServiceClient).getAllCarts();
    }

    @Test
    void testDeleteCartByCartId_Success() {
        String cartId = "c-2";
        when(cartServiceClient.deleteCartByCartId(cartId)).thenReturn(Mono.just(new CartResponseDTO()));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}", cartId)
                .exchange()
                .expectStatus().isOk();

        verify(cartServiceClient).deleteCartByCartId(cartId);
    }

    @Test
    void testDeleteCartByCartId_NotFound() {
        String cartId = "missing";
        when(cartServiceClient.deleteCartByCartId(cartId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}", cartId)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient).deleteCartByCartId(cartId);
    }

    @Test
    void testRemoveProductFromCart_Success() {
        String cartId = "c-3", productId = "p-9";
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.just(new CartResponseDTO()));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/{productId}", cartId, productId)
                .exchange()
                .expectStatus().isOk();

        verify(cartServiceClient).removeProductFromCart(cartId, productId);
    }

    @Test
    void testRemoveProductFromCart_NotFound() {
        String cartId = "c-3", productId = "p-missing";
        when(cartServiceClient.removeProductFromCart(cartId, productId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/{productId}", cartId, productId)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient).removeProductFromCart(cartId, productId);
    }

    @Test
    void testAddProductToCart_Success() {
        String cartId = "c-10";
        CartResponseDTO dto = new CartResponseDTO();
        when(cartServiceClient.addProductToCart(eq(cartId), any())).thenReturn(Mono.just(dto));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", cartId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO("p-1", 2))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/v1/carts/" + cartId + "/items/p-1");

        verify(cartServiceClient).addProductToCart(eq(cartId), any());
    }

    @Test
    void testAddProductToCart_BadRequest400_WithMessage() {
        String cartId = "c-10";
        InvalidInputException ex = new InvalidInputException("Only 10 items left in stock");
        when(cartServiceClient.addProductToCart(eq(cartId), any())).thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", cartId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(CartResponseDTO.class)
                .consumeWith(r -> assertEquals("Only 10 items left in stock", r.getResponseBody().getMessage()));
    }

    @Test
    void testAddProductToCart_NotFound404() {
        String cartId = "c-10";
        when(cartServiceClient.addProductToCart(eq(cartId), any()))
                .thenReturn(Mono.error(new org.webjars.NotFoundException("nope")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", cartId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateProductQuantityInCart_422() {
        String cartId = "c-11", productId = "p-1";
        when(cartServiceClient.updateProductQuantityInCart(eq(cartId), eq(productId), any()))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "bad")));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", cartId, productId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void testUpdateProductQuantityInCart_404() {
        String cartId = "c-11", productId = "p-1";
        when(cartServiceClient.updateProductQuantityInCart(eq(cartId), eq(productId), any()))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "nope")));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", cartId, productId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testMoveFromWishListToCart_422() {
        String cartId = "c-1", productId = "p-2";
        when(cartServiceClient.moveProductFromWishListToCart(cartId, productId))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "bad")));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void testMoveFromCartToWishlist_404() {
        String cartId = "c-1", productId = "p-x";
        when(cartServiceClient.moveProductFromCartToWishlist(cartId, productId))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "nope")));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testAddProductToWishList_404() {
        String cartId = "c-1", productId = "p-x";
        when(cartServiceClient.addProductToWishList(cartId, productId, 2))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "nope")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", cartId, productId, 2)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testRemoveProductFromWishlist_422() {
        String cartId = "c-1", productId = "p-1";
        when(cartServiceClient.removeProductFromWishlist(cartId, productId))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "bad")));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", cartId, productId)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void testGetAllPromos_Smoke() {
        when(cartServiceClient.getAllPromoCodes()).thenReturn(reactor.core.publisher.Flux.empty());
        client.get().uri("/api/v2/gateway/carts/promos").exchange().expectStatus().isOk();
        verify(cartServiceClient).getAllPromoCodes();
    }

    @Test
    void testGetPromoCodeById_NotFound() {
        when(cartServiceClient.getPromoCodeById("x")).thenReturn(Mono.empty());
        client.get().uri("/api/v2/gateway/carts/promos/{id}", "x").exchange().expectStatus().isNotFound();
        verify(cartServiceClient).getPromoCodeById("x");
    }


    @Test
    void testUpdateProductQuantityInCart_Success() {
        String cartId = "c-11", productId = "p-1";
        when(cartServiceClient.updateProductQuantityInCart(eq(cartId), eq(productId), any()))
                .thenReturn(Mono.just(new CartResponseDTO()));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", cartId, productId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateProductQuantityInCart_5xx_Generic() {
        String cartId = "c-11", productId = "p-1";
        when(cartServiceClient.updateProductQuantityInCart(eq(cartId), eq(productId), any()))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", cartId, productId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testMoveFromCartToWishlist_422() {
        when(cartServiceClient.moveProductFromCartToWishlist("c-1", "p-1"))
                .thenReturn(Mono.error(mockUnprocessable()));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", "c-1", "p-1")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void testMoveFromCartToWishlist_5xx_Generic() {
        when(cartServiceClient.moveProductFromCartToWishlist("c-1", "p-1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", "c-1", "p-1")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testMoveFromWishListToCart_5xx_Generic() {
        when(cartServiceClient.moveProductFromWishListToCart("c-1", "p-2"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", "c-1", "p-2")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testAddProductToWishList_422() {
        when(cartServiceClient.addProductToWishList("c-1", "p-2", 2))
                .thenReturn(Mono.error(mockUnprocessable()));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", "c-1", "p-2", 2)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void testAddProductToWishList_5xx_Generic() {
        when(cartServiceClient.addProductToWishList("c-1", "p-2", 2))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", "c-1", "p-2", 2)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testRemoveProductFromWishlist_404() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-x"))
                .thenReturn(Mono.error(mockNotFound()));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", "c-1", "p-x")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testRemoveProductFromWishlist_5xx_Generic() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", "c-1", "p-1")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testAddProductToCartFromProducts_Success() {
        when(cartServiceClient.addProductToCartFromProducts("c-1", "p-7"))
                .thenReturn(Mono.just(new CartResponseDTO()));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/{productId}", "c-1", "p-7")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testAddProductToCartFromProducts_400_InvalidInput() {
        when(cartServiceClient.addProductToCartFromProducts("c-1", "p-7"))
                .thenReturn(Mono.error(new InvalidInputException("bad qty")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/{productId}", "c-1", "p-7")
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void testAddProductToCartFromProducts_409_ConflictPropagated() {
        when(cartServiceClient.addProductToCartFromProducts("c-1", "p-7"))
                .thenReturn(Mono.error(mockWithStatus(org.springframework.http.HttpStatus.CONFLICT)));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/{productId}", "c-1", "p-7")
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void testAddProductToCartFromProducts_400_BadRequestFromWebClient() {
        var ex = Mockito.mock(org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest.class);
        when(ex.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);

        when(cartServiceClient.addProductToCartFromProducts("c-1", "p-7"))
                .thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/{productId}", "c-1", "p-7")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void testAddProductToCartFromProducts_5xx_Unexpected() {
        when(cartServiceClient.addProductToCartFromProducts("c-1", "p-7"))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/{productId}", "c-1", "p-7")
                .exchange()
                .expectStatus().is5xxServerError();
    }
    @Test
    void testAddProductToCart_409_ConflictPropagated() {
        when(cartServiceClient.addProductToCart(eq("c-10"), any()))
                .thenReturn(Mono.error(mockWithStatus(org.springframework.http.HttpStatus.CONFLICT)));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", "c-10")
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void testUpdateProductQuantityInCart_404_Subclass() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-11"), eq("p-1"), any()))
                .thenReturn(Mono.error(mockNotFound())); // hits `instanceof WebClientResponseException.NotFound`

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", "c-11", "p-1")
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void testCheckoutCart_Success() {
        when(cartServiceClient.checkoutCart("c-1")).thenReturn(Mono.just(new CartResponseDTO()));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/checkout", "c-1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testCheckoutCart_NotFound_DefaultIfEmpty() {
        when(cartServiceClient.checkoutCart("missing")).thenReturn(Mono.empty());

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/checkout", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void testMoveFromWishListToCart_404_Subclass() {
        when(cartServiceClient.moveProductFromWishListToCart("c-1", "p-2"))
                .thenReturn(Mono.error(mockNotFound()));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", "c-1", "p-2")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testMoveFromCartToWishlist_422_Subclass() {
        when(cartServiceClient.moveProductFromCartToWishlist("c-1", "p-1"))
                .thenReturn(Mono.error(mockUnprocessable()));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", "c-1", "p-1")
                .exchange()
                .expectStatus().isEqualTo(422);
    }
    @Test
    void testMoveFromWishListToCart_NotFound_DefaultIfEmpty_Branch() {
        when(cartServiceClient.moveProductFromWishListToCart("c-1", "p-2"))
                .thenReturn(Mono.empty());

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", "c-1", "p-2")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testMoveFromCartToWishlist_NotFound_DefaultIfEmpty_Branch() {
        when(cartServiceClient.moveProductFromCartToWishlist("c-1", "p-3"))
                .thenReturn(Mono.empty());

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", "c-1", "p-3")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testAddProductToWishList_NotFound_DefaultIfEmpty_Branch() {
        when(cartServiceClient.addProductToWishList("c-1", "p-4", 2))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", "c-1", "p-4", 2)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testRemoveProductFromWishlist_NotFound_DefaultIfEmpty_Branch() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-5"))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", "c-1", "p-5")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testAddProductToCart_5xx_GenericElseBranch() {
        when(cartServiceClient.addProductToCart(eq("c-10"), any()))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", "c-10")
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().is5xxServerError();
    }
    @Test
    void testAddProductToCart_400_BadRequestFromWebClient() {
        var ex = Mockito.mock(WebClientResponseException.BadRequest.class);
        when(ex.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        when(cartServiceClient.addProductToCart(eq("c-10"), any()))
                .thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", "c-10")
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().isBadRequest();
    }



    @Test
    void testMoveFromWishListToCart_422_Subclass() {
        when(cartServiceClient.moveProductFromWishListToCart("c-1", "p-2"))
                .thenReturn(Mono.error(mockUnprocessable()));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", "c-1", "p-2")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void testMoveFromCartToWishlist_404_Subclass() {
        when(cartServiceClient.moveProductFromCartToWishlist("c-1", "p-miss"))
                .thenReturn(Mono.error(mockNotFound()));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", "c-1", "p-miss")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testRemoveProductFromWishlist_422_Subclass() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-1"))
                .thenReturn(Mono.error(mockUnprocessable()));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", "c-1", "p-1")
                .exchange()
                .expectStatus().isEqualTo(422);
    }
    @Test
    void testAddProductToCart_BadRequest_WithMessage() {
        String cartId = "cart-2";
        InvalidInputException ex = new InvalidInputException("Only 10 items left in stock");
        when(cartServiceClient.addProductToCart(eq(cartId), any())).thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", cartId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(CartResponseDTO.class)
                .consumeWith(r -> {
                    Assertions.assertEquals("Only 10 items left in stock", r.getResponseBody().getMessage());
                });
    }

    @Test
    void testAddProductToCart_NotFound() {
        String cartId = "cart-2";
        when(cartServiceClient.addProductToCart(eq(cartId), any()))
                .thenReturn(Mono.error(new org.webjars.NotFoundException("nope")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", cartId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void testAddProductToCart_Conflict() {
        String cartId = "cart-2";
        WebClientResponseException ex = WebClientResponseException.create(409, "Conflict", null, null, null);
        when(cartServiceClient.addProductToCart(eq(cartId), any())).thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", cartId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void testAddProductToCart_UnexpectedError() {
        String cartId = "cart-2";
        when(cartServiceClient.addProductToCart(eq(cartId), any())).thenReturn(Mono.error(new RuntimeException("boom")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", cartId)
                .bodyValue(new com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO())
                .exchange()
                .expectStatus().is5xxServerError();
    }
    @Test
    void testMoveAllWishlistToCart_NotFound() {
        String cartId = "cart-3";
        WebClientResponseException ex = WebClientResponseException.create(404, "Not Found", null, null, null);
        when(cartServiceClient.moveAllWishlistToCart(cartId)).thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/moveAll", cartId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(CartResponseDTO.class)
                .consumeWith(r -> {
                    Assertions.assertEquals("Cart not found: cart-3", r.getResponseBody().getMessage());
                });
    }

    @Test
    void testMoveAllWishlistToCart_UnprocessableEntity() {
        String cartId = "cart-3";
        WebClientResponseException ex = WebClientResponseException.create(422, "Unprocessable Entity", null, null, null);
        when(cartServiceClient.moveAllWishlistToCart(cartId)).thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/moveAll", cartId)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(CartResponseDTO.class)
                .consumeWith(r -> {
                    Assertions.assertEquals("422 Unprocessable Entity", r.getResponseBody().getMessage());
                });
    }
    @Test
    void testMoveAllWishlistToCart_UnexpectedError() {
        String cartId = "cart-3";
        when(cartServiceClient.moveAllWishlistToCart(cartId)).thenReturn(Mono.error(new RuntimeException("boom")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/moveAll", cartId)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(CartResponseDTO.class)
                .consumeWith(r -> {
                    Assertions.assertEquals("Unexpected error", r.getResponseBody().getMessage());
                });
    }
    @Test
    void testAddProductToWishList_Success() {
        // Arrange
        String cartId = "cart-1";
        String productId = "prod-1";
        int quantity = 2;
        CartResponseDTO response = new CartResponseDTO();
        when(cartServiceClient.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.just(response));

        // Act & Assert
        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", cartId, productId, quantity)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class);
    }

    @Test
    void testAddProductToWishList_NotFound() {
        String cartId = "cart-2";
        String productId = "prod-2";
        int quantity = 1;
        when(cartServiceClient.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", cartId, productId, quantity)
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void testAddProductToWishList_UnprocessableEntity() {
        String cartId = "cart-3";
        String productId = "prod-3";
        int quantity = 1;
        WebClientResponseException.UnprocessableEntity ex =
                (WebClientResponseException.UnprocessableEntity) WebClientResponseException.create(422, "Unprocessable Entity", null, null, null);
        when(cartServiceClient.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", cartId, productId, quantity)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void testAddProductToWishList_UnexpectedError() {
        String cartId = "cart-4";
        String productId = "prod-4";
        int quantity = 1;
        RuntimeException ex = new RuntimeException("boom");
        when(cartServiceClient.addProductToWishList(cartId, productId, quantity))
                .thenReturn(Mono.error(ex));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}/quantity/{quantity}", cartId, productId, quantity)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    @Test
    void testMoveProductFromCartToWishlist_Success() {
        String cartId = "cart-1", productId = "prod-1";
        CartResponseDTO dto = new CartResponseDTO();
        when(cartServiceClient.moveProductFromCartToWishlist(cartId, productId)).thenReturn(Mono.just(dto));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testMoveProductFromCartToWishlist_422() {
        String cartId = "cart-2", productId = "prod-2";
        WebClientResponseException ex = WebClientResponseException.create(
                422, "Unprocessable Entity", null, null, null);
        when(cartServiceClient.moveProductFromCartToWishlist(cartId, productId)).thenReturn(Mono.error(ex));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .exchange()
                .expectStatus().isEqualTo(422);
    }
    @Test
    void testMoveProductFromCartToWishlist_404() {
        String cartId = "cart-3", productId = "prod-3";
        WebClientResponseException ex = WebClientResponseException.create(
                404, "Not Found", null, null, null);
        when(cartServiceClient.moveProductFromCartToWishlist(cartId, productId)).thenReturn(Mono.error(ex));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testMoveProductFromCartToWishlist_UnexpectedError() {
        String cartId = "cart-4", productId = "prod-4";
        RuntimeException ex = new RuntimeException("boom");
        when(cartServiceClient.moveProductFromCartToWishlist(cartId, productId)).thenReturn(Mono.error(ex));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toWishList", cartId, productId)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    @Test
    void testMoveProductFromWishListToCart_Success() {
        String cartId = "cart-1", productId = "prod-1";
        CartResponseDTO dto = new CartResponseDTO();
        when(cartServiceClient.moveProductFromWishListToCart(cartId, productId)).thenReturn(Mono.just(dto));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testMoveProductFromWishListToCart_422() {
        String cartId = "cart-2", productId = "prod-2";
        WebClientResponseException ex = WebClientResponseException.create(
                422, "Unprocessable Entity", null, null, null);
        when(cartServiceClient.moveProductFromWishListToCart(cartId, productId)).thenReturn(Mono.error(ex));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .exchange()
                .expectStatus().isEqualTo(422);
    }
    @Test
    void testMoveProductFromWishListToCart_404() {
        String cartId = "cart-3", productId = "prod-3";
        WebClientResponseException ex = WebClientResponseException.create(
                404, "Not Found", null, null, null);
        when(cartServiceClient.moveProductFromWishListToCart(cartId, productId)).thenReturn(Mono.error(ex));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testMoveProductFromWishListToCart_UnexpectedError() {
        String cartId = "cart-4", productId = "prod-4";
        RuntimeException ex = new RuntimeException("boom");
        when(cartServiceClient.moveProductFromWishListToCart(cartId, productId)).thenReturn(Mono.error(ex));

        client.put()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}/toCart", cartId, productId)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    @Test
    void testGetAllCarts_Empty() {
        when(cartServiceClient.getAllCarts()).thenReturn(Flux.empty());

        client.get()
                .uri("/api/v2/gateway/carts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartResponseDTO.class)
                .hasSize(0);

        verify(cartServiceClient).getAllCarts();
    }
}
