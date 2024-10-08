package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

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
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .productName("Cat Litter")
            .productDescription("Clumping cat litter with odor control")
            .productSalePrice(12.99)
            .quantityInCart(1)
            .averageRating(3.0)
            .build();

    List<CartProduct> wishListProducts = List.of(wishListProduct1, wishlistProduct2);


    private final Cart cart1 = Cart.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .customerId("1")
            .products(products)
            .wishListProducts(wishListProducts)
            .build();

    // UUID for non-existent cart
    private final String nonExistentCartId = "a7d573-bcab-4db3-956f-773324b92a80";
    private final String validCustomerId = "f470653d-05c5-4c45-b7a0-7d70f003d2ac";
    private final String nonExistentCustomerId = "non-existent-customer-id";
    private final String nonExistentProductId = "a7d573-bcab-5555-956f-773324b92a80";


     @Test
     public void whenGetCartById_thenReturnCartResponseModel() {
         when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));

         Mono<CartResponseModel> result = cartService.getCartByCartId(cart1.getCartId());

         StepVerifier.create(result)
                 .expectNextMatches(cartResponseModel -> cartResponseModel.getCartId().equals(cart1.getCartId()))
                 .verifyComplete();
     }

     @Test
     public void whenGetCartByCartId_withNonExistentCartId_thenThrowNotFoundException() {
         when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());

         Mono<CartResponseModel> result = cartService.getCartByCartId(nonExistentCartId);

         StepVerifier.create(result)
                 .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                         && throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId))
                 .verify();
     }

     @Test
     public void whenGetAllCarts_thenReturnCartResponseModel() {
         when(cartRepository.findAll()).thenReturn(Flux.just(cart1));

         Flux<CartResponseModel> result = cartService.getAllCarts();

         StepVerifier.create(result)
                 .expectNextMatches(cartResponseModel -> cartResponseModel.getCartId().equals(cart1.getCartId()))
                 .verifyComplete();
     }


//    @Test
//    public void getCartItemCount_Success() {
//        Cart cart = new Cart();
//        cart.setProductIds(Arrays.asList("prod1", "prod2", "prod3"));
//
//        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.just(cart));
//
//        StepVerifier.create(cartService.getCartItemCount("cart1"))
//                .expectNext(3) // Expect 3 items in the cart
//                .verifyComplete();
//    }

//     @Test
//     public void getCartItemCount_CartNotFound() {
//         when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.empty());

//         StepVerifier.create(cartService.getCartItemCount("cart1"))
//                 .expectError(NotFoundException.class) // Expect a NotFoundException
//                 .verify();
//     }

//     @Test
//     public void whenCreateCart_thenReturnCartResponse() {

//         // arrange
//         CartRequestModel cartRequest = new CartRequestModel("123", null);
//         Cart expectedCart = new Cart();
//         expectedCart.setCartId("abc-123-xyz");
//         expectedCart.setCustomerId("123");

//         // When
//         when(cartRepository.save(any(Cart.class)))
//                 .thenReturn(Mono.just(expectedCart));

//         Mono<CartResponseModel> actualResponse = cartService.createNewCart(cartRequest);

//         // Assert
//         StepVerifier.create(actualResponse)
//                 .expectNextMatches(cart -> cart.getCustomerId().equals("123")
//                         && cart.getCartId().equals("abc-123-xyz"))
//                 .verifyComplete();


//     }


//    @Test
//    public void clearCart_Success() {
//        Cart mockCart = Cart.builder()
//                .cartId("cart1")
//                .products(products)
//                .customerId("customerId1")
//                .build();
//
//        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.just(mockCart));
//
//        ProductResponseModel product1 = new ProductResponseModel("prod1", "Product1", "Desc1", 100.0);
//        ProductResponseModel product2 = new ProductResponseModel("prod2", "Product2", "Desc2", 200.0);
//
//        when(productClient.getProductByProductId("prod1")).thenReturn(Mono.just(product1));
//        when(productClient.getProductByProductId("prod2")).thenReturn(Mono.just(product2));
//
//        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(mockCart));
//
//        StepVerifier.create(cartService.clearCart("cart1"))
//                .expectNext(product1)
//                .expectNext(product2)
//                .verifyComplete();
//
//        verify(cartRepository, times(1)).save(mockCart);
//        assertTrue(mockCart.getProducts().isEmpty());
//    }


    

