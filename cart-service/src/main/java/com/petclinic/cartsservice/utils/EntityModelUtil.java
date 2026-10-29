package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.*;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CustomerResponseModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.OrderResponseModel;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class EntityModelUtil {

    public static CartResponseModel toCartResponseModel(Cart cart, List<CartProduct> products) {
        CartResponseModel resp = new CartResponseModel();
        BeanUtils.copyProperties(cart, resp);

        //This prevents a crash if products is null
        List<CartProduct> safeProducts = products == null ? List.of() : products;

        double subtotal = 0.0;
        for (CartProduct p : safeProducts) {
            double price = p.getProductSalePrice() != null ? p.getProductSalePrice() : 0.0;
            int qty = p.getQuantityInCart() != null ? p.getQuantityInCart() : 0;
            subtotal += price * qty;
        }
        subtotal = round2(subtotal);

        Double promo = resp.getPromoPercent();
        double discountedSubtotal = subtotal;
        if (promo != null && promo >= 0.0 && promo <= 100.0) {
            discountedSubtotal = round2(subtotal * (1.0 - (promo / 100.0)));
        }

        ShippingMethod shippingMethod = cart.getShippingMethod() == null
                ? ShippingMethod.PICKUP
                : cart.getShippingMethod();
        double shippingCost = safeProducts.isEmpty() ? 0.0 : round2(shippingMethod.getCost());
        double taxableSubtotal = round2(discountedSubtotal + shippingCost);

        double tvq = round2(taxableSubtotal * 0.09975); // QC
        double tvc = round2(taxableSubtotal * 0.05);    // CA/GST
        double total = round2(taxableSubtotal + tvq + tvc);

        resp.setProducts(safeProducts);
        resp.setSubtotal(taxableSubtotal);
        resp.setShippingMethod(shippingMethod);
        resp.setShippingCost(shippingCost);
        resp.setTvq(tvq);
        resp.setTvc(tvc);
        resp.setTotal(total);

        return resp;
    }

    public static CartResponseModel toCartResponseModel(Cart cart, List<CartProduct> products, String customerName) {
        CartResponseModel model = toCartResponseModel(cart, products);
        model.setCustomerName(customerName);
        return model;
    }

    public static Cart toCartEntity(CartRequestModel cartRequestModel) {
        return Cart.builder()
                .cartId(generateUUIDString())
                .customerId(cartRequestModel.getCustomerId())
                //Default shipping of PICKUP with cost of 0.00 when a new cart is created
                .shippingMethod(ShippingMethod.PICKUP)
                .shippingCost(ShippingMethod.PICKUP.getCost())
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
        if (promoCodeRequestModel.getActive() != null) {
            promoCode.setActive(promoCodeRequestModel.getActive());
        }
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
        responseModel.setActive(promoCode.isActive() && isPromoActive(promoCode.getExpirationDate()));
        return responseModel;
    }

    public static boolean isPromoActive(LocalDateTime expirationDate) {
        return expirationDate != null && expirationDate.isAfter(LocalDateTime.now());
    }

    public static OrderResponseModel toOrderResponseModel(Order order) {
        OrderResponseModel responseModel = new OrderResponseModel();
        BeanUtils.copyProperties(order, responseModel);
        return responseModel;
    }

    public static Order toOrder(Cart cart, CustomerResponseModel customer, String stripeSessionId) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(customer.getCustomerId())
                .customerName(customer.getFullName())
                .customerAddress(customer.getAddress())
                .products(copyCartProducts(cart.getProducts()))
                .subtotal(cart.getSubtotal())
                .promoPercent(cart.getPromoPercent())
                .tvq(cart.getTvq())
                .tvc(cart.getTvc())
                .total(cart.getTotal())
                .shippingMethod(cart.getShippingMethod())
                .shippingCost(cart.getShippingCost())
                .invoiceId(cart.getInvoiceId())
                .orderDate(LocalDateTime.now())
                .stripeSessionId(stripeSessionId)
                .paymentStatus(PaymentStatus.PAID)
                .paidDate(LocalDateTime.now())
                .build();
    }


    private static List<CartProduct> copyCartProducts(
            List<CartProduct> products
    ) {
        Objects.requireNonNull(products, "Cart products must not be null");

        return products.stream()
                .map(product -> {
                    CartProduct copy = new CartProduct();
                    BeanUtils.copyProperties(product, copy);
                    return copy;
                })
                .toList();
    }


    public static Cart storeCartOrder(Cart cart) {
        Cart orderCart = new Cart();

        BeanUtils.copyProperties(cart, orderCart);

        List<CartProduct> products =
                copyCartProducts(cart.getProducts());

        orderCart.setProducts(products);

        CartResponseModel calculatedCart =
                toCartResponseModel(orderCart, products);

        orderCart.setSubtotal(calculatedCart.getSubtotal());
        orderCart.setPromoPercent(calculatedCart.getPromoPercent());
        orderCart.setTvq(calculatedCart.getTvq());
        orderCart.setTvc(calculatedCart.getTvc());
        orderCart.setTotal(calculatedCart.getTotal());
        orderCart.setShippingMethod(calculatedCart.getShippingMethod());
        orderCart.setShippingCost(calculatedCart.getShippingCost());

        return orderCart;
    }



    public static List<CartProduct> mergeRecentPurchases(List<CartProduct> recentPurchases, List<CartProduct> purchasedProducts)
    {
        Map<String, CartProduct> merged = new LinkedHashMap<>();

        if (recentPurchases != null) {
            for (CartProduct product : recentPurchases) {
                merged.put(product.getProductId(), copyCartProduct(product));
            }
        }

        for (CartProduct purchased : purchasedProducts) {
            String productId = purchased.getProductId();

            merged.merge(productId, copyCartProduct(purchased),
                    (existing, incoming) -> {
                        existing.setQuantityInCart(existing.getQuantityInCart() + incoming.getQuantityInCart());
                        return existing;
                    }
            );
        }

        return new ArrayList<>(merged.values());
    }


    private static CartProduct copyCartProduct(CartProduct product) {
        CartProduct copy = new CartProduct();
        BeanUtils.copyProperties(product, copy);
        return copy;
    }


    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
