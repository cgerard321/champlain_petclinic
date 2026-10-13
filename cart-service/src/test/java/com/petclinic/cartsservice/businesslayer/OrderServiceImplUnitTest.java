
package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.domainclientlayer.CustomerResponseModel;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.presentationlayer.OrderResponseModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private final String customerId = "customer-123";
    private final String orderId = "order-123";

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
                .customerId(customerId)
                .products(List.of(product1, product2))
                .subtotal(125.0)
                .tvq(12.47)
                .tvc(6.25)
                .total(143.72)
                .build();

        order = Order.builder()
                .orderId(orderId)
                .customerId(customerId)
                .customerName("Test Customer")
                .customerAddress("123 Test Street")
                .products(List.of(product1, product2))
                .subtotal(125.0)
                .tvq(12.47)
                .tvc(6.25)
                .total(143.72)
                .orderDate(LocalDateTime.now())
                .build();
    }


    @Test
    void createOrder_ValidCart_ReturnsOrderResponseModel() {
        // Arrange
        CustomerResponseModel customer = mock(CustomerResponseModel.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(customer.getFullName()).thenReturn("Test Customer");
        when(customer.getAddress()).thenReturn("123 Test Street");

        when(customerClient.getCustomerById(customerId))
                .thenReturn(Mono.just(customer));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .assertNext(response -> {
                    assertNotNull(response.getOrderId());
                    assertEquals(customerId, response.getCustomerId());
                    assertEquals("Test Customer", response.getCustomerName());
                    assertEquals("123 Test Street", response.getCustomerAddress());
                    assertEquals(2, response.getProducts().size());
                    assertNotNull(response.getOrderDate());
                })
                .verifyComplete();

        verify(customerClient, times(1)).getCustomerById(customerId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_ValidCart_SavesCorrectFinancialValues() {
        // Arrange
        CustomerResponseModel customer = mock(CustomerResponseModel.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(customer.getFullName()).thenReturn("Test Customer");
        when(customer.getAddress()).thenReturn("123 Test Street");

        when(customerClient.getCustomerById(customerId))
                .thenReturn(Mono.just(customer));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .assertNext(response -> {
                    assertEquals(125.0, response.getSubtotal(), 0.001);
                    assertEquals(12.47, response.getTvq(), 0.001);
                    assertEquals(6.25, response.getTvc(), 0.001);
                    assertEquals(143.72, response.getTotal(), 0.001);
                })
                .verifyComplete();

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_CustomerNotFound_ReturnsEmpty() {
        // Arrange
        when(customerClient.getCustomerById(customerId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .expectNextCount(0)
                .verifyComplete();

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_RepositoryError_ThrowsException() {
        // Arrange
        CustomerResponseModel customer = mock(CustomerResponseModel.class);

        when(customer.getCustomerId()).thenReturn(customerId);
        when(customer.getFullName()).thenReturn("Test Customer");
        when(customer.getAddress()).thenReturn("123 Test Street");

        when(customerClient.getCustomerById(customerId))
                .thenReturn(Mono.just(customer));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(Mono.error(
                        new RuntimeException("Database error")
                ));

        // Act & Assert
        StepVerifier.create(orderService.createOrder(cart))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database error"))
                .verify();

        verify(orderRepository, times(1)).save(any(Order.class));
    }


    @Test
    void getOrderByOrderId_ExistingOrder_ReturnsOrderResponseModel() {
        // Arrange
        when(orderRepository.findOrderByOrderId(orderId))
                .thenReturn(Mono.just(order));

        // Act & Assert
        StepVerifier.create(orderService.getOrderByOrderId(orderId))
                .expectNextMatches(response ->
                        response.getOrderId().equals(orderId) &&
                                response.getCustomerId().equals(customerId) &&
                                response.getProducts().size() == 2 &&
                                response.getTotal() == 143.72)
                .verifyComplete();

        verify(orderRepository, times(1)).findOrderByOrderId(orderId);
    }

    @Test
    void getOrderByOrderId_NonExistentOrder_ThrowsNotFound() {
        // Arrange
        when(orderRepository.findOrderByOrderId(orderId))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(orderService.getOrderByOrderId(orderId))
                .expectErrorMatches(error ->
                        error instanceof ResponseStatusException &&
                                ((ResponseStatusException) error)
                                        .getStatusCode().value() == 404)
                .verify();

        verify(orderRepository, times(1)).findOrderByOrderId(orderId);
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
                        response.getOrderId().equals(orderId))
                .expectNextMatches(response ->
                        response.getOrderId().equals("order-456"))
                .verifyComplete();

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getAllOrders_NoOrders_ReturnsEmptyList() {
        // Arrange
        when(orderRepository.findAll())
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(orderService.getAllOrders())
                .expectNextCount(0)
                .verifyComplete();

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void findOrdersByCustomerId_ExistingCustomer_ReturnsOrders() {
        // Arrange
        Order olderOrder = Order.builder()
                .orderId("older-order")
                .customerId(customerId)
                .products(List.of(product1))
                .total(100.0)
                .orderDate(LocalDateTime.now().minusDays(5))
                .build();

        when(orderRepository.findAllByCustomerId(customerId))
                .thenReturn(Flux.just(olderOrder, order));

        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(customerId))
                .expectNextMatches(response ->
                        response.getOrderId().equals(orderId))
                .expectNextMatches(response ->
                        response.getOrderId().equals("older-order"))
                .verifyComplete();

        verify(orderRepository, times(1)).findAllByCustomerId(customerId);
    }

    @Test
    void findOrdersByCustomerId_NoOrders_ReturnsEmptyList() {
        // Arrange
        when(orderRepository.findAllByCustomerId(customerId))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(customerId))
                .expectNextCount(0)
                .verifyComplete();

        verify(orderRepository, times(1)).findAllByCustomerId(customerId);
    }

    @Test
    void findOrdersByCustomerId_NullCustomerId_ThrowsInvalidInput() {
        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(null))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals(
                                        "customerId must not be null or empty"))
                .verify();

        verifyNoInteractions(orderRepository);
    }

    @Test
    void findOrdersByCustomerId_BlankCustomerId_ThrowsInvalidInput() {
        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId("   "))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException &&
                                error.getMessage().equals(
                                        "customerId must not be null or empty"))
                .verify();

        verifyNoInteractions(orderRepository);
    }

    @Test
    void findOrdersByCustomerId_RepositoryError_ThrowsException() {
        // Arrange
        when(orderRepository.findAllByCustomerId(customerId))
                .thenReturn(Flux.error(
                        new RuntimeException("Database error")
                ));

        // Act & Assert
        StepVerifier.create(orderService.findOrdersByCustomerId(customerId))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().equals("Database error"))
                .verify();

        verify(orderRepository, times(1)).findAllByCustomerId(customerId);
    }


    @Test
    void toOrder_ValidOrderResponse_ReturnsOrder() {
        // Arrange
        OrderResponseModel response = OrderResponseModel.builder()
                .orderId(orderId)
                .customerId(customerId)
                .customerName("Test Customer")
                .customerAddress("123 Test Street")
                .products(List.of(product1, product2))
                .subtotal(125.0)
                .tvq(12.47)
                .tvc(6.25)
                .total(143.72)
                .orderDate(LocalDateTime.now())
                .build();

        // Act
        Order result = orderService.toOrder(response);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals("Test Customer", result.getCustomerName());
        assertEquals("123 Test Street", result.getCustomerAddress());
        assertEquals(2, result.getProducts().size());
        assertEquals(125.0, result.getSubtotal(), 0.001);
        assertEquals(12.47, result.getTvq(), 0.001);
        assertEquals(6.25, result.getTvc(), 0.001);
        assertEquals(143.72, result.getTotal(), 0.001);
    }


    @Test
    void storeCartOrder_ValidCart_PreservesOriginalProductsAndTotals() {
        // Arrange
        cart.setPromoPercent(10.0);

        int originalQuantity =
                cart.getProducts().get(0).getQuantityInCart();

        // Act
        Cart orderCart = orderService.storeCartOrder(cart);

        // Assert
        assertNotNull(orderCart);

        assertNotSame(cart, orderCart);
        assertEquals(cart.getCustomerId(), orderCart.getCustomerId());
        assertEquals(cart.getPromoPercent(), orderCart.getPromoPercent());

        assertEquals(2, orderCart.getProducts().size());
        assertNotSame(cart.getProducts(), orderCart.getProducts());
        assertNotSame(cart.getProducts().get(0), orderCart.getProducts().get(0));
        assertEquals(cart.getProducts().get(0).getProductId(), orderCart.getProducts().get(0).getProductId());
        assertEquals(originalQuantity, orderCart.getProducts().get(0).getQuantityInCart());
        assertEquals(125.0, orderCart.getSubtotal(), 0.001);
        assertEquals(11.22, orderCart.getTvq(), 0.001);
        assertEquals(5.63, orderCart.getTvc(), 0.001);
        assertEquals(129.35, orderCart.getTotal(), 0.001);

        cart.getProducts().get(0).setQuantityInCart(5);

        assertEquals(originalQuantity, orderCart.getProducts().get(0).getQuantityInCart());
        verifyNoInteractions(orderRepository);
        verifyNoInteractions(customerClient);
    }

}
