package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.junit.jupiter.api.Test;

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
        final CartProduct product3 = CartProduct.builder()
                .productId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")
                .productName("Product3")
                .productDescription("Description3")
                .productSalePrice(300.0)
                .quantityInCart(1)
                .averageRating(3.5)
                .build();

        List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));

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
