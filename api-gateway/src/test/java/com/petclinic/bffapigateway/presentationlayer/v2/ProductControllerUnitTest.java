package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = ProductController.class)
@ContextConfiguration(classes = {
        ProductController.class,
        ProductsServiceClient.class
})
public class ProductControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductsServiceClient productsServiceClient;


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


}
