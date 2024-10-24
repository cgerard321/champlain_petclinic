package com.petclinic.products.datalayer.products;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class ProductRepositoryIntegrationTest {
    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setupDB(){
        StepVerifier.create(productRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }
    //Helper method to create a product
    private Product createProduct(String productName, String productDescription, Double productSalePrice, Double averageRating) {
        return Product.builder()
                .productId(UUID.randomUUID().toString())
                .productName(productName)
                .productDescription(productDescription)
                .productSalePrice(productSalePrice)
                .averageRating(averageRating)
                .build();
    }

    //Helper method to save a product
    private void saveProducts(Product... products) {
        StepVerifier.create(productRepository.saveAll(Flux.just(products)))
                .expectNextCount(products.length)
                .verifyComplete();
    }

    @Test
    void whenGetAllProducts_thenReturnAllProducts() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        Product product1 = Product.builder()
                .productId(id1)
                .productName("Testing Product 1")
                .productDescription("This is a testing product 1")
                .productSalePrice(10.00)
                .averageRating(5.00)
                .build();
        Product product2 = Product.builder()
                .productId(id2)
                .productName("Testing Product 2")
                .productDescription("This is a testing product 2")
                .productSalePrice(20.00)
                .averageRating(6.00)
                .build();
        Product product3 = Product.builder()
                .productId(id3)
                .productName("Testing Product 3")
                .productDescription("Testing product 3")
                .productSalePrice(30.00)
                .averageRating(7.00)
                .build();
        StepVerifier.create(productRepository.saveAll(Flux.just(product1,product2,product3)))
                .expectNextCount(3)
                .verifyComplete();
        StepVerifier.create(productRepository.findAll())
                .expectNextMatches(product -> product.getProductId().equals(id1) || product.getProductId().equals(id2) || product.getProductId().equals(id3))
                .expectNextMatches(product -> product.getProductId().equals(id1) || product.getProductId().equals(id2) || product.getProductId().equals(id3))
                .expectNextMatches(product -> product.getProductId().equals(id1) || product.getProductId().equals(id2) || product.getProductId().equals(id3))
                .verifyComplete();



    }
    @Test
    void whenNoProductsExist_thenReturnEmpty() {

        StepVerifier.create(productRepository.deleteAll())
                .verifyComplete();


        StepVerifier.create(productRepository.findAll())
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    void whenFoundProduct_thenReturnProduct(){
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(id)
                .productName("Sample Product")
                .productDescription("Sample Description")
                .productSalePrice(20.00)
                .averageRating(5.00)
                .build();

        StepVerifier.create(productRepository.save(product))
                .consumeNextWith(insEnrollement -> {
                    assertNotNull(insEnrollement);
                    assertEquals(product.getProductId(), insEnrollement.getProductId());
                })
                .verifyComplete();
        StepVerifier.create(productRepository.findProductByProductId(id))
                .consumeNextWith(foundProduct -> {
                    assertNotNull(foundProduct);
                    assertEquals(product.getProductId(), foundProduct.getProductId());
                })
                .verifyComplete();
    }

    @Test
    void whenProductNotFound_thenEmptyMono(){
        String productId = UUID.randomUUID().toString();
        StepVerifier
                .create(productRepository.findProductByProductId(productId))
                .consumeNextWith(isFound -> {
                    assertNull(isFound);
                });
    }

    @Test
    void whenProductCreated_thenReturnProduct(){
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(id)
                .productName("Sample Product")
                .productDescription("Sample Description")
                .productSalePrice(20.00)
                .averageRating(5.00)
                .build();

        StepVerifier.create(productRepository.save(product))
                .consumeNextWith(insEnrollement -> {
                    assertNotNull(insEnrollement);
                    assertEquals(product.getProductId(), insEnrollement.getProductId());
                })
                .verifyComplete();
    }

    @Test
    public void whenProductUpdated_thenReturnUpdatedProduct(){
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(id)
                .productName("Sample Product")
                .productDescription("Sample Description")
                .productSalePrice(20.00)
                .averageRating(5.00)
                .build();

        Product updatedProduct = Product.builder()
                .productId(id)
                .productName("Changed Product")
                .productDescription("Changed Description")
                .productSalePrice(50.00)
                .averageRating(2.00)
                .build();

        StepVerifier.create(productRepository.save(product))
                .consumeNextWith(insEnrollement -> {
                    assertNotNull(insEnrollement);
                    assertEquals(product.getProductId(), insEnrollement.getProductId());
                })
                .verifyComplete();
        StepVerifier.create(productRepository.findProductByProductId(id))
                .consumeNextWith(foundProduct -> {
                    assertNotNull(foundProduct);
                    assertEquals(product.getProductId(), foundProduct.getProductId());
                    updatedProduct.setId(foundProduct.getId());
                })
                .verifyComplete();
        StepVerifier.create(productRepository.save(updatedProduct))
                .consumeNextWith(newProduct -> {
                    assertNotNull(newProduct);
                    assertEquals(newProduct.getId(), updatedProduct.getId());
                    assertEquals(newProduct.getProductId(), updatedProduct.getProductId());
                    assertEquals(newProduct.getProductName(), updatedProduct.getProductName());
                    assertEquals(newProduct.getProductDescription(), updatedProduct.getProductDescription());
                    assertEquals(newProduct.getProductSalePrice(), updatedProduct.getProductSalePrice());
                    // Skip rating, as is fetched by the Rating service, not saved on the Product database
                })
                .verifyComplete();
        StepVerifier.create(productRepository.findProductByProductId(id))
                .consumeNextWith(foundProduct -> {
                    assertNotNull(foundProduct);
                    assertEquals(foundProduct.getId(), updatedProduct.getId());
                    assertEquals(foundProduct.getProductId(), updatedProduct.getProductId());
                    assertEquals(foundProduct.getProductName(), updatedProduct.getProductName());
                    assertEquals(foundProduct.getProductDescription(), updatedProduct.getProductDescription());
                    assertEquals(foundProduct.getProductSalePrice(), updatedProduct.getProductSalePrice());
                })
                .verifyComplete();
    }
    @Test
    public void testUpdateDeliveryType() {
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(id)
                .productName("Test Product")
                .deliveryType(DeliveryType.NO_DELIVERY_OPTION)
                .build();

        productRepository.save(product).block();

        product.setDeliveryType(DeliveryType.PICKUP);
        productRepository.save(product).block();


        Product foundProduct = productRepository.findProductByProductId(id).block();
        assertNotNull(foundProduct);
        assertEquals(DeliveryType.PICKUP, foundProduct.getDeliveryType());
    }


    @Test
    void whenDeliveryTypeNotFound_thenEmptyMono() {
        // Arrange
        String productId = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(productId)
                .productName("Test Product")
                .deliveryType(null)
                .build();

        productRepository.save(product).block();

        StepVerifier
                .create(productRepository.findProductByProductId(productId))
                .consumeNextWith(foundProduct -> {
                    assertNotNull(foundProduct);
                    assertNull(foundProduct.getDeliveryType());
                })
                .verifyComplete();
    }


    @Test
    public void whenDeliveryTypeUpdated_thenReturnUpdatedDeliveryType() {
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(id)
                .productName("Changing Product")
                .productDescription("Sample Description")
                .productSalePrice(20.00)
                .averageRating(5.00)
                .deliveryType(DeliveryType.DELIVERY)
                .build();


        Product savedProduct = productRepository.save(product).block();
        assertNotNull(savedProduct);
        assertEquals(DeliveryType.DELIVERY, savedProduct.getDeliveryType());


        savedProduct.setDeliveryType(DeliveryType.DELIVERY_AND_PICKUP);
        Product updatedProduct = productRepository.save(savedProduct).block();
        assertNotNull(updatedProduct);
        assertEquals(DeliveryType.DELIVERY_AND_PICKUP, updatedProduct.getDeliveryType());


        Product foundProduct = productRepository.findProductByProductId(id).block();
        assertNotNull(foundProduct);
        assertEquals(DeliveryType.DELIVERY_AND_PICKUP, foundProduct.getDeliveryType());
    }


    @Test
    void whenProductDeleted_thenMonoVoid(){
        String id = UUID.randomUUID().toString();
        Product product = Product.builder()
                .productId(id)
                .productName("Sample Product")
                .productDescription("Sample Description")
                .productSalePrice(20.00)
                .averageRating(5.00)
                .build();

        StepVerifier.create(productRepository.save(product))
                .consumeNextWith(insEnrollement -> {
                    assertNotNull(insEnrollement);
                    assertEquals(product.getProductId(), insEnrollement.getProductId());
                })
                .verifyComplete();

        StepVerifier.create(productRepository.delete(product))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenFindByProductSalePriceBetween_thenReturnProductsInRange() {
        // Given
        Product product1 = createProduct("Product 1", "Description 1", 10.00, 4.0);
        Product product2 = createProduct("Product 2", "Description 2", 20.00, 4.5);
        Product product3 = createProduct("Product 3", "Description 3", 30.00, 5.0);

        saveProducts(product1, product2, product3);

        // When
        Double minPrice = 15.00;
        Double maxPrice = 25.00;

        // Then
        StepVerifier.create(productRepository.findByProductSalePriceBetween(minPrice, maxPrice))
                .expectNextMatches(product -> product.getProductId().equals(product2.getProductId()))
                .verifyComplete();
    }

    @Test
    void whenNoProductsMatchCriteria_thenReturnEmptyFlux() {
        // Given
        Product product1 = createProduct("Product 1", "Description 1", 10.00, 4.0);
        Product product2 = createProduct("Product 2", "Description 2", 20.00, 4.5);

        saveProducts(product1, product2);

        // When
        Double minPrice = 50.00;
        Double maxPrice = 100.00;

        // Then
        StepVerifier.create(productRepository.findByProductSalePriceBetween(minPrice, maxPrice))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void whenMinPriceGreaterThanMaxPrice_thenReturnEmptyFlux() {
        // Given
        Product product1 = createProduct("Product 1", "Description 1", 10.00, 4.0);
        Product product2 = createProduct("Product 2", "Description 2", 20.00, 4.5);

        saveProducts(product1, product2);

        // When
        Double minPrice = 30.00;
        Double maxPrice = 10.00;

        // Then
        StepVerifier.create(productRepository.findByProductSalePriceBetween(minPrice, maxPrice))
                .expectNextCount(0)
                .verifyComplete();
    }



}