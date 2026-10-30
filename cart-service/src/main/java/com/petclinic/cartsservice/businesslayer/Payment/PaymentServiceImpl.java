package com.petclinic.cartsservice.businesslayer.Payment;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeProductRequestModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.stripe.model.Coupon;
import com.stripe.model.checkout.Session;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

import static com.petclinic.cartsservice.utils.EntityModelUtil.createLineItem;
import static com.petclinic.cartsservice.utils.EntityModelUtil.toCents;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public PaymentServiceImpl(CartRepository cartRepository, OrderRepository orderRepository, CartService cartService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @Value("${frontend.dockerUrl:http://localhost:3000}")
    private String frontendDockerUrl;

    @Value("${frontend.url2:http://localhost:4200}")
    private String frontendUrl2;

    @Override
    public Mono<StripeResponseModel> createStripeCheckoutSession(StripeProductRequestModel sessionRequestModel, String cartId) {

        return Mono.just(sessionRequestModel)
                .filter(request -> cartId.equals(request.getCartId()))
                .switchIfEmpty(Mono.error(
                        new InvalidInputException("Cart ids does not match.")
                ))
                .flatMap(request -> cartRepository.findCartByCartId(cartId))
                .switchIfEmpty(Mono.error(new NotFoundException("Cart not found.")))
                .flatMap(cart -> {

                    List<CartProduct> cartProducts = cart.getProducts();

                    if(cartProducts == null || cartProducts.isEmpty()){
                        return Mono.error(new InvalidInputException("No products found in cart."));
                    }

                    CartResponseModel cartTotals = EntityModelUtil.toCartResponseModel(cart, cartProducts);
                    long expectedTotalCents = toCents(cartTotals.getTotal());

                    if (expectedTotalCents <= 0) {
                        return Mono.error(new InvalidInputException("Invalid total price amount."));
                    }

                    double promoPercent = cart.getPromoPercent() == null
                            ? 0.0
                            : cart.getPromoPercent();

                    if(promoPercent < 0 || promoPercent > 100) {
                        return Mono.error(new InvalidInputException("Invalid promo percent."));
                    }

                    List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
                    long stripeSumCents = 0L;

                    // Use original unit prices so Stripe line items match the cart.
                    // Promo is applied once against the cart subtotal, not per unit.
                    for (CartProduct cartProduct : cartProducts) {

                        if (cartProduct.getQuantityInCart() == null || cartProduct.getQuantityInCart() <= 0) {
                            return Mono.error(new InvalidInputException("Invalid price amount."));
                        }

                        if (cartProduct.getProductSalePrice() == null || cartProduct.getProductSalePrice() <= 0) {
                            return Mono.error(new InvalidInputException("Invalid price amount."));
                        }

                        long priceInCents = Math.round(cartProduct.getProductSalePrice() * 100.0);

                        if (priceInCents <= 0) {
                            return Mono.error(new InvalidInputException("Invalid price amount."));
                        }

                        long quantity = cartProduct.getQuantityInCart().longValue();

                        lineItems.add(createLineItem(
                                cartProduct.getProductName(),
                                priceInCents,
                                quantity));
                        stripeSumCents += priceInCents * cartProduct.getQuantityInCart();
                    }

                    long shippingInCents = toCents(cartTotals.getShippingCost());
                    if(shippingInCents > 0){
                        lineItems.add(createLineItem("Shipping", shippingInCents, 1L));
                        stripeSumCents += shippingInCents;
                    }

                    long tvqInCents = toCents(cartTotals.getTvq());
                    if(tvqInCents > 0){
                        lineItems.add(createLineItem("TVQ Taxes", tvqInCents, 1L));
                        stripeSumCents += tvqInCents;
                    }

                    long tvcInCents = toCents(cartTotals.getTvc());
                    if(tvcInCents > 0){
                        lineItems.add(createLineItem("TVC Taxes", tvcInCents, 1L));
                        stripeSumCents += tvcInCents;
                    }

                    String frontendType = sessionRequestModel.getFrontendTypeUrl();
                    String frontendUrl;
                    if (frontendType == null || frontendType.isBlank()
                            || "react".equalsIgnoreCase(frontendType)) {
                        frontendUrl = frontendDockerUrl;
                    } else if ("angular".equalsIgnoreCase(frontendType)) {
                        frontendUrl = frontendUrl2;
                    } else {
                        return Mono.error(new InvalidInputException("Invalid frontend type"));
                    }

                    long discountCents = stripeSumCents - expectedTotalCents;

                    return Mono.fromCallable(() -> {
                                SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                                        .setMode(SessionCreateParams.Mode.PAYMENT)
                                        .setSuccessUrl(frontendUrl + "/carts/" + cartId + "?payment=success&session_id={CHECKOUT_SESSION_ID}")
                                        .setCancelUrl(frontendUrl + "/carts/" + cartId + "?payment=cancelled")
                                        .addAllLineItem(lineItems)
                                        .putMetadata("cartId", cart.getCartId())
                                        .putMetadata("expectedAmountCents", String.valueOf(expectedTotalCents));

                                if (discountCents > 0) {
                                    Coupon coupon = Coupon.create(CouponCreateParams.builder()
                                            .setAmountOff(discountCents)
                                            .setCurrency("cad")
                                            .setDuration(CouponCreateParams.Duration.ONCE)
                                            .setName("Cart promo")
                                            .build());
                                    paramsBuilder.addDiscount(SessionCreateParams.Discount.builder()
                                            .setCoupon(coupon.getId())
                                            .build());
                                } else if (discountCents < 0) {
                                    paramsBuilder.addLineItem(
                                            createLineItem("Rounding adjustment", -discountCents, 1L));
                                }

                                Session session = Session.create(paramsBuilder.build());
                                return new StripeResponseModel(session.getId(), session.getUrl());
                            })
                            .subscribeOn(Schedulers.boundedElastic());

                });
    }

    @Override
    public Mono<Void> processSuccessfulPayment(Session session) {
        if (!"paid".equals(session.getPaymentStatus())) {
            return Mono.error(
                    new InvalidInputException("Payment is not confirmed.")
            );
        }

        String cartId = session.getMetadata() == null
                ? null
                : session.getMetadata().get("cartId");

        if (cartId == null || cartId.isEmpty()) {
            return Mono.error(new InvalidInputException("Cart id is empty."));
        }

        String stripeSessionId = session.getId();

        if(stripeSessionId == null || stripeSessionId.isEmpty()) {
            return Mono.error(new InvalidInputException("Stripe session id is empty."));
        }

        return orderRepository.existsByStripeSessionId(stripeSessionId)
                .flatMap(alreadyProcessed -> {
                    if(alreadyProcessed){
                        return Mono.empty();
                    }
                    return cartRepository.findCartByCartId(cartId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Cart not found.")))
                            .flatMap(cart -> {

                                double total = EntityModelUtil.toCartResponseModel(cart, cart.getProducts()).getTotal();

                                long expectedAmount = Math.round(total * 100); //convert it into cents

                                if (total <= 0){
                                    return Mono.error(new InvalidInputException("Total amount is invalid."));
                                }

                                if(session.getAmountTotal() == null || session.getAmountTotal() != expectedAmount){
                                    return Mono.error(new InvalidInputException("Total amount is invalid."));
                                }

                                if (session.getCurrency() == null
                                        || !"cad".equalsIgnoreCase(session.getCurrency())){
                                    return Mono.error(new InvalidInputException("Currency is invalid."));
                                }

                                return cartService.checkoutCart(cartId, stripeSessionId).then();
                            });

                });
    }

}
