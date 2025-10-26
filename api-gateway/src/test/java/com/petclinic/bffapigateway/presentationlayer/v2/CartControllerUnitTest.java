package com.petclinic.bffapigateway.presentationlayer.v2;

/*

                .contains(cart1, cart2);

        verify(cartServiceClient).getAllCarts();
    }

    @Test
    void testDeleteCartByCartId_Success() {
        String cartId = "c-2";
        when(cartServiceClient.deleteCartByCartId(cartId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}", cartId)
                .exchange()
                .expectStatus().isNoContent();

        verify(cartServiceClient).deleteCartByCartId(cartId);
    }

    @Test
    void testDeleteCartByCartId_NotFound() {
        String cartId = "missing";
                when(cartServiceClient.deleteCartByCartId(cartId)).thenReturn(Mono.error(new NotFoundException("missing")));

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
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", cartId, productId)
                .exchange()
                .expectStatus().isNoContent();

        verify(cartServiceClient).removeProductFromCart(cartId, productId);
    }

    @Test
    void testRemoveProductFromCart_NotFound() {
        String cartId = "c-3", productId = "p-missing";
        when(cartServiceClient.removeProductFromCart(cartId, productId))
                .thenReturn(Mono.error(new NotFoundException("missing")));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", cartId, productId)
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
                .expectHeader().valueEquals("Location", "/api/v1/carts/" + cartId + "/products/p-1");

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
    void testAddProductToWishlist_Success() {
        String cartId = "c-1";
        WishlistItemRequestDTO request = new WishlistItemRequestDTO("p-1", 2);
        when(cartServiceClient.addProductToWishlist(eq(cartId), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.just(new CartResponseDTO()))
                ;

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist", cartId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void testAddProductToWishlist_404() {
        String cartId = "c-1";
        WishlistItemRequestDTO request = new WishlistItemRequestDTO("p-x", 2);
        when(cartServiceClient.addProductToWishlist(eq(cartId), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(new NotFoundException("nope")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist", cartId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testAddProductToWishlist_422() {
        String cartId = "c-1";
        WishlistItemRequestDTO request = new WishlistItemRequestDTO("p-2", 2);
        when(cartServiceClient.addProductToWishlist(eq(cartId), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(new InvalidInputException("bad")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist", cartId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

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
    void testRemoveProductFromWishlist_NoContent_OnEmptyCompletion() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-5"))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", "c-1", "p-5")
                .exchange()
                .expectStatus().isNoContent();
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
        void testCreateWishlistTransfer_NotFound() {
                String cartId = "cart-3";
                WebClientResponseException ex = WebClientResponseException.create(404, "Not Found", null, null, null);
                when(cartServiceClient.createWishlistTransfer(eq(cartId), anyList(), eq(WishlistTransferDirectionDTO.TO_CART))).thenReturn(Mono.error(ex));

                client.post()
                                .uri("/api/v2/gateway/carts/{cartId}/wishlist-transfers", cartId)
                                .bodyValue(new WishlistTransferRequestDTO(null, WishlistTransferDirectionDTO.TO_CART))
                                .exchange()
                                .expectStatus().isNotFound()
                                .expectBody(CartResponseDTO.class)
                                .consumeWith(r -> Assertions.assertEquals("Cart not found: cart-3", r.getResponseBody().getMessage()));
        }

        @Test
        void testCreateWishlistTransfer_UnprocessableEntity() {
                String cartId = "cart-3";
                WebClientResponseException ex = WebClientResponseException.create(422, "Unprocessable Entity", null, null, null);
                when(cartServiceClient.createWishlistTransfer(eq(cartId), anyList(), eq(WishlistTransferDirectionDTO.TO_CART))).thenReturn(Mono.error(ex));

                client.post()
                                .uri("/api/v2/gateway/carts/{cartId}/wishlist-transfers", cartId)
                                .bodyValue(new WishlistTransferRequestDTO(null, WishlistTransferDirectionDTO.TO_CART))
                                .exchange()
                                .expectStatus().isEqualTo(422)
                                .expectBody(CartResponseDTO.class)
                                .consumeWith(r -> Assertions.assertEquals("422 Unprocessable Entity", r.getResponseBody().getMessage()));
        }

        @Test
        void testCreateWishlistTransfer_UnexpectedError() {
                String cartId = "cart-3";
                when(cartServiceClient.createWishlistTransfer(eq(cartId), anyList(), eq(WishlistTransferDirectionDTO.TO_CART))).thenReturn(Mono.error(new RuntimeException("boom")));

                client.post()
                                .uri("/api/v2/gateway/carts/{cartId}/wishlist-transfers", cartId)
                                .bodyValue(new WishlistTransferRequestDTO(null, WishlistTransferDirectionDTO.TO_CART))
                                .exchange()
                                .expectStatus().is5xxServerError()
                                .expectBody(CartResponseDTO.class)
                                .consumeWith(r -> Assertions.assertEquals("Unexpected error", r.getResponseBody().getMessage()));
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
*/

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartItemRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.UpdateProductQuantityRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.WishlistItemRequestDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CartControllerUnitTest {

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

    @Test
    void getCartById_returnsCart() {
        when(cartServiceClient.getCartByCartId("c-1"))
                .thenReturn(Mono.just(sampleCart("c-1")));

        CartResponseDTO body = client.get()
                .uri("/api/v2/gateway/carts/{cartId}", "c-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).isNotNull();
        assertThat(body.getCartId()).isEqualTo("c-1");
        verify(cartServiceClient).getCartByCartId("c-1");
    }

    @Test
    void getCartById_missing_returns404() {
        when(cartServiceClient.getCartByCartId("missing"))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/v2/gateway/carts/{cartId}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllCarts_returnsFlux() {
        CartResponseDTO cart1 = sampleCart("c-1");
        CartResponseDTO cart2 = sampleCart("c-2");
        when(cartServiceClient.getAllCarts()).thenReturn(Flux.just(cart1, cart2));

        List<CartResponseDTO> response = client.get()
                .uri("/api/v2/gateway/carts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).containsExactly(cart1, cart2);
        verify(cartServiceClient).getAllCarts();
    }

    @Test
    void deleteCartById_success_returns204() {
        when(cartServiceClient.deleteCartByCartId("c-1")).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}", "c-1")
                .exchange()
                .expectStatus().isNoContent();

        verify(cartServiceClient).deleteCartByCartId("c-1");
    }

    @Test
    void deleteCartById_notFound_returns404() {
        when(cartServiceClient.deleteCartByCartId("missing"))
                .thenReturn(Mono.error(new NotFoundException("missing")));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void removeProductFromCart_success_returns204() {
        when(cartServiceClient.removeProductFromCart("c-1", "p-1"))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", "c-1", "p-1")
                .exchange()
                .expectStatus().isNoContent();

        verify(cartServiceClient).removeProductFromCart("c-1", "p-1");
    }

    @Test
    void removeProductFromCart_notFound_returns404() {
        when(cartServiceClient.removeProductFromCart("c-1", "missing"))
                .thenReturn(Mono.error(new NotFoundException("missing")));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", "c-1", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addProductToCart_created_returnsLocation() {
        CartItemRequestDTO request = new CartItemRequestDTO("p-99", 3);
        when(cartServiceClient.addProductToCart(eq("c-1"), any(CartItemRequestDTO.class)))
                .thenReturn(Mono.just(sampleCart("c-1")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", "c-1")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/v1/carts/c-1/products/p-99");
    }

    @Test
    void addProductToCart_badRequestPropagates() {
        when(cartServiceClient.addProductToCart(eq("c-1"), any(CartItemRequestDTO.class)))
                .thenReturn(Mono.error(new InvalidInputException("invalid")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", "c-1")
                .bodyValue(new CartItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void addProductToCart_notFoundReturns404() {
        when(cartServiceClient.addProductToCart(eq("c-1"), any(CartItemRequestDTO.class)))
                .thenReturn(Mono.error(new NotFoundException("missing")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/products", "c-1")
                .bodyValue(new CartItemRequestDTO("p-1", 1))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateProductQuantity_patch_success() {
        UpdateProductQuantityRequestDTO requestDTO = new UpdateProductQuantityRequestDTO();
        requestDTO.setQuantity(4);

        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.just(sampleCart("c-1")));

        client.patch()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", "c-1", "p-1")
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateProductQuantity_unprocessable_returns422() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(new WebClientResponseException(
                        "422",
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                        null,
                        null,
                        null,
                        null)));

        client.patch()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void updateProductQuantity_notFound_returns404() {
        when(cartServiceClient.updateProductQuantityInCart(eq("c-1"), eq("p-1"), any(UpdateProductQuantityRequestDTO.class)))
                .thenReturn(Mono.error(new WebClientResponseException(
                        "404",
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        null,
                        null,
                        null,
                        null)));

        client.patch()
                .uri("/api/v2/gateway/carts/{cartId}/products/{productId}", "c-1", "p-1")
                .bodyValue(new UpdateProductQuantityRequestDTO())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addProductToWishlist_created() {
        WishlistItemRequestDTO request = new WishlistItemRequestDTO("p-10", 2);
        when(cartServiceClient.addProductToWishlist(eq("c-1"), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.just(sampleCart("c-1")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist", "c-1")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void addProductToWishlist_notFound() {
        when(cartServiceClient.addProductToWishlist(eq("c-1"), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(new NotFoundException("missing")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist", "c-1")
                .bodyValue(new WishlistItemRequestDTO("p-404", 1))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addProductToWishlist_badRequest() {
        when(cartServiceClient.addProductToWishlist(eq("c-1"), any(WishlistItemRequestDTO.class)))
                .thenReturn(Mono.error(new InvalidInputException("invalid")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist", "c-1")
                .bodyValue(new WishlistItemRequestDTO("p-1", 0))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void removeProductFromWishlist_success_returns204() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "p-1"))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", "c-1", "p-1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void removeProductFromWishlist_notFound_returns404() {
        when(cartServiceClient.removeProductFromWishlist("c-1", "missing"))
                .thenReturn(Mono.error(new NotFoundException("missing")));

        client.delete()
                .uri("/api/v2/gateway/carts/{cartId}/wishlist/{productId}", "c-1", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void checkoutCart_success_returns200() {
        when(cartServiceClient.checkoutCart("c-1")).thenReturn(Mono.just(sampleCart("c-1")));

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/checkout", "c-1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void checkoutCart_missing_returns404() {
        when(cartServiceClient.checkoutCart("missing")).thenReturn(Mono.empty());

        client.post()
                .uri("/api/v2/gateway/carts/{cartId}/checkout", "missing")
                .exchange()
                .expectStatus().isNotFound();
    }
}
