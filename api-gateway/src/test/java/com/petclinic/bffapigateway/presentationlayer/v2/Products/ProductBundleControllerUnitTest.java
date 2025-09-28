package com.petclinic.bffapigateway.presentationlayer.v2.Products;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.ProductBundleRequestDTO;
import com.petclinic.bffapigateway.dtos.Products.ProductBundleResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.ProductController;
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

import java.util.Arrays;

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
class ProductBundleControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductsServiceClient productsServiceClient;

    private final String baseURL = "/api/v2/gateway/products/bundles";

    private final ProductBundleResponseDTO bundleResponse1 = ProductBundleResponseDTO.builder()
            .bundleId("bundle1")
            .bundleName("Bundle 1")
            .bundleDescription("Description 1")
            .productIds(Arrays.asList("product1", "product2"))
            .originalTotalPrice(150.0)
            .bundlePrice(120.0)
            .build();

    private final ProductBundleResponseDTO bundleResponse2 = ProductBundleResponseDTO.builder()
            .bundleId("bundle2")
            .bundleName("Bundle 2")
            .bundleDescription("Description 2")
            .productIds(Arrays.asList("product3", "product4"))
            .originalTotalPrice(200.0)
            .bundlePrice(170.0)
            .build();

    private final ProductBundleRequestDTO bundleRequest = ProductBundleRequestDTO.builder()
            .bundleName("New Bundle")
            .bundleDescription("New Bundle Description")
            .productIds(Arrays.asList("product5", "product6"))
            .bundlePrice(180.0)
            .build();

    @Test
    void getAllProductBundles_whenBundlesExist_thenReturnFluxProductBundleResponseDTO() {
        when(productsServiceClient.getAllProductBundles())
                .thenReturn(Flux.just(bundleResponse1, bundleResponse2));

        webTestClient.get()
                .uri(baseURL)
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

        verify(productsServiceClient, times(1)).getAllProductBundles();
    }

    @Test
    void getProductBundleById_whenBundleExists_thenReturnProductBundleResponseDTO() {
        when(productsServiceClient.getProductBundleById("bundle1"))
                .thenReturn(Mono.just(bundleResponse1));

        webTestClient.get()
                .uri(baseURL + "/bundle1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductBundleResponseDTO.class)
                .value(bundle -> {
                    assertNotNull(bundle);
                    assertEquals("bundle1", bundle.getBundleId());
                });

        verify(productsServiceClient, times(1)).getProductBundleById("bundle1");
    }

    @Test
    void createProductBundle_whenValidRequest_thenReturnCreatedBundle() {
        when(productsServiceClient.createProductBundle(bundleRequest))
                .thenReturn(Mono.just(bundleResponse1));

        webTestClient.post()
                .uri(baseURL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bundleRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductBundleResponseDTO.class)
                .value(bundle -> {
                    assertNotNull(bundle);
                    assertEquals("bundle1", bundle.getBundleId());
                });

        verify(productsServiceClient, times(1)).createProductBundle(bundleRequest);
    }

    @Test
    void updateProductBundle_whenBundleExists_thenReturnUpdatedBundle() {
        when(productsServiceClient.updateProductBundle(eq("bundle1"), any(ProductBundleRequestDTO.class)))
                .thenReturn(Mono.just(bundleResponse1));

        webTestClient.put()
                .uri(baseURL + "/bundle1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bundleRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductBundleResponseDTO.class)
                .value(bundle -> {
                    assertNotNull(bundle);
                    assertEquals("bundle1", bundle.getBundleId());
                });

        verify(productsServiceClient, times(1)).updateProductBundle(eq("bundle1"), any(ProductBundleRequestDTO.class));
    }

    @Test
    void deleteProductBundle_whenBundleExists_thenReturnNoContent() {
        when(productsServiceClient.deleteProductBundle("bundle1"))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(baseURL + "/bundle1")
                .exchange()
                .expectStatus().isNoContent();

        verify(productsServiceClient, times(1)).deleteProductBundle("bundle1");
    }
}
