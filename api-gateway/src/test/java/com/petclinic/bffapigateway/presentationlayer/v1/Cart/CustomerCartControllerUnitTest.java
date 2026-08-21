package com.petclinic.bffapigateway.presentationlayer.v1.Cart;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartProductResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.CustomerCartController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        CustomerCartController.class,
        CartServiceClient.class
})
@WebFluxTest(controllers = CustomerCartController.class)
@AutoConfigureWebTestClient
class CustomerCartControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartServiceClient cartServiceClient;

    private static final String BASE_URL = "/api/gateway/customers";

    @Test
    @DisplayName("GET /api/gateway/customers/{customerId}/cart - returns cart")
    void getCartForCustomer_withValidId_returnsCart() {
        String customerId = "customer-456";
        CartResponseDTO cart = new CartResponseDTO();
        cart.setCustomerId(customerId);

        when(cartServiceClient.getCartByCustomerId(customerId)).thenReturn(Mono.just(cart));

        webTestClient.get()
                .uri(BASE_URL + "/{customerId}/cart", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .isEqualTo(cart);

        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }

    @Test
    @DisplayName("GET /api/gateway/customers/{customerId}/cart - returns 404 when missing")
    void getCartForCustomer_withMissingCart_returnsNotFound() {
        String customerId = "missing-customer";

        when(cartServiceClient.getCartByCustomerId(customerId)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri(BASE_URL + "/{customerId}/cart", customerId)
                .exchange()
                .expectStatus().isNotFound();

        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }

    @Test
    @DisplayName("GET /api/gateway/customers/{customerId}/cart/recent-purchases - returns items")
    void getRecentPurchasesForCustomer_returnsList() {
        String customerId = "customer-recent";
        List<CartProductResponseDTO> items = List.of(CartProductResponseDTO.builder().productId("prod-1").build());

        when(cartServiceClient.getRecentPurchasesByCustomerId(customerId)).thenReturn(Mono.just(items));

        webTestClient.get()
                .uri(BASE_URL + "/{customerId}/cart/recent-purchases", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartProductResponseDTO.class)
                .isEqualTo(items);

        verify(cartServiceClient, times(1)).getRecentPurchasesByCustomerId(customerId);
    }

    @Test
    @DisplayName("GET /api/gateway/customers/{customerId}/cart/recommendation-purchases - returns items")
    void getRecommendationPurchasesForCustomer_returnsList() {
        String customerId = "customer-rec";
        List<CartProductResponseDTO> items = List.of(CartProductResponseDTO.builder().productId("prod-2").build());

        when(cartServiceClient.getRecommendationPurchasesByCustomerId(customerId)).thenReturn(Mono.just(items));

        webTestClient.get()
                .uri(BASE_URL + "/{customerId}/cart/recommendation-purchases", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartProductResponseDTO.class)
                .isEqualTo(items);

        verify(cartServiceClient, times(1)).getRecommendationPurchasesByCustomerId(customerId);
    }
}
