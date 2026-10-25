package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import java.util.List;

/** Shared eligibility rules for promo creation and cart calculation. */
public final class PromoEligibility {
    private PromoEligibility() { }

    public static String scope(PromoCode promo) {
        return promo.getScope() == null ? "CART" : promo.getScope();
    }

    public static boolean isEligible(PromoCode promo, CartProduct product) {
        return switch (scope(promo)) {
            case "CART" -> true;
            case "PRODUCT" -> promo.getProductIds() != null && promo.getProductIds().contains(product.getProductId());
            case "PRODUCT_TYPE" -> !product.isPromoLookupFailed() && promo.getProductTypes() != null
                    && promo.getProductTypes().contains(product.getProductType());
            default -> false;
        };
    }

    public static void setScope(PromoCode promo, PromoCodeRequestModel request) {
        String scope = request.getScope() == null ? scope(promo) : request.getScope();
        List<String> ids = clean(request.getProductIds() == null ? promo.getProductIds() : request.getProductIds());
        List<String> types = clean(request.getProductTypes() == null ? promo.getProductTypes() : request.getProductTypes());
        if (!List.of("CART", "PRODUCT", "PRODUCT_TYPE").contains(scope)
                || (scope.equals("PRODUCT") && ids.isEmpty())
                || (scope.equals("PRODUCT_TYPE") && types.isEmpty())) {
            throw new InvalidInputException("Select a valid promo scope and at least one eligible item for targeted promos");
        }
        if (!Double.isFinite(request.getDiscount()) || request.getDiscount() < 0 || request.getDiscount() > 100) {
            throw new InvalidInputException("Discount must be between 0 and 100");
        }
        promo.setScope(scope);
        promo.setProductIds(scope.equals("PRODUCT") ? ids : List.of());
        promo.setProductTypes(scope.equals("PRODUCT_TYPE") ? types : List.of());
    }

    private static List<String> clean(List<String> values) {
        return values == null ? List.of() : values.stream().filter(v -> v != null && !v.isBlank())
                .map(String::trim).distinct().toList();
    }
}
