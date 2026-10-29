package com.petclinic.bffapigateway.presentationlayer.v1.Cart;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.OrderReportRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.OrderReportResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.OrderResponseDTO;

import com.petclinic.bffapigateway.presentationlayer.v1.OrderControllerV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

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
                    .uri("/api/gateway/orders/customer/{customerId}",
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
                    .uri("/api/gateway/orders/customer/{customerId}",
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
                    .uri("/api/gateway/orders/customer/{customerId}",
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
                    .uri("/api/gateway/orders/customer/{customerId}",
                            customerId)
                    .exchange()
                    .expectStatus().isNotFound();

            verify(cartServiceClient, times(1))
                    .getOrdersByCustomerId(customerId);
        }

        @Test
        void createOrderReport_successReturnsCreatedReport() {
            OrderReportRequestDTO request = OrderReportRequestDTO.builder()
                    .reason("DAMAGED_ITEM")
                    .productIds(List.of("product-123"))
                    .description("The product arrived damaged.")
                    .build();
            OrderReportResponseDTO response = sampleReport("RPT-123");

            when(cartServiceClient.createOrderReport(
                    CUSTOMER_ID,
                    "order-123",
                    request
            )).thenReturn(Mono.just(response));

            client.post()
                    .uri(
                            "/api/gateway/orders/customer/{customerId}/{orderId}/reports",
                            CUSTOMER_ID,
                            "order-123"
                    )
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(OrderReportResponseDTO.class)
                    .value(report -> {
                        assertThat(report.getReportId())
                                .isEqualTo("RPT-123");
                        assertThat(report.getStatus()).isEqualTo("NEW");
                    });

            verify(cartServiceClient).createOrderReport(
                    CUSTOMER_ID,
                    "order-123",
                    request
            );
        }

        @Test
        void getCustomerOrderReports_successReturnsReports() {
            when(cartServiceClient.getCustomerOrderReports(CUSTOMER_ID))
                    .thenReturn(Flux.just(
                            sampleReport("RPT-123"),
                            sampleReport("RPT-456")
                    ));

            client.get()
                    .uri(
                            "/api/gateway/orders/customer/{customerId}/reports",
                            CUSTOMER_ID
                    )
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(OrderReportResponseDTO.class)
                    .value(reports -> {
                        assertThat(reports).hasSize(2);
                        assertThat(reports.get(0).getReportId())
                                .isEqualTo("RPT-123");
                        assertThat(reports.get(1).getReportId())
                                .isEqualTo("RPT-456");
                    });

            verify(cartServiceClient)
                    .getCustomerOrderReports(CUSTOMER_ID);
        }

        private OrderReportResponseDTO sampleReport(String reportId) {
            return OrderReportResponseDTO.builder()
                    .reportId(reportId)
                    .orderId("order-123")
                    .customerId(CUSTOMER_ID)
                    .customerName("Test Customer")
                    .reason("DAMAGED_ITEM")
                    .affectedItems(List.of(
                            OrderReportResponseDTO.AffectedItemDTO.builder()
                                    .productId("product-123")
                                    .productName("Dog Food")
                                    .build()
                    ))
                    .description("The product arrived damaged.")
                    .status("NEW")
                    .submittedAt(LocalDateTime.of(
                            2026, 10, 25, 10, 0
                    ))
                    .build();
        }
    }
