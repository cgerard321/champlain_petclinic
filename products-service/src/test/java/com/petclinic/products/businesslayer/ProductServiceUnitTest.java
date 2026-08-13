package com.petclinic.products.businesslayer;

import com.petclinic.products.businesslayer.products.ProductServiceImpl;
import com.petclinic.products.datalayer.products.DeliveryType;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import com.petclinic.products.presentationlayer.products.ProductEnumsResponseModel;
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
import java.util.List;

import static com.petclinic.products.datalayer.products.DeliveryType.DELIVERY;
import static com.petclinic.products.datalayer.products.DeliveryType.PICKUP;
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
            .productType(ProductType.FOOD)
            .build();

    Product product2 = Product.builder()
            .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .productName("Cat Litter")
            .productDescription("Clumping cat litter with odor control")
            .productSalePrice(12.99)
            .averageRating(0.0)
            .productType(ProductType.ACCESSORY)
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
        String deliveryType=null;
        String productType = null;

        Product product1 = createProduct("1", 50.0,3.5);
        Product product2 = createProduct("2", 60.0,3.7);
        Product product3 = createProduct("3", 70.0,5.0);

        when(productRepository.findAll()).thenReturn(Flux.just(product1, product2, product3));

        // Mock ratings for each product
        when(ratingRepository.findRatingsByProductId("1")).thenReturn(
                Flux.just(
                        Rating.builder().productId("1").rating((byte) 4).build(),
                        Rating.builder().productId("1").rating((byte) 4).build()
                )
        );
        when(ratingRepository.findRatingsByProductId("2")).thenReturn(
                Flux.just(
                        Rating.builder().productId("2").rating((byte) 3).build()
                )
        );
        when(ratingRepository.findRatingsByProductId("3")).thenReturn(
                Flux.just(
                        Rating.builder().productId("3").rating((byte) 5).build(),
                        Rating.builder().productId("3").rating((byte) 5).build()
                )
        );

        // When
        Flux<ProductResponseModel> result = productService.getAllProducts(minPrice, maxPrice, minRating, maxRating, sort, deliveryType, productType);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(product -> product.getProductId().equals("1") && product.getAverageRating() == 4.0)
                .expectNextMatches(product -> product.getProductId().equals("2") && product.getAverageRating() == 3.0)
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
        String deliveryType=null;
        String productType = null;


        // When & Then
        try {
            productService.getAllProducts(minPrice, maxPrice, minRating, maxRating, invalidSort,deliveryType, productType);
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


        Flux<ProductResponseModel> result = productService.getAllProducts(null,null,null,null,null,null, null);


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
    public void whenGetAllProductsFilteredByDeliveryType_thenReturnFilteredProducts() {
        DeliveryType deliveryType = DeliveryType.DELIVERY;

        product1.setDeliveryType(DeliveryType.DELIVERY);
        product2.setDeliveryType(DeliveryType.PICKUP);

        when(productRepository.findAll()).thenReturn(Flux.just(product1, product2));
        when(ratingRepository.findRatingsByProductId(product1.getProductId())).thenReturn(Flux.just(rating1));
        when(ratingRepository.findRatingsByProductId(product2.getProductId())).thenReturn(Flux.just(rating2));

        Flux<ProductResponseModel> result = productService.getAllProducts(null, null, null, null, null, deliveryType.toString(), null);


        StepVerifier.create(result)
                .expectNextMatches(product -> product.getProductId().equals(product1.getProductId()) &&
                        product.getDeliveryType() == DeliveryType.DELIVERY)
                .verifyComplete();

        verify(productRepository, times(1)).findAll();
    }


    @Test
    public void whenGetAllProductsWithEmptyStringDeliveryType_thenReturnAllProducts() {

        String deliveryType = "";
        Product product1 = createProduct("1", 50.0, 4.0);
        product1.setDeliveryType(DeliveryType.DELIVERY);
        Product product2 = createProduct("2", 60.0, 3.5);
        product2.setDeliveryType(DeliveryType.PICKUP);

        when(productRepository.findAll()).thenReturn(Flux.just(product1, product2));
        when(ratingRepository.findRatingsByProductId(product1.getProductId())).thenReturn(Flux.just(rating1));
        when(ratingRepository.findRatingsByProductId(product2.getProductId())).thenReturn(Flux.just(rating2));

        Flux<ProductResponseModel> result = productService.getAllProducts(null, null, null, null, null, deliveryType,null);

        StepVerifier.create(result)
                .expectNextMatches(product -> product.getProductId().equals("1") && product.getDeliveryType().equals(DeliveryType.DELIVERY))
                .expectNextMatches(product -> product.getProductId().equals("2") && product.getDeliveryType().equals(DeliveryType.PICKUP))
                .verifyComplete();

        verify(productRepository, times(1)).findAll();
    }



    @Test
    public void whenNoProductsFound_thenReturnEmptyFlux() {

        when(productRepository.findAll())
                .thenReturn(Flux.empty());

        Flux<ProductResponseModel> result = productService.getAllProducts(null,null,null,null,null,null, null);

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
                                product.getProductType().equals(ProductType.FOOD))
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
    @Test
    public void whenGetProductsEnums_thenReturnProductEnums() {
        ProductEnumsResponseModel enumsResponseDTO = new ProductEnumsResponseModel(
                List.of(ProductType.FOOD, ProductType.MEDICATION, ProductType.ACCESSORY, ProductType.EQUIPMENT),
                List.of(ProductStatus.AVAILABLE, ProductStatus.PRE_ORDER, ProductStatus.OUT_OF_STOCK),
                List.of(DeliveryType.DELIVERY, DeliveryType.PICKUP, DeliveryType.DELIVERY_AND_PICKUP, DeliveryType.NO_DELIVERY_OPTION)
        );

        Mono<ProductEnumsResponseModel> result = productService.getProductsEnumValues();

        StepVerifier.create(result)
                .expectNextMatches(enums ->
                        enums.getProductStatus().equals(List.of(ProductStatus.AVAILABLE, ProductStatus.PRE_ORDER, ProductStatus.OUT_OF_STOCK)) &&
                                enums.getProductType().equals(List.of(ProductType.FOOD, ProductType.MEDICATION, ProductType.ACCESSORY, ProductType.EQUIPMENT)) &&
                                enums.getDeliveryType().equals(List.of(DeliveryType.DELIVERY, DeliveryType.PICKUP, DeliveryType.DELIVERY_AND_PICKUP, DeliveryType.NO_DELIVERY_OPTION))
                )
                .verifyComplete();
    }
}
