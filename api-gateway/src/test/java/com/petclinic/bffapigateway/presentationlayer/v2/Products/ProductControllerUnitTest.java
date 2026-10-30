package com.petclinic.bffapigateway.presentationlayer.v2.Products;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.DeliveryType;
import com.petclinic.bffapigateway.dtos.Products.ProductQuantityRequest;
import com.petclinic.bffapigateway.dtos.Products.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.ProductController;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ProductController.class,
        ProductsServiceClient.class,
        GlobalExceptionHandler.class
})
@WebFluxTest(controllers = ProductController.class)
@AutoConfigureWebTestClient
class ProductControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private ProductsServiceClient productsServiceClient;

    private final String baseInventoryURL = "/api/v2/gateway/products";

    private ProductRequestDTO productRequest1 = ProductRequestDTO.builder()
            .productName("Product 1")
            .productDescription("Product 1 Description")
            .productSalePrice(100.0)
            .averageRating(0.00)
            .build();

    private ProductResponseDTO productResponse1 = ProductResponseDTO.builder()
            .productId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .productName("Product 1")
            .productDescription("Product 1 Description")
            .productSalePrice(100.0)
            .averageRating(0.00)
            .isUnlisted(true)
            .build();

    private final ProductResponseDTO productResponseDTO1 = ProductResponseDTO.builder()
            .productId("06a7d573-bcab-4db3-956f-773324b92a80")
            .productName("Dog Food")
            .productDescription("Premium dog food")
            .productSalePrice(49.99)
            .averageRating(4.5)
            .build();

    private final ProductResponseDTO productResponseDTO2 = ProductResponseDTO.builder()
            .productId("1501f30e-1db1-44b2-a555-bca6f64450e4")
            .productName("Fish Tank Heater")
            .productDescription("Submersible heater for tropical fish tanks")
            .productSalePrice(14.99)
            .averageRating(0.0)
            .build();


    @Test
    void whenGetAllProductsWithValidMinAndMaxRating_thenReturnFluxProductResponseDTO() {
        when(productsServiceClient.getAllProducts(null, null, 3.0, 5.0, null,null,null))
                .thenReturn(Flux.just(productResponseDTO1, productResponseDTO2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/products")
                        .queryParam("minRating", "3.0")
                        .queryParam("maxRating", "5.0")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(2, productResponseDTOS.size());
                    assertEquals(productResponseDTO1.getProductId(), productResponseDTOS.get(0).getProductId());
                    assertEquals(productResponseDTO2.getProductId(), productResponseDTOS.get(1).getProductId());
                });

        verify(productsServiceClient, times(1)).getAllProducts(null, null, 3.0, 5.0, null,null,null);
    }

    @Test
    void whenGetAllProductsWithMinRatingGreaterThanMaxRating_thenReturnBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/products")
                        .queryParam("minRating", "5.0")
                        .queryParam("maxRating", "3.0")
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> {
                    assertNotNull(response);
                    assertTrue(response.contains("minRating cannot be greater than maxRating"));
                });
    }



    @Test
    void getAllProducts_whenProductsExist_thenReturnFluxProductResponseDTO() {

        when(productsServiceClient.getAllProducts(null, null,null,null,null,null,null))
                .thenReturn(Flux.just(productResponseDTO1,productResponseDTO2));

        webTestClient.get()
                .uri("/api/v2/gateway/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(2, productResponseDTOS.size());
                    assertEquals(productResponseDTO1.getProductId(), productResponseDTOS.get(0).getProductId());
                    assertEquals(productResponseDTO2.getProductId(), productResponseDTOS.get(1).getProductId());
                });
        verify(productsServiceClient, times(1)).getAllProducts(null, null,null,null,null,null,null);
    }

    @Test
    void getAllProducts_whenNoProductsExist_thenReturnEmptyFlux() {

        when(productsServiceClient.getAllProducts(null, null,null,null,null,null,null))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v2/gateway/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(0, productResponseDTOS.size());
                });
        verify(productsServiceClient, times(1)).getAllProducts(null, null,null,null,null,null,null);
    }

    @Test
    void whenGetAllProductsWithValidDeliveryType_thenReturnFilteredProducts() {
        DeliveryType deliveryType = DeliveryType.DELIVERY;


        productResponseDTO1.setDeliveryType(DeliveryType.DELIVERY);
        productResponseDTO2.setDeliveryType(DeliveryType.PICKUP);

        when(productsServiceClient.getAllProducts(null, null, null, null, null, deliveryType.toString(),null))
                .thenReturn(Flux.just(productResponseDTO1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/products")
                        .queryParam("deliveryType", deliveryType.toString())
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(1, productResponseDTOS.size());
                    assertEquals(productResponseDTO1.getProductId(), productResponseDTOS.get(0).getProductId());
                    assertEquals(DeliveryType.DELIVERY, productResponseDTOS.get(0).getDeliveryType());
                });

        verify(productsServiceClient, times(1)).getAllProducts(null, null, null, null, null, deliveryType.toString(),null);
    }

    @Test
    void whenGetAllProductsWithInvalidDeliveryType_thenReturnAllProducts() {
        String invalidDeliveryType = "INVALID_DELIVERY_TYPE";


        when(productsServiceClient.getAllProducts(null, null, null, null, null, invalidDeliveryType,null))
                .thenReturn(Flux.just(productResponseDTO1, productResponseDTO2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/products")
                        .queryParam("deliveryType", invalidDeliveryType)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(2, productResponseDTOS.size());
                    assertEquals(productResponseDTO1.getProductId(), productResponseDTOS.get(0).getProductId());
                    assertEquals(productResponseDTO2.getProductId(), productResponseDTOS.get(1).getProductId());
                });

        verify(productsServiceClient, times(1)).getAllProducts(null, null, null, null, null, invalidDeliveryType,null);
    }





    @Test
    public void whenAddProduct_thenReturnProduct() {
        when(productsServiceClient.createProduct(productRequest1)).thenReturn(Mono.just(productResponse1));

        webTestClient.post()
                .uri(baseInventoryURL)
                .bodyValue(productRequest1)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(productResponse1);

        verify(productsServiceClient, times(1)).createProduct(productRequest1);
    }

    @Test
    public void whenUpdateProduct_thenReturnUpdatedProduct() {
        when(productsServiceClient.updateProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", productRequest1))
                .thenReturn(Mono.just(productResponse1));

        webTestClient.put()
                .uri(baseInventoryURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .bodyValue(productRequest1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(productResponse1);

        verify(productsServiceClient, times(1)).updateProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", productRequest1);
    }

    @Test
    public void whenUpdateNonExistentProduct_thenReturnNotFound() {
        String nonExistentProductId = "non-existent-id";

        when(productsServiceClient.updateProduct(nonExistentProductId, productRequest1))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri(baseInventoryURL + "/" + nonExistentProductId)
                .bodyValue(productRequest1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(response -> {
                    assertTrue(response.getResponseBody() == null || response.getResponseBody().length == 0);
                });

        verify(productsServiceClient, times(1)).updateProduct(nonExistentProductId, productRequest1);
    }

    @Test
    public void whenUpdateProductServiceThrowsException_thenReturnInternalServerError() {
        String productId = "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a";

        when(productsServiceClient.updateProduct(productId, productRequest1))
                .thenReturn(Mono.error(new RuntimeException("Service layer exception")));

        webTestClient.put()
                .uri(baseInventoryURL + "/" + productId)
                .bodyValue(productRequest1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Internal Server Error"));
                });

        verify(productsServiceClient, times(1)).updateProduct(productId, productRequest1);
    }

    @Test
    public void whenPatchListingStatus_thenReturnUpdatedProduct() {
        ProductRequestDTO patchProductRequest = ProductRequestDTO.builder()
                .isUnlisted(true)
                .build();

        when(productsServiceClient.patchListingStatus("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", patchProductRequest))
                .thenReturn(Mono.just(productResponse1));

        webTestClient.patch()
                .uri(baseInventoryURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/status")
                .bodyValue(patchProductRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(productResponse1);

        verify(productsServiceClient, times(1)).patchListingStatus("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", patchProductRequest);
    }

    @Test
    public void whenPatchListingStatusNonExistentProduct_thenReturnNotFound() {
        ProductRequestDTO patchProductRequest = ProductRequestDTO.builder()
                .isUnlisted(true)
                .build();

        WebClientResponseException notFoundException = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                null,
                null,
                null
        );

        when(productsServiceClient.patchListingStatus(anyString(), eq(patchProductRequest)))
                .thenReturn(Mono.error(notFoundException));

        webTestClient.patch()
                .uri(baseInventoryURL + "/691e6945-0d4a-4b20-85cc-afd251faccfd/status")
                .bodyValue(patchProductRequest)
                .exchange()
                .expectStatus().isNotFound();

        verify(productsServiceClient, times(1)).patchListingStatus(anyString(), eq(patchProductRequest));
    }

    @Test
    public void whenPatchListingStatusWithInvalidProductId_thenReturnUnprocessableEntity() {
        ProductRequestDTO patchProductRequest = ProductRequestDTO.builder()
                .isUnlisted(true)
                .build();

        WebClientResponseException unprocessableEntityException = WebClientResponseException.create(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                null,
                null,
                null
        );

        when(productsServiceClient.patchListingStatus(anyString(), eq(patchProductRequest)))
                .thenReturn(Mono.error(unprocessableEntityException));

        webTestClient.patch()
                .uri(baseInventoryURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/status")
                .bodyValue(patchProductRequest)
                .exchange()
                .expectStatus().is4xxClientError();  // Expect 422 status

        verify(productsServiceClient, times(1)).patchListingStatus(anyString(), eq(patchProductRequest));
    }

    @Test
    public void whenPatchListingStatusServiceThrowsException_thenReturnInternalServerError() {
        ProductRequestDTO patchProductRequest = ProductRequestDTO.builder()
                .isUnlisted(true)
                .build();

        when(productsServiceClient.patchListingStatus("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", patchProductRequest))
                .thenReturn(Mono.error(new RuntimeException("Service layer exception")));

        webTestClient.patch()
                .uri(baseInventoryURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/status")
                .bodyValue(patchProductRequest)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Internal Server Error"));
                });

        verify(productsServiceClient, times(1)).patchListingStatus("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", patchProductRequest);
    }

//    @Test
//    public void whenDeleteProduct_thenReturnNothing() {
//        when(productsServiceClient.deleteProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")).thenReturn(Mono.empty());
//
//        webTestClient.delete()
//                .uri(baseInventoryURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
//                .exchange()
//                .expectStatus().isNoContent();
//
//        verify(productsServiceClient, times(1)).deleteProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
//    }

    @Test
    void whenGetAllProductsWithNegativeMinPrice_thenReturnBadRequest() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v2/gateway/products")
                        .queryParam("minPrice", "-10")
                        .queryParam("maxPrice", "100")
                        .build())
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> {
                    assertNotNull(response);
                    assertTrue(response.contains("Price and rating values cannot be negative"));
                });
    }

    @Test
    void whenGetAllProductsWithMinPriceGreaterThanMaxPrice_thenReturnBadRequest() {
        webTestClient.get()
                .uri("/api/v2/gateway/products?minPrice=50&maxPrice=30")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> {
                    assertNotNull(response);
                    assertTrue(response.contains("minPrice cannot be greater than maxPrice"));
                });
    }

    @Test
    void whenGetAllProductsWithValidPriceRange_thenReturnProducts() {
        when(productsServiceClient.getAllProducts(10.0, 100.0, null, null, null, null, null))
                .thenReturn(Flux.just(productResponseDTO1));

        webTestClient.get()
                .uri(baseInventoryURL + "?minPrice=10&maxPrice=100")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(1);

        verify(productsServiceClient).getAllProducts(10.0, 100.0, null, null, null, null, null);
    }

    @Test
    void whenGetAllProductsWithOnlyMinPriceAndMinRating_thenReturnProducts() {
        when(productsServiceClient.getAllProducts(10.0, null, 1.0, null, null, null, null))
                .thenReturn(Flux.just(productResponseDTO1));

        webTestClient.get()
                .uri(baseInventoryURL + "?minPrice=10&minRating=1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(1);

        verify(productsServiceClient).getAllProducts(10.0, null, 1.0, null, null, null, null);
    }

    @Test
    void whenGetAllProductsWithNegativeMaxPrice_thenReturnBadRequest() {
        webTestClient.get()
                .uri(baseInventoryURL + "?maxPrice=-5")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> assertTrue(response.contains("Price and rating values cannot be negative")));

        verifyNoInteractions(productsServiceClient);
    }

    @Test
    void whenGetAllProductsWithNegativeMinRating_thenReturnBadRequest() {
        webTestClient.get()
                .uri(baseInventoryURL + "?minRating=-1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> assertTrue(response.contains("Price and rating values cannot be negative")));

        verifyNoInteractions(productsServiceClient);
    }

    @Test
    void whenGetAllProductsWithNegativeMaxRating_thenReturnBadRequest() {
        webTestClient.get()
                .uri(baseInventoryURL + "?maxRating=-1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> assertTrue(response.contains("Price and rating values cannot be negative")));

        verifyNoInteractions(productsServiceClient);
    }

    @Test
    void whenGetProductByValidProductId_thenReturnProduct() {
        when(productsServiceClient.getProductByProductId(productResponseDTO1.getProductId()))
                .thenReturn(Mono.just(productResponseDTO1));

        webTestClient.get()
                .uri(baseInventoryURL + "/" + productResponseDTO1.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(product -> assertEquals(productResponseDTO1.getProductId(), product.getProductId()));

        verify(productsServiceClient).getProductByProductId(productResponseDTO1.getProductId());
    }

    @Test
    void whenGetProductByNonExistentProductId_thenReturnNotFound() {
        String nonExistentProductId = "non-existent-id";
        when(productsServiceClient.getProductByProductId(nonExistentProductId))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri(baseInventoryURL + "/" + nonExistentProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(productsServiceClient).getProductByProductId(nonExistentProductId);
    }

    @Test
    void whenIncrementRequestCount_thenReturnNoContent() {
        when(productsServiceClient.requestCount(productResponseDTO1.getProductId()))
                .thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseInventoryURL + "/" + productResponseDTO1.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productsServiceClient).requestCount(productResponseDTO1.getProductId());
    }

    @Test
    void whenGetProductsByType_thenReturnProducts() {
        when(productsServiceClient.getProductsByType("FOOD"))
                .thenReturn(Flux.just(productResponseDTO1, productResponseDTO2));

        webTestClient.get()
                .uri(baseInventoryURL + "/filter/FOOD")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .hasSize(2);

        verify(productsServiceClient).getProductsByType("FOOD");
    }

    @Test
    void whenDecreaseProductQuantity_thenReturnNoContent() {
        when(productsServiceClient.decreaseProductQuantity(productResponseDTO1.getProductId()))
                .thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseInventoryURL + "/" + productResponseDTO1.getProductId() + "/decrease")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(productsServiceClient).decreaseProductQuantity(productResponseDTO1.getProductId());
    }

    @Test
    void whenChangeProductQuantity_thenReturnNoContent() {
        ProductQuantityRequest request = new ProductQuantityRequest();
        request.setProductQuantity(10);

        when(productsServiceClient.changeProductQuantity(productResponseDTO1.getProductId(), 10))
                .thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseInventoryURL + "/" + productResponseDTO1.getProductId() + "/quantity")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNoContent();

        verify(productsServiceClient).changeProductQuantity(productResponseDTO1.getProductId(), 10);
    }

    @Test
    void whenChangeProductQuantityForNonExistentProduct_thenReturnNotFound() {
        String nonExistentProductId = "non-existent-id";
        ProductQuantityRequest request = new ProductQuantityRequest();
        request.setProductQuantity(10);

        when(productsServiceClient.changeProductQuantity(nonExistentProductId, 10))
                .thenReturn(Mono.error(new NotFoundException("Product not found")));

        webTestClient.patch()
                .uri(baseInventoryURL + "/" + nonExistentProductId + "/quantity")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();

        verify(productsServiceClient).changeProductQuantity(nonExistentProductId, 10);
    }

    @Test
    void whenChangeProductQuantityServiceThrowsException_thenReturnInternalServerError() {
        ProductQuantityRequest request = new ProductQuantityRequest();
        request.setProductQuantity(10);

        when(productsServiceClient.changeProductQuantity(productResponseDTO1.getProductId(), 10))
                .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.patch()
                .uri(baseInventoryURL + "/" + productResponseDTO1.getProductId() + "/quantity")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(productsServiceClient).changeProductQuantity(productResponseDTO1.getProductId(), 10);
    }
}
