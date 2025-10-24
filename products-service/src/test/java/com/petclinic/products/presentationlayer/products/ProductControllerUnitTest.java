package com.petclinic.products.presentationlayer.products;

import com.petclinic.products.businesslayer.products.ProductBundleService;
import com.petclinic.products.businesslayer.products.ProductService;
import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import com.petclinic.products.datalayer.products.ProductType;
import com.petclinic.products.datalayer.products.ProductStatus;
import com.petclinic.products.datalayer.products.DeliveryType;
import com.petclinic.products.presentationlayer.products.ProductEnumsResponseModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = ProductController.class)
public class ProductControllerUnitTest {
    @MockBean
    private ProductService productService;

    @MockBean
    private ProductBundleService productBundleService;

    @Autowired
    private WebTestClient webClient;

    @Test
    public void whenGetAllProductsThenReturnProducts() {
        ProductResponseModel  productResponseModel1 = ProductResponseModel.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        ProductResponseModel  productResponseModel2 = ProductResponseModel.builder()
                .productId("baee7cd2-b67a-449f-b262-91f45dde8a6d")
                .productName("Flea Collar")
                .productDescription("Flea and tick prevention for small dogs")
                .productSalePrice(9.99)
                .averageRating(0.0)
                .build();
        when(productService.getAllProducts(null,null,null,null,null,null,null)).thenReturn(Flux.just(productResponseModel1, productResponseModel2));

        webClient.get().uri("/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class);

        verify(productService).getAllProducts(null,null,null,null,null,null,null);
    }
    @Test
    public void whenNoProductsExist_thenReturnEmptyList() {

        when(productService.getAllProducts(null,null,null,null,null,null,null)).thenReturn(Flux.empty());

        webClient.get().uri("/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(0, productResponseModel.size());
                });

        verify(productService).getAllProducts(null,null,null,null,null,null,null);
    }

    @Test
    public void whenGetProductByValidProductId_thenReturnProduct() {
        ProductResponseModel productResponseModel = ProductResponseModel.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        when(productService.getProductByProductId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")).thenReturn(Mono.just(productResponseModel));

        webClient.get().uri("/products/ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", response.getProductId());
                });

