package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.PromoEligibility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutomaticDiscountService {

    private final PromoRepository promoRepository;

    public Mono<CartResponseModel> apply(CartResponseModel response) {
        return promoRepository.findAllByAutomaticTrue()
                .filter(this::isUsable)
                .sort(
                        Comparator.comparingDouble(PromoCode::getDiscount)
                                .reversed()
                                .thenComparingDouble(
                                        PromoCode::getMinimumPurchaseAmount
                                )
                )
                .collectList()
                .map(offers -> {
                    response.setAutomaticDiscountApplied(false);
                    response.setAutomaticDiscountMinimumAmount(0);
                    response.setAutomaticDiscountPercentage(0);

                    if (offers.isEmpty()) {
                        return response;
                    }

                    List<CartProduct> products = response.getProducts() == null
                            ? List.of()
                            : response.getProducts();

                    double subtotal = Math.round(
                            products.stream()
                                    .mapToDouble(product -> {
                                        double price =
                                                product.getProductSalePrice() == null
                                                        ? 0
                                                        : product.getProductSalePrice();
                                        int quantity =
                                                product.getQuantityInCart() == null
                                                        ? 0
                                                        : product.getQuantityInCart();
                                        return price * quantity;
                                    })
                                    .sum() * 100.0
                    ) / 100.0;

                    PromoCode eligible = offers.stream()
                            .filter(offer ->
                                    subtotal >= offer.getMinimumPurchaseAmount()
                            )
                            .findFirst()
                            .orElse(null);

                    PromoCode displayed = eligible != null
                            ? eligible
                            : offers.stream()
                                    .min(Comparator.comparingDouble(
                                            PromoCode::getMinimumPurchaseAmount
                                    ))
                                    .orElseThrow();

                    response.setAutomaticDiscountMinimumAmount(
                            displayed.getMinimumPurchaseAmount()
                    );
                    response.setAutomaticDiscountPercentage(
                            displayed.getDiscount()
                    );

                    if (eligible == null || products.isEmpty() || subtotal <= 0) {
                        return response;
                    }

                    Cart automaticCart = Cart.builder()
                            .appliedPromo(eligible)
                            .shippingMethod(response.getShippingMethod())
                            .build();

                    CartResponseModel automatic =
                            EntityModelUtil.toCartResponseModel(
                                    automaticCart,
                                    products
                            );

                    if (automatic.getDiscountAmount()
                            > response.getDiscountAmount()) {
                        response.setProducts(automatic.getProducts());
                        response.setDiscountAmount(automatic.getDiscountAmount());
                        response.setSubtotal(automatic.getSubtotal());
                        response.setTvq(automatic.getTvq());
                        response.setTvc(automatic.getTvc());
                        response.setTotal(automatic.getTotal());
                        response.setAutomaticDiscountApplied(true);
                    }

                    return response;
                });
    }

    private boolean isUsable(PromoCode promo) {
        Double minimum = promo.getMinimumPurchaseAmount();

        return promo.isAutomatic()
                && promo.isActive()
                && EntityModelUtil.isPromoActive(promo.getExpirationDate())
                && "CART".equals(PromoEligibility.scope(promo))
                && minimum != null
                && Double.isFinite(minimum)
                && minimum >= 0
                && Double.isFinite(promo.getDiscount())
                && promo.getDiscount() > 0
                && promo.getDiscount() <= 100;
    }
}
