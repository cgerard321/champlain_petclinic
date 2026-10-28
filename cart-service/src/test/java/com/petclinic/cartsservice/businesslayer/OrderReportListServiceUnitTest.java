package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportReason;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportRepository;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportStatus;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReportListServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private OrderReportRepository orderReportRepository;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                customerClient,
                orderReportRepository
        );
    }

    @Test
    void getAllReports_returnsReportsFromRepository() {
        OrderReport report = OrderReport.builder()
                .reportId("RPT-123")
                .orderId("order-123")
                .customerId("customer-123")
                .customerName("Test Customer")
                .customerEmail("test@email.com")
                .reason(OrderReportReason.DAMAGED_ITEM)
                .description("The package arrived with damaged dog food.")
                .status(OrderReportStatus.NEW)
                .submittedAt(LocalDateTime.now())
                .build();

        when(orderReportRepository.findAllByOrderBySubmittedAtDesc())
                .thenReturn(Flux.just(report));

        StepVerifier.create(orderService.getAllReports())
                .assertNext(response -> {
                    assertEquals("RPT-123", response.getReportId());
                    assertEquals("test@email.com", response.getCustomerEmail());
                })
                .verifyComplete();
    }

    @Test
    void getAllReports_noReports_returnsEmpty() {
        when(orderReportRepository.findAllByOrderBySubmittedAtDesc())
                .thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAllReports())
                .verifyComplete();
    }
}
