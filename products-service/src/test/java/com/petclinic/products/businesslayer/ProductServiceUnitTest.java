package com.petclinic.products.businesslayer;

import com.petclinic.products.businesslayer.products.ProductServiceImpl;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.utils.exceptions.NotFoundException;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RatingRepository ratingRepository;


    Product product1 = Product.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .productName("Dog Food")
            .productDescription("Premium dry food for adult dogs")
            .productSalePrice(45.99)
            .averageRating(0.0)
            .productType("Food")
            .build();

    Product product2 = Product.builder()
            .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .productName("Cat Litter")
            .productDescription("Clumping cat litter with odor control")
            .productSalePrice(12.99)
            .averageRating(0.0)
            .productType("Accessory")
            .build();


    Rating rating1 = Rating.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .rating((byte) 4)
            .build();

    Rating rating2 = Rating.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .rating((byte) 5)
            .build();

    private Product createProduct(String productId, Double salePrice, Double averageRating) {
        return Product.builder()
                .productId(productId)
                .productSalePrice(salePrice)
                .averageRating(averageRating)
                .build();
    }

    @Test
    void whenGetAllProductsWithinRatingRange_thenReturnProductsWithinRatingRange() {
        // Given
        Double minRating = 3.0;
        Double maxRating = 4.5;
        Double minPrice = null;
        Double maxPrice = null;
        String sort = null;

        Product product1 = createProduct("1", 50.0, 4.0);
        Product product2 = createProduct("2", 60.0, 3.0);
        Product product3 = createProduct("3", 70.0, 5.0);

        when(productRepository.findAll()).thenReturn(Flux.just(product1, product2, product3));

        when(ratingRepository.findRatingsByProductId(anyString())).thenReturn(Flux.empty());


        // When
        Flux<ProductResponseModel> result = productService.getAllProducts(minPrice, maxPrice, minRating, maxRating, sort);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product -> product.getAverageRating() == 4.0)
                .expectNextMatches(product -> product.getAverageRating() == 3.0)
                .expectComplete()
                .verify();

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void whenGetAllProductsWithInvalidSortParameter_thenThrowInvalidInputException() {

        Double minPrice = null;
        Double maxPrice = null;
        Double minRating = null;
        Double maxRating = null;
        String invalidSort = "invalidSort";

        // When & Then
        try {
            productService.getAllProducts(minPrice, maxPrice, minRating, maxRating, invalidSort);
        } catch (InvalidInputException e) {
            assertNotNull(e);
            assertEquals("Invalid sort parameter: " + invalidSort, e.getMessage());
        }

        //This is to make sure the repository is not called when getting an invalid sort input
        verifyNoInteractions(productRepository);
    }


    @Test
    public void whenGetAllProducts_thenReturnAllProductsWithAverageRatings() {

        when(productRepository.findAll())
                .thenReturn(Flux.just(product1, product2));
        when(ratingRepository.findRatingsByProductId(product1.getProductId()))
                .thenReturn(Flux.just(rating1));
        when(ratingRepository.findRatingsByProductId(product2.getProductId()))
                .thenReturn(Flux.just(rating2));


        Flux<ProductResponseModel> result = productService.getAllProducts(null,null,null,null,null);


        StepVerifier.create(result)
                .expectNextMatches(product ->
                        product.getProductId().equals(product1.getProductId()) &&
                        product.getAverageRating() == 4)
                .expectNextMatches(product ->
                        product.getProductId().equals(product2.getProductId()) &&
                        product.getAverageRating() == 5)
                .verifyComplete();

    }
    @Test
    public void whenNoProductsFound_thenReturnEmptyFlux() {

        when(productRepository.findAll())
                .thenReturn(Flux.empty());

        Flux<ProductResponseModel> result = productService.getAllProducts(null,null,null,null,null);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    public void whenGetProductsByType_thenReturnFilteredProducts() {
        when(productRepository.findProductsByProductType("Food"))
                .thenReturn(Flux.just(product1));

        Flux<ProductResponseModel> result = productService.getProductsByType("Food");

        StepVerifier.create(result)
                .expectNextMatches(product ->
                        product.getProductId().equals(product1.getProductId()) &&
                                product.getProductType().equals("Food"))
                .verifyComplete();
    }

    @Test
    public void whenNoProductsOfTypeFound_thenReturnEmptyFlux() {
        when(productRepository.findProductsByProductType("Toys"))
                .thenReturn(Flux.empty());

        Flux<ProductResponseModel> result = productService.getProductsByType("Toys");

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void whenGetProductWithNonExistentProductId_thenThrowException() {
        when(productRepository.findProductByProductId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb77"))
                .thenReturn(Mono.empty());

        Mono<ProductResponseModel> product = productService.getProductByProductId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb77");

        StepVerifier
                .create(product)
                .expectNextCount(0)
                .expectError(NotFoundException.class)
                .verify();
    }

}
