package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

        ProductResponseModel product1 = new ProductResponseModel("prod-1", "Product 1", "Description 1", 100.0);
        ProductResponseModel product2 = new ProductResponseModel("prod-2", "Product 2", "Description 2", 200.0);
        List<ProductResponseModel> products = new ArrayList<>();
        products.add(product1);
        products.add(product2);

        // Act
        CartResponseModel cartResponseModel = EntityModelUtil.toCartResponseModel(cart, products);

        // Assert
        assertEquals("cart-123", cartResponseModel.getCartId());
        assertEquals("customer-456", cartResponseModel.getCustomerId());
        assertEquals(2, cartResponseModel.getProducts().size());
        assertEquals("prod-1", cartResponseModel.getProducts().get(0).getProductId());
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
}
