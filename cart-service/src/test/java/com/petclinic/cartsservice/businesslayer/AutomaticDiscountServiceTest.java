package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.presentationlayer.BillingRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.OrderResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.PromoEligibility;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutomaticDiscountServiceTest {

    private final PromoRepository promos = mock(PromoRepository.class);
    private final AutomaticDiscountService service =
            new AutomaticDiscountService(promos);

    @Test
    void reachingAndFallingBelowMinimum_recalculatesDiscount() {
        offers(offer(100, 10));
        Cart cart = cart(50, 2);

        StepVerifier.create(service.apply(response(cart)))
                .assertNext(result -> {
                    assertTrue(result.isAutomaticDiscountApplied());
                    assertEquals(10, result.getDiscountAmount(), 0.001);
                    assertEquals(103.48, result.getTotal(), 0.001);
                })
                .verifyComplete();

        cart.getProducts().get(0).setQuantityInCart(1);

        StepVerifier.create(service.apply(response(cart)))
                .assertNext(result -> {
                    assertFalse(result.isAutomaticDiscountApplied());
                    assertEquals(0, result.getDiscountAmount(), 0.001);
                    assertEquals(
                            100,
                            result.getAutomaticDiscountMinimumAmount(),
                            0.001
                    );
                })
                .verifyComplete();
    }

    @Test
    void highestEligibleOffer_isUsed() {
        offers(offer(100, 10), offer(200, 20), offer(100, 15));

        StepVerifier.create(service.apply(response(cart(100, 1))))
                .assertNext(result ->
                        assertEquals(15, result.getDiscountAmount(), 0.001)
                )
                .verifyComplete();
    }

    @Test
    void betterManualCode_isKept() {
        offers(offer(100, 10));
        Cart cart = cart(100, 1);
        cart.setPromoPercent(20.0);

        StepVerifier.create(service.apply(response(cart)))
                .assertNext(result -> {
                    assertFalse(result.isAutomaticDiscountApplied());
                    assertEquals(20, result.getDiscountAmount(), 0.001);
                })
                .verifyComplete();
    }

    @Test
    void inactiveAndExpiredOffers_areIgnored() {
        PromoCode inactive = offer(100, 20);
        inactive.setActive(false);
        PromoCode expired = offer(100, 30);
        expired.setExpirationDate(LocalDateTime.now().minusDays(1));

        offers(inactive, expired);

        StepVerifier.create(service.apply(response(cart(100, 1))))
                .assertNext(result -> {
                    assertFalse(result.isAutomaticDiscountApplied());
                    assertEquals(0, result.getDiscountAmount(), 0.001);
                })
                .verifyComplete();
    }

    @Test
    void employeeCreateAndUpdate_preserveAutomaticSettings() {
        when(promos.save(any(PromoCode.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        PromoCodeServiceImpl management =
                new PromoCodeServiceImpl(promos, null);

        PromoCodeRequestModel request = PromoCodeRequestModel.builder()
                .Name("Automatic offer")
                .automatic(true)
                .minimumPurchaseAmount(100.0)
                .discount(10)
                .active(true)
                .expirationDate("2099-12-31T23:59:59")
                .scope("CART")
                .build();

        var created = management.createPromo(request).block();
        assertNotNull(created);
        assertTrue(created.isAutomatic());
        assertEquals(100, created.getMinimumPurchaseAmount(), 0.001);

        PromoCode stored = offer(100, 10);
        stored.setId(created.getId());

        when(promos.findById(created.getId()))
                .thenReturn(Mono.just(stored));

        request.setMinimumPurchaseAmount(150.0);
        request.setDiscount(15);

        var updated = management.updatePromoCodeById(
                request,
                created.getId()
        ).block();

        assertNotNull(updated);
        assertTrue(updated.isAutomatic());
        assertEquals(150, updated.getMinimumPurchaseAmount(), 0.001);
        assertEquals(15, updated.getDiscount(), 0.001);
    }

    @Test
    void invalidAutomaticConfiguration_isRejected() {
        PromoCode promo = offer(100, 10);
        PromoCodeRequestModel request = PromoCodeRequestModel.builder()
                .automatic(true)
                .minimumPurchaseAmount(-1.0)
                .build();

        assertThrows(
                InvalidInputException.class,
                () -> PromoEligibility.setAutomatic(promo, request)
        );
    }

    @Test
    void automaticCode_cannotBeEnteredManually() {
        when(promos.findPromoCodeByCode("AUTO-test"))
                .thenReturn(Mono.just(offer(100, 10)));

        CartServiceImpl carts = new CartServiceImpl(
                null, null, null, null, promos, service
        );

        StepVerifier.create(
                        carts.applyPromoCodeToCart("cart", "AUTO-test")
                )
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void customerCodeList_excludesAutomaticOffers() {
        PromoCode manual = offer(0, 20);
        manual.setAutomatic(false);
        manual.setCode("SAVE20");

        when(promos.findAllByExpirationDateGreaterThanEqual(
                any(LocalDateTime.class)
        )).thenReturn(Flux.just(offer(100, 10), manual));

        StepVerifier.create(
                        new PromoCodeServiceImpl(promos, null).getActivePromos()
                )
                .assertNext(result -> assertEquals("SAVE20", result.getCode()))
                .verifyComplete();
    }

    @Test
    void checkout_savesSelectedAmounts() {
        offers(offer(100, 10));

        CartRepository repository = mock(CartRepository.class);
        var products = mock(
                com.petclinic.cartsservice.domainclientlayer.ProductClient.class
        );
        OrderServiceImpl orders = mock(OrderServiceImpl.class);

        CartServiceImpl carts = new CartServiceImpl(
                repository, products, null, orders, promos, service
        );

        Cart cart = cart(100, 1);
        cart.setCartId("cart");

        when(repository.findCartByCartId("cart"))
                .thenReturn(Mono.just(cart));
        when(products.getProductByProductId("product"))
                .thenReturn(Mono.empty());
        when(repository.save(any(Cart.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );
        when(orders.createOrder(
                any(Cart.class),
                any(BillingRequestModel.class)
        )).thenReturn(Mono.just(new OrderResponseModel()));

        StepVerifier.create(carts.checkoutCart("cart", new BillingRequestModel()))
                .assertNext(result -> {
                    assertEquals(10, result.getDiscountAmount(), 0.001);
                    assertEquals(103.48, result.getTotal(), 0.001);
                })
                .verifyComplete();

        ArgumentCaptor<Cart> snapshot = ArgumentCaptor.forClass(Cart.class);
        verify(orders).createOrder(
                snapshot.capture(),
                any(BillingRequestModel.class)
        );

        assertEquals(103.48, snapshot.getValue().getTotal(), 0.001);
        assertEquals(
                10,
                snapshot.getValue().getProducts().get(0).getDiscountAmount(),
                0.001
        );
    }

    @Test
    void noOffers_keepsAnUndiscountedCartAndNullablePromoPercent() {
        offers();
        Cart cart = cart(100, 1);

        StepVerifier.create(service.apply(response(cart)))
                .assertNext(result -> {
                    assertEquals(0, result.getAutomaticDiscountPercentage());
                    assertFalse(result.isAutomaticDiscountApplied());
                    Cart snapshot = EntityModelUtil.storeCartOrder(cart, result);
                    assertNull(snapshot.getPromoPercent());
                    assertEquals(result.getTotal(), snapshot.getTotal());
                })
                .verifyComplete();
    }

    @Test
    void shippingAndTaxes_doNotCountTowardMinimum() {
        offers(offer(100, 10));
        Cart cart = cart(95, 1);
        cart.setShippingMethod(com.petclinic.cartsservice.dataaccesslayer.ShippingMethod.EXPRESS);

        StepVerifier.create(service.apply(response(cart)))
                .assertNext(result -> {
                    assertTrue(result.getTotal() > 100);
                    assertFalse(result.isAutomaticDiscountApplied());
                    assertEquals(0, result.getDiscountAmount());
                })
                .verifyComplete();
    }

    @Test
    void automaticOffer_beatsAScopedCodeWithoutChangingStoredProducts() {
        offers(offer(100, 10));
        Cart cart = cart(20, 1);
        cart.setProducts(List.of(cart.getProducts().get(0), CartProduct.builder()
                .productId("other").productSalePrice(80.0).quantityInCart(1).build()));
        cart.setAppliedPromo(PromoCode.builder().code("MANUAL").discount(20)
                .isActive(true).expirationDate(LocalDateTime.now().plusDays(1))
                .scope("PRODUCT").productIds(List.of("product")).build());

        StepVerifier.create(service.apply(response(cart)))
                .assertNext(result -> {
                    assertTrue(result.isAutomaticDiscountApplied());
                    assertEquals(10, result.getDiscountAmount());
                    assertEquals(2, result.getProducts().get(0).getDiscountAmount());
                    assertEquals(8, result.getProducts().get(1).getDiscountAmount());
                    assertEquals("MANUAL", cart.getAppliedPromo().getCode());
                    assertEquals(0, cart.getProducts().get(0).getDiscountAmount());
                })
                .verifyComplete();
    }

    @Test
    void emptyCart_doesNotReceiveAZeroMinimumOffer() {
        offers(offer(0, 10));
        Cart cart = Cart.builder().build();

        StepVerifier.create(service.apply(response(cart)))
                .assertNext(result -> {
                    assertFalse(result.isAutomaticDiscountApplied());
                    assertEquals(0, result.getTotal());
                    assertEquals(10, result.getAutomaticDiscountPercentage());
                })
                .verifyComplete();
    }

    @Test
    void invalidOrTargetedOffers_areIgnored() {
        PromoCode missingMinimum = offer(100, 10);
        missingMinimum.setMinimumPurchaseAmount(null);
        PromoCode negativeMinimum = offer(-1, 10);
        PromoCode invalidPercentage = offer(100, Double.NaN);
        PromoCode targeted = offer(100, 10);
        targeted.setScope("PRODUCT");
        offers(missingMinimum, negativeMinimum, invalidPercentage, targeted);

        StepVerifier.create(service.apply(response(cart(100, 1))))
                .assertNext(result -> assertEquals(0, result.getDiscountAmount()))
                .verifyComplete();
    }

    @Test
    void invalidPercentagesAndExpiredActiveConfiguration_areRejected() {
        for (double percentage : new double[]{0, -1, 101, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(InvalidInputException.class, () -> PromoEligibility.setAutomatic(
                    offer(100, percentage), PromoCodeRequestModel.builder().automatic(true).build()));
        }
        PromoCode expired = offer(100, 10);
        expired.setExpirationDate(LocalDateTime.now().minusDays(1));
        assertThrows(InvalidInputException.class, () -> PromoEligibility.setAutomatic(
                expired, PromoCodeRequestModel.builder().automatic(true).build()));
    }

    @Test
    void updateWithoutNewFields_preservesSettingsAndCanSwitchBackToManual() {
        PromoCode promo = offer(100, 10);
        PromoEligibility.setAutomatic(promo, PromoCodeRequestModel.builder().build());
        assertTrue(promo.isAutomatic());
        assertEquals(100, promo.getMinimumPurchaseAmount());

        PromoCodeRequestModel manual = PromoCodeRequestModel.builder()
                .automatic(false).code("SAVE10").build();
        PromoEligibility.setAutomatic(promo, manual);
        assertFalse(promo.isAutomatic());
        assertNull(promo.getMinimumPurchaseAmount());
    }

    private void offers(PromoCode... values) {
        when(promos.findAllByAutomaticTrue())
                .thenReturn(Flux.fromArray(values));
    }

    private PromoCode offer(double minimum, double percentage) {
        return PromoCode.builder()
                .id("test")
                .automatic(true)
                .minimumPurchaseAmount(minimum)
                .discount(percentage)
                .isActive(true)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .scope("CART")
                .build();
    }

    private Cart cart(double price, int quantity) {
        return Cart.builder()
                .products(List.of(
                        CartProduct.builder()
                                .productId("product")
                                .productSalePrice(price)
                                .quantityInCart(quantity)
                                .build()
                ))
                .build();
    }

    private CartResponseModel response(Cart cart) {
        return EntityModelUtil.toCartResponseModel(cart, cart.getProducts());
    }
}