//     @Test
//     public void clearCart_ReturnsProducts() {
//         Cart cart = Cart.builder()
//                 .cartId("cartId1")
//                 .productIds(List.of("prod1", "prod2"))
//                 .customerId("customerId1")
//                 .build();

//         ProductResponseModel product1 = new ProductResponseModel("prod1", "Product1", "Description1", 100.0);
//         ProductResponseModel product2 = new ProductResponseModel("prod2", "Product2", "Description2", 200.0);

//         when(cartRepository.findCartByCartId("cartId1")).thenReturn(Mono.just(cart));
//         when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));
//         when(productClient.getProductByProductId("prod1")).thenReturn(Mono.just(product1));
//         when(productClient.getProductByProductId("prod2")).thenReturn(Mono.just(product2));

//         StepVerifier.create(cartService.clearCart("cartId1"))
//                 .expectNext(product1)
//                 .expectNext(product2)
//                 .verifyComplete();

//         verify(cartRepository, times(1)).save(cart);
//         assertTrue(cart.getProductIds().isEmpty());
//     }

//     @Test

//     public void whenDeleteCartById_withExistingCart_thenCartIsDeleted() {
//         // Arrange
//         String cartId = cart1.getCartId();
//         when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
//         when(cartRepository.delete(cart1)).thenReturn(Mono.empty());
//         when(productClient.getProductByProductId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")).thenReturn(Mono.just(product1));
//         when(productClient.getProductByProductId("d819e4f4-25af-4d33-91e9-2c45f0071606")).thenReturn(Mono.just(product2));

//         // Act
//         Mono<CartResponseModel> result = cartService.deleteCartByCartId(cartId);

//         // Assert
//         StepVerifier.create(result)
//                 .expectNextMatches(cartResponseModel ->
//                     cartResponseModel.getCartId().equals(cart1.getCartId()))

//                 .verifyComplete();

//         verify(cartRepository, times(1)).findCartByCartId(cartId);
//         verify(cartRepository, times(1)).delete(cart1);
//     }

//     @Test
//     public void whenDeleteCartById_withNonExistentCartId_thenThrowNotFoundException() {
//         // Arrange
//         when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());

//         // Act
//         Mono<CartResponseModel> result = cartService.deleteCartByCartId(nonExistentCartId);

//         // Assert
//         StepVerifier.create(result)
//                 .expectErrorMatches(throwable -> throwable instanceof NotFoundException
//                         && throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId))
//                 .verify();

