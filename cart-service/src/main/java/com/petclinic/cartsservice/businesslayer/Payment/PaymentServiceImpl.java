package com.petclinic.cartsservice.businesslayer.Payment;

import com.petclinic.cartsservice.businesslayer.AutomaticDiscountService;
import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.presentationlayer.BillingRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeProductRequestModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Address;
import com.stripe.model.Coupon;
import com.stripe.model.checkout.Session;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
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
    private final AutomaticDiscountService automaticDiscountService;

    public PaymentServiceImpl(CartRepository cartRepository, OrderRepository orderRepository, CartService cartService,
                              AutomaticDiscountService automaticDiscountService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.automaticDiscountService = automaticDiscountService;
    }

    @Value("${frontend.customer-url:http://localhost:3000}")
    private String customerFrontendUrl;

    @Value("${frontend.employee-url:http://localhost:4200}")
    private String employeeFrontendUrl;

    // Frontend addresses Stripe is allowed to send the customer back to (regex)
    @Value("${frontend.allowed-return-origins:http://localhost:(3000|4200)}")
    private String allowedReturnOrigins;

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

                    // same totals the customer sees on the cart page, automatic discount included
                    return automaticDiscountService.apply(EntityModelUtil.toCartResponseModel(cart, cartProducts))
                            .flatMap(cartTotals -> {
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

                                String frontendUrl = resolveReturnUrl(sessionRequestModel);
                                if (frontendUrl == null) {
                                    return Mono.error(new InvalidInputException("Invalid frontend type"));
                                }

                                long discountCents = stripeSumCents - expectedTotalCents;

                                return Mono.fromCallable(() -> {
                                            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                                                    .setMode(SessionCreateParams.Mode.PAYMENT)
                                                    .setSuccessUrl(frontendUrl + "/carts/" + cartId + "?payment=success&session_id={CHECKOUT_SESSION_ID}")
                                                    .setCancelUrl(frontendUrl + "/carts/" + cartId + "?payment=cancelled")
                                                    .addAllLineItem(lineItems)
                                                    // Stripe collects the name, email and address that go on the order
                                                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                                                    .putMetadata("cartId", cart.getCartId())
                                                    .putMetadata("expectedAmountCents", String.valueOf(expectedTotalCents));

                                            if (discountCents > 0) {
                                                Coupon coupon = getOrCreatePromoCoupon(discountCents);
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

                });
    }

    // Send the customer back to the site they paid from (localhost, a PR preview or production),
    // but only if it is on the allowlist so the checkout cannot redirect to someone else's site.
    // Otherwise fall back to the configured frontend URL.
    private String resolveReturnUrl(StripeProductRequestModel request) {
        String origin = request.getFrontendOrigin();
        if (origin != null && allowedReturnOrigins != null && origin.matches(allowedReturnOrigins)) {
            return origin;
        }

        String frontendType = request.getFrontendType();
        if (frontendType == null || frontendType.isBlank() || "react".equalsIgnoreCase(frontendType)) {
            return customerFrontendUrl;
        }
        if ("angular".equalsIgnoreCase(frontendType)) {
            return employeeFrontendUrl;
        }
        return null;
    }

    // One coupon per discount amount, reused across checkouts instead of creating a new one every time
    private Coupon getOrCreatePromoCoupon(long discountCents) throws StripeException {
        String couponId = "cart-promo-" + discountCents;
        Coupon coupon = null;
        try {
            coupon = Coupon.retrieve(couponId);
        } catch (InvalidRequestException e) {
            log.info("Stripe coupon {} does not exist yet, creating it", couponId);
        }
        if (coupon != null) {
            return coupon;
        }
        return Coupon.create(CouponCreateParams.builder()
                .setId(couponId)
                .setAmountOff(discountCents)
                .setCurrency("cad")
                .setDuration(CouponCreateParams.Duration.ONCE)
                .setName("Cart promo")
                .build());
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

                                // The amount we asked Stripe to charge when the session was created
                                long expectedAmount = readExpectedAmountCents(session);

                                if (expectedAmount <= 0) {
                                    return Mono.error(new InvalidInputException("Total amount is invalid."));
                                }

                                if(session.getAmountTotal() == null || session.getAmountTotal() != expectedAmount){
                                    return Mono.error(new InvalidInputException("Total amount is invalid."));
                                }

                                if (session.getCurrency() == null
                                        || !"cad".equalsIgnoreCase(session.getCurrency())){
                                    return Mono.error(new InvalidInputException("Currency is invalid."));
                                }

                                // The order is built from the cart, so it must still match what was paid for
                                return automaticDiscountService.apply(EntityModelUtil.toCartResponseModel(cart, cart.getProducts()))
                                        .flatMap(cartTotals -> {
                                            long cartAmount = Math.round(cartTotals.getTotal() * 100);
                                            if (cartAmount != expectedAmount) {
                                                log.error("Cart {} changed after Stripe session {} was paid. Paid {} cents, cart is now {} cents.",
                                                        cartId, stripeSessionId, expectedAmount, cartAmount);
                                                return Mono.error(new InvalidInputException("Cart changed after payment was started."));
                                            }
                                            return cartService.checkoutCart(cartId, stripeSessionId, toBillingRequest(session)).then();
                                        });
                            });

                })
                // Stripe can send the same event twice at once; the unique index stops the second order
                .onErrorResume(DuplicateKeyException.class, e -> {
                    log.info("Stripe session {} was already processed", stripeSessionId);
                    return Mono.empty();
                });
    }

    private static long readExpectedAmountCents(Session session) {
        String value = session.getMetadata().get("expectedAmountCents");
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static BillingRequestModel toBillingRequest(Session session) {
        BillingRequestModel billing = new BillingRequestModel();
        Session.CustomerDetails details = session.getCustomerDetails();
        if (details == null) {
            return billing;
        }
        billing.setCustomerName(details.getName());
        billing.setEmail(details.getEmail());

        Address address = details.getAddress();
        if (address != null) {
            String street = address.getLine1();
            if (address.getLine2() != null && !address.getLine2().isBlank()) {
                street = street + ", " + address.getLine2();
            }
            billing.setAddress(street);
            billing.setCity(address.getCity());
            billing.setProvince(address.getState());
            billing.setPostalCode(address.getPostalCode());
        }
        return billing;
    }

}
