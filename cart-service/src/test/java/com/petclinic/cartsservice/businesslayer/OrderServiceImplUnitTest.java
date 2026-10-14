
package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.domainclientlayer.CustomerResponseModel;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private CustomerClient customerClient;

    private Cart cart;
    private Order order;
    private CartProduct product1;
    private CartProduct product2;

    private static final String CUSTOMER_ID = "customer-123";
    private static final String ORDER_ID = "order-123";
    private static final String CUSTOMER_NAME = "Test Customer";
    private static final String CUSTOMER_ADDRESS = "123 Test Street";

    @BeforeEach
    void init() {

        product1 = CartProduct.builder()
                .productId("product-123")
                .productName("Dog Food")
                .productSalePrice(50.0)
                .quantityInCart(2)
                .productQuantity(10)
                .build();

        product2 = CartProduct.builder()
                .productId("product-456")
                .productName("Cat Toy")
                .productSalePrice(25.0)
                .quantityInCart(1)
                .productQuantity(5)
                .build();

        cart = Cart.builder()
                .cartId("cart-123")
                .customerId(CUSTOMER_ID)
                .products(List.of(product1, product2))
                .subtotal(125.0)
                .tvq(12.47)
                .tvc(6.25)
                .total(143.72)
                .build();

        order = Order.builder()
                .orderId(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .customerName(CUSTOMER_NAME)
                .customerAddress(CUSTOMER_ADDRESS)
                .products(List.of(product1, product2))
                .subtotal(125.0)
                .tvq(12.47)
                .tvc(6.25)
                .total(143.72)
                .orderDate(LocalDateTime.of(2026, 1, 15, 12, 0))
                .build();
    }

    // =========================================================
    // CREATE ORDER TESTS
    // =========================================================

    @Test
    void createOrder_ValidCart_ReturnsOrderResponseModel() {
        // Arrange
        CustomerResponseModel customer = mockCustomer();

        when(customerClient.getCustomerById(CUSTOMER_ID))
                .thenReturn(Mono.just(customer));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0)));

        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .assertNext(response -> {
                    assertNotNull(response.getOrderId());
                    assertEquals(CUSTOMER_ID, response.getCustomerId());
                    assertEquals(CUSTOMER_NAME, response.getCustomerName());
                    assertEquals(CUSTOMER_ADDRESS, response.getCustomerAddress());
                    assertEquals(2, response.getProducts().size());
                    assertNotNull(response.getOrderDate());
                })
                .verifyComplete();

        // Verify saved order
        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(CUSTOMER_ID, savedOrder.getCustomerId());
        assertEquals(CUSTOMER_NAME, savedOrder.getCustomerName());
        assertEquals(CUSTOMER_ADDRESS, savedOrder.getCustomerAddress());
        assertEquals(2, savedOrder.getProducts().size());
        verify(customerClient).getCustomerById(CUSTOMER_ID);
    }

    @Test
    void createOrder_ValidCart_SavesCorrectFinancialValues() {
        // Arrange
        CustomerResponseModel customer = mockCustomer();

        when(customerClient.getCustomerById(CUSTOMER_ID)).thenReturn(Mono.just(customer));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0)));

        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .assertNext(response -> {
                    assertEquals(125.0, response.getSubtotal(), 0.001);
                    assertEquals(12.47, response.getTvq(), 0.001);
                    assertEquals(6.25, response.getTvc(), 0.001);
                    assertEquals(143.72, response.getTotal(), 0.001);
                }).verifyComplete();

        // Verify financial values saved in repository
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertEquals(125.0, savedOrder.getSubtotal(), 0.001);
        assertEquals(12.47, savedOrder.getTvq(), 0.001);
        assertEquals(6.25, savedOrder.getTvc(), 0.001);
        assertEquals(143.72, savedOrder.getTotal(), 0.001);
    }

    @Test
    void createOrder_CustomerNotFound_ThrowsNotFoundException() {
        // Arrange
        when(customerClient.getCustomerById(CUSTOMER_ID)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .expectErrorMatches(error ->
                        error instanceof NotFoundException &&
                                error.getMessage().equals("Customer not found")).verify();

        verify(customerClient).getCustomerById(CUSTOMER_ID);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_RepositoryError_ThrowsException() {
        // Arrange
        CustomerResponseModel customer = mockCustomer();

        when(customerClient.getCustomerById(CUSTOMER_ID)).thenReturn(Mono.just(customer));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));
        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database error")).verify();

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderByOrderId_ExistingOrder_ReturnsOrderResponseModel() {
        // Arrange
        when(orderRepository.findOrderByOrderId(ORDER_ID))
                .thenReturn(Mono.just(order));

        // Act & Assert
        StepVerifier.create(orderService.getOrderByOrderId(ORDER_ID))
                .assertNext(response -> {
                    assertEquals(ORDER_ID, response.getOrderId());
                    assertEquals(CUSTOMER_ID, response.getCustomerId());
                    assertEquals(2, response.getProducts().size());
                    assertEquals(143.72, response.getTotal(), 0.001);
                })
                .verifyComplete();

        verify(orderRepository).findOrderByOrderId(ORDER_ID);
    }

    @Test
    void getOrderByOrderId_NonExistentOrder_ThrowsNotFound() {
        // Arrange
        when(orderRepository.findOrderByOrderId(ORDER_ID)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderService.getOrderByOrderId(ORDER_ID))
                .expectErrorMatches(error ->
                        error instanceof ResponseStatusException &&
                                ((ResponseStatusException) error)
                                        .getStatusCode().value() == 404).verify();
        verify(orderRepository).findOrderByOrderId(ORDER_ID);
    }


    @Test
    void getAllOrders_ReturnsMultipleOrders() {
        // Arrange
        Order secondOrder = Order.builder()
                .orderId("order-456")
                .customerId("customer-456")
                .products(List.of(product1))
                .total(100.0)
                .build();

        when(orderRepository.findAll())
                .thenReturn(Flux.just(order, secondOrder));

        // Act & Assert
        StepVerifier.create(orderService.getAllOrders())
                .expectNextMatches(response ->
                        response.getOrderId().equals(ORDER_ID))
                .expectNextMatches(response ->
                        response.getOrderId().equals("order-456"))
                .verifyComplete();

        verify(orderRepository).findAll();
    }

    @Test
    void getAllOrders_NoOrders_ReturnsEmptyList() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(orderService.getAllOrders()).verifyComplete();
        verify(orderRepository).findAll();
    }



    @Test
    void findOrdersByCustomerId_ExistingCustomer_ReturnsOrders() {
        // Arrange
        Order olderOrder = Order.builder()
                .orderId("older-order")
                .customerId(CUSTOMER_ID)
                .products(List.of(product1))
                .total(100.0)
                .orderDate(LocalDateTime.of(2026, 1, 10, 12, 0))
                .build();

        when(orderRepository.findAllByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.just(olderOrder, order));

        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(CUSTOMER_ID))
                .expectNextMatches(response ->
                        response.getOrderId().equals(ORDER_ID))
                .expectNextMatches(response ->
                        response.getOrderId().equals("older-order")).verifyComplete();

        verify(orderRepository).findAllByCustomerId(CUSTOMER_ID);
    }

    @Test
    void findOrdersByCustomerId_NoOrders_ReturnsEmptyList() {
        // Arrange
        when(orderRepository.findAllByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(CUSTOMER_ID))
                .verifyComplete();

        verify(orderRepository).findAllByCustomerId(CUSTOMER_ID);
    }

    @Test
    void findOrdersByCustomerId_NullCustomerId_ThrowsInvalidInput() {
        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(null))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals(
                                        "customerId must not be null or empty")).verify();

        verifyNoInteractions(orderRepository);
    }

    @Test
    void findOrdersByCustomerId_BlankCustomerId_ThrowsInvalidInput() {
        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId("   "))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals(
                                        "customerId must not be null or empty")).verify();

        verifyNoInteractions(orderRepository);
    }

    @Test
    void findOrdersByCustomerId_RepositoryError_ThrowsException() {
        // Arrange
        when(orderRepository.findAllByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.error(
                        new RuntimeException("Database error")
                ));

        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(CUSTOMER_ID))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database error"))
                .verify();

        verify(orderRepository).findAllByCustomerId(CUSTOMER_ID);
    }


    private CustomerResponseModel mockCustomer() {
        CustomerResponseModel customer = mock(CustomerResponseModel.class);

        lenient().when(customer.getCustomerId())
                .thenReturn(CUSTOMER_ID);

        lenient().when(customer.getFullName())
                .thenReturn(CUSTOMER_NAME);

        lenient().when(customer.getAddress())
                .thenReturn(CUSTOMER_ADDRESS);

        return customer;
    }
}
