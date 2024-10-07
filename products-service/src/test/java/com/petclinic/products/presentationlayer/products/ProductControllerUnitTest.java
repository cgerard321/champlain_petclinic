package com.petclinic.products.presentationlayer.products;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        when(productService.getAllProducts(null,null)).thenReturn(Flux.just(productResponseModel1, productResponseModel2));

        webClient.get().uri("/api/v1/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseModel.class);

        verify(productService).getAllProducts(null,null);
    }
    @Test
    public void whenNoProductsExist_thenReturnEmptyList() {

        when(productService.getAllProducts(null,null)).thenReturn(Flux.empty());

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

        verify(productService).getAllProducts(null,null);
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

        webClient.get().uri("/api/v1/products/ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
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

        webClient.get().uri("/api/v1/products/ae2d3af7-f2a2-407f-ad31-ca7d8220cb7")
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
                .averageRating(0.0)
                .build();

        ProductResponseModel productResponseModel = ProductResponseModel.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        when(productService.addProduct(any(Mono.class))).thenReturn(Mono.just(productResponseModel));

        webClient.post().uri("/api/v1/products")
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
                .averageRating(0.0)
                .build();

        ProductResponseModel productResponseModel = ProductResponseModel.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        when(productService.updateProductByProductId(eq("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a"), any(Mono.class))).thenReturn(Mono.just(productResponseModel));

        webClient.put().uri("/api/v1/products/ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
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
    public void whenDeleteProduct_thenReturnDeletedProduct() {
        ProductResponseModel productResponseModel = ProductResponseModel.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .averageRating(0.0)
                .build();

        when(productService.deleteProductByProductId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")).thenReturn(Mono.just(productResponseModel));

        webClient.delete().uri("/api/v1/products/ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseModel.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", response.getProductId());
                });

        verify(productService).deleteProductByProductId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a");
    }

}
