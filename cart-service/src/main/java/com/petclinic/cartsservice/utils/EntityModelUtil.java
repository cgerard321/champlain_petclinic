package com.petclinic.cartsservice.utils;


import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.UUID;

public class EntityModelUtil {


    // Overloaded method for getCartByCartId
    public static CartResponseModel toCartResponseModel(Cart cart, List<CartProduct> products) {
        double subtotal = 0;
        for (CartProduct product : products) {
            subtotal += product.getProductSalePrice() * product.getQuantityInCart();
        }
        double tvq = subtotal * 0.09975; // Example tax rate for Quebec
        double tvc = subtotal * 0.05; // Example tax rate for Canada
        double total = subtotal + tvq + tvc;

        CartResponseModel cartResponseModel = new CartResponseModel();
        BeanUtils.copyProperties(cart, cartResponseModel);
        cartResponseModel.setSubtotal(subtotal);
        cartResponseModel.setTvq(tvq);
        cartResponseModel.setTvc(tvc);
        cartResponseModel.setTotal(total);
        return cartResponseModel;
    }


    public static Cart toCartEntity(CartRequestModel cartRequestModel) {
        return Cart.builder()
                .cartId(generateUUIDString())
                .customerId(cartRequestModel.getCustomerId())
                .build();
    }

    public static CartProduct toCartProductEntity(ProductResponseModel productResponseModel, CartRequestModel cartRequestModel) {
        return CartProduct.builder()
                .productId(productResponseModel.getProductId())
                .productId(productResponseModel.getProductId())
                .productName(productResponseModel.getProductName())
                .productSalePrice(productResponseModel.getProductSalePrice())
                .build();
    }

    public static ProductResponseModel toProductResponseModel(CartProduct cartProduct) {
        ProductResponseModel productResponseModel = new ProductResponseModel();
        BeanUtils.copyProperties(cartProduct, productResponseModel);
        return productResponseModel;
    }

    public static String generateUUIDString() {
        return UUID.randomUUID().toString();
    }
}
