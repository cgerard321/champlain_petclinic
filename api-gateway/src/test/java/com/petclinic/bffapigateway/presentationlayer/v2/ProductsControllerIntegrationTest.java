package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.ProductsServiceClient;
import com.petclinic.bffapigateway.dtos.Products.ProductResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService;
import com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigProductsServices;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidAdmin;
import static com.petclinic.bffapigateway.presentationlayer.v2.mockservers.MockServerConfigAuthService.jwtTokenForValidOwnerId;
import static org.junit.Assert.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductsControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private MockServerConfigProductsServices mockServerConfigProductsServices;

    private MockServerConfigAuthService mockServerConfigAuthService;
    @Autowired
    private ProductsServiceClient productsServiceClient;


    @BeforeAll
    public void startMockServer(){
        mockServerConfigProductsServices= new MockServerConfigProductsServices();
        mockServerConfigProductsServices.registerGetAllProductsEndpoint();


        mockServerConfigAuthService = new MockServerConfigAuthService();
        mockServerConfigAuthService.registerValidateTokenForOwnerEndpoint();
        mockServerConfigAuthService.registerValidateTokenForAdminEndpoint();
        mockServerConfigAuthService.registerValidateTokenForVetEndpoint();
    }

    @AfterAll
    public void stopMockServer() {
        mockServerConfigProductsServices.stopMockServer();
        mockServerConfigAuthService.stopMockServer();
    }


    @Test
    void whenGetAllProducts_thenReturnProducts() {
        webTestClient.get()
                .uri("/api/v2/gateway/products")
                .cookie("Bearer", jwtTokenForValidAdmin)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(ProductResponseDTO.class)
                .value(productResponseDTOS -> {
                    assertNotNull(productResponseDTOS);
                    assertEquals(8,productResponseDTOS.size());
                });


    }
    @Test
    void whenGetAllProductsWithWrongEndpoint_thenReturnNotFound() {
        webTestClient.get()
                .uri("/api/v2/gateway/produc")  //missing "ts"
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isNotFound();
    }


}

