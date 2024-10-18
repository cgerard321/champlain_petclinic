package com.petclinic.products.businesslayer;


import com.petclinic.products.businesslayer.products.ProductServiceImpl;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @Test
    void shouldReturnProductsWhenProductTypeIsValid() {
        // Arrange
        Product foodProduct1 = new Product();
        foodProduct1.setProductId("1");
        foodProduct1.setProductName("Dog Food");
        foodProduct1.setProductType(ProductType.FOOD);

        Product foodProduct2 = new Product();
        foodProduct2.setProductId("2");
        foodProduct2.setProductName("Cat Food");
        foodProduct2.setProductType(ProductType.FOOD);

        List<Product> foodProducts = Arrays.asList(foodProduct1, foodProduct2);

        when(productRepository.findByProductType(ProductType.FOOD)).thenReturn(foodProducts);

        // Act
        List<Product> result = productService.getProductsByType(ProductType.FOOD);

        // Assert
        assertEquals(2, result.size());
        assertEquals(ProductType.FOOD, result.get(0).getProductType());
        verify(productRepository, times(1)).findByProductType(ProductType.FOOD);
    }


    @Test
    void shouldReturnEmptyListWhenProductTypeIsInvalid() {
        // Arrange
        when(productRepository.findByProductType(ProductType.ACCESSORY)).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.getProductsByType(ProductType.ACCESSORY);

        // Assert
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByProductType(ProductType.ACCESSORY);
    }


    @Test
    public void testDecreaseProductCount() {
        String productId = "testProductId";
        Product product = new Product();
        product.setProductId(productId);
        product.setRequestCount(10);
        product.setProductQuantity(20);

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));

        StepVerifier.create(productService.DecreaseProductCount(productId))
                .verifyComplete();

        verify(productRepository).findProductByProductId(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void testDecreaseProductCount_NotFound() {
        String productId = "nonExistentProductId";

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.DecreaseProductCount(productId))
                .expectError(NotFoundException.class)
                .verify();

        verify(productRepository).findProductByProductId(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testChangeProductQuantity_Increase() {
        String productId = "06a7d573-bcab-4db3-956f-773324b92a80";
        Integer newQuantity = 15;
        Product product = new Product();
        product.setProductId(productId);
        product.setProductQuantity(10);

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));

        StepVerifier.create(productService.changeProductQuantity(productId, newQuantity))
                .verifyComplete();

        verify(productRepository).findProductByProductId(productId);
        verify(productRepository).save(argThat(savedProduct ->
                savedProduct.getProductQuantity() == 15 // 10 + 15
        ));
    }

    @Test
    public void testChangeProductQuantity_Decrease() {
        String productId = "06a7d573-bcab-4db3-956f-773324b92a80";
        Integer newQuantity = 5;
        Product product = new Product();
        product.setProductId(productId);
        product.setProductQuantity(10);

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(product));

        StepVerifier.create(productService.changeProductQuantity(productId, newQuantity))
                .verifyComplete();

        verify(productRepository).findProductByProductId(productId);
        verify(productRepository).save(argThat(savedProduct ->
                savedProduct.getProductQuantity() == 5 // 10 - 5
        ));
    }

    @Test
    public void testChangeProductQuantity_NotFound() {
        String productId = "06a7d573-bcab-4db3-956f-773324b92a81";
        Integer quantityChange = 5;

        when(productRepository.findProductByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(productService.changeProductQuantity(productId, quantityChange))
                .expectError(NotFoundException.class)
                .verify();

        verify(productRepository).findProductByProductId(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void patchProductStatus_UpdatesStatusesCorrectly() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        List<Product> products = Arrays.asList(
                createProduct("1", yesterday, ProductStatus.PRE_ORDER),
                createProduct("2", today, ProductStatus.PRE_ORDER),
                createProduct("3", tomorrow, ProductStatus.AVAILABLE),
                createProduct("4", null, ProductStatus.PRE_ORDER)
        );

        when(productRepository.findAll()).thenReturn(Flux.fromIterable(products));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<Void> result = productService.patchProductStatus();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepository, times(4)).save(any(Product.class));
        verify(productRepository).save(argThat(product ->
                product.getProductId().equals("1") && product.getProductStatus() == ProductStatus.AVAILABLE
        ));
        verify(productRepository).save(argThat(product ->
                product.getProductId().equals("2") && product.getProductStatus() == ProductStatus.AVAILABLE
        ));
        verify(productRepository).save(argThat(product ->
                product.getProductId().equals("3") && product.getProductStatus() == ProductStatus.PRE_ORDER
        ));
        verify(productRepository).save(argThat(product ->
                product.getProductId().equals("4") && product.getProductStatus() == ProductStatus.AVAILABLE
        ));
    }

    @Test
    void patchProductStatus_NoProducts_CompletesSuccessfully() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Flux.empty());

        // Act
        Mono<Void> result = productService.patchProductStatus();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void patchProductStatus_RepositoryError_PropagatesError() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Flux.error(new RuntimeException("Database error")));

        // Act
        Mono<Void> result = productService.patchProductStatus();

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(productRepository, never()).save(any(Product.class));
    }

    private Product createProduct(String id, LocalDate releaseDate, ProductStatus currentStatus) {
        Product product = new Product();
        product.setProductId(id);
        product.setReleaseDate(releaseDate);
        product.setProductStatus(currentStatus);
        return product;
    }



}

