package com.petclinic.bffapigateway.presentationlayer.v1.Products;


import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.*;
import com.petclinic.bffapigateway.presentationlayer.v1.ProductControllerV1;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ProductControllerV1.class,
        ProductsServiceClient.class,
        GlobalExceptionHandler.class
})
@WebFluxTest(controllers = ProductControllerV1.class)
@AutoConfigureWebTestClient
class ProductControllerV1UnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductsServiceClient productsServiceClient;

    private final String baseProductsURL = "/api/gateway/products";
    private final String baseBundlesURL = "/api/gateway/products/bundles";


    private final String invalidProductId = "ae2d3af7-f2a2-407f-ad31-ca7d8220cb";

    private ProductRequestDTO productRequest1 = ProductRequestDTO.builder()
            .productName("Product 1")
            .productDescription("Product 1 Description")
            .productSalePrice(100.0)
            .averageRating(0.00)
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

    private ProductBundleRequestDTO bundleRequest1 = ProductBundleRequestDTO.builder()
            .bundleName("Bundle 1")
            .bundleDescription("Description 1")
            .bundlePrice(49.99)
            .build();

    private final ProductBundleResponseDTO bundleResponseDTO1 = ProductBundleResponseDTO.builder()
            .bundleId("bundle1")
            .bundleName("Bundle 1")
            .bundleDescription("Description 1")
            .bundlePrice(49.99)
            .build();

    private final ProductBundleResponseDTO bundleResponseDTO2 = ProductBundleResponseDTO.builder()
            .bundleId("bundle2")
            .bundleName("Bundle 2")
            .bundleDescription("Description 2")
            .bundlePrice(119.99)
            .build();

    private ProductTypeResponseDTO productType1 = ProductTypeResponseDTO.builder()
            .productTypeId("6a247af0-52d9-4179-a5b4-ad4b92e686b1")
            .typeName("ACCESSORY")
            .build();


