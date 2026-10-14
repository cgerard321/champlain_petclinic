
package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "server.port=0",
                "spring.data.mongodb.port=0"
        }
)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class OrderControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    private final String CUSTOMER_ID =
            "f470653d-05c5-4c45-b7a0-7d70f003d2ac";

    private final String ORDER_ID =
            "98f7b33a-d62a-420a-a84a-05a27c85fc91";

    private Order order1;
    private Order order2;

    @BeforeEach
    void initOrders() {

        CartProduct product = CartProduct.builder()
                .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
                .productName("Dog Food")
                .productSalePrice(50.0)
                .quantityInCart(2)
                .build();

        order1 = Order.builder()
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .customerName("Test Customer")
                .products(List.of(product))
                .subtotal(100.0)
                .tvq(9.98)
                .tvc(5.0)
                .total(114.98)
                .orderDate(LocalDateTime.now())
                .build();

        order2 = Order.builder()
                .orderId("d819e4f4-25af-4d33-91e9-2c45f0071606")
                .customerId(CUSTOMER_ID)
                .products(List.of(product))
                .total(114.98)
                .orderDate(LocalDateTime.now().minusDays(5))
                .build();

        StepVerifier.create(
                        orderRepository.deleteAll()
                                .thenMany(Flux.just(order1, order2))
                                .flatMap(orderRepository::save)
                )
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getOrderByOrderId_ExistingOrder_ReturnsOrder() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/" + ORDER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(response -> {
                    assertEquals(ORDER_ID, response.getOrderId());
                    assertEquals(CUSTOMER_ID, response.getCustomerId());
                    assertEquals(114.98, response.getTotal(), 0.001);
                    assertEquals(1, response.getProducts().size());
                });
    }

    @Test
    void getOrdersByCustomerId_ReturnsMultipleOrders() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/orders/customer/" + CUSTOMER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponseModel.class)
                .value(orders -> {
                    assertEquals(2, orders.size());
                    assertEquals(ORDER_ID, orders.get(0).getOrderId());
                    assertEquals(CUSTOMER_ID, orders.get(0).getCustomerId());
                    assertEquals(CUSTOMER_ID, orders.get(1).getCustomerId());
                });
    }
}
