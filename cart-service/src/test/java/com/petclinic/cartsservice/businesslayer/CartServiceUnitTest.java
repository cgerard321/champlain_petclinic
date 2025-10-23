package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.domainclientlayer.CustomerResponseModel;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.petclinic.cartsservice.utils.exceptions.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private CustomerClient customerClient;

    @BeforeEach
    void init() {
        lenient().when(cartRepository.findAll()).thenReturn(Flux.empty());
        lenient().when(cartRepository.findCartByCartId(anyString())).thenReturn(Mono.empty());
        lenient().when(cartRepository.findCartByCustomerId(anyString())).thenReturn(Mono.empty());
        lenient().when(cartRepository.save(any(Cart.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        lenient().when(customerClient.getCustomerById(anyString()))
                .thenReturn(Mono.just(new CustomerResponseModel()));
    }

    private final CartProduct product1 = CartProduct.builder()
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .productName("Product1")
            .productDescription("Description1")
            .productSalePrice(100.0)
            .quantityInCart(1)
            .averageRating(4.5)
            .productQuantity(10) // Initialized productQuantity
            .build();

    private final CartProduct product2 = CartProduct.builder()
            .productId("d819e4f4-25af-4d33-91e9-2c45f0071606")
            .imageId("08a5af6b-3501-4157-9a99-1aa82387b9e4")
            .productName("Product2")
            .productDescription("Description2")
            .productSalePrice(200.0)
            .quantityInCart(1)
            .averageRating(4.0)
            .productQuantity(5) // Initialized productQuantity
            .build();

    private final CartProduct product3 = CartProduct.builder()
            .productId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")
            .imageId("36b06c01-10f3-4645-9c45-900afc5a8b8a")
            .productName("Product3")
            .productDescription("Description3")
            .productSalePrice(300.0)
            .quantityInCart(1)
            .averageRating(3.5)
            .productQuantity(15) // Initialized productQuantity
            .build();

    private final List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));

    CartProduct wishListProduct1 = CartProduct.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .imageId("be4e60a4-2369-46e8-abee-20c1a8dce3e5")
            .productName("Dog Food")
            .productDescription("Premium dry food for adult dogs")
            .productSalePrice(45.99)
            .quantityInCart(2)
            .averageRating(5.0)
            .productQuantity(8) // Initialized productQuantity
            .build();

    CartProduct wishlistProduct2 = CartProduct.builder()
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .imageId("7074e0ef-d041-452f-8a0f-cb9ab20d1fed")
            .productName("Cat Litter")
            .productDescription("Clumping cat litter with odor control")
            .productSalePrice(12.99)
            .quantityInCart(1)
            .averageRating(3.0)
            .productQuantity(5) // Initialized productQuantity
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
                .expectNextMatches(cartResponseModel -> {
                    // Assert cartId
                    boolean cartIdMatches = cartResponseModel.getCartId().equals(cart1.getCartId());

                    // Assert imageId for products in the cart
                    boolean product1ImageIdMatches = cartResponseModel.getProducts().get(0).getImageId() == null; // product1 has no imageId
                    boolean product2ImageIdMatches = cartResponseModel.getProducts().get(1).getImageId().equals(product2.getImageId());

                    // Assert imageId for wishlist products
                    boolean wishlistProduct1ImageIdMatches = cartResponseModel.getWishListProducts().get(0).getImageId().equals(wishListProduct1.getImageId());
                    boolean wishlistProduct2ImageIdMatches = cartResponseModel.getWishListProducts().get(1).getImageId().equals(wishlistProduct2.getImageId());

                    return cartIdMatches && product1ImageIdMatches && product2ImageIdMatches &&
                            wishlistProduct1ImageIdMatches && wishlistProduct2ImageIdMatches;
                })
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
                .expectNextMatches(cartResponseModel -> {
                    // Assert cartId
                    boolean cartIdMatches = cartResponseModel.getCartId().equals(cart1.getCartId());

                    // Assert imageId for products in the cart
                    boolean product1ImageIdMatches = cartResponseModel.getProducts().get(0).getImageId() == null; // product1 has no imageId
                    boolean product2ImageIdMatches = cartResponseModel.getProducts().get(1).getImageId().equals(product2.getImageId());

                    // Assert imageId for wishlist products
                    boolean wishlistProduct1ImageIdMatches = cartResponseModel.getWishListProducts().get(0).getImageId().equals(wishListProduct1.getImageId());
                    boolean wishlistProduct2ImageIdMatches = cartResponseModel.getWishListProducts().get(1).getImageId().equals(wishlistProduct2.getImageId());

                    return cartIdMatches && product1ImageIdMatches && product2ImageIdMatches &&
                            wishlistProduct1ImageIdMatches && wishlistProduct2ImageIdMatches;
                })
                .verifyComplete();
    }




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
                                product.getQuantityInCart() == (quantityToAdd + 1))) // already 1 in cart
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
    void findCartByCustomerId_withNonExistentId_thenReturnNewCart() {
        // Arrange
        Mockito.when(cartRepository.findCartByCustomerId(nonExistentCustomerId))
                .thenReturn(Mono.empty());
        Mockito.when(cartRepository.save(Mockito.any(Cart.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act & Assert
        StepVerifier.create(cartService.findCartByCustomerId(nonExistentCustomerId))
                .expectNextMatches(cartResponse ->
                        cartResponse.getCustomerId().equals(nonExistentCustomerId) &&
                                cartResponse.getProducts().isEmpty()
                )
                .verifyComplete();
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
                .wishListProducts(cart1.getWishListProducts()) // maintain existing wishlist
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

    @Test
    void whenMoveProductFromCartToWishlist_thenSuccess() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();

        // Create mutable copies of products and wishlist
        List<CartProduct> updatedProducts = new ArrayList<>(cart1.getProducts());
        updatedProducts.remove(product1);

        List<CartProduct> updatedWishListProducts = cart1.getWishListProducts() != null
                ? new ArrayList<>(cart1.getWishListProducts())
                : new ArrayList<>();
        updatedWishListProducts.add(product1);

        // The cart after the operation
        Cart updatedCart = Cart.builder()
                .cartId(cartId)
                .customerId(cart1.getCustomerId())
                .products(updatedProducts)
                .wishListProducts(updatedWishListProducts)
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



    @Test
    void whenMoveProductFromCartToWishlist_thenProductNotFoundInCart() {
        // Arrange
        String cartId = cart1.getCartId(); // Use a valid cart ID
        String productId = "a7d573-bcab-5555-956f-773324b92a80"; // This should be a non-existent product ID
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));

        // Act
        Mono<CartResponseModel> result = cartService.moveProductFromCartToWishlist(cartId, productId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Product not found in cart: " + productId))
                .verify();
    }

    @Test
    void whenMoveProductFromCartToWishlist_thenCartNotFound() {
        // Arrange
        String cartId = nonExistentCartId; // Use a non-existent cart ID
        String productId = product1.getProductId(); // Use a valid product ID
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        // Act
        Mono<CartResponseModel> result = cartService.moveProductFromCartToWishlist(cartId, productId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Cart not found: " + cartId))
                .verify();
    }



    @Test
    void whenMoveProductFromWishListToCart_thenSuccess() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();

        // Create mutable copies of products and wishlist
        List<CartProduct> updatedProducts = new ArrayList<>(cart1.getProducts());
        List<CartProduct> updatedWishListProducts = cart1.getWishListProducts() != null
                ? new ArrayList<>(cart1.getWishListProducts())
                : new ArrayList<>();

        // Move the product from wishlist to cart
        CartProduct productToMove = updatedWishListProducts.stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found in wishlist: " + productId));
        updatedWishListProducts.remove(productToMove);
        updatedProducts.add(productToMove);

        // The cart after the operation
        Cart updatedCart = Cart.builder()
                .cartId(cartId)
                .customerId(cart1.getCustomerId())
                .products(updatedProducts)
                .wishListProducts(updatedWishListProducts)
                .build();

        // Mocking repository behavior
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(updatedCart));

        // Act
        Mono<CartResponseModel> result = cartService.moveProductFromWishListToCart(cartId, productId);

        // Assert using StepVerifier
        StepVerifier.create(result)
                .expectNextMatches(cartResponse -> {
                    // Check that the product is removed from the wishlist and added to the products list
                    return cartResponse.getWishListProducts().stream().noneMatch(p -> p.getProductId().equals(productId)) &&
                            cartResponse.getProducts().stream().anyMatch(p -> p.getProductId().equals(productId));
                })
                .verifyComplete();

        // Verify cartRepository interactions
        verify(cartRepository, times(1)).save(any(Cart.class));

        // Ensure that cart1 is properly modified and its wishlist is updated
        assertTrue(updatedWishListProducts.stream().noneMatch(p -> p.getProductId().equals(productId)));
        assertTrue(updatedProducts.stream().anyMatch(p -> p.getProductId().equals(productId)));
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
                        throwable.getMessage().equals("Product not found in wishlist: " + productId))
                .verify();
    }

    @Test
    void whenMoveProductFromWishListToCart_thenCartNotFound() {
        // Arrange
        String cartId = nonExistentCartId; // Use a non-existent cart ID
        String productId = product1.getProductId(); // Use a valid product ID
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        // Act
        Mono<CartResponseModel> result = cartService.moveProductFromWishListToCart(cartId, productId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Cart not found: " + cartId))
                .verify();
    }

    @Test
    void whenAddProductToWishList_withProductNotAlreadyInWishList() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product3.getProductId();

        List<CartProduct> updatedProducts = new ArrayList<>(cart1.getProducts());
        List<CartProduct> updatedWishListProducts = new ArrayList<>(cart1.getWishListProducts());

        updatedWishListProducts.add(product3);

        Cart updatedCart = Cart.builder()
                .cartId(cartId)
                .customerId(cart1.getCustomerId())
                .products(updatedProducts)
                .wishListProducts(updatedWishListProducts)
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Product3")
                .productDescription("Desc3")
                .productSalePrice(300.0)
                .productQuantity(10)
                .build()));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(updatedCart));

        // Act
        Mono<CartResponseModel> result = cartService.addProductToWishList(cartId, productId, 1);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(cartResponse ->
                        cartResponse.getWishListProducts().stream().anyMatch(p -> p.getProductId().equals(productId) && p.getQuantityInCart() == 1)
                )
                .verifyComplete();

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void whenAddProductToWishList_withProductAlreadyInWishList() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product1.getProductId(); // Use a product already in the wishlist

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Product1")
                .productDescription("Desc1")
                .productSalePrice(100.0)
                .productQuantity(10)
                .build()));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart1));

        // Act
        Mono<CartResponseModel> result = cartService.addProductToWishList(cartId, productId, 1);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel -> cartResponseModel.getWishListProducts().stream()
                        .anyMatch(product -> product.getProductId().equals(productId) && product.getQuantityInCart() == 2))
                .verifyComplete();
    }

    @Test
    void whenAddProductToWishList_thenProductNotFound() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = nonExistentProductId; // Use a non-existent product ID

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(nonExistentProductId)).thenReturn(Mono.empty());

        // Act
        Mono<CartResponseModel> result = cartService.addProductToWishList(cartId, productId, 1);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Product not found: " + nonExistentProductId))
                .verify();
    }

    @Test
    void whenAddProductToWishList_thenCartNotFound() {
        // Arrange
        String cartId = nonExistentCartId; // Use a non-existent cart ID
        String productId = product3.getProductId(); // Use a valid product ID

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        // Act
        Mono<CartResponseModel> result = cartService.addProductToWishList(cartId, productId, 1);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Cart not found: " + nonExistentCartId))
                .verify();
    }

    @Test
    void whenAddProductToWishList_thenProductQuantityLessThanOrEqualToZero() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product1.getProductId(); // Use a valid product ID
        int quantity = 0; // Use an invalid quantity

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productQuantity(0)
                .build()));

        // Act
        Mono<CartResponseModel> result = cartService.addProductToWishList(cartId, productId, quantity);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof InvalidInputException &&
                        throwable.getMessage().equals("Quantity must be greater than zero"))
                .verify();
    }

    @Test
    void whenAddProductToWishList_thenNotEnoughStock() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product1.getProductId(); // Use a valid product ID
        int quantity = 11; // Use a quantity greater than the stock

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productQuantity(10) // Use a stock of 10
                .build()));

        // Act
        Mono<CartResponseModel> result = cartService.addProductToWishList(cartId, productId, quantity);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof OutOfStockException &&
                        throwable.getMessage().equals("Only 10 items left in stock. You added: " + quantity))
                .verify();
    }


    @Test
    void addProductToCart_ProductAlreadyInWishlist_DoesNotAddAgain() {
        // Arrange: Mock the cart that already has the product in the wishlist
        String cartId = cart1.getCartId();
        String productId = wishlistProduct2.getProductId();

        List<CartProduct> wishListWithProduct = List.of(wishlistProduct2);
        cart1.setWishListProducts(wishListWithProduct);

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Cat Litter")
                .productQuantity(0) // Out of stock
                .build()));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart1));  // Mock the save method to return the saved cart

        // Act: Attempt to add the product again
        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, 1);

        // Assert: Ensure it is not added again to the wishlist
        StepVerifier.create(result)
                .expectNextMatches(cartResponse -> cartResponse.getWishListProducts().size() == 1 &&
                        cartResponse.getWishListProducts().get(0).getProductId().equals(productId))
                .verifyComplete();

        // Verify repository interaction
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addProductToCart_ProductOutOfStock_WishlistInitiallyNull() {
        // Arrange: Create a cart with a null wishlist
        Cart cartWithNullWishlist = Cart.builder()
                .cartId(cart1.getCartId())
                .customerId(cart1.getCustomerId())
                .products(new ArrayList<>(cart1.getProducts()))
                .wishListProducts(null) // Wishlist is null initially
                .build();

        String productId = product3.getProductId();

        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cartWithNullWishlist));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("OutOfStockProduct")
                .productQuantity(0) // Out of stock
                .build()));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cartWithNullWishlist));  // Mock the save method to return the saved cart

        // Act: Add out-of-stock product
        Mono<CartResponseModel> result = cartService.addProductToCart(cart1.getCartId(), productId, 1);

        // Assert: Verify the product is added to the newly initialized wishlist
        StepVerifier.create(result)
                .expectNextMatches(cartResponse -> cartResponse.getWishListProducts() != null &&
                        cartResponse.getWishListProducts().stream()
                                .anyMatch(product -> product.getProductId().equals(productId)))
                .verifyComplete();

        // Verify repository interaction
        verify(cartRepository, times(1)).save(any(Cart.class));
    }


    @Test
    void addProductToCart_QuantityExceedsStock_ThrowsOutOfStockException() {
        // Arrange: Prepare cart and product with limited stock
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int quantityToAdd = 11; // Exceeding stock of 10

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Product1")
                .productQuantity(10) // Only 10 in stock
                .build()));

        // Act: Try to add more products than available
        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, quantityToAdd);

        // Assert: Expect OutOfStockException
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof OutOfStockException &&
                        throwable.getMessage().contains("Only 10 items left in stock"))
                .verify();
    }

    @Test
    void addProductToCart_ExistingProductQuantityExceedsStock_ThrowsOutOfStockException() {
        // Arrange: Product1 is already in the cart with a quantity of 1
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();
        int quantityToAdd = 10; // Trying to add 10 more, exceeding the stock of 10

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1)); // Cart already has product1 with quantity 1
        when(productClient.getProductByProductId(productId)).thenReturn(Mono.just(ProductResponseModel.builder()
                .productId(productId)
                .productName("Product1")
                .productSalePrice(100.0)
                .productQuantity(10) // Only 10 in stock
                .build()));

        // Act: Try to add more products than the total stock in the cart
        Mono<CartResponseModel> result = cartService.addProductToCart(cartId, productId, quantityToAdd);

        // Assert: Expect OutOfStockException to be thrown
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof OutOfStockException &&
                        throwable.getMessage().equals("You cannot add more than 10 items. Only 10 items left in stock."))
                .verify();

        // Verify repository interactions
        verify(cartRepository, times(1)).findCartByCartId(cartId);
        verifyNoMoreInteractions(cartRepository); // Ensure no save operation happens
    }

        @Test
        void deleteAllItemsInCart_CartNotFound_ThrowsNotFoundException() {
        // Arrange: Mock the repository to return empty when the cart is not found
        String cartId = nonExistentCartId;

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty()); // Cart not found

        // Act: Attempt to clear a non-existent cart
                Mono<Void> result = cartService.deleteAllItemsInCart(cartId);

        // Assert: Verify that a NotFoundException is thrown
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Cart not found: " + cartId))
                .verify();

        // Verify repository interactions
        verify(cartRepository, times(1)).findCartByCartId(cartId);
        verify(cartRepository, never()).save(any(Cart.class)); // Ensure save is never called
    }


    @Test
        void deleteAllItemsInCart_AlreadyEmptyCart_CompletesSuccessfully() {
        // Arrange: Create a cart that has no products
        Cart emptyCart = Cart.builder()
                .cartId("emptyCartId")
                .customerId("customerWithEmptyCart")
                .products(Collections.emptyList()) // No products
                .build();

        when(cartRepository.findCartByCartId(emptyCart.getCartId())).thenReturn(Mono.just(emptyCart)); // Empty cart found
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(emptyCart)); // Mock saving the empty cart

        // Act: Attempt to clear the already empty cart
                Mono<Void> result = cartService.deleteAllItemsInCart(emptyCart.getCartId());

        // Assert: No products should be returned, and the cart remains empty
        StepVerifier.create(result)
                .verifyComplete();

        // Verify repository interactions
        verify(cartRepository, times(1)).findCartByCartId(emptyCart.getCartId());
        verify(cartRepository, times(1)).save(any(Cart.class)); // Ensure the cleared cart was saved
    }

    @Test
    void addProductToCart_WhenCartDoesNotExist_ShouldReturnNotFoundException() {
        String cartId = "non-existent-cart-id";
        String productId = "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        cartService.addProductToCartFromProducts(cartId, productId)
                .as(StepVerifier::create)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("Cart not found: " + cartId))
                .verify();
    }



    // positive path
    @Test
    void addProductToCartFromProducts_addNewProduct() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product3.getProductId();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(
                Mono.just(ProductResponseModel.builder()
                        .productId(productId)
                        .productName("Product3")
                        .productQuantity(7)
                        .productSalePrice(10.0)
                        .build())
        );
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.addProductToCartFromProducts(cartId, productId))
                .expectNextMatches(res ->
                        res.getProducts().stream()
                                .anyMatch(p -> p.getProductId().equals(productId) && p.getQuantityInCart() == 1)
                )
                .verifyComplete();
    }

    // positive path
    @Test
    void addProductToCartFromProducts_existingProduct_plusOne() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = product1.getProductId();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId(productId)).thenReturn(
                Mono.just(ProductResponseModel.builder()
                        .productId(productId)
                        .productQuantity(10)
                        .productSalePrice(5.0)
                        .build())
        );
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // Act + Assert
        StepVerifier.create(cartService.addProductToCartFromProducts(cartId, productId))
                .expectNextMatches(res ->
                        res.getProducts().stream()
                                .anyMatch(p -> p.getProductId().equals(productId) && p.getQuantityInCart() == 2)
                )
                .verifyComplete();
    }

    // positive path
    @Test
    void addProductToCartFromProducts_outOfStock_goesToWishlist() {
        // Arrange
        String cartId = cart1.getCartId();
        String productId = "out-stock-1";

        Cart emptyLists = Cart.builder()
                .cartId(cartId)
                .customerId(cart1.getCustomerId())
                .products(new ArrayList<>(cart1.getProducts()))
                .wishListProducts(new ArrayList<>())
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(emptyLists));
        when(productClient.getProductByProductId(productId)).thenReturn(
                Mono.just(ProductResponseModel.builder()
                        .productId(productId)
                        .productName("Broken Bone")
                        .productQuantity(0)
                        .productSalePrice(10.0)
                        .build())
        );
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // Act + Assert
        StepVerifier.create(cartService.addProductToCartFromProducts(cartId, productId))
                .expectNextMatches(res ->
                        res.getMessage() != null &&
                                res.getWishListProducts() != null &&
                                res.getWishListProducts().stream().anyMatch(p -> p.getProductId().equals(productId))
                )
                .verifyComplete();
    }

    // negative path
    @Test
    void addProductToCartFromProducts_exceedsStock() {
        // Arrange
        String cartId = "cart-xx";
        String productId = "p-1";

        CartProduct already = CartProduct.builder()
                .productId(productId).quantityInCart(1).productSalePrice(3.0).build();

        Cart cart = Cart.builder()
                .cartId(cartId)
                .products(new ArrayList<>(List.of(already)))
                .wishListProducts(new ArrayList<>())
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));
        when(productClient.getProductByProductId(productId)).thenReturn(
                Mono.just(ProductResponseModel.builder()
                        .productId(productId)
                        .productQuantity(1)
                        .productSalePrice(3.0)
                        .build())
        );

        StepVerifier.create(cartService.addProductToCartFromProducts(cartId, productId))
                .expectError(OutOfStockException.class)
                .verify();

        verify(cartRepository, never()).save(any());
    }

    // positive path
    @Test
    void removeProductFromWishlist_Success() {
        String cartId = cart1.getCartId();
        String productId = cart1.getWishListProducts().get(0).getProductId();

        Cart mutable = Cart.builder()
                .cartId(cart1.getCartId())
                .customerId(cart1.getCustomerId())
                .products(new ArrayList<>(cart1.getProducts()))
                .wishListProducts(new ArrayList<>(cart1.getWishListProducts()))
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(mutable));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.removeProductFromWishlist(cartId, productId))
                .expectNextMatches(res ->
                        res.getWishListProducts().stream().noneMatch(p -> p.getProductId().equals(productId))
                )
                .verifyComplete();
    }

    // negative path
    @Test
    void removeProductFromWishlist_emptyList_notFound() {
        String cartId = cart1.getCartId();
        Cart cartNoWish = Cart.builder()
                .cartId(cartId)
                .customerId(cart1.getCustomerId())
                .products(new ArrayList<>(cart1.getProducts()))
                .wishListProducts(new ArrayList<>())
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cartNoWish));

        StepVerifier.create(cartService.removeProductFromWishlist(cartId, "any-id"))
                .expectError(NotFoundException.class)
                .verify();
    }

    // positive path
    @Test
    void assignCartToCustomer_newCart_addsProduct() {
        String customerId = "cust-A";
        CartProduct toAdd = CartProduct.builder()
                .productId("PX")
                .quantityInCart(2)
                .productSalePrice(1.0)
                .build();

        when(cartRepository.findCartByCustomerId(customerId)).thenReturn(Mono.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));


        StepVerifier.create(cartService.assignCartToCustomer(customerId, List.of(toAdd)))
                .expectNextMatches(res ->
                        res.getCustomerId().equals(customerId) &&
                                res.getCartId() != null &&
                                res.getProducts().stream().anyMatch(p -> p.getProductId().equals("PX") && p.getQuantityInCart() == 2)
                )
                .verifyComplete();
    }

    // positive path
    @Test
    void assignCartToCustomer_existingCart_increment() {
        String customerId = "cust-B";

        CartProduct existing = CartProduct.builder()
                .productId("PY").quantityInCart(1).productSalePrice(2.0).build();

        Cart existingCart = Cart.builder()
                .cartId("cart-1")
                .customerId(customerId)
                .products(new ArrayList<>(List.of(existing)))
                .build();

        CartProduct incoming = CartProduct.builder()
                .productId("PY").quantityInCart(3).productSalePrice(2.0).build();

        when(cartRepository.findCartByCustomerId(customerId)).thenReturn(Mono.just(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.assignCartToCustomer(customerId, List.of(incoming)))
                .expectNextMatches(res ->
                        res.getProducts().stream().anyMatch(p -> p.getProductId().equals("PY") && p.getQuantityInCart() == 4)
                )
                .verifyComplete();
    }

    // negative
    @Test
    void checkoutCart_notFound_throwsNotFound() {
        String cartId = "missing";
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.checkoutCart(cartId))
                .expectError(NotFoundException.class)
                .verify();

        verify(cartRepository, never()).save(any());
    }

    // negative
    @Test
    void checkoutCart_emptyCart_throwsInvalidInput() {
        String cartId = "cart-empty";

        Cart empty = Cart.builder().cartId(cartId).products(new ArrayList<>()).build();
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(empty));

        StepVerifier.create(cartService.checkoutCart(cartId))
                .expectError(InvalidInputException.class)
                .verify();

        verify(cartRepository, never()).save(any());
    }

        // negative
    @Test
    void deleteCartByCartId_notFound_throwsNotFound() {

        String cartId = "nope";
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.deleteCartByCartId(cartId))
                .expectError(NotFoundException.class)
                .verify();

        verify(cartRepository, never()).delete(any());
    }
    @Test
    void testGetRecentPurchases_ReturnsList() {
        CartRepository cartRepository = Mockito.mock(CartRepository.class);
        CartServiceImpl service = new CartServiceImpl(cartRepository, null, customerClient);

        String cartId = "cart123";
        List<CartProduct> recentPurchases = List.of(
                CartProduct.builder().productId("prod1").build(),
                CartProduct.builder().productId("prod2").build()
        );
        Cart cart = Cart.builder().cartId(cartId).recentPurchases(recentPurchases).build();

        Mockito.when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));

        StepVerifier.create(service.getRecentPurchases(cartId))
                .expectNextMatches(list -> list.size() == 2 && "prod1".equals(list.get(0).getProductId()))
                .verifyComplete();
    }

    @Test
    void testGetRecentPurchases_ReturnsEmptyListIfNull() {
        CartRepository cartRepository = Mockito.mock(CartRepository.class);
        CartServiceImpl service = new CartServiceImpl(cartRepository, null, customerClient);

        String cartId = "cart456";
        Cart cart = Cart.builder().cartId(cartId).recentPurchases(null).build();

        Mockito.when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));

        StepVerifier.create(service.getRecentPurchases(cartId))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }
    @Test
    void testMoveAllWishlistToCart_CartNotFound() {
        String cartId = "missing";
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.moveAllWishlistToCart(cartId))
                .expectErrorMatches(e -> e instanceof NotFoundException &&
                        e.getMessage().contains("Cart not found"))
                .verify();
    }
    @Test
    void testMoveAllWishlistToCart_EmptyWishlist() {
        String cartId = "cart-1";
        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setWishListProducts(Collections.emptyList());
        cart.setProducts(new ArrayList<>());

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.moveAllWishlistToCart(cartId))
                .assertNext(resp -> {
                    assertEquals("No items in wishlist to move.", resp.getMessage());
                    assertTrue(resp.getProducts().isEmpty());
                })
                .verifyComplete();
    }
    @Test
    void testMoveAllWishlistToCart_ItemsMoved() {
        String cartId = "cart-2";
        CartProduct wishItem = CartProduct.builder()
                .productId("prod-1")
                .productName("Test Product")
                .quantityInCart(2)
                .productSalePrice(10.0) // <-- Add this line!
                .build();
        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setWishListProducts(List.of(wishItem));
        cart.setProducts(new ArrayList<>());

        Cart savedCart = new Cart();
        savedCart.setCartId(cartId);
        savedCart.setWishListProducts(new ArrayList<>());
        savedCart.setProducts(List.of(wishItem));

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any())).thenReturn(Mono.just(savedCart));

        StepVerifier.create(cartService.moveAllWishlistToCart(cartId))
                .assertNext(resp -> {
                    assertEquals("Moved 2 item(s) to cart.", resp.getMessage());
                    assertEquals(1, resp.getProducts().size());
                    assertEquals("prod-1", resp.getProducts().get(0).getProductId());
                    assertEquals(2, resp.getProducts().get(0).getQuantityInCart());
                })
                .verifyComplete();
    }
    @Test
    void testMoveAllWishlistToCart_MergeQuantities() {
        String cartId = "cart-3";
        CartProduct wishItem = CartProduct.builder()
                .productId("prod-1")
                .productName("Test Product")
                .quantityInCart(2)
                .productSalePrice(10.0) // <-- Add this line!
                .build();
        CartProduct cartItem = CartProduct.builder()
                .productId("prod-1")
                .productName("Test Product")
                .quantityInCart(3)
                .productSalePrice(10.0) // <-- Add this line!
                .build();
        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setWishListProducts(List.of(wishItem));
        cart.setProducts(List.of(cartItem));

        Cart savedCart = new Cart();
        savedCart.setCartId(cartId);
        savedCart.setWishListProducts(new ArrayList<>());
        CartProduct merged = CartProduct.builder()
                .productId("prod-1")
                .productName("Test Product")
                .quantityInCart(5) // 3 + 2
                .productSalePrice(10.0) // <-- Add this line!
                .build();
        savedCart.setProducts(List.of(merged));

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any())).thenReturn(Mono.just(savedCart));

        StepVerifier.create(cartService.moveAllWishlistToCart(cartId))
                .assertNext(resp -> {
                    assertEquals("Moved 2 item(s) to cart.", resp.getMessage());
                    assertEquals(1, resp.getProducts().size());
                    assertEquals(5, resp.getProducts().get(0).getQuantityInCart());
                })
                .verifyComplete();
    }
    @Test
    void checkoutCart_CartNotFound_ThrowsNotFoundException() {
        String cartId = "missing";
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.checkoutCart(cartId))
                .expectErrorMatches(e -> e instanceof NotFoundException &&
                        e.getMessage().equals("Cart not found: " + cartId))
                .verify();
    }
    @Test
    void checkoutCart_EmptyCart_ThrowsInvalidInputException() {
        String cartId = "emptyCart";
        Cart emptyCart = Cart.builder()
                .cartId(cartId)
                .products(Collections.emptyList())
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(emptyCart));

        StepVerifier.create(cartService.checkoutCart(cartId))
                .expectErrorMatches(e -> e instanceof InvalidInputException &&
                        e.getMessage().equals("Cart is empty"))
                .verify();
    }
    @Test
    void testCheckoutCart_AllBranches() {
        String cartId = "test-cart-id";
        Cart cart = Cart.builder()
                .cartId(cartId)
                .products(Arrays.asList(product1, product2))
                .recentPurchases(Arrays.asList(product1))
                .build();

        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.checkoutCart(cartId))
                .assertNext(response -> {
                    assertEquals(cartId, response.getCartId());
                    assertNotNull(response.getInvoiceId());
                    assertEquals(2, response.getProducts().size());
                    assertTrue(response.getTotal() > 0);
                })
                .verifyComplete();

        when(cartRepository.findCartByCartId("notfound")).thenReturn(Mono.empty());
        StepVerifier.create(cartService.checkoutCart("notfound"))
                .expectErrorMatches(e -> e instanceof NotFoundException)
                .verify();

        Cart emptyCart = Cart.builder().cartId(cartId).products(Collections.emptyList()).build();
        when(cartRepository.findCartByCartId("empty")).thenReturn(Mono.just(emptyCart));
        StepVerifier.create(cartService.checkoutCart("empty"))
                .expectErrorMatches(e -> e instanceof InvalidInputException)
                .verify();
    }

    @Test
    void testGetRecommendationPurchases_ReturnsRecommendations() {
        CartRepository cartRepository = Mockito.mock(CartRepository.class);
        ProductClient productClient = Mockito.mock(ProductClient.class);
        CartServiceImpl service = new CartServiceImpl(cartRepository, productClient, customerClient);

        String cartId = "test-cart-id";
        List<CartProduct> recommendations = List.of(CartProduct.builder().productId("prod1").build());
        Cart cart = Mockito.mock(Cart.class);

        Mockito.when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));
        Mockito.when(cart.getRecommendationPurchase()).thenReturn(recommendations);

        Mono<List<CartProduct>> result = service.getRecommendationPurchases(cartId);

        StepVerifier.create(result)
                .expectNextMatches(list -> list.size() == 1 && "prod1".equals(list.get(0).getProductId()))
                .verifyComplete();
    }

    @Test
    void testGetRecommendationPurchases_ReturnsEmptyList() {
        CartRepository cartRepository = Mockito.mock(CartRepository.class);
        ProductClient productClient = Mockito.mock(ProductClient.class);
        CartServiceImpl service = new CartServiceImpl(cartRepository, productClient, customerClient);

        String cartId = "empty-cart-id";
        Cart cart = Mockito.mock(Cart.class);

        Mockito.when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart));
        Mockito.when(cart.getRecommendationPurchase()).thenReturn(null);

        Mono<List<CartProduct>> result = service.getRecommendationPurchases(cartId);

        StepVerifier.create(result)
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }
}