//TODO: GetALL
    @Test
    void getAllProducts_whenProductsExist_thenReturnFluxProductResponseDTO() {

        when(productsServiceClient.getAllProducts(null, null,null,null,null,null,null))
                .thenReturn(Flux.just(productResponseDTO1,productResponseDTO2));

        webTestClient.get()
                .uri(baseProductsURL)
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
                .uri(baseProductsURL)
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

//TODO: Rating
    @Test
    void whenGetAllProductsWithValidMinAndMaxRating_thenReturnFluxProductResponseDTO() {
        when(productsServiceClient.getAllProducts(null, null, 3.0, 5.0, null,null,null))
                .thenReturn(Flux.just(productResponseDTO1, productResponseDTO2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseProductsURL)
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
                .uri(uriBuilder -> uriBuilder.path(baseProductsURL)
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


//TODO: Price
@Test
void whenGetAllProductsWithNegativeMinPrice_thenReturnBadRequest() {
    webTestClient.get()
            .uri(uriBuilder -> uriBuilder.path(baseProductsURL)
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
                .uri(baseProductsURL + "?minPrice=50&maxPrice=30")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(response -> {
                    assertNotNull(response);
                    assertTrue(response.contains("minPrice cannot be greater than maxPrice"));
                });
    }


//TODO: DeliveryType
    @Test
    void whenGetAllProductsWithValidDeliveryType_thenReturnFilteredProducts() {
        DeliveryType deliveryType = DeliveryType.DELIVERY;


        productResponseDTO1.setDeliveryType(DeliveryType.DELIVERY);
        productResponseDTO2.setDeliveryType(DeliveryType.PICKUP);

        when(productsServiceClient.getAllProducts(null, null, null, null, null, deliveryType.toString(),null))
                .thenReturn(Flux.just(productResponseDTO1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseProductsURL)
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
                .uri(uriBuilder -> uriBuilder.path(baseProductsURL)
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


//TODO: ProductType

    @Test
    void whenGetAllProductsWithValidProductType_thenReturnFilteredProducts() {
        ProductType productType = ProductType.FOOD;


        productResponseDTO1.setProductType(ProductType.ACCESSORY);
        productResponseDTO2.setProductType(ProductType.MEDICATION);

        when(productsServiceClient.getAllProducts(null, null, null, null, null,null, productType.toString()))
                .thenReturn(Flux.just(productResponseDTO1));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseProductsURL)
                        .queryParam("productType", productType.toString())
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(1, productResponseDTOS.size());
                    assertEquals(productResponseDTO1.getProductId(), productResponseDTOS.get(0).getProductId());
                    assertEquals(ProductType.ACCESSORY, productResponseDTOS.get(0).getProductType());
                });

        verify(productsServiceClient, times(1)).getAllProducts(null, null, null, null, null,null, productType.toString());
    }

    @Test
    void whenGetAllProductsWithInvalidProductType_thenReturnAllProducts() {
        String invalidProductType = "INVALID_PRODUCT_TYPE";


        when(productsServiceClient.getAllProducts(null, null, null, null, null, null,invalidProductType))
                .thenReturn(Flux.just(productResponseDTO1, productResponseDTO2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseProductsURL)
                        .queryParam("productType", invalidProductType)
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

        verify(productsServiceClient, times(1)).getAllProducts(null, null, null, null, null, null,invalidProductType);
    }


//TODO: ByID
    @Test
    public void whenGetProductByValidProductId_thenReturnProduct() {

        when(productsServiceClient.getProductByProductId(productResponseDTO1.getProductId())).thenReturn(Mono.just(productResponseDTO1));

        webTestClient.get().uri(baseProductsURL + "/" + productResponseDTO1.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(productResponseDTO1.getProductId(), response.getProductId());
                });

        verify(productsServiceClient).getProductByProductId(productResponseDTO1.getProductId());
    }


//TODO: ADD
    @Test
    public void whenAddProduct_thenReturnProduct() {
        when(productsServiceClient.createProduct(productRequest1)).thenReturn(Mono.just(productResponseDTO1));

        webTestClient.post()
                .uri(baseProductsURL)
                .bodyValue(productRequest1)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(productResponseDTO1);

        verify(productsServiceClient, times(1)).createProduct(productRequest1);
    }


//TODO: Update
@Test
public void whenUpdateProduct_thenReturnUpdatedProduct() {
    when(productsServiceClient.updateProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", productRequest1))
            .thenReturn(Mono.just(productResponseDTO1));

    webTestClient.put()
            .uri(baseProductsURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .bodyValue(productRequest1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProductResponseDTO.class)
            .isEqualTo(productResponseDTO1);

    verify(productsServiceClient, times(1)).updateProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", productRequest1);
}

    @Test
    public void whenUpdateNonExistentProduct_thenReturnNotFound() {
        String nonExistentProductId = "non-existent-id";

        when(productsServiceClient.updateProduct(nonExistentProductId, productRequest1))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri(baseProductsURL + "/" + nonExistentProductId)
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
                .uri(baseProductsURL + "/" + productId)
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


//TODO: Patch

    @Test
    public void whenPatchListingStatus_thenReturnUpdatedProduct() {
        ProductRequestDTO patchProductRequest = ProductRequestDTO.builder()
                .isUnlisted(true)
                .build();

        when(productsServiceClient.patchListingStatus("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", patchProductRequest))
                .thenReturn(Mono.just(productResponseDTO1));

        webTestClient.patch()
                .uri(baseProductsURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/status")
                .bodyValue(patchProductRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDTO.class)
                .isEqualTo(productResponseDTO1);

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
                .uri(baseProductsURL + "/691e6945-0d4a-4b20-85cc-afd251faccfd/status")
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
                .uri(baseProductsURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/status")
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
                .uri(baseProductsURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a/status")
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


//TODO:Delete
//@Test
//public void whenDeleteProduct_thenReturnNothing() {
//    when(productsServiceClient.deleteProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")).thenReturn(Mono.empty());
//
//    webTestClient.delete()
//            .uri(baseProductsURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
//            .exchange()
//            .expectStatus().isNoContent();
//
//    verify(productsServiceClient, times(1)).deleteProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
//}


//TODO: Bundles
@Test
public void whenGetAllProductBundles_thenReturnBundles() {

    when(productsServiceClient.getAllProductBundles()).thenReturn(Flux.just(bundleResponseDTO1, bundleResponseDTO2));

    webTestClient.get().uri(baseBundlesURL)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(ProductBundleResponseDTO.class)
            .value(bundles -> {
                assertNotNull(bundles);
                assertEquals(2, bundles.size());
                assertEquals("bundle1", bundles.get(0).getBundleId());
                assertEquals("bundle2", bundles.get(1).getBundleId());
            });

    verify(productsServiceClient).getAllProductBundles();
}




    @Test
    public void whenGetProductBundleById_thenReturnBundle() {
        when(productsServiceClient.getProductBundleById(bundleResponseDTO1.getBundleId()))
                .thenReturn(Mono.just(bundleResponseDTO1));

        webTestClient.get().uri(baseBundlesURL + "/" +bundleResponseDTO1.getBundleId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductBundleResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(bundleResponseDTO1.getBundleId(), response.getBundleId());
                });

        verify(productsServiceClient).getProductBundleById(bundleResponseDTO1.getBundleId());
    }

    @Test
    public void whenGetProductBundleByInvalidId_thenReturnNotFound() {
        when(productsServiceClient.getProductBundleById("invalid-id"))
                .thenReturn(Mono.empty());

        webTestClient.get().uri(baseBundlesURL + "/invalid-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(productsServiceClient).getProductBundleById("invalid-id");
    }

    @Test
    public void whenCreateProductBundle_thenReturnCreatedBundle() {

        when(productsServiceClient.createProductBundle(bundleRequest1))
                .thenReturn(Mono.just(bundleResponseDTO1));

        webTestClient.post().uri(baseBundlesURL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bundleRequest1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductBundleResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(bundleResponseDTO1.getBundleId(), response.getBundleId());
                });

        verify(productsServiceClient).createProductBundle(bundleRequest1);
    }

    @Test
    public void whenUpdateProductBundle_thenReturnUpdatedBundle() {
        when(productsServiceClient.updateProductBundle(eq(bundleResponseDTO1.getBundleId()), eq(bundleRequest1)))
                .thenReturn(Mono.just(bundleResponseDTO1));

        webTestClient.put().uri(baseBundlesURL +"/" +bundleResponseDTO1.getBundleId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bundleRequest1)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductBundleResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(bundleResponseDTO1.getBundleId(), response.getBundleId());
                });

        verify(productsServiceClient, times(1))
                .updateProductBundle(eq(bundleResponseDTO1.getBundleId()), eq(bundleRequest1));
    }

    @Test
    public void whenDeleteProductBundle_thenReturnNoContent() {
        when(productsServiceClient.deleteProductBundle(bundleResponseDTO1.getBundleId())).thenReturn(Mono.empty());

        webTestClient.delete().uri(baseBundlesURL + "/" +bundleResponseDTO1.getBundleId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productsServiceClient).deleteProductBundle(bundleResponseDTO1.getBundleId());
    }


//TODO: Product Quantity
    @Test
    void whenDecreaseProductQuantity_thenReturnNoContent() {
        when(productsServiceClient.decreaseProductQuantity(anyString()))
                .thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseProductsURL + "/" + productResponseDTO1.getProductId() + "/decrease")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(productsServiceClient, times(1))
                .decreaseProductQuantity(productResponseDTO1.getProductId());
    }


    @Test
    void whenChangeProductQuantity_thenReturnNoContent() {
        ProductQuantityRequest request = new ProductQuantityRequest();
        request.setProductQuantity(10);

        when(productsServiceClient.changeProductQuantity(eq(productResponseDTO1.getProductId()), eq(10)))
                .thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseProductsURL + "/" + productResponseDTO1.getProductId() + "/quantity")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNoContent();

        verify(productsServiceClient, times(1))
                .changeProductQuantity(eq(productResponseDTO1.getProductId()), eq(10));
    }


    @Test
    void whenIncrementRequestCount_thenReturnNoContent() {

        when(productsServiceClient.requestCount(productResponseDTO1.getProductId())).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseProductsURL + "/" + productResponseDTO1.getProductId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productsServiceClient, times(1)).requestCount(productResponseDTO1.getProductId());
    }

    @Test
    public void whenGetProductEnums_thenReturnProductEnums() {
        ProductEnumsResponseDTO enumsResponseDTO = new ProductEnumsResponseDTO(
                List.of(ProductType.FOOD, ProductType.MEDICATION, ProductType.ACCESSORY, ProductType.EQUIPMENT),
                List.of(ProductStatus.AVAILABLE, ProductStatus.PRE_ORDER, ProductStatus.OUT_OF_STOCK),
                List.of(DeliveryType.DELIVERY, DeliveryType.PICKUP, DeliveryType.DELIVERY_AND_PICKUP, DeliveryType.NO_DELIVERY_OPTION)
        );
        when(productsServiceClient.getProductEnumsValues()).thenReturn(Mono.just(enumsResponseDTO));
        webTestClient.get().uri(baseProductsURL + "/enums")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductEnumsResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(enumsResponseDTO.getProductType(), response.getProductType());
                    assertEquals(enumsResponseDTO.getProductStatus(), response.getProductStatus());
                    assertEquals(enumsResponseDTO.getDeliveryType(), response.getDeliveryType());
                });

        verify(productsServiceClient).getProductEnumsValues();
    }

    @Test
    void whenGetAllProductTypes_thenReturnAllProductTypes() {
        when(productsServiceClient.getAllProductTypes())
                .thenReturn(Flux.just(productType1));

        webTestClient
                .get()
                .uri(baseProductsURL + "/types")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(ProductTypeResponseDTO.class)
                .value(productTypes -> {
                    assertNotNull(productTypes);
                    assertTrue(productTypes.size() > 0);
                });

        verify(productsServiceClient, times(1)).getAllProductTypes();
    }

    // ✅ Get All Product Types
    @Test
    void whenGetAllProductTypes_thenReturnFluxProductTypeResponseDTO() {
        when(productsServiceClient.getAllProductTypes())
                .thenReturn(Flux.just(productType1));

        webTestClient.get()
                .uri(baseProductsURL + "/types")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductTypeResponseDTO.class)
                .value(productTypeResponseDTOS -> {
                    assertNotNull(productTypeResponseDTOS);
                    assertEquals(1, productTypeResponseDTOS.size());
                    assertEquals(productType1.getProductTypeId(), productTypeResponseDTOS.get(0).getProductTypeId());
                    assertEquals(productType1.getTypeName(), productTypeResponseDTOS.get(0).getTypeName());
                });

        verify(productsServiceClient, times(1)).getAllProductTypes();
    }

    // ✅ Get Product Type by valid ID
    @Test
    void whenGetProductTypeByValidId_thenReturnProductTypeResponseDTO() {
        when(productsServiceClient.getProductTypeByProductTypeId(productType1.getProductTypeId()))
                .thenReturn(Mono.just(productType1));

        webTestClient.get()
                .uri(baseProductsURL + "/types/" + productType1.getProductTypeId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductTypeResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(productType1.getProductTypeId(), response.getProductTypeId());
                    assertEquals(productType1.getTypeName(), response.getTypeName());
                });

        verify(productsServiceClient, times(1)).getProductTypeByProductTypeId(productType1.getProductTypeId());
    }

    // ✅ Get Product Type by invalid ID
    @Test
    void whenGetProductTypeByInvalidId_thenReturnNotFound() {
        String invalidId = "non-existent-id";
        when(productsServiceClient.getProductTypeByProductTypeId(invalidId))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri(baseProductsURL + "/types/" + invalidId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(productsServiceClient, times(1)).getProductTypeByProductTypeId(invalidId);
    }

    // ✅ Create Product Type
    @Test
    void whenCreateProductType_thenReturnCreatedProductTypeResponseDTO() {
        ProductTypeRequestDTO requestDTO = ProductTypeRequestDTO.builder()
                .typeName("TOY")
                .build();

        ProductTypeResponseDTO createdResponse = ProductTypeResponseDTO.builder()
                .productTypeId("b12345")
                .typeName("TOY")
                .build();

        when(productsServiceClient.createProductType(requestDTO))
                .thenReturn(Mono.just(createdResponse));

        webTestClient.post()
                .uri(baseProductsURL + "/types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductTypeResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("TOY", response.getTypeName());
                    assertEquals("b12345", response.getProductTypeId());
                });

        verify(productsServiceClient, times(1)).createProductType(requestDTO);
    }

    // ✅ Update Product Type (valid ID)
    @Test
    void whenUpdateProductTypeWithValidId_thenUpdateProductType() {
        ProductTypeRequestDTO updateRequest = ProductTypeRequestDTO.builder()
                .typeName("UPDATED_ACCESSORY")
                .build();

        ProductTypeResponseDTO updatedResponse = ProductTypeResponseDTO.builder()
                .productTypeId(productType1.getProductTypeId())
                .typeName("UPDATED_ACCESSORY")
                .build();

        when(productsServiceClient.updateProductType(productType1.getProductTypeId(), updateRequest))
                .thenReturn(Mono.just(updatedResponse));

        webTestClient.put()
                .uri(baseProductsURL + "/types/" + productType1.getProductTypeId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductTypeResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("UPDATED_ACCESSORY", response.getTypeName());
                    assertEquals(productType1.getProductTypeId(), response.getProductTypeId());
                });

        verify(productsServiceClient, times(1)).updateProductType(productType1.getProductTypeId(), updateRequest);
    }

    // ✅ Update Product Type (nonexistent ID)
    @Test
    void whenUpdateProductTypeWithInvalidId_thenReturnNotFound() {
        String invalidId = "invalid-id";
        ProductTypeRequestDTO updateRequest = ProductTypeRequestDTO.builder()
                .typeName("UPDATED_ACCESSORY")
                .build();

        when(productsServiceClient.updateProductType(invalidId, updateRequest))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri(baseProductsURL + "/types/" + invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();

        verify(productsServiceClient, times(1)).updateProductType(invalidId, updateRequest);
    }

    // ✅ Delete Product Type (valid ID)
    @Test
    void whenDeleteProductTypeWithValidId_thenReturnDeletedProductType() {
        when(productsServiceClient.deleteProductType(productType1.getProductTypeId()))
                .thenReturn(Mono.just(productType1));

        webTestClient.delete()
                .uri(baseProductsURL + "/types/" + productType1.getProductTypeId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductTypeResponseDTO.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(productType1.getProductTypeId(), response.getProductTypeId());
                });

        verify(productsServiceClient, times(1)).deleteProductType(productType1.getProductTypeId());
    }

    // ✅ Delete Product Type (invalid ID)
    @Test
    void whenDeleteProductTypeWithNonExistingId_thenThrowNotFoundError() {
        String invalidId = "non-existent-id";
        when(productsServiceClient.deleteProductType(invalidId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(baseProductsURL + "/types/" + invalidId)
                .exchange()
                .expectStatus().isNotFound();

        verify(productsServiceClient, times(1)).deleteProductType(invalidId);
    }

}