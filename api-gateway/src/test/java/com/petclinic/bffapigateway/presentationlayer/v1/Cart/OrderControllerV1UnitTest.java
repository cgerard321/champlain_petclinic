package com.petclinic.bffapigateway.presentationlayer.v1.Cart;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.OrderResponseDTO;

import com.petclinic.bffapigateway.presentationlayer.v1.OrderControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


class OrderControllerV1UnitTest {
  


        @Mock
        private CartServiceClient cartServiceClient;

        private WebTestClient client;

        private final String CUSTOMER_ID = "customer-123";

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);

            OrderControllerV1 controller =
                    new OrderControllerV1(cartServiceClient);

            client = WebTestClient.bindToController(controller).build();
        }

        private OrderResponseDTO sampleOrder(String orderId) {
            OrderResponseDTO response = new OrderResponseDTO();

            response.setOrderId(orderId);
            response.setCustomerId(CUSTOMER_ID);
            response.setCustomerName("Test Customer");
            response.setSubtotal(100.0);
            response.setTvq(9.98);
            response.setTvc(5.0);
            response.setTotal(114.98);
            response.setOrderDate(LocalDateTime.of(
                    2026, 10, 11, 14, 30
            ));

            return response;
        }


        @Test
        void getOrdersByCustomerId_Success_ReturnsOrders() {
            // Arrange
            OrderResponseDTO order1 = sampleOrder("order-123");
            OrderResponseDTO order2 = sampleOrder("order-456");

            when(cartServiceClient.getOrdersByCustomerId(CUSTOMER_ID))
                    .thenReturn(Flux.just(order1, order2));

            // Act & Assert
            client.get()
                    .uri("/api/v1/gateway/orders/customer/{customerId}",
                            CUSTOMER_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(OrderResponseDTO.class)
                    .value(orders -> {
                        assertThat(orders).hasSize(2);

                        assertThat(orders.get(0).getOrderId())
                                .isEqualTo("order-123");

                        assertThat(orders.get(1).getOrderId())
                                .isEqualTo("order-456");

                        assertThat(orders.get(0).getCustomerId())
                                .isEqualTo(CUSTOMER_ID);

                        assertThat(orders.get(0).getTotal())
                                .isEqualTo(114.98);
                    });

            verify(cartServiceClient, times(1))
                    .getOrdersByCustomerId(CUSTOMER_ID);
        }

        @Test
        void getOrdersByCustomerId_NoOrders_ReturnsEmptyList() {
            // Arrange
            when(cartServiceClient.getOrdersByCustomerId(CUSTOMER_ID))
                    .thenReturn(Flux.empty());

            // Act & Assert
            client.get()
                    .uri("/api/v1/gateway/orders/customer/{customerId}",
                            CUSTOMER_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(OrderResponseDTO.class)
                    .value(orders ->
                            assertThat(orders).isEmpty()
                    );

            verify(cartServiceClient, times(1))
                    .getOrdersByCustomerId(CUSTOMER_ID);
        }

        @Test
        void getOrdersByCustomerId_ServiceError_ReturnsServerError() {
            // Arrange
            when(cartServiceClient.getOrdersByCustomerId(CUSTOMER_ID))
                    .thenReturn(Flux.error(
                            new RuntimeException("Database error")
                    ));

            // Act & Assert
            client.get()
                    .uri("/api/v1/gateway/orders/customer/{customerId}",
                            CUSTOMER_ID)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(cartServiceClient, times(1))
                    .getOrdersByCustomerId(CUSTOMER_ID);
        }

        @Test
        void getOrdersByCustomerId_NotFound_Returns404() {
            // Arrange
            String customerId = "customer-missing";

            when(cartServiceClient.getOrdersByCustomerId(customerId))
                    .thenReturn(Flux.error(
                            new org.springframework.web.server.ResponseStatusException(
                                    org.springframework.http.HttpStatus.NOT_FOUND,
                                    "Customer not found"
                            )
                    ));

            // Act & Assert
            client.get()
                    .uri("/api/v1/gateway/orders/customer/{customerId}",
                            customerId)
                    .exchange()
                    .expectStatus().isNotFound();

            verify(cartServiceClient, times(1))
                    .getOrdersByCustomerId(customerId);
        }
    }