        verify(productService).getProductByProductId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a");
    }

    @Test
    public void whenGetProductWithInvalidProductId_thenThrowException() {
        when(productService.getProductByProductId(eq("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7"))).thenReturn(Mono
                .error(new InvalidInputException("Provided product id is invalid: ae2d3af7-f2a2-407f-ad31-ca7d8220cb7")));

        webClient.get().uri("/products/ae2d3af7-f2a2-407f-ad31-ca7d8220cb7")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(ProductResponseModel.class)
                .value(Assertions::assertNotNull);

        verify(productService, times(0)).getProductByProductId(eq("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7"));
    }

    @Test
    public void whenAddProduct_thenReturnCreatedProduct() {
        ProductRequestModel productRequestModel = ProductRequestModel.builder()
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .build();

        ProductResponseModel productResponseModel = ProductResponseModel.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        when(productService.addProduct(any(Mono.class))).thenReturn(Mono.just(productResponseModel));

        webClient.post().uri("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", response.getProductId());
                });

        verify(productService).addProduct(any(Mono.class));
    }

    @Test
    public void whenUpdateProduct_thenReturnUpdatedProduct() {
        ProductRequestModel productRequestModel = ProductRequestModel.builder()
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .build();

        ProductResponseModel productResponseModel = ProductResponseModel.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        when(productService.updateProductByProductId(eq("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a"), any(Mono.class))).thenReturn(Mono.just(productResponseModel));

        webClient.put().uri("/products/ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", response.getProductId());
                });

        verify(productService).updateProductByProductId(eq("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a"), any(Mono.class));
    }

    @Test
    void whenDeleteProduct_withCascadeTrue_thenReturnDeletedProductJson() {
        // arrange
        String id = "ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a";
        ProductResponseModel expected = ProductResponseModel.builder()
                .productId(id)
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        when(productService.deleteProductByProductId(id, true))
                .thenReturn(Mono.just(expected));

        // act + assert
        webClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/products/{productId}")
                        .queryParam("cascadeBundles", true)   // <-- include query param
                        .build(id))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(ProductResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals(expected.getProductId(), resp.getProductId());
                    assertEquals(expected.getProductName(), resp.getProductName());
                    assertEquals(expected.getProductDescription(), resp.getProductDescription());
                    assertEquals(expected.getProductSalePrice(), resp.getProductSalePrice());
                    assertEquals(expected.getAverageRating(), resp.getAverageRating());
                });

        verify(productService).deleteProductByProductId(id, true);
    }

    @Test
    public void whenGetAllProductBundles_thenReturnBundles() {
        ProductBundleResponseModel bundle1 = ProductBundleResponseModel.builder()
                .bundleId("bundle1")
                .bundleName("Bundle 1")
                .bundleDescription("Description 1")
                .bundlePrice(49.99)
                .build();

        ProductBundleResponseModel bundle2 = ProductBundleResponseModel.builder()
                .bundleId("bundle2")
                .bundleName("Bundle 2")
                .bundleDescription("Description 2")
                .bundlePrice(119.99)
                .build();

        when(productBundleService.getAllProductBundles()).thenReturn(Flux.just(bundle1, bundle2));

        webClient.get().uri("/products/bundles")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductBundleResponseModel.class)
                .value(bundles -> {
                    assertNotNull(bundles);
                    assertEquals(2, bundles.size());
                    assertEquals("bundle1", bundles.get(0).getBundleId());
                    assertEquals("bundle2", bundles.get(1).getBundleId());
                });

        verify(productBundleService).getAllProductBundles();
    }

    @Test
    public void whenGetProductBundleById_thenReturnBundle() {
        ProductBundleResponseModel bundle = ProductBundleResponseModel.builder()
                .bundleId("bundle1")
                .bundleName("Bundle 1")
                .bundleDescription("Description 1")
                .bundlePrice(49.99)
                .build();

        when(productBundleService.getProductBundleById("bundle1")).thenReturn(Mono.just(bundle));

        webClient.get().uri("/products/bundles/bundle1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductBundleResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("bundle1", response.getBundleId());
                });

        verify(productBundleService).getProductBundleById("bundle1");
    }

    @Test
    public void whenGetProductBundleByInvalidId_thenReturnNotFound() {
        when(productBundleService.getProductBundleById("invalid-id")).thenReturn(Mono.empty());

        webClient.get().uri("/products/bundles/invalid-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(productBundleService).getProductBundleById("invalid-id");
    }

    @Test
    public void whenCreateProductBundle_thenReturnCreatedBundle() {
        ProductBundleRequestModel requestModel = ProductBundleRequestModel.builder()
                .bundleName("Bundle 1")
                .bundleDescription("Description 1")
                .bundlePrice(49.99)
                .build();

        ProductBundleResponseModel responseModel = ProductBundleResponseModel.builder()
                .bundleId("bundle1")
                .bundleName("Bundle 1")
                .bundleDescription("Description 1")
                .bundlePrice(49.99)
                .build();

        when(productBundleService.createProductBundle(any(Mono.class))).thenReturn(Mono.just(responseModel));

        webClient.post().uri("/products/bundles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductBundleResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("bundle1", response.getBundleId());
                });

        verify(productBundleService).createProductBundle(any(Mono.class));
    }

    @Test
    public void whenUpdateProductBundle_thenReturnUpdatedBundle() {
        ProductBundleRequestModel requestModel = ProductBundleRequestModel.builder()
                .bundleName("Updated Bundle")
                .bundleDescription("Updated Description")
                .bundlePrice(49.99)
                .build();

        ProductBundleResponseModel responseModel = ProductBundleResponseModel.builder()
                .bundleId("bundle1")
                .bundleName("Updated Bundle")
                .bundleDescription("Updated Description")
                .bundlePrice(49.99)
                .build();

        when(productBundleService.updateProductBundle(eq("bundle1"), any(Mono.class))).thenReturn(Mono.just(responseModel));

        webClient.put().uri("/products/bundles/bundle1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestModel)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductBundleResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("bundle1", response.getBundleId());
                });

        verify(productBundleService).updateProductBundle(eq("bundle1"), any(Mono.class));
    }

    @Test
    public void whenDeleteProductBundle_thenReturnNoContent() {
        when(productBundleService.deleteProductBundle("bundle1")).thenReturn(Mono.empty());

        webClient.delete().uri("/products/bundles/bundle1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        verify(productBundleService).deleteProductBundle("bundle1");
    }

    @Test
    public void whenGetProductEnums_thenReturnEnums() {
        ProductEnumsResponseModel enumsResponseDTO = new ProductEnumsResponseModel(
                List.of(ProductType.FOOD, ProductType.MEDICATION, ProductType.ACCESSORY, ProductType.EQUIPMENT),
                List.of(ProductStatus.AVAILABLE, ProductStatus.PRE_ORDER, ProductStatus.OUT_OF_STOCK),
                List.of(DeliveryType.DELIVERY, DeliveryType.PICKUP, DeliveryType.DELIVERY_AND_PICKUP, DeliveryType.NO_DELIVERY_OPTION)
        );

        when(productService.getProductsEnumValues()).thenReturn(Mono.just(enumsResponseDTO));

        webClient.get().uri("/products/enums")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductEnumsResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(List.of(ProductType.FOOD, ProductType.MEDICATION, ProductType.ACCESSORY, ProductType.EQUIPMENT), response.getProductType());
                    assertEquals(List.of(ProductStatus.AVAILABLE, ProductStatus.PRE_ORDER, ProductStatus.OUT_OF_STOCK), response.getProductStatus());
                    assertEquals(List.of(DeliveryType.DELIVERY, DeliveryType.PICKUP, DeliveryType.DELIVERY_AND_PICKUP, DeliveryType.NO_DELIVERY_OPTION), response.getDeliveryType());
                });

        verify(productService).getProductsEnumValues();
    }

}
