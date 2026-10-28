package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.ShippingMethod;
import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportStatus;
import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.domainclientlayer.CustomerResponseModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import com.petclinic.cartsservice.presentationlayer.BillingRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.CreateOrderReportRequestModel;
import com.petclinic.cartsservice.presentationlayer.OrderReportResponseModel;
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
        List<CartProduct> safeProducts = products == null ? List.of() : copyCartProducts(products);
        PromoCode appliedPromo = cart.getAppliedPromo();
        Double promo = appliedPromo == null ? cart.getPromoPercent()
                : (appliedPromo.isActive() && isPromoActive(appliedPromo.getExpirationDate())
                    ? appliedPromo.getDiscount() : null);
        resp.setPromoPercent(promo);
        double discount = 0.0;

        double subtotal = 0.0;
        for (CartProduct p : safeProducts) {
            double price = p.getProductSalePrice() != null ? p.getProductSalePrice() : 0.0;
            int qty = p.getQuantityInCart() != null ? p.getQuantityInCart() : 0;
            subtotal += price * qty;
            boolean eligible = appliedPromo == null || PromoEligibility.isEligible(appliedPromo, p);
            double lineDiscount = eligible && promo != null && promo >= 0 && promo <= 100
                    ? round2(price * qty * promo / 100.0) : 0.0;
            p.setDiscountAmount(lineDiscount);
            discount += lineDiscount;
        }
        subtotal = round2(subtotal);

        resp.setDiscountAmount(round2(discount));
        double discountedSubtotal = round2(subtotal - discount);

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
        PromoEligibility.setScope(promoCode, promoCodeRequestModel);
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
        responseModel.setScope(PromoEligibility.scope(promoCode));
        responseModel.setProductIds(promoCode.getProductIds());
        responseModel.setProductTypes(promoCode.getProductTypes());
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

    public static Order toOrder(Cart cart, CustomerResponseModel customer, BillingRequestModel billing) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(customer.getCustomerId())
                .customerName(billing.getCustomerName())
                .address(billing.getAddress())
                .city(billing.getCity())
                .email(billing.getEmail())
                .postalCode(billing.getPostalCode())
                .province(billing.getProvince())
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
                .build();
    }

    public static OrderReport toOrderReport(
            String orderId,
            String customerId,
            Order order,
            CreateOrderReportRequestModel request,
            List<OrderReport.AffectedItem> affectedItems
    ) {
        return OrderReport.builder()
                .reportId("RPT-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT))
                .orderId(orderId)
                .customerId(customerId)
                .customerName(order.getCustomerName())
                .reason(request.getReason())
                .affectedItems(affectedItems)
                .description(request.getDescription().trim())
                .status(OrderReportStatus.NEW)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    public static OrderReportResponseModel toOrderReportResponseModel(
            OrderReport report
    ) {
        List<OrderReportResponseModel.AffectedItemResponse> affectedItems =
                report.getAffectedItems() == null
                        ? List.of()
                        : report.getAffectedItems().stream()
                        .map(item -> OrderReportResponseModel.AffectedItemResponse
                                .builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .build())
                        .toList();

        return OrderReportResponseModel.builder()
                .reportId(report.getReportId())
                .orderId(report.getOrderId())
                .customerId(report.getCustomerId())
                .customerName(report.getCustomerName())
                .reason(report.getReason())
                .affectedItems(affectedItems)
                .description(report.getDescription())
                .status(report.getStatus())
                .submittedAt(report.getSubmittedAt())
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
        orderCart.setProducts(calculatedCart.getProducts());
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
