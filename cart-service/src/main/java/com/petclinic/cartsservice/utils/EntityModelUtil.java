package com.petclinic.cartsservice.utils;


import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.UUID;

public class EntityModelUtil {

    public static CartResponseModel toCartResponseModel(Cart cart, List<ProductResponseModel> products) {
        CartResponseModel cartResponseModel = new CartResponseModel();
        BeanUtils.copyProperties(cart, cartResponseModel);
        cartResponseModel.setProducts(products);
        return cartResponseModel;
    }

    public static Cart toCartEntity(CartRequestModel cartRequestModel) {
        return Cart.builder()
                .cartId(generateUUIDString())
                .customerId(cartRequestModel.getCustomerId())
                .build();
    }

    public static String generateUUIDString() {
        return UUID.randomUUID().toString();
    }
}
