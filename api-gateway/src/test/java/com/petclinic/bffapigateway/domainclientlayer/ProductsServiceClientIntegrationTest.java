package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Products.*;
import com.petclinic.bffapigateway.dtos.Products.DeliveryType;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ProductsServiceClientIntegrationTest {

    @MockBean
    private ProductsServiceClient productsServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void initialize() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        productsServiceClient = new ProductsServiceClient(webClientBuilder, "localhost",
                String.valueOf(mockWebServer.getPort()));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    @Test
    void getAllProducts_ThenReturnProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("data:{\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":0.0}\n\n" +
                        "data:{\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":0.0}\n\n"
                ).setHeader("Content-Type", "text/event-stream")
        );


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null,null,null,null,null,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") && product.getProductName().equals("Rabbit Hutch"))
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") && product.getProductName().equals("Flea Collar"))
                .verifyComplete();
    }
    @Test
    void getAllProducts_WithRatingFiltering_ThenReturnFilteredProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("data:{\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":4.5}\n\n" +
                        "data:{\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":3.0}\n\n" +
                        "data:{\"productId\":\"1234567\",\"productName\":\"Cheap Collar\",\"productDescription\":\"Cheap flea and tick prevention\",\"productSalePrice\":4.99,\"averageRating\":1.0}\n\n"
                ).setHeader("Content-Type", "text/event-stream")
        );

        Double minRating = 3.0;
        Double maxRating = 5.0;


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null, minRating, maxRating, null,null,null);

        // Verify the results
        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") &&
                        product.getAverageRating() >= minRating && product.getAverageRating() <= maxRating)
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") &&
                        product.getAverageRating() >= minRating && product.getAverageRating() <= maxRating)
                .expectComplete()
                .verify();
    }


    @Test
    void getAllProducts_ThenReturnEmptyResponse() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .setHeader("Content-Type", "text/event-stream")
        );

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null,null,null,null,null,null,null);

        StepVerifier.create(productsFlux)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getAllProducts_WithPriceFiltering_ThenReturnFilteredProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("data:{\"productId\":\"4affcab7-3ab1-4917-a114-2b6301aa5565\",\"productName\":\"Rabbit Hutch\",\"productDescription\":\"Outdoor wooden hutch for rabbits\",\"productSalePrice\":79.99,\"averageRating\":0.0}\n\n" +
                        "data:{\"productId\":\"baee7cd2-b67a-449f-b262-91f45dde8a6d\",\"productName\":\"Flea Collar\",\"productDescription\":\"Flea and tick prevention for small dogs\",\"productSalePrice\":9.99,\"averageRating\":0.0}\n\n"
                ).setHeader("Content-Type", "text/event-stream")
        );

        // Define the minimum and maximum price for filtering
        Double minPrice = 5.00;
        Double maxPrice = 80.00;

        // Call the method with price filters
        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(minPrice, maxPrice,null,null,null,null, null);


            // Verify the results using StepVerifier
        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("4affcab7-3ab1-4917-a114-2b6301aa5565") && product.getProductSalePrice() >= minPrice && product.getProductSalePrice() <= maxPrice)
                .expectNextMatches(product -> product.getProductId().equals("baee7cd2-b67a-449f-b262-91f45dde8a6d") && product.getProductSalePrice() >= minPrice && product.getProductSalePrice() <= maxPrice)
                .verifyComplete();
    }
    @Test
    void getAllProducts_WithDeliveryTypeFiltering_ThenReturnFilteredProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody(
                        "data:{\"productId\":\"1\",\"productName\":\"Product A\",\"productDescription\":\"Description A\",\"productSalePrice\":29.99,\"averageRating\":4.0,\"deliveryType\":\"DELIVERY\"}\n\n" +
                                "data:{\"productId\":\"2\",\"productName\":\"Product B\",\"productDescription\":\"Description B\",\"productSalePrice\":49.99,\"averageRating\":4.5,\"deliveryType\":\"DELIVERY\"}\n\n"
                )
                .setHeader("Content-Type", "text/event-stream")
        );

        String deliveryType = "DELIVERY";


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null, null, null, null, deliveryType,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("1") && product.getDeliveryType() == DeliveryType.DELIVERY)
                .expectNextMatches(product -> product.getProductId().equals("2") && product.getDeliveryType() == DeliveryType.DELIVERY)
                .verifyComplete();
    }


    @Test
    void getAllProducts_WithProductTypeFiltering_ThenReturnFilteredProductList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody(
                        "data:{\"productId\":\"1\",\"productName\":\"Product A\",\"productDescription\":\"Description A\",\"productSalePrice\":29.99,\"averageRating\":4.0,\"deliveryType\":\"DELIVERY\",\"productType\":\"EQUIPMENT\"}\n\n" +
                                "data:{\"productId\":\"2\",\"productName\":\"Product B\",\"productDescription\":\"Description B\",\"productSalePrice\":49.99,\"averageRating\":4.5,\"deliveryType\":\"PICKUP\",\"productType\":\"EQUIPMENT\"}\n\n"
                )
                .setHeader("Content-Type", "text/event-stream")
        );

        String productType = "EQUIPMENT";


        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null, null, null, null, null, productType);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("1") && product.getProductType() == ProductType.EQUIPMENT)
                .expectNextMatches(product -> product.getProductId().equals("2") && product.getProductType() == ProductType.EQUIPMENT)
                .verifyComplete();
    }


    @Test
    void getAllProducts_WithSortAscending_ThenReturnProductsInAscendingOrder() {

        mockWebServer.enqueue(new MockResponse()
                .setBody(
                        "data:{\"productId\":\"1\",\"productName\":\"Alpha\",\"productDescription\":\"Description A\",\"productSalePrice\":19.99,\"averageRating\":4.0}\n\n" +
                                "data:{\"productId\":\"2\",\"productName\":\"Bravo\",\"productDescription\":\"Description B\",\"productSalePrice\":29.99,\"averageRating\":3.5}\n\n" +
                                "data:{\"productId\":\"3\",\"productName\":\"Charlie\",\"productDescription\":\"Description C\",\"productSalePrice\":39.99,\"averageRating\":4.5}\n\n"
                )
                .setHeader("Content-Type", "text/event-stream")
        );
        String sort = "asc";

        Flux<ProductResponseDTO> productsFlux = productsServiceClient.getAllProducts(null, null, null, null, sort, null,null);

        StepVerifier.create(productsFlux)
                .expectNextMatches(product -> product.getProductId().equals("1") && product.getProductName().equals("Alpha"))
                .expectNextMatches(product -> product.getProductId().equals("2") && product.getProductName().equals("Bravo"))
                .expectNextMatches(product -> product.getProductId().equals("3") && product.getProductName().equals("Charlie"))
                .verifyComplete();
    }



    @Test
    void whenAddProduct_thenReturnProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "imageId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0,
                6,
                false,
                ProductType.FOOD,
                ProductStatus.AVAILABLE,
                DeliveryType.DELIVERY
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .createProduct(new ProductRequestDTO());

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(product -> product.getProductId().equals("productId"))
                .verifyComplete();
    }

    @Test
    void whenUpdateProduct_thenReturnUpdatedProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "imageId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0,
                6,
                false,
                ProductType.FOOD,
                ProductStatus.AVAILABLE,
                DeliveryType.PICKUP
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .updateProduct(productResponseDTO.getProductId(), new ProductRequestDTO());

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(product -> product.getProductId().equals("productId"))
                .verifyComplete();
    }

    @Test
    void whenPatchListingStatus_thenReturnUpdatedProduct() throws JsonProcessingException {
        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
                "productId",
                "imageId",
                "Product 1",
                "desc",
                10.00,
                0.00,
                0,
                6,
                true,
                ProductType.FOOD,
                ProductStatus.AVAILABLE,
                DeliveryType.DELIVERY_AND_PICKUP
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(productResponseDTO))
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus(productResponseDTO.getProductId(), new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectNextMatches(product -> product.getProductId().equals("productId"))
                .verifyComplete();
    }

    @Test
    void whenPatchListingStatusWithNonExistingProductId_thenThrowNotFoundException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Product not found for ProductId: 691e6945-0d4a-4b20-85cc-afd251faccfd")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("691e6945-0d4a-4b20-85cc-afd251faccfd", new ProductRequestDTO(
                        null, null, null, null, null,
                        null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Product not found for ProductId: 691e6945-0d4a-4b20-85cc-afd251faccfd"))
                .verify();
    }

    @Test
    void whenPatchListingStatusWithInvalidProductId_thenThrowInvalidInputException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(422)
                .setBody("Invalid input for ProductId: invalid-product-id")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("invalid-product-id", new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Invalid input for ProductId: invalid-product-id"))
                .verify();
    }

    @Test
    void whenPatchListingStatus_andCauseServerFailure_thenThrowIllegalArgumentException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Invalid input for ProductId: productId")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("productId", new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Something went wrong with the server"))
                .verify();
    }

    @Test
    void whenPatchListingStatus_andCauseClientFailure_thenThrowIllegalArgumentException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Invalid input for ProductId: productId")
                .addHeader("Content-Type", "application/json"));

        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
                .patchListingStatus("productId", new ProductRequestDTO(
                        null, null, null, null, null, null, false, null, null, null,null));

        StepVerifier.create(productResponseDTOMono)
                .expectErrorMatches(throwable -> throwable != null &&
                        throwable.getMessage().equals("Client error"))
                .verify();
    }