//         verify(cartRepository, times(1)).findCartByCartId(nonExistentCartId);
//         verify(cartRepository, never()).delete(any(Cart.class));
//     }

    @Test
     void getAllCarts_ReturnsCartResponseModelWithProducts() {
         //mocking cart retrieval
         when(cartRepository.findAll()).thenReturn(Flux.just(cart1));


         //run the test
         StepVerifier.create(cartService.getAllCarts())
                 .expectNextMatches(cartResponseModel ->
                         cartResponseModel.getCustomerId().equals("1") &&
                                 cartResponseModel.getProducts().size() == 2 &&
                                 cartResponseModel.getProducts().get(0).getProductName().equals("Product1") &&
                                 cartResponseModel.getProducts().get(1).getProductName().equals("Product2")
                 )
                 .verifyComplete();

         // Verify interactions
         verify(cartRepository, times(1)).findAll();
     }

     @Test
     void getAllCarts_ReturnsCartWithoutProducts() {
         //mocking cart retrieval with an empty product list
         Cart cartWithNoProducts = Cart.builder()
                 .cartId("123")
                 .customerId("2")
                 .products(new ArrayList<>())
                 .build();

         when(cartRepository.findAll()).thenReturn(Flux.just(cartWithNoProducts));

         // Run the test
         StepVerifier.create(cartService.getAllCarts())
                 .expectNextMatches(cartResponseModel ->
                         cartResponseModel.getCustomerId().equals("2") &&
                                 cartResponseModel.getProducts().isEmpty() //no products in the cart
                 )
                 .verifyComplete();

         // Verify interactions
         verify(cartRepository, times(1)).findAll();
         verifyNoInteractions(productClient); //no products, so productClient shouldn't be called
     }

     @Test
     void getAllCarts_ReturnsMultipleCartsWithProducts() {
        when(cartRepository.findAll()).thenReturn(Flux.just(cart1));

        //mocking product retrieval

        //run the test
        StepVerifier.create(cartService.getAllCarts())
                .expectNextMatches(cartResponseModel ->
                        cartResponseModel.getCustomerId().equals("1") &&
                                cartResponseModel.getProducts().size() == 2 && //2 products in the cart
                                cartResponseModel.getProducts().get(0).getProductName().equals("Product1") &&
                                cartResponseModel.getProducts().get(1).getProductName().equals("Product2")
                )
                .verifyComplete();

         // Verify interactions
         verify(cartRepository, times(1)).findAll();
         verifyNoInteractions(productClient); //no products, so productClient shouldn't be called
     }

     @Test
     void getAllCarts_ReturnsEmptyListWhenNoCarts() {
         //mocking empty cart retrieval
         when(cartRepository.findAll()).thenReturn(Flux.empty());

         //run the test
         StepVerifier.create(cartService.getAllCarts())
                 .expectNextCount(0) //no carts should be returned
                 .verifyComplete();

         //verify interactions
         verify(cartRepository, times(1)).findAll();
         verifyNoInteractions(productClient); //no carts, so productClient shouldn't be called
     }

    @Test
    void addProductToCart_NewProduct_Success() {
        String cartId = cart1.getCartId();
        String productId = product3.getProductId();
        int quantityToAdd = 2;

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Product3")
                .productDescription("Desc3")
                .productSalePrice(300.0)
                .productQuantity(10) // 10 in stock
                .build()));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart1));

        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, quantityToAdd);

        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel -> cartResponseModel.getProducts().stream()
                        .anyMatch(product -> product.getProductId().equals(productId) &&
                                product.getQuantityInCart() == quantityToAdd))
                .verifyComplete();

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addProductToCart_ExistingProduct_Success() {
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int quantityToAdd = 3;

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Product1")
                .productQuantity(10) // 10 in stock
                .build()));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart1));

        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, quantityToAdd);

        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel -> cartResponseModel.getProducts().stream()
                        .anyMatch(product -> product.getProductId().equals(productId) &&
                                product.getQuantityInCart() == quantityToAdd + 1)) // already 1 in cart
                .verifyComplete();

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addProductToCart_QuantityGreaterThanStock_ThrowsOutOfStockException() {
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int quantityToAdd = 11; // exceeding stock of 10

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Product1")
                .productQuantity(10) // Only 10 in stock
                .build()));

        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, quantityToAdd);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof OutOfStockException &&
                        throwable.getMessage().contains("Only 10 items left in stock"))
                .verify();
    }

    @Test
    void addProductToCart_QuantityLessThanOrEqualToZero_ThrowsInvalidInputException() {
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int quantityToAdd = 0; // Invalid quantity

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productQuantity(10)
                .build()));

        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, quantityToAdd);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidInputException &&
                        throwable.getMessage().contains("Quantity must be greater than zero"))
                .verify();
    }

    @Test
    void addProductToCart_CartNotFound_ThrowsNotFoundException() {
        String cartId = nonExistentCartId;
        String productId = product1.getProductId();
        int quantityToAdd = 1;

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, quantityToAdd);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().contains("Cart not found"))
                .verify();
    }

    @Test
    void updateProductQuantityInCart_Success() {
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int newQuantity = 3;

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productQuantity(10) // Stock of 10
                .build()));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart1));

        Mono<CartResponseModel> result = cartService.updateProductQuantityInCart(cartId, productId, newQuantity);

        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel -> cartResponseModel.getProducts().stream()
                        .anyMatch(product -> product.getProductId().equals(productId) &&
                                product.getQuantityInCart() == newQuantity))
                .verifyComplete();

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateProductQuantityInCart_QuantityExceedsStock_ThrowsOutOfStockException() {
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int newQuantity = 15; // Exceeds stock

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productQuantity(10) // Stock of 10
                .build()));

        Mono<CartResponseModel> result = cartService.updateProductQuantityInCart(cartId, productId, newQuantity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof OutOfStockException &&
                        throwable.getMessage().contains("Only 10 items left in stock"))
                .verify();
    }

    @Test
    void updateProductQuantityInCart_ProductNotInCart_ThrowsNotFoundException() {
        String cartId = cart1.getCartId();
        String productId = product3.getProductId(); // Product not in the cart
        int newQuantity = 2;

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productQuantity(10)
                .build()));

        Mono<CartResponseModel> result = cartService.updateProductQuantityInCart(cartId, productId, newQuantity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().contains("Product not found in cart"))
                .verify();
    }

    @Test
    void updateProductQuantityInCart_QuantityLessThanOrEqualToZero_ThrowsInvalidInputException() {
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int newQuantity = 0; // Invalid quantity

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productQuantity(10)
                .build()));

        Mono<CartResponseModel> result = cartService.updateProductQuantityInCart(cartId, productId, newQuantity);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidInputException &&
                        throwable.getMessage().contains("Quantity must be greater than zero"))
                .verify();
    }



    @Test
    void whenCheckoutCart_thenReturnUpdatedCartWithPaymentProcessed() {
        // Given
        String cartId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setCustomerId("customer1");

        CartProduct product = new CartProduct();
        product.setProductId("product1");
        product.setProductSalePrice(100.0);
        product.setQuantityInCart(3); // 3 items at $100 each

        cart.setProducts(List.of(product));

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        Mono<CartResponseModel> result = cartService.checkoutCart(cartId);

        // Then
        StepVerifier.create(result)
                .assertNext(cartResponseModel -> {
                    assertEquals(cartId, cartResponseModel.getCartId());
                    assertEquals(300.0, cartResponseModel.getSubtotal()); // 3 * 100.0
                    assertEquals(29.925, cartResponseModel.getTvq());      // 9.975% tax
                    assertEquals(15.0, cartResponseModel.getTvc());        // 5% tax
                    assertEquals(344.925, cartResponseModel.getTotal());   // subtotal + taxes
                    assertEquals("Payment Processed", cartResponseModel.getPaymentStatus());
                })
                .verifyComplete();
    }

    @Test
    void findCartByCustomerId_withExistingId_thenReturnCartResponseModel() {
        // Arrange
        Mockito.when(cartRepository.findCartByCustomerId(validCustomerId))
                .thenReturn(Mono.just(cart1));

        // Act & Assert
        StepVerifier.create(cartService.findCartByCustomerId(validCustomerId))
                .assertNext(cartResponseModel -> {
                    assertNotNull(cartResponseModel);
                    assertEquals(cart1.getCartId(), cartResponseModel.getCartId());
                    assertEquals(cart1.getCustomerId(), cartResponseModel.getCustomerId());
                    assertEquals(products.size(), cartResponseModel.getProducts().size());
                })
                .verifyComplete(); //test
    }

    @Test
    void findCartByCustomerId_withNonExistentId_thenReturnNotFoundException() {
        // Arrange
        Mockito.when(cartRepository.findCartByCustomerId(nonExistentCustomerId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(cartService.findCartByCustomerId(nonExistentCustomerId))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Cart for customer id was not found: " + nonExistentCustomerId))
                .verify();
    }

    @Test
    void removeProductFromCart_RemovesProductSuccessfully() {
        // Arrange: Mock the cart retrieval
        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));

        // Simulate removing product1 from the cart (which leaves only product2)
        Cart updatedCart = Cart.builder()
                .cartId(cart1.getCartId())
                .customerId(cart1.getCustomerId())
                .products(Collections.singletonList(product2)) // only product2 remains after removal
                .build();

        // Mock the save method to return the updated cart
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(updatedCart));

        // Act: Call the removeProductFromCart method
        StepVerifier.create(cartService.removeProductFromCart(cart1.getCartId(), product1.getProductId()))
                .expectNextMatches(cartResponse ->
                        cartResponse.getProducts().size() == 1 && // one product removed
                                cartResponse.getProducts().get(0).getProductId().equals(product2.getProductId()) // remaining product is product2
                )
                .verifyComplete();

        // Assert: Verify that the repository was called
        verify(cartRepository, times(1)).findCartByCartId(cart1.getCartId());
        verify(cartRepository, times(1)).save(any(Cart.class)); // check that the cart is saved with the remaining products
    }


    @Test
    void removeProductFromCart_CartNotFound() {
        // Arrange: Mock an empty Mono for cart retrieval
        when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());

        // Act & Assert: Expect a NotFoundException when the cart is not found
        StepVerifier.create(cartService.removeProductFromCart(nonExistentCartId, product1.getProductId()))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().contains("Cart id was not found:" + nonExistentCartId))
                .verify();

        // Verify repository interaction
        verify(cartRepository, times(1)).findCartByCartId(nonExistentCartId);
    }

    @Test
    void removeProductFromCart_ProductNotFoundInCart() {
        // Arrange: Mock the cart retrieval to return the existing cart1
        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));

        // Act & Assert: Expect a NotFoundException when trying to remove a product that is not in the cart
        StepVerifier.create(cartService.removeProductFromCart(cart1.getCartId(), product3.getProductId())) // product3 is not in cart1
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("Product id was not found: " + product3.getProductId()) // check exception message
                )
                .verify();

        // Verify repository interaction
        verify(cartRepository, times(1)).findCartByCartId(cart1.getCartId());
        verifyNoMoreInteractions(cartRepository); // No save operation should happen since the product is not found
    }


    void whenMoveProductFromCartToWishlist_thenSuccess() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();

        // Create mutable copies of products and wishlist
        List<CartProduct> updatedProducts = new ArrayList<>(cart1.getProducts());
        updatedProducts.remove(product1);

        wishListProducts = new ArrayList<>(wishListProducts);
        wishListProducts.add(product1);

        // The cart after the operation
        Cart updatedCart = Cart.builder()
                .cartId(cartId)
                .customerId(cart1.getCustomerId())
                .products(updatedProducts)
                .wishListProducts(wishListProducts)
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(updatedCart));

        // Act
        Mono<CartResponseModel> result = cartService.moveProductFromCartToWishlist(cartId, productId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(cartResponse -> {
                    // Check that the product is removed from the products list
                    return cartResponse.getProducts().stream().noneMatch(p -> p.getProductId().equals(productId)) &&
                            cartResponse.getWishListProducts().stream().anyMatch(p -> p.getProductId().equals(productId));
                })
                .verifyComplete();

        // Verify cartRepository interactions
        verify(cartRepository, times(1)).save(any(Cart.class));

        // Ensure that cart1 is properly modified and its products list is updated
        assertTrue(updatedProducts.stream().noneMatch(p -> p.getProductId().equals(productId)));
    }


//    @Test
//    void whenMoveProductFromCartToWishlist_thenProductNotFoundInCart() {
//        // Arrange
//        String cartId = cart1.getCartId(); // Use a valid cart ID
//        String productId = "a7d573-bcab-5555-956f-773324b92a80"; // This should be a non-existent product ID
//        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
//
//        // Act
//        Mono<CartResponseModel> result = cartService.moveProductFromCartToWishlist(cartId, productId);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
//                        throwable.getMessage().equals("Product:" + productId + " not found in cart: " + cartId))
//                .verify();
//    }



    @Test
    void whenMoveProductFromWishListToCart_thenSuccess() {
        // Arrange
        String cartId = cart1.getCartId();
        String wishListProductId = wishListProduct1.getProductId();

        // Make wishlist and products mutable
        List<CartProduct> updatedWishListProducts = new ArrayList<>(cart1.getWishListProducts());
        List<CartProduct> updatedProducts = new ArrayList<>(cart1.getProducts());

        // Move product from wishlist to cart
        CartProduct productToMove = updatedWishListProducts.stream()
                .filter(product -> product.getProductId().equals(wishListProductId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found in wishlist"));

        updatedProducts.add(productToMove);
        updatedWishListProducts.remove(productToMove);

        // Updated cart after the move
        Cart updatedCart = Cart.builder()
                .cartId(cartId)
                .customerId(cart1.getCustomerId())
                .products(updatedProducts)
                .wishListProducts(updatedWishListProducts)
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(updatedCart));

        // Act
        Mono<CartResponseModel> result = cartService.moveProductFromWishListToCart(cartId, wishListProductId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(cartResponse -> cartResponse.getProducts().size() == updatedProducts.size())
                .verifyComplete();

        verify(cartRepository, times(1)).save(any(Cart.class));
        assertTrue(updatedWishListProducts.size() == 1); // Ensure wishlist is not empty
    }





    @Test
    void whenMoveProductFromWishListToCart_thenProductNotFoundInWishlist() {
        // Arrange
        String cartId = cart1.getCartId(); // Use the existing cart ID for the test
        String productId = "nonExistentProductId"; // Define a non-existent product ID
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));

        // Act
        Mono<CartResponseModel> result = cartService.moveProductFromWishListToCart(cartId, productId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Product: " + productId + " not found in wishlist of cart: " + cartId))
                .verify();
    }



}


