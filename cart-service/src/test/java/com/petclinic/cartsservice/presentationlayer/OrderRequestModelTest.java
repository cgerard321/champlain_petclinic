
package com.petclinic.cartsservice.presentationlayer;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRequestModelUnitTest {

    private final String customerId =
            "f470653d-05c5-4c45-b7a0-7d70f003d2ac";

    private final String productId1 =
            "9a29fff7-564a-4cc9-8fe1-36f6ca9bc223";

    private final String productId2 =
            "d819e4f4-25af-4d33-91e9-2c45f0071606";


    @Test
    void orderRequestModel_Builder_ReturnsCorrectValues() {
        // Arrange
        LocalDateTime orderDate = LocalDateTime.now();
        List<String> productIds =
                Arrays.asList(productId1, productId2);

        // Act
        OrderRequestModel request = OrderRequestModel.builder()
                .customerId(customerId)
                .productIds(productIds)
                .orderDate(orderDate)
                .build();

        // Assert
        assertEquals(customerId, request.getCustomerId());
        assertEquals(2, request.getProductIds().size());
        assertEquals(productIds, request.getProductIds());
        assertEquals(orderDate, request.getOrderDate());
    }

    @Test
    void orderRequestModel_NoArgsConstructor_ReturnsEmptyModel() {
        // Act
        OrderRequestModel request = new OrderRequestModel();

        // Assert
        assertNotNull(request);
        assertNull(request.getCustomerId());
        assertNull(request.getProductIds());
        assertNull(request.getOrderDate());
    }

    @Test
    void orderRequestModel_AllArgsConstructor_ReturnsCorrectValues() {
        // Arrange
        LocalDateTime orderDate = LocalDateTime.now();

        // Act
        OrderRequestModel request = new OrderRequestModel(
                customerId,
                List.of(productId1, productId2),
                orderDate
        );

        // Assert
        assertEquals(customerId, request.getCustomerId());
        assertEquals(2, request.getProductIds().size());
        assertEquals(orderDate, request.getOrderDate());
    }


    @Test
    void orderRequestModel_Setters_UpdateValues() {
        // Arrange
        OrderRequestModel request = new OrderRequestModel();
        LocalDateTime orderDate = LocalDateTime.now();

        // Act
        request.setCustomerId(customerId);
        request.setProductIds(List.of(productId1, productId2));
        request.setOrderDate(orderDate);

        // Assert
        assertEquals(customerId, request.getCustomerId());
        assertEquals(2, request.getProductIds().size());
        assertEquals(orderDate, request.getOrderDate());
    }


    @Test
    void orderRequestModel_EmptyProductList_ReturnsEmptyList() {
        // Arrange
        OrderRequestModel request = OrderRequestModel.builder()
                .customerId(customerId)
                .productIds(Collections.emptyList())
                .build();

        // Act & Assert
        assertTrue(request.getProductIds().isEmpty());
        assertEquals(customerId, request.getCustomerId());
    }

    @Test
    void orderRequestModel_NullProductList_ReturnsNull() {
        // Arrange
        OrderRequestModel request = OrderRequestModel.builder()
                .customerId(customerId)
                .productIds(null)
                .build();

        // Act & Assert
        assertNull(request.getProductIds());
    }

    @Test
    void orderRequestModel_SameValues_AreEqual() {
        // Arrange
        LocalDateTime date = LocalDateTime.now();

        OrderRequestModel request1 = OrderRequestModel.builder()
                .customerId(customerId)
                .productIds(List.of(productId1))
                .orderDate(date)
                .build();

        OrderRequestModel request2 = OrderRequestModel.builder()
                .customerId(customerId)
                .productIds(List.of(productId1))
                .orderDate(date)
                .build();

        // Act & Assert
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
}
