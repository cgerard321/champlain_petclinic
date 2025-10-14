package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartResponseModelTest {
    @Test
    void testConstructorSetsFieldsCorrectly() {
        String invoiceId = "inv-123";
        String cartId = "cart-456";
        List<CartProduct> products = List.of(
                CartProduct.builder().productId("prod1").build(),
                CartProduct.builder().productId("prod2").build()
        );
        double total = 99.99;

        CartResponseModel model = new CartResponseModel(invoiceId, cartId, products, total);

        assertEquals(invoiceId, model.getInvoiceId());
        assertEquals(cartId, model.getCartId());
        assertEquals(products, model.getProducts());
        assertEquals(total, model.getTotal());
    }
}
