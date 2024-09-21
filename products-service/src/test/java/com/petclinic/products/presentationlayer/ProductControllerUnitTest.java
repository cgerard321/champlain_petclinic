package com.petclinic.products.presentationlayer;

import com.petclinic.products.businesslayer.products.ProductService;
import com.petclinic.products.presentationlayer.products.ProductController;
import com.petclinic.products.presentationlayer.products.ProductResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = ProductController.class)
public class ProductControllerUnitTest {
    @MockBean
    private ProductService productService;

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
        when(productService.getAllProducts()).thenReturn(Flux.just(productResponseModel1, productResponseModel2));

        webClient.get().uri("/api/v1/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class);

        verify(productService).getAllProducts();
    }
    @Test
    public void whenNoProductsExist_thenReturnEmptyList() {

        when(productService.getAllProducts()).thenReturn(Flux.empty());


        webClient.get().uri("/api/v1/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class)
                .value(productResponseModel -> {
                    assertNotNull(productResponseModel);
                    assertEquals(0, productResponseModel.size());
                });

        verify(productService).getAllProducts();
    }



}
