package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportReason;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportStatus;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.CreateOrderReportRequestModel;
import com.petclinic.cartsservice.presentationlayer.OrderReportResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityModelUtilTest {

    @Test
    void testToCartResponseModel() {
        // Arrange
        Cart cart = new Cart();
        cart.setCartId("cart-123");
        cart.setCustomerId("customer-456");

        final CartProduct product1 = CartProduct.builder()
                .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
                .productName("Product1")
                .productDescription("Description1")
                .productSalePrice(100.0)
                .quantityInCart(1)
                .averageRating(4.5)
                .build();
        final CartProduct product2 = CartProduct.builder()
                .productId("d819e4f4-25af-4d33-91e9-2c45f0071606")
                .productName("Product2")
                .productDescription("Description2")
                .productSalePrice(200.0)
                .quantityInCart(1)
                .averageRating(4.0)
                .build();

        List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));

        cart.setProducts(products);

        // Act
        CartResponseModel cartResponseModel = EntityModelUtil.toCartResponseModel(cart, products);

        // Assert
        assertEquals(cart.getCartId(), cartResponseModel.getCartId());
        assertEquals(cart.getCustomerId(), cartResponseModel.getCustomerId());
        assertEquals(cart.getProducts().size(), cartResponseModel.getProducts().size());
    }

    @Test
    void testToCartEntity() {
        // Arrange
        CartRequestModel cartRequestModel = new CartRequestModel();
        cartRequestModel.setCustomerId("customer-456");

        // Act
        Cart cart = EntityModelUtil.toCartEntity(cartRequestModel);

        // Assert
        assertNotNull(cart.getCartId());
        assertEquals("customer-456", cart.getCustomerId());
    }

    @Test
    void testGenerateUUIDString() {
        // Act
        String uuidString = EntityModelUtil.generateUUIDString();

        // Assert
        assertNotNull(uuidString);
        assertDoesNotThrow(() -> UUID.fromString(uuidString));  // Check if it's a valid UUID
    }

    @Test
    void testMapPromoCode_updatesActive_whenProvided() {
        // Arrange
        PromoCode promoCode = new PromoCode();
        promoCode.setActive(true);
        PromoCodeRequestModel request = new PromoCodeRequestModel();
        request.setActive(false);

        // Act
        PromoCode result = EntityModelUtil.mapPromoCode(promoCode, request);

        // Assert
        assertFalse(result.isActive());
    }

    @Test
    void testMapPromoCode_keepsActive_whenNotProvided() {
        // Arrange
        PromoCode promoCode = new PromoCode();
        promoCode.setActive(true);

        // Act
        PromoCode result = EntityModelUtil.mapPromoCode(promoCode, new PromoCodeRequestModel());

        // Assert
        assertTrue(result.isActive());
    }

    @Test
    void testToPromoCodeResponseModel_activeOnlyWhenFlagOnAndNotExpired() {
        // Arrange
        PromoCode promoCode = new PromoCode();
        promoCode.setExpirationDate(LocalDateTime.now().plusDays(1));

        // Act & Assert
        promoCode.setActive(true);
        assertTrue(EntityModelUtil.toPromoCodeResponseModel(promoCode).isActive());

        promoCode.setActive(false);
        assertFalse(EntityModelUtil.toPromoCodeResponseModel(promoCode).isActive());

        promoCode.setActive(true);
        promoCode.setExpirationDate(LocalDateTime.now().minusDays(1));
        assertFalse(EntityModelUtil.toPromoCodeResponseModel(promoCode).isActive());
    }

    @Test
    void testValidateExpirationDate_throwsWhenFormatInvalid() {
        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> EntityModelUtil.validateExpirationDate("2024-12-31"));
    }

    @Test
    void toOrderReport_mapsRequestAndOrderData() {
        Order order = Order.builder()
                .customerName("Test Customer")
                .build();
        CreateOrderReportRequestModel request =
                CreateOrderReportRequestModel.builder()
                        .reason(OrderReportReason.DAMAGED_ITEM)
                        .description("  The product arrived damaged.  ")
                        .build();
        List<OrderReport.AffectedItem> affectedItems = List.of(
                OrderReport.AffectedItem.builder()
                        .productId("product-123")
                        .productName("Dog Food")
                        .build()
        );

        OrderReport report = EntityModelUtil.toOrderReport(
                "order-123",
                "customer-123",
                order,
                request,
                affectedItems
        );

        assertTrue(report.getReportId().startsWith("RPT-"));
        assertEquals("order-123", report.getOrderId());
        assertEquals("customer-123", report.getCustomerId());
        assertEquals("Test Customer", report.getCustomerName());
        assertEquals(OrderReportReason.DAMAGED_ITEM, report.getReason());
        assertEquals("The product arrived damaged.", report.getDescription());
        assertEquals(OrderReportStatus.NEW, report.getStatus());
        assertEquals(affectedItems, report.getAffectedItems());
        assertNotNull(report.getSubmittedAt());
    }

    @Test
    void toOrderReportResponseModel_mapsEntityData() {
        LocalDateTime submittedAt = LocalDateTime.now();
        OrderReport report = OrderReport.builder()
                .reportId("RPT-123")
                .orderId("order-123")
                .customerId("customer-123")
                .customerName("Test Customer")
                .reason(OrderReportReason.OTHER)
                .affectedItems(List.of(
                        OrderReport.AffectedItem.builder()
                                .productId("product-123")
                                .productName("Dog Food")
                                .build()
                ))
                .description("There is another problem with this order.")
                .status(OrderReportStatus.IN_REVIEW)
                .submittedAt(submittedAt)
                .build();

        OrderReportResponseModel response =
                EntityModelUtil.toOrderReportResponseModel(report);

        assertEquals("RPT-123", response.getReportId());
        assertEquals("order-123", response.getOrderId());
        assertEquals("customer-123", response.getCustomerId());
        assertEquals("Test Customer", response.getCustomerName());
        assertEquals(OrderReportReason.OTHER, response.getReason());
        assertEquals(OrderReportStatus.IN_REVIEW, response.getStatus());
        assertEquals(submittedAt, response.getSubmittedAt());
        assertEquals(1, response.getAffectedItems().size());
        assertEquals(
                "product-123",
                response.getAffectedItems().get(0).getProductId()
        );
    }
}
