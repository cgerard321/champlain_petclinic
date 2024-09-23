package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.ProductRequestDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    void getAllProducts_whenProductsExist_thenReturnFluxProductResponseDTO() {

        when(productsServiceClient.getAllProducts(null, null))
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
        verify(productsServiceClient, times(1)).getAllProducts(null, null);
    }

    @Test
    void getAllProducts_whenNoProductsExist_thenReturnEmptyFlux() {

        when(productsServiceClient.getAllProducts(null, null))
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
        verify(productsServiceClient, times(1)).getAllProducts(null, null);
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
        when(productsServiceClient.updateProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", productRequest1)).thenReturn(Mono.just(productResponse1));

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
    public void whenDeleteProduct_thenReturnNothing() {
        when(productsServiceClient.deleteProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(baseInventoryURL + "/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .exchange()
                .expectStatus().isNoContent();

        verify(productsServiceClient, times(1)).deleteProduct("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
    }

}