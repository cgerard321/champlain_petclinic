package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.*;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.*;
import com.petclinic.cartsservice.utils.*;
import com.petclinic.cartsservice.utils.exceptions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ScopedPromoTest {
    private final CartRepository carts = mock(CartRepository.class);
    private final PromoRepository promos = mock(PromoRepository.class);
    private final ProductClient products = mock(ProductClient.class);
    private final CartServiceImpl service = new CartServiceImpl(carts, products, mock(CustomerClient.class),
            mock(OrderServiceImpl.class), promos);
    private Cart cart;
    private PromoCode promo;

    @BeforeEach
    void setup() {
        cart = Cart.builder().cartId("cart").shippingMethod(ShippingMethod.STANDARD)
                .products(new ArrayList<>(List.of(line("food", "FOOD", 20, 2), line("toy", "TOY", 60, 1)))).build();
        promo = PromoCode.builder().code("SAVE").discount(25).isActive(true)
                .expirationDate(LocalDateTime.now().plusDays(1)).build();
        lenient().when(carts.findCartByCartId("cart")).thenReturn(Mono.just(cart));
        lenient().when(carts.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        lenient().when(promos.findPromoCodeByCode("SAVE")).thenReturn(Mono.just(promo));
    }

    private CartProduct line(String id, String type, double price, int quantity) {
        return CartProduct.builder().productId(id).productType(type).productSalePrice(price).quantityInCart(quantity).build();
    }

    @ParameterizedTest
    @CsvSource({"CART,25,10,15", "PRODUCT,10,10,0", "PRODUCT_TYPE,10,10,0"})
    void mixedCartDiscountsOnlyEligibleLines(String scope, double discount, double foodDiscount, double toyDiscount) {
        promo.setScope(scope);
        promo.setProductIds(List.of("food"));
        promo.setProductTypes(List.of("FOOD"));
        if (scope.equals("PRODUCT_TYPE")) {
            // Refresh pre-existing cart snapshots from authoritative products.
            cart.getProducts().forEach(p -> p.setProductType(null));
            when(products.getProductByProductId("food")).thenReturn(Mono.just(ProductResponseModel.builder().productType("FOOD").build()));
            when(products.getProductByProductId("toy")).thenReturn(Mono.just(ProductResponseModel.builder().productType("TOY").build()));
        }
        StepVerifier.create(service.applyPromoCodeToCart("cart", " SAVE ")).assertNext(result -> {
            assertEquals(discount, result.getDiscountAmount());
            assertEquals(foodDiscount, result.getProducts().get(0).getDiscountAmount());
            assertEquals(toyDiscount, result.getProducts().get(1).getDiscountAmount());
            double taxable = 100 - discount + ShippingMethod.STANDARD.getCost();
            assertEquals(taxable, result.getSubtotal(), 0.001);
            assertEquals(Math.round(taxable * .09975 * 100) / 100.0, result.getTvq());
            assertEquals(Math.round(taxable * .05 * 100) / 100.0, result.getTvc());
        }).verifyComplete();
        assertSame(promo, cart.getAppliedPromo());
        assertEquals(0, cart.getProducts().get(0).getDiscountAmount(), "Response calculation must not mutate stored lines");
        Cart order = EntityModelUtil.storeCartOrder(cart);
        assertEquals(100 - discount + ShippingMethod.STANDARD.getCost(), order.getSubtotal(), .001);
        assertEquals(foodDiscount, order.getProducts().get(0).getDiscountAmount());
    }

    @Test
    void failedLookupSkipsOnlyThatDiscountAndRetryRestoresIt() {
        promo.setScope("PRODUCT_TYPE");
        promo.setProductTypes(List.of("FOOD"));
        when(products.getProductByProductId("food"))
                .thenReturn(Mono.error(new RuntimeException("Products unavailable")));
        when(products.getProductByProductId("toy"))
                .thenReturn(Mono.just(ProductResponseModel.builder().productType("FOOD").build()));

        StepVerifier.create(service.applyPromoCodeToCart("cart", "SAVE"))
                .assertNext(result -> {
                    assertEquals(2, result.getProducts().size());
                    assertTrue(result.getProducts().get(0).isPromoLookupFailed());
                    assertEquals(0, result.getProducts().get(0).getDiscountAmount());
                    assertFalse(result.getProducts().get(1).isPromoLookupFailed());
                    assertEquals(15, result.getProducts().get(1).getDiscountAmount());
                    assertEquals(15, result.getDiscountAmount());
                }).verifyComplete();
        verify(products).getProductByProductId("toy");
        var reloaded = EntityModelUtil.toCartResponseModel(cart, cart.getProducts());
        assertTrue(reloaded.getProducts().get(0).isPromoLookupFailed());
        assertEquals(15, reloaded.getDiscountAmount());
        Cart checkout = EntityModelUtil.storeCartOrder(cart);
        assertEquals(0, checkout.getProducts().get(0).getDiscountAmount());
        assertEquals(15, checkout.getProducts().get(1).getDiscountAmount());

        when(products.getProductByProductId("food"))
                .thenReturn(Mono.just(ProductResponseModel.builder().productType("FOOD").build()));
        StepVerifier.create(service.applyPromoCodeToCart("cart", "SAVE"))
                .assertNext(result -> {
                    assertFalse(result.getProducts().get(0).isPromoLookupFailed());
                    assertEquals(25, result.getDiscountAmount());
                }).verifyComplete();
    }

    @Test
    void emptyProductLookupIsExcludedAndClearingPromoClearsWarning() {
        promo.setScope("PRODUCT_TYPE");
        promo.setProductTypes(List.of("FOOD"));
        when(products.getProductByProductId("food")).thenReturn(Mono.empty());
        when(products.getProductByProductId("toy"))
                .thenReturn(Mono.just(ProductResponseModel.builder().productType("FOOD").build()));
        StepVerifier.create(service.applyPromoCodeToCart("cart", "SAVE"))
                .assertNext(result -> assertTrue(result.getProducts().get(0).isPromoLookupFailed()))
                .verifyComplete();
        StepVerifier.create(service.clearPromoFromCart("cart"))
                .assertNext(result -> {
                    assertNull(result.getPromoPercent());
                    assertEquals(0, result.getDiscountAmount());
                    assertTrue(result.getProducts().stream().noneMatch(CartProduct::isPromoLookupFailed));
                }).verifyComplete();
    }

    @Test
    void allLookupsFailNeverSavesOrChangesExistingCart() {
        promo.setScope("PRODUCT_TYPE");
        promo.setProductTypes(List.of("FOOD"));
        when(products.getProductByProductId(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Products unavailable")));
        StepVerifier.create(service.applyPromoCodeToCart("cart", "SAVE"))
                .expectError(InvalidInputException.class).verify();
        verify(carts, never()).save(any());
        assertNull(cart.getAppliedPromo());
        assertTrue(cart.getProducts().stream().noneMatch(CartProduct::isPromoLookupFailed));
    }

    @Test
    void discountRecalculatesAfterQuantityChangeRemovalAndClear() {
        promo.setScope("PRODUCT"); promo.setProductIds(List.of("food"));
        service.applyPromoCodeToCart("cart", "SAVE").block();
        cart.getProducts().get(0).setQuantityInCart(3);
        assertEquals(15, EntityModelUtil.toCartResponseModel(cart, cart.getProducts()).getDiscountAmount());
        cart.getProducts().remove(0);
        assertEquals(0, EntityModelUtil.toCartResponseModel(cart, cart.getProducts()).getDiscountAmount());
        StepVerifier.create(service.clearPromoFromCart("cart")).assertNext(result -> {
            assertNull(result.getPromoPercent()); assertEquals(0, result.getDiscountAmount());
        }).verifyComplete();
        assertNull(cart.getAppliedPromo());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void missingCodeRejected(String code) {
        StepVerifier.create(service.applyPromoCodeToCart("cart", code)).expectError(InvalidInputException.class).verify();
        verifyNoInteractions(promos);
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "inactive", "expired", "zero", "negative", "over100", "nan", "infinite", "noMatch", "emptyCart", "missingCart"})
    void invalidOrIneligibleCodeNeverSaved(String reason) {
        switch (reason) {
            case "unknown" -> when(promos.findPromoCodeByCode("SAVE")).thenReturn(Mono.empty());
            case "inactive" -> promo.setActive(false);
            case "expired" -> promo.setExpirationDate(LocalDateTime.now().minusDays(1));
            case "zero" -> promo.setDiscount(0);
            case "negative" -> promo.setDiscount(-1);
            case "over100" -> promo.setDiscount(101);
            case "nan" -> promo.setDiscount(Double.NaN);
            case "infinite" -> promo.setDiscount(Double.POSITIVE_INFINITY);
            case "noMatch" -> { promo.setScope("PRODUCT"); promo.setProductIds(List.of("other")); }
            case "emptyCart" -> cart.setProducts(null);
            case "missingCart" -> when(carts.findCartByCartId("cart")).thenReturn(Mono.empty());
        }
        StepVerifier.create(service.applyPromoCodeToCart("cart", "SAVE"))
                .expectError(reason.equals("missingCart") ? NotFoundException.class : InvalidInputException.class).verify();
        verify(carts, never()).save(any());
    }

    @Test
    void expiredAppliedPromoStopsDiscounting() {
        cart.setAppliedPromo(promo); cart.setPromoPercent(25.0);
        promo.setExpirationDate(LocalDateTime.now().minusSeconds(1));
        var response = EntityModelUtil.toCartResponseModel(cart, cart.getProducts());
        assertEquals(0, response.getDiscountAmount()); assertNull(response.getPromoPercent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CART", "PRODUCT", "PRODUCT_TYPE"})
    void createAndUpdatePersistScopeAndEligibleItems(String scope) {
        var request = PromoCodeRequestModel.builder().Name("Save").code("SAVE").discount(25)
                .expirationDate("2099-12-31T23:59:59").scope(scope)
                .productIds(Arrays.asList(null, " ", " food ", "food"))
                .productTypes(List.of(" FOOD ", "FOOD")).build();
        when(promos.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(promos.findById("id")).thenReturn(Mono.just(promo));
        var management = new PromoCodeServiceImpl(promos);
        var created = management.createPromo(request).block();
        var updated = management.updatePromoCodeById(request, "id").block();
        for (var result : List.of(created, updated)) {
            assertEquals(scope, result.getScope());
            assertEquals(scope.equals("PRODUCT") ? List.of("food") : List.of(), result.getProductIds());
            assertEquals(scope.equals("PRODUCT_TYPE") ? List.of("FOOD") : List.of(), result.getProductTypes());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"OTHER", "PRODUCT", "PRODUCT_TYPE"})
    void invalidScopeOrEmptySelectionRejected(String scope) {
        var request = PromoCodeRequestModel.builder().scope(scope).build();
        assertThrows(InvalidInputException.class, () -> PromoEligibility.setScope(promo, request));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 101, Double.NaN, Double.POSITIVE_INFINITY})
    void invalidDiscountRejected(double discount) {
        assertThrows(InvalidInputException.class, () -> PromoEligibility.setScope(promo,
                PromoCodeRequestModel.builder().discount(discount).build()));
    }

    @Test
    void legacyPromoDefaultsToCartAndUnknownScopeFailsClosed() {
        assertEquals("CART", EntityModelUtil.toPromoCodeResponseModel(promo).getScope());
        assertTrue(PromoEligibility.isEligible(promo, cart.getProducts().get(0)));
        promo.setScope("UNKNOWN"); assertFalse(PromoEligibility.isEligible(promo, cart.getProducts().get(0)));
        promo.setScope("PRODUCT"); assertFalse(PromoEligibility.isEligible(promo, cart.getProducts().get(0)));
        promo.setScope("PRODUCT_TYPE"); assertFalse(PromoEligibility.isEligible(promo, cart.getProducts().get(0)));
    }

    @Test
    void updateWithoutScopePreservesExistingEligibility() {
        promo.setScope("PRODUCT"); promo.setProductIds(List.of("food"));
        EntityModelUtil.mapPromoCode(promo, PromoCodeRequestModel.builder().discount(20).build());
        assertEquals("PRODUCT", promo.getScope());
        assertEquals(List.of("food"), promo.getProductIds());
    }
}
