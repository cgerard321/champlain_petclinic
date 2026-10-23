package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportRepository;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.presentationlayer.CreateOrderReportRequestModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReportServiceUnitTest {

    private static final String CUSTOMER_ID = "customer-123";
    private static final String ORDER_ID = "order-123";
    private static final String PRODUCT_ID = "product-123";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private OrderReportRepository orderReportRepository;

    private OrderServiceImpl orderService;
    private Order order;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                customerClient,
                orderReportRepository
        );

        CartProduct product = CartProduct.builder()
                .productId(PRODUCT_ID)
                .productName("Dog Food")
                .quantityInCart(2)
                .build();

        order = Order.builder()
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .customerName("Test Customer")
                .products(List.of(product))
                .build();
    }

    @Test
    void createOrderReport_validRequest_savesNewReport() {
        CreateOrderReportRequestModel request =
                CreateOrderReportRequestModel.builder()
                        .reason(OrderReport.Reason.DAMAGED_ITEM)
                        .productIds(List.of(PRODUCT_ID))
                        .description(
                                "The package arrived with damaged dog food."
                        )
                        .build();

        when(orderRepository.findByOrderIdAndCustomerId(
                ORDER_ID,
                CUSTOMER_ID
        )).thenReturn(Mono.just(order));

        when(orderReportRepository.existsByOrderIdAndStatusIn(
                eq(ORDER_ID),
                anyCollection()
        )).thenReturn(Mono.just(false));

        when(orderReportRepository.save(any(OrderReport.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        StepVerifier.create(orderService.createOrderReport(
                        CUSTOMER_ID,
                        ORDER_ID,
                        request
                ))
                .assertNext(response -> {
                    assertNotNull(response.getReportId());
                    assertTrue(
                            response.getReportId().startsWith("RPT-")
                    );
                    assertEquals(
                            OrderReport.Status.NEW,
                            response.getStatus()
                    );
                    assertEquals(ORDER_ID, response.getOrderId());
                    assertEquals(CUSTOMER_ID, response.getCustomerId());
                    assertEquals(1, response.getAffectedItems().size());
                    assertNotNull(response.getSubmittedAt());
                })
                .verifyComplete();

        ArgumentCaptor<OrderReport> captor =
                ArgumentCaptor.forClass(OrderReport.class);

        verify(orderReportRepository).save(captor.capture());

        assertEquals(
                "Test Customer",
                captor.getValue().getCustomerName()
        );
        assertEquals(
                OrderReport.Status.NEW,
                captor.getValue().getStatus()
        );
    }

    @Test
    void createOrderReport_orderDoesNotBelongToCustomer_returnsNotFound() {
        when(orderRepository.findByOrderIdAndCustomerId(
                ORDER_ID,
                CUSTOMER_ID
        )).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrderReport(
                        CUSTOMER_ID,
                        ORDER_ID,
                        validOtherRequest()
                ))
                .expectError(NotFoundException.class)
                .verify();

        verifyNoInteractions(orderReportRepository);
    }

    @Test
    void createOrderReport_openReportExists_returnsConflict() {
        when(orderRepository.findByOrderIdAndCustomerId(
                ORDER_ID,
                CUSTOMER_ID
        )).thenReturn(Mono.just(order));

        when(orderReportRepository.existsByOrderIdAndStatusIn(
                eq(ORDER_ID),
                anyCollection()
        )).thenReturn(Mono.just(true));

        StepVerifier.create(orderService.createOrderReport(
                        CUSTOMER_ID,
                        ORDER_ID,
                        validOtherRequest()
                ))
                .expectErrorMatches(error ->
                        error instanceof ResponseStatusException
                                && ((ResponseStatusException) error)
                                .getStatusCode()
                                .equals(HttpStatus.CONFLICT)
                )
                .verify();

        verify(orderReportRepository, never())
                .save(any(OrderReport.class));
    }

    @Test
    void createOrderReport_itemReasonWithoutProduct_returnsInvalidInput() {
        CreateOrderReportRequestModel request =
                CreateOrderReportRequestModel.builder()
                        .reason(OrderReport.Reason.MISSING_ITEM)
                        .productIds(List.of())
                        .description(
                                "An item was missing from my package."
                        )
                        .build();

        StepVerifier.create(orderService.createOrderReport(
                        CUSTOMER_ID,
                        ORDER_ID,
                        request
                ))
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(orderReportRepository);
    }

    @Test
    void createOrderReport_productNotInOrder_returnsInvalidInput() {
        CreateOrderReportRequestModel request =
                CreateOrderReportRequestModel.builder()
                        .reason(OrderReport.Reason.WRONG_ITEM)
                        .productIds(List.of("another-product"))
                        .description(
                                "The wrong product was included in the package."
                        )
                        .build();

        when(orderRepository.findByOrderIdAndCustomerId(
                ORDER_ID,
                CUSTOMER_ID
        )).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.createOrderReport(
                        CUSTOMER_ID,
                        ORDER_ID,
                        request
                ))
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(orderReportRepository);
    }

    @Test
    void createOrderReport_descriptionTooShort_returnsInvalidInput() {
        CreateOrderReportRequestModel request =
                CreateOrderReportRequestModel.builder()
                        .reason(OrderReport.Reason.OTHER)
                        .description("Too short")
                        .build();

        StepVerifier.create(orderService.createOrderReport(
                        CUSTOMER_ID,
                        ORDER_ID,
                        request
                ))
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrderReport_packageNotReceived_allowsNoProducts() {
        CreateOrderReportRequestModel request =
                CreateOrderReportRequestModel.builder()
                        .reason(
                                OrderReport.Reason.PACKAGE_NOT_RECEIVED
                        )
                        .productIds(List.of())
                        .description(
                                "The complete package has not been delivered."
                        )
                        .build();

        when(orderRepository.findByOrderIdAndCustomerId(
                ORDER_ID,
                CUSTOMER_ID
        )).thenReturn(Mono.just(order));

        when(orderReportRepository.existsByOrderIdAndStatusIn(
                eq(ORDER_ID),
                anyCollection()
        )).thenReturn(Mono.just(false));

        when(orderReportRepository.save(any(OrderReport.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        StepVerifier.create(orderService.createOrderReport(
                        CUSTOMER_ID,
                        ORDER_ID,
                        request
                ))
                .assertNext(response ->
                        assertTrue(response.getAffectedItems().isEmpty())
                )
                .verifyComplete();
    }

    private CreateOrderReportRequestModel validOtherRequest() {
        return CreateOrderReportRequestModel.builder()
                .reason(OrderReport.Reason.OTHER)
                .productIds(List.of())
                .description(
                        "I need help with a problem related to this order."
                )
                .build();
    }
}
