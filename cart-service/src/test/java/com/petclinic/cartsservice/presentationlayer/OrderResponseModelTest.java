
package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderResponseModelUnitTest {

    private final String orderId = "order-123";
    private final String customerId = "customer-123";

    private final CartProduct product1 = CartProduct.builder()
            .productId("product-123")
            .productName("Dog Food")
            .productSalePrice(50.0)
            .quantityInCart(2)
            .build();


    @Test
    void orderResponseModel_Builder_ReturnsCorrectValues() {
        // Arrange
        LocalDateTime orderDate = LocalDateTime.now();

        // Act
        OrderResponseModel response = OrderResponseModel.builder()
                .orderId(orderId)
                .customerId(customerId)
                .customerName("Test Customer")
                .address("123 Test Street")
                .orderDate(orderDate)
                .products(List.of(product1))
                .subtotal(100.0)
                .promoPercent(10.0)
                .tvq(8.98)
                .tvc(4.50)
                .total(103.48)
                .invoiceId("invoice-123")
                .build();

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals("Test Customer", response.getCustomerName());
        assertEquals("123 Test Street", response.getAddress());
        assertEquals(orderDate, response.getOrderDate());
        assertEquals(1, response.getProducts().size());
        assertEquals(100.0, response.getSubtotal(), 0.001);
        assertEquals(10.0, response.getPromoPercent(), 0.001);
        assertEquals(8.98, response.getTvq(), 0.001);
        assertEquals(4.50, response.getTvc(), 0.001);
        assertEquals(103.48, response.getTotal(), 0.001);
        assertEquals("invoice-123", response.getInvoiceId());
    }


    @Test
    void orderResponseModel_NoArgsConstructor_ReturnsEmptyModel() {
        // Act
        OrderResponseModel response = new OrderResponseModel();

        // Assert
        assertNotNull(response);
        assertNull(response.getOrderId());
        assertNull(response.getCustomerId());
        assertNull(response.getProducts());
        assertNull(response.getInvoiceId());
        assertEquals(0.0, response.getSubtotal());
        assertEquals(0.0, response.getTotal());
    }

    @Test
    void orderResponseModel_AllArgsConstructor_ReturnsCorrectValues() {
        // Arrange
        LocalDateTime orderDate = LocalDateTime.now();

        // Act
        OrderResponseModel response = new OrderResponseModel(
                orderId,
                customerId,
                "Test Customer",
                "123 Test Street",
                orderDate,
                List.of(product1),
                100.0,
                10.0,
                8.98,
                4.50,
                103.48,
                "invoice-123"
        );

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(orderDate, response.getOrderDate());
        assertEquals(1, response.getProducts().size());
        assertEquals(103.48, response.getTotal(), 0.001);
        assertEquals("invoice-123", response.getInvoiceId());
    }

    @Test
    void orderResponseModel_CustomConstructor_ReturnsCorrectValues() {
        // Act
        OrderResponseModel response = new OrderResponseModel(
                "invoice-123",
                orderId,
                List.of(product1),
                114.98
        );

        // Assert
        assertEquals("invoice-123", response.getInvoiceId());
        assertEquals(orderId, response.getOrderId());
        assertEquals(1, response.getProducts().size());
        assertEquals(114.98, response.getTotal(), 0.001);
    }


    @Test
    void orderResponseModel_Setters_UpdateValues() {
        // Arrange
        OrderResponseModel response = new OrderResponseModel();
        LocalDateTime orderDate = LocalDateTime.now();

        // Act
        response.setOrderId(orderId);
        response.setCustomerId(customerId);
        response.setCustomerName("Test Customer");
        response.setAddress("123 Test Street");
        response.setOrderDate(orderDate);
        response.setProducts(List.of(product1));
        response.setSubtotal(100.0);
        response.setPromoPercent(10.0);
        response.setTvq(8.98);
        response.setTvc(4.50);
        response.setTotal(103.48);
        response.setInvoiceId("invoice-123");

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals("Test Customer", response.getCustomerName());
        assertEquals("123 Test Street", response.getAddress());
        assertEquals(orderDate, response.getOrderDate());
        assertEquals(1, response.getProducts().size());
        assertEquals(100.0, response.getSubtotal(), 0.001);
        assertEquals(10.0, response.getPromoPercent(), 0.001);
        assertEquals(8.98, response.getTvq(), 0.001);
        assertEquals(4.50, response.getTvc(), 0.001);
        assertEquals(103.48, response.getTotal(), 0.001);
        assertEquals("invoice-123", response.getInvoiceId());
    }


    @Test
    void orderResponseModel_WithMultipleProducts_ReturnsProducts() {
        // Arrange
        CartProduct product2 = CartProduct.builder()
                .productId("product-456")
                .productName("Cat Toy")
                .productSalePrice(25.0)
                .quantityInCart(3)
                .build();

        // Act
        OrderResponseModel response = OrderResponseModel.builder()
                .orderId(orderId)
                .products(List.of(product1, product2))
                .build();

        // Assert
        assertEquals(2, response.getProducts().size());
        assertEquals("Dog Food",
                response.getProducts().get(0).getProductName());
        assertEquals(2,
                response.getProducts().get(0).getQuantityInCart());
        assertEquals("Cat Toy",
                response.getProducts().get(1).getProductName());
        assertEquals(3,
                response.getProducts().get(1).getQuantityInCart());
    }

    // NULL VALUES TEST

    @Test
    void orderResponseModel_NullOptionalFields_ReturnsNull() {
        // Act
        OrderResponseModel response = OrderResponseModel.builder()
                .orderId(orderId)
                .promoPercent(null)
                .invoiceId(null)
                .build();

        // Assert
        assertEquals(orderId, response.getOrderId());
        assertNull(response.getPromoPercent());
        assertNull(response.getInvoiceId());
    }


    @Test
    void orderResponseModel_SameValues_AreEqual() {
        // Arrange
        OrderResponseModel response1 = OrderResponseModel.builder()
                .orderId(orderId)
                .customerId(customerId)
                .total(114.98)
                .build();

        OrderResponseModel response2 = OrderResponseModel.builder()
                .orderId(orderId)
                .customerId(customerId)
                .total(114.98)
                .build();

        // Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
}
