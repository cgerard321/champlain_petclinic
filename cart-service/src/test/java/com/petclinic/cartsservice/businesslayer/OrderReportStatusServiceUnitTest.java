package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportReason;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportRepository;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportStatus;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.presentationlayer.UpdateOrderReportStatusRequestModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReportStatusServiceUnitTest {

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

    private OrderReport reportWithStatus(OrderReportStatus status) {
        return OrderReport.builder()
                .reportId("RPT-123")
                .orderId("order-123")
                .customerId("customer-123")
                .reason(OrderReportReason.DAMAGED_ITEM)
                .description("The package arrived with damaged dog food.")
                .status(status)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    private UpdateOrderReportStatusRequestModel request(
            OrderReportStatus status,
            String note
    ) {
        return UpdateOrderReportStatusRequestModel.builder()
                .status(status)
                .resolutionNote(note)
                .build();
    }

    private void mockFindAndSave(OrderReport report) {
        when(orderReportRepository.findByReportId("RPT-123"))
                .thenReturn(Mono.just(report));
        when(orderReportRepository.save(any(OrderReport.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    void updateReportStatus_inReview_setsStatusWithoutResolution() {
        mockFindAndSave(reportWithStatus(OrderReportStatus.NEW));

        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.IN_REVIEW, "ignored")
                ))
                .assertNext(response -> {
                    assertEquals(OrderReportStatus.IN_REVIEW, response.getStatus());
                    assertNull(response.getResolutionNote());
                    assertNull(response.getResolvedAt());
                })
                .verifyComplete();
    }

    @Test
    void updateReportStatus_resolvedWithNote_savesNoteAndDate() {
        mockFindAndSave(reportWithStatus(OrderReportStatus.IN_REVIEW));

        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.RESOLVED, "  Refund sent  ")
                ))
                .assertNext(response -> {
                    assertEquals(OrderReportStatus.RESOLVED, response.getStatus());
                    assertEquals("Refund sent", response.getResolutionNote());
                    assertNotNull(response.getResolvedAt());
                })
                .verifyComplete();
    }

    @Test
    void updateReportStatus_resolvedWithoutNote_savesDate() {
        mockFindAndSave(reportWithStatus(OrderReportStatus.NEW));

        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.RESOLVED, "   ")
                ))
                .assertNext(response -> {
                    assertEquals(OrderReportStatus.RESOLVED, response.getStatus());
                    assertNull(response.getResolutionNote());
                    assertNotNull(response.getResolvedAt());
                })
                .verifyComplete();
    }

    @Test
    void updateReportStatus_rejectedWithNote_savesNote() {
        mockFindAndSave(reportWithStatus(OrderReportStatus.IN_REVIEW));

        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.REJECTED, "Item was not damaged")
                ))
                .assertNext(response -> {
                    assertEquals(OrderReportStatus.REJECTED, response.getStatus());
                    assertEquals("Item was not damaged", response.getResolutionNote());
                })
                .verifyComplete();
    }

    @Test
    void updateReportStatus_rejectedWithoutNote_throwsInvalidInput() {
        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.REJECTED, null)
                ))
                .expectError(InvalidInputException.class)
                .verify();

        verify(orderReportRepository, never()).findByReportId(any());
    }

    @Test
    void updateReportStatus_backToNew_throwsInvalidInput() {
        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.NEW, null)
                ))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void updateReportStatus_missingStatus_throwsInvalidInput() {
        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(null, null)
                ))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void updateReportStatus_nullRequest_throwsInvalidInput() {
        StepVerifier.create(orderService.updateReportStatus("RPT-123", null))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void updateReportStatus_blankReportId_throwsInvalidInput() {
        StepVerifier.create(orderService.updateReportStatus(
                        " ",
                        request(OrderReportStatus.IN_REVIEW, null)
                ))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void updateReportStatus_noteTooLong_throwsInvalidInput() {
        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.RESOLVED, "a".repeat(501))
                ))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void updateReportStatus_reportNotFound_throwsNotFound() {
        when(orderReportRepository.findByReportId("RPT-123"))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.IN_REVIEW, null)
                ))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateReportStatus_alreadyClosed_throwsConflict() {
        when(orderReportRepository.findByReportId("RPT-123"))
                .thenReturn(Mono.just(reportWithStatus(OrderReportStatus.RESOLVED)));

        StepVerifier.create(orderService.updateReportStatus(
                        "RPT-123",
                        request(OrderReportStatus.REJECTED, "Too late")
                ))
                .expectErrorMatches(error ->
                        error instanceof ResponseStatusException exception
                                && exception.getStatusCode() == HttpStatus.CONFLICT
                )
                .verify();

        verify(orderReportRepository, never()).save(any());
    }
}