//    @Test
//    void whenDeleteProduct_thenDeleteProduct() throws JsonProcessingException {
//        ProductResponseDTO productResponseDTO = new ProductResponseDTO(
//                "productId",
//                "imageId",
//                "Product 1",
//                "desc",
//                10.00,
//                0.00,
//                0,
//                6,
//                false,
//                ProductType.FOOD,
//                ProductStatus.AVAILABLE,
//                DeliveryType.DELIVERY_AND_PICKUP
//        );
//
//        mockWebServer.enqueue(new MockResponse()
//                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .setBody(objectMapper.writeValueAsString(productResponseDTO))
//                .addHeader("Content-Type", "application/json"));
//
//        Mono<ProductResponseDTO> productResponseDTOMono = productsServiceClient
//                .deleteProduct("productId");
//
//        StepVerifier.create(productResponseDTOMono)
//                .expectNextMatches(product -> product.getProductId().equals("productId"))
//                .verifyComplete();
//    }



    @Test
    void whenRequestCount_thenSucceed() {
        String productId = "abc123";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        Mono<Void> resultMono = productsServiceClient.requestCount(productId);

        StepVerifier.create(resultMono)
                .verifyComplete();
    }




    //----------------------------------------------
//TODO: Quantity
    @Test
    void whenDecreaseProductQuantity_thenSucceed() {
        String productId = "abc123";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<Void> resultMono = productsServiceClient.decreaseProductQuantity(productId);

        StepVerifier.create(resultMono)
                .verifyComplete();
    }





    @Test
    void whenChangeProductQuantity_thenSucceed(){
        String productId = "abc123";
        Integer newQuantity = 10;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<Void> resultMono = productsServiceClient.changeProductQuantity(productId, newQuantity);

        StepVerifier.create(resultMono)
                .verifyComplete();
    }



    //--------------------------------------
    //TODO: Bundles

    @Test
    void getAllProductBundles_ThenReturnBundleList(){
        mockWebServer.enqueue(new MockResponse()
                .setBody(
                        "data:{\"bundleId\":\"1\"," +
                                "\"bundleName\":\"Dog Bundle\"," +
                                "\"bundleDescription\":\"Dog Food and Flea Collar\"," +
                                "\"productIds\":[\"p1\",\"p2\",\"p3\"]," +
                                "\"originalTotalPrice\":55.98," +
                                "\"bundlePrice\":49.99}\n\n" +

                                "data:{\"bundleId\":\"2\"," +
                                "\"bundleName\":\"Accessory Bundle\"," +
                                "\"bundleDescription\":\"All Accessories\"," +
                                "\"productIds\":[\"p4\",\"p5\"]," +
                                "\"originalTotalPrice\":157.95," +
                                "\"bundlePrice\":129.99}\n\n"
                )
                .setHeader("Content-Type", "text/event-stream")
        );

        Flux<ProductBundleResponseDTO> bundlesFlux = productsServiceClient.getAllProductBundles();

        StepVerifier.create(bundlesFlux)
                .expectNextMatches(bundle ->
                        bundle.getBundleId().equals("1") &&
                                bundle.getBundleName().equals("Dog Bundle") &&
                                bundle.getProductIds().size() == 3 &&
                                bundle.getBundlePrice().equals(49.99))
                .expectNextMatches(bundle ->
                        bundle.getBundleId().equals("2") &&
                                bundle.getBundleName().equals("Accessory Bundle") &&
                                bundle.getProductIds().size() == 2 &&
                                bundle.getBundlePrice().equals(129.99))
                .verifyComplete();
    }



    @Test
    void getProductBundleById_ThenReturnBundle() throws JsonProcessingException{
        ProductBundleResponseDTO responseDTO = new ProductBundleResponseDTO(
                "1",
                "Dog Bundle",
                "Dog Food and Flea Collar",
                Arrays.asList("p1", "p2", "p3"),
                55.98,
                49.99
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(responseDTO))
                .setHeader("Content-Type", "application/json")
        );

        String bundleId = "1";

        Mono<ProductBundleResponseDTO> bundleMono = productsServiceClient.getProductBundleById(bundleId);

        StepVerifier.create(bundleMono)
                .expectNextMatches(bundle ->
                        bundle.getBundleId().equals("1") &&
                                bundle.getBundleName().equals("Dog Bundle") &&
                                bundle.getBundleDescription().equals("Dog Food and Flea Collar") &&
                                bundle.getProductIds().size() == 3 &&
                                bundle.getBundlePrice().equals(49.99))
                .verifyComplete();
    }


    @Test
    void whenCreateProductBundle_thenReturnBundle() throws JsonProcessingException {
        ProductBundleResponseDTO responseDTO = new ProductBundleResponseDTO(
                "1",
                "Dog Bundle",
                "Dog Food and Flea Collar",
                Arrays.asList("p1", "p2", "p3"),
                55.98,
                49.99
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(responseDTO))
                .addHeader("Content-Type", "application/json"));

        ProductBundleRequestDTO requestDTO = ProductBundleRequestDTO.builder()
                .bundleName("Dog Bundle")
                .bundleDescription("Dog Food and Flea Collar")
                .productIds(Arrays.asList("p1", "p2", "p3"))
                .bundlePrice(49.99)
                .build();

        Mono<ProductBundleResponseDTO> resultMono = productsServiceClient.createProductBundle(requestDTO);

        StepVerifier.create(resultMono)
                .expectNextMatches(bundle ->
                        "1".equals(bundle.getBundleId()) &&
                                "Dog Bundle".equals(bundle.getBundleName()) &&
                                bundle.getProductIds() != null &&
                                bundle.getProductIds().size() == 3 &&
                                Double.valueOf(49.99).equals(bundle.getBundlePrice()))
                .verifyComplete();
    }


    @Test
    void whenUpdateProductBundle_thenReturnUpdatedBundle() throws JsonProcessingException {
        ProductBundleResponseDTO responseDTO = new ProductBundleResponseDTO(
                "1",
                " Updated Dog Bundle",
                "Dog Food and Flea Collar",
                List.of("p1", "p2", "p3"),
                60.00,
                54.99
        );

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(responseDTO))
                .addHeader("Content-Type", "application/json"));

        ProductBundleRequestDTO requestDTO = ProductBundleRequestDTO.builder()
                .bundleName("Updated Dog Bundle")
                .bundleDescription("Dog Food and Flea Collar")
                .productIds(List.of("p1", "p2", "p3"))
                .bundlePrice(54.99)
                .build();

        Mono<ProductBundleResponseDTO> resultMono = productsServiceClient
                .updateProductBundle(responseDTO.getBundleId(), requestDTO);

        StepVerifier.create(resultMono)
                .expectNextMatches(bundle ->
                        bundle.getBundleId().equals("1"))
                .verifyComplete();
    }



    @Test
    void whenDeleteProductBundle_thenCompleteSuccessfully() throws JsonProcessingException {
        String bundleId = "1";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NO_CONTENT.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<Void> resultMono = productsServiceClient.deleteProductBundle(bundleId);

        StepVerifier.create(resultMono)
                .verifyComplete();
    }

    @Test
    void whenGetProductEnums_ThenReturnEnumsValues() throws JsonProcessingException{
        ProductEnumsResponseDTO responseDTO = new ProductEnumsResponseDTO(
                List.of(ProductType.FOOD, ProductType.MEDICATION, ProductType.ACCESSORY, ProductType.EQUIPMENT),
                List.of(ProductStatus.AVAILABLE, ProductStatus.PRE_ORDER, ProductStatus.OUT_OF_STOCK),
                List.of(DeliveryType.DELIVERY, DeliveryType.PICKUP, DeliveryType.DELIVERY_AND_PICKUP, DeliveryType.NO_DELIVERY_OPTION)
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(responseDTO))
                .addHeader("Content-Type", "application/json")
        );

        Mono<ProductEnumsResponseDTO> enumsMono = productsServiceClient.getProductEnumsValues();

        StepVerifier.create(enumsMono)
            .expectNextMatches(enums ->
                enums.getProductType().equals(List.of(
                    ProductType.FOOD,
                    ProductType.MEDICATION,
                    ProductType.ACCESSORY,
                    ProductType.EQUIPMENT)) &&
                enums.getProductStatus().equals(List.of(
                    ProductStatus.AVAILABLE,
                    ProductStatus.PRE_ORDER,
                    ProductStatus.OUT_OF_STOCK)) &&
                enums.getDeliveryType().equals(List.of(
                    DeliveryType.DELIVERY,
                    DeliveryType.PICKUP, DeliveryType.DELIVERY_AND_PICKUP,
                    DeliveryType.NO_DELIVERY_OPTION))
    )
    .verifyComplete();
    }


    @Test
    void getAllProductTypes_ThenReturnTypeList() {

        mockWebServer.enqueue(new MockResponse()
                .setBody("data:{\"productTypeId\":\"1\",\"typeName\":\"FOOD\"}\n\n" +
                        "data:{\"productTypeId\":\"2\",\"typeName\":\"EQUIPMENT\"}\n\n")
                .setHeader("Content-Type", "text/event-stream")
        );

        Flux<ProductTypeResponseDTO> typesFlux = productsServiceClient.getAllProductTypes();

        StepVerifier.create(typesFlux)
                .expectNextMatches(type -> type.getProductTypeId().equals("1") && type.getTypeName().equals("FOOD"))
                .expectNextMatches(type -> type.getProductTypeId().equals("2") && type.getTypeName().equals("EQUIPMENT"))
                .verifyComplete();
    }


    @Test
    void whenGetProductTypeById_thenReturnSingleType() throws JsonProcessingException {
        ProductTypeResponseDTO typeResponse = new ProductTypeResponseDTO("1", "FOOD");

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(typeResponse))
        );

        Mono<ProductTypeResponseDTO> result = productsServiceClient.getProductTypeByProductTypeId("1");

        StepVerifier.create(result)
                .expectNextMatches(type -> type.getProductTypeId().equals("1") && type.getTypeName().equals("FOOD"))
                .verifyComplete();
    }


    @Test
    void whenAddProductType_thenReturnCreatedType() throws JsonProcessingException {
        ProductTypeResponseDTO typeResponse = new ProductTypeResponseDTO("3", "ACCESSORY");

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(typeResponse))
        );

        Mono<ProductTypeResponseDTO> result = productsServiceClient.createProductType(new ProductTypeRequestDTO("ACCESSORY"));

        StepVerifier.create(result)
                .expectNextMatches(type -> type.getProductTypeId().equals("3") && type.getTypeName().equals("ACCESSORY"))
                .verifyComplete();
    }


    @Test
    void whenUpdateProductType_thenReturnUpdatedType() throws JsonProcessingException {
        ProductTypeResponseDTO typeResponse = new ProductTypeResponseDTO("2", "UPDATED_TYPE");

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(typeResponse))
        );

        Mono<ProductTypeResponseDTO> result = productsServiceClient.updateProductType("2", new ProductTypeRequestDTO("UPDATED_TYPE"));

        StepVerifier.create(result)
                .expectNextMatches(type -> type.getProductTypeId().equals("2") && type.getTypeName().equals("UPDATED_TYPE"))
                .verifyComplete();
    }


    @Test
    void whenDeleteProductType_thenReturnDeletedType() throws JsonProcessingException {
        ProductTypeResponseDTO typeResponse = new ProductTypeResponseDTO("2", "EQUIPMENT");

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(typeResponse))
        );

        Mono<ProductTypeResponseDTO> result = productsServiceClient.deleteProductType("2");

        StepVerifier.create(result)
                .expectNextMatches(type -> type.getProductTypeId().equals("2") && type.getTypeName().equals("EQUIPMENT"))
                .verifyComplete();
    }

}