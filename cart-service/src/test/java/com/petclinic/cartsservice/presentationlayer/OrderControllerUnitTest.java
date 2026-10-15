
package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.OrderService;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = OrderController.class)
class OrderControllerUnitTest {

    @MockBean
    private OrderService orderService;

    @Autowired
    private WebTestClient webTestClient;

    private final String VALID_ORDER_ID =
            "98f7b33a-d62a-420a-a84a-05a27c85fc91";

    private final String NOT_FOUND_ORDER_ID =
            "98f7b33a-d62a-420a-a84a-05a27c85fc92";

    private final String CUSTOMER_ID =
            "f470653d-05c5-4c45-b7a0-7d70f003d2ac";

    private OrderResponseModel order1;
    private OrderResponseModel order2;

    @BeforeEach
    void init() {
        CartProduct product1 = CartProduct.builder()
                .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
                .productName("Dog Food")
                .productSalePrice(50.0)
                .quantityInCart(2)
                .build();

        order1 = OrderResponseModel.builder()
                .orderId(VALID_ORDER_ID)
                .customerId(CUSTOMER_ID)
                .customerName("Test Customer")
                .products(List.of(product1))
                .subtotal(100.0)
                .tvq(9.98)
                .tvc(5.0)
                .total(114.98)
                .orderDate(LocalDateTime.now())
                .build();

        order2 = OrderResponseModel.builder()
                .orderId("d819e4f4-25af-4d33-91e9-2c45f0071606")
                .customerId(CUSTOMER_ID)
                .products(List.of(product1))
                .total(114.98)
                .orderDate(LocalDateTime.now().minusDays(5))
                .build();
    }

    @Test
    void whenGetOrderByOrderId_thenReturnOrderResponseModel() {
        // Arrange
        when(orderService.getOrderByOrderId(VALID_ORDER_ID))
                .thenReturn(Mono.just(order1));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/" + VALID_ORDER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OrderResponseModel.class)
                .value(response -> {
                    assertEquals(VALID_ORDER_ID, response.getOrderId());
                    assertEquals(CUSTOMER_ID, response.getCustomerId());
                    assertEquals(1, response.getProducts().size());
                    assertEquals(114.98, response.getTotal(), 0.001);
                });

        verify(orderService, times(1))
                .getOrderByOrderId(VALID_ORDER_ID);
    }

    @Test
    void getOrderByOrderId_NonExistentOrder_ReturnsNotFound() {
        // Arrange
        when(orderService.getOrderByOrderId(NOT_FOUND_ORDER_ID))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/" + NOT_FOUND_ORDER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(orderService, times(1))
                .getOrderByOrderId(NOT_FOUND_ORDER_ID);
    }

    @Test
    void getOrderByOrderId_InvalidId_ReturnsError() {
        // Arrange
        String invalidOrderId = "invalid-id";

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/" + invalidOrderId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);

        verify(orderService, never())
                .getOrderByOrderId(anyString());
    }


    @Test
    void getAllOrders_ReturnsMultipleOrders() {
        // Arrange
        when(orderService.getAllOrders())
                .thenReturn(Flux.just(order1, order2));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(OrderResponseModel.class)
                .value(orders -> {
                    assertEquals(2, orders.size());
                    assertEquals(VALID_ORDER_ID,
                            orders.get(0).getOrderId());
                    assertEquals(order2.getOrderId(),
                            orders.get(1).getOrderId());
                });

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getAllOrders_NoOrders_ReturnsEmptyList() {
        // Arrange
        when(orderService.getAllOrders())
                .thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(OrderResponseModel.class)
                .value(List::isEmpty);

        verify(orderService, times(1)).getAllOrders();
    }


    @Test
    void getOrdersByCustomerId_ExistingCustomer_ReturnsOrders() {
        // Arrange
        when(orderService.findOrdersByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.just(order1, order2));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/customer/" + CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(OrderResponseModel.class)
                .value(orders -> {
                    assertEquals(2, orders.size());
                    assertEquals(CUSTOMER_ID,
                            orders.get(0).getCustomerId());
                    assertEquals(CUSTOMER_ID,
                            orders.get(1).getCustomerId());
                });

        verify(orderService, times(1))
                .findOrdersByCustomerId(CUSTOMER_ID);
    }

    @Test
    void getOrdersByCustomerId_NoOrders_ReturnsEmptyList() {
        // Arrange
        when(orderService.findOrdersByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/customer/" + CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponseModel.class)
                .value(List::isEmpty);

        verify(orderService, times(1))
                .findOrdersByCustomerId(CUSTOMER_ID);
    }
}
