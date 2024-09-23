package com.petclinic.products.businesslayer;

import com.petclinic.products.businesslayer.products.ProductServiceImpl;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

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
            .build();

    Product product2 = Product.builder()
            .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .productName("Cat Litter")
            .productDescription("Clumping cat litter with odor control")
            .productSalePrice(12.99)
            .averageRating(0.0)
            .build();


    Rating rating1 = Rating.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .rating((byte) 4)
            .build();

    Rating rating2 = Rating.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .rating((byte) 5)
            .build();


    @Test
    public void whenGetAllProducts_thenReturnAllProductsWithAverageRatings() {

        when(productRepository.findAll())
                .thenReturn(Flux.just(product1, product2));
        when(ratingRepository.findRatingsByProductId(product1.getProductId()))
                .thenReturn(Flux.just(rating1));
        when(ratingRepository.findRatingsByProductId(product2.getProductId()))
                .thenReturn(Flux.just(rating2));


        Flux<ProductResponseModel> result = productService.getAllProducts(null,null);


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

        Flux<ProductResponseModel> result = productService.getAllProducts(null,null);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

}
