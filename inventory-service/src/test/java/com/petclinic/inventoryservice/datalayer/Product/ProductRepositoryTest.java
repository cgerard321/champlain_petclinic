package com.petclinic.inventoryservice.datalayer.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class ProductRepositoryTest {
    @Autowired
    ProductRepository productRepository;

    Product product1;
    Product product2;

    @BeforeEach
    public void setupDB() {
        product1 = buildProduct("inventoryId_4", "productId_1", "Desc", "name", 100.00, 10, 15.99);
        product2 = buildProduct("inventoryId_4", "productId_2", "Desc", "name", 100.00, 10, 15.99);

        Publisher<Product> setup1 = productRepository.deleteAll()
                .then(productRepository.save(product1));

        Publisher<Product> setup2 = productRepository.save(product2);

        StepVerifier
                .create(setup1)
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier
                .create(setup2)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void ShouldSaveSingleProduct(){
        //arrange
        Product newProduct = buildProduct("inventoryId_1", "sku_1", "product_1", "product_1", 10.0, 10, 15.99);
        Publisher<Product> setup = productRepository.save(newProduct);
        //Act and Assert
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void ShouldDeleteSingleProduct_byProductId(){
        //arrange
        Product product1 = buildProduct("inventoryId_1", "sku_1", "product_1", "product_1", 10.0, 10, 15.99);
        Publisher<Void> setup = productRepository.deleteByProductId(product1.getProductId());
        //act and assert
        StepVerifier
                .create(setup)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void shouldGetTwoProductsByInventoryIdAndProductDescriptionAndProductPriceAndProductQuantityAndProductSalePrice(){
        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductNameAndProductPriceAndProductQuantityAndProductSalePrice(
                        product1.getProductDescription(),
                        product1.getInventoryId(),
                        product1.getProductPrice(),
                        product1.getProductQuantity(),
                        product1.getProductSalePrice()
                ))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void shouldGetTwoProductsByInventoryIdAndProductPriceAndProductQuantity(){
        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductPriceAndProductQuantity(
                        product1.getInventoryId(),
                        product1.getProductPrice(),
                        product1.getProductQuantity()
                ))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void shouldGetTwoProductsByInventoryIdAndProductPrice(){
        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductPrice(
                        product1.getInventoryId(),
                        product1.getProductPrice()
                ))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void shouldGetTwoProductsByInventoryIdAndProductQuantity(){
        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductQuantity(
                        product1.getInventoryId(),
                        product1.getProductQuantity()
                ))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void shouldGetTwoProductsByInventoryIdAndProductName(){
        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductName(
                        product1.getInventoryId(),
                        product1.getProductName()
                ))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void shouldGetTwoProductsByInventoryIdAndProductSalePrice(){
        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductSalePrice(
                        product1.getInventoryId(),
                        product1.getProductSalePrice()
                ))
                .expectNextCount(2)
                .verifyComplete();
    }



    @Test
    public void shouldGetSingleProductByProductIdAndInventoryId(){
        StepVerifier
                .create(productRepository.findProductByInventoryIdAndProductId(
                        product1.getInventoryId(),
                        product1.getProductId()
                ))
                .expectNextCount(1)
                .verifyComplete();
    }



    @Test
    public void ShouldDeleteAllProducts() {
        // Arrange
        Product product1 = buildProduct("inventoryId_1", "sku_1", "product_1", "product_1_desc", 10.0, 10, 15.99);
        Product product2 = buildProduct("inventoryId_2", "sku_2", "product_2", "product_2_desc", 15.0, 5, 15.99);

        productRepository.save(product1).block();
        productRepository.save(product2).block();

        // Act
        Publisher<Void> deleteAllOperation = productRepository.deleteAll();

        // Assert
        StepVerifier
                .create(deleteAllOperation)
                .verifyComplete();

        // Check that there are no products in the repository anymore
        StepVerifier
                .create(productRepository.findAll())
                .expectNextCount(0);

    }

    @Test
    public void testFindProductByProductId() {
        // Arrange
        String productIdToFind = "productId";
        Product newProduct = buildProduct("inventoryId_1", productIdToFind, "product_1", "product_1", 10.0, 10, 15.99);

        productRepository.save(newProduct).block();

        // Act
        Mono<Product> productMono = productRepository.findProductByProductId(productIdToFind);

        // Assert
        StepVerifier.create(productMono)
                .expectNextMatches(product -> {
                    assertNotNull(product);
                    assertEquals(productIdToFind, product.getProductId());
                    return true;
                })
                .verifyComplete();
    }
    @Test
    public void shouldFindProductByInventoryIdAndOneCharNameRegex() {

        String regexPattern = "(?i)^n.*";
        Product product = buildProduct("inventoryId_1", "productId_1", "name", "Desc", 100.00, 10, 15.99);
        productRepository.save(product).block();

        // Act & Assert
        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductNameRegex("inventoryId_1", regexPattern))
                .expectNextMatches(result -> result.getProductId().equals("productId_1"))
                .verifyComplete();
    }
    @Test
    public void shouldFindProductByInventoryIdAndFullNameRegex() {

        String regexPattern = "(?i)^name.*";
        Product product = buildProduct("inventoryId_2", "productId_2", "name", "Desc", 100.00, 10, 15.99);
        productRepository.save(product).block();


        StepVerifier
                .create(productRepository.findAllProductsByInventoryIdAndProductNameRegex("inventoryId_2", regexPattern))
                .expectNextMatches(result -> result.getProductId().equals("productId_2"))
                .verifyComplete();
    }
    @Test
    public void shouldFindProductByInventoryId() {
        // Arrange
        Product product = buildProduct("inventoryId_3", "productId_3", "AnotherName", "Desc", 100.00, 10, 15.99);
        productRepository.save(product).block();

        // Act & Assert
        StepVerifier
                .create(productRepository.findAllProductsByInventoryId("inventoryId_3"))
                .expectNextMatches(result -> result.getProductId().equals("productId_3"))
                .verifyComplete();
    }
    @Test
    public void shouldNotFindProductByInvalidInventoryId() {
        // No products saved for this test

        // Act & Assert
        StepVerifier
                .create(productRepository.findAllProductsByInventoryId("non_existent_id"))
                .expectNextCount(0)
                .verifyComplete();
    }


    private Product buildProduct(String inventoryId, String productId, String productName, String productDescription, Double productPrice, Integer productQuantity, Double productSalePrice) {
        return Product.builder()
                .inventoryId(inventoryId)
                .productId(productId)
                .productName(productName)
                .productDescription(productDescription)
                .productPrice(productPrice)
                .productQuantity(productQuantity)
                .productSalePrice(productSalePrice)
                .build();
    }


}