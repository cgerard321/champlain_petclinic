package com.petclinic.products.businesslayer;


import com.petclinic.products.businesslayer.products.ProductServiceImpl;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testResetRequestCounts() {
        // Arrange: Create multiple products with non-zero request counts
        Product product1 = Product.builder()
                .productId("06a7d573-bcab-4db3-956f-773324b92a80")
                .productName("Dog Food")
                .productDescription("Premium dry food for adult dogs")
                .productSalePrice(45.99)
                .requestCount(10)
                .build();

        Product product2 = Product.builder()
                .productId("7f2bff03-b304-4b42-9a1d-415e5e6f8ef6")
                .productName("Cat Toy")
                .productDescription("Interactive toy for cats")
                .productSalePrice(15.99)
                .requestCount(5)
                .build();

        // Mocking the behavior of the repository
        when(productRepository.findAll()).thenReturn(Flux.just(product1, product2));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return Mono.just(p);
        });

        // Act: Call the method to reset request counts
        Mono<Void> result = productService.resetRequestCounts();

        // Assert: Verify that the Mono completes successfully
        StepVerifier.create(result)
                .verifyComplete();

        // Verify that save was called for each product
        verify(productRepository, times(2)).save(any(Product.class));

        // Verify that the requestCount is now reset to 0 for both products
        verify(productRepository).save(argThat(product ->
                product.getProductId().equals("06a7d573-bcab-4db3-956f-773324b92a80") &&
                        product.getRequestCount() == 0
        ));
        verify(productRepository).save(argThat(product ->
                product.getProductId().equals("7f2bff03-b304-4b42-9a1d-415e5e6f8ef6") &&
                        product.getRequestCount() == 0
        ));
    }

    @Test
    void testRequestCount_ProductFound() {
        // Arrange
        String productId = "testProductId";
        Product product = Product.builder()
                .productId(productId)
                .requestCount(5)
                .build();

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Void> result = productService.requestCount(productId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepository).findProductByProductId(productId);
        verify(productRepository).save(argThat(savedProduct ->
                savedProduct.getProductId().equals(productId) &&
                        savedProduct.getRequestCount() == 6
        ));
    }

    @Test
    void testRequestCount_ProductNotFound() {
        // Arrange
        String productId = "nonExistentProductId";

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = productService.requestCount(productId);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assert error instanceof NotFoundException;
                    assert error.getMessage().equals("Product id was not found: " + productId);
                })
                .verify();

        verify(productRepository).findProductByProductId(productId);
        verify(productRepository, never()).save(any(Product.class));
    }
}
