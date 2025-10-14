package com.petclinic.products.presentationlayer;


import com.petclinic.products.businesslayer.products.ProductService;
import com.petclinic.products.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @Test
    void incrementRequestCount_WhenProductExists_ShouldReturnNoContent() {
       
        String productId = "06a7d573-bcab-4db3-956f-773324b92a80";
        when(productService.requestCount(productId)).thenReturn(Mono.empty());

       
        webTestClient.patch()
                .uri("/products/" + productId)
                .exchange()
                .expectStatus().isNoContent();

        verify(productService).requestCount(productId);
    }

    @Test
    void incrementRequestCount_WhenProductNotFound_ShouldReturnNotFound() {
       
        String productId = "06a7d573-bcab-4db3-956f-773324b92a77";
        when(productService.requestCount(productId))
                .thenReturn(Mono.error(new NotFoundException("Product id was not found: " + productId)));

       
        webTestClient.patch()
                .uri("/products/" + productId)
                .exchange()
                .expectStatus().isNotFound();

        verify(productService).requestCount(productId);
    }
}
