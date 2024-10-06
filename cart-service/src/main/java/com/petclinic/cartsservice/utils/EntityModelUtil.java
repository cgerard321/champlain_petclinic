package com.petclinic.cartsservice.utils;


import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

public class EntityModelUtil {

    public static CartResponseModel toCartResponseModel(Cart cart, List<ProductResponseModel> products) {
        CartResponseModel cartResponseModel = new CartResponseModel();
        BeanUtils.copyProperties(cart, cartResponseModel);
        cartResponseModel.setProducts(products);
        return cartResponseModel;
    }

    // Overloaded method for getCartByCartId
    public static CartResponseModel toCartResponseModel(
            Cart cart,
            List<ProductResponseModel> products,
            double subtotal,
            double tvq,
            double tvc,
            double total) {

        CartResponseModel cartResponseModel = toCartResponseModel(cart, products);
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

    public static String generateUUIDString() {
        return UUID.randomUUID().toString();
    }


    public static PromoCode mapPromoCode(PromoCode promoCode, PromoCodeRequestModel promoCodeRequestModel){
        if (StringUtils.hasText(promoCodeRequestModel.getName())){
            promoCode.setName(promoCodeRequestModel.getName());
        }
        if (StringUtils.hasText(promoCodeRequestModel.getCode())){
            promoCode.setCode(promoCodeRequestModel.getCode());
        }

        if (StringUtils.hasText(promoCodeRequestModel.getExpirationDate())){
            LocalDateTime validatedExpirationDate =  validateExpirationDate(promoCodeRequestModel.getExpirationDate());
            promoCode.setExpirationDate(validatedExpirationDate);
        }
        promoCode.setDiscount(promoCodeRequestModel.getDiscount());
        return promoCode;
    }

    public static LocalDateTime validateExpirationDate(String stringDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        try {
            return LocalDateTime.parse(stringDate, formatter);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "The expiration date is not valid");
        }
    }

    public static PromoCodeResponseModel toPromoCodeResponseModel(PromoCode promoCode) {
        PromoCodeResponseModel responseModel = new PromoCodeResponseModel();
        responseModel.setId(promoCode.getId());
        responseModel.setName(promoCode.getName());
        responseModel.setCode(promoCode.getCode());
        responseModel.setDiscount(promoCode.getDiscount());
        responseModel.setExpirationDate(promoCode.getExpirationDate());
        responseModel.setActive(promoCode.isActive());
        return responseModel;
    }
}
