package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.OrderReportResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.OrderResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OrderReportControllerUnitTest {

    private static final String BASE_URL = "/api/v2/gateway/orders";

    @Mock
    private CartServiceClient cartServiceClient;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        OrderReportController controller = new OrderReportController(cartServiceClient);
        client = WebTestClient.bindToController(controller).build();
    }

    private OrderReportResponseDTO sampleReport(String status) {
        return OrderReportResponseDTO.builder()
                .reportId("RPT-123")
                .orderId("order-123")
                .customerName("Test Customer")
                .customerEmail("test@email.com")
                .reason("DAMAGED_ITEM")
                .status(status)
                .build();
    }

    @Test
    void getAllOrderReports_returnsReports() {
        when(cartServiceClient.getAllOrderReports())
                .thenReturn(Flux.just(sampleReport("NEW")));

        client.get()
                .uri(BASE_URL + "/reports")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderReportResponseDTO.class)
                .value(reports -> {
                    assertThat(reports).hasSize(1);
                    assertThat(reports.get(0).getCustomerEmail()).isEqualTo("test@email.com");
                });

        verify(cartServiceClient).getAllOrderReports();
    }

    @Test
    void getOrderByOrderId_returnsOrder() {
        OrderResponseDTO order = new OrderResponseDTO();
        order.setOrderId("order-123");
        order.setEmail("test@email.com");

        when(cartServiceClient.getOrderByOrderId("order-123"))
                .thenReturn(Mono.just(order));

        client.get()
                .uri(BASE_URL + "/order-123")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDTO.class)
                .value(response -> assertThat(response.getOrderId()).isEqualTo("order-123"));

        verify(cartServiceClient).getOrderByOrderId("order-123");
    }
}
