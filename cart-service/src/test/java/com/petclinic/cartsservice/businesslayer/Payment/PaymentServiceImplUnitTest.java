package com.petclinic.cartsservice.businesslayer.Payment;

import com.petclinic.cartsservice.businesslayer.AutomaticDiscountService;
import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeProductRequestModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.stripe.model.Address;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import com.petclinic.cartsservice.presentationlayer.BillingRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.stripe.model.Coupon;
import com.stripe.param.CouponCreateParams;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicReference;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplUnitTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private CartService cartService;

    @Mock
    private AutomaticDiscountService automaticDiscountService;

    // by default no automatic discount applies, so the totals pass through unchanged
    @BeforeEach
    void noAutomaticDiscountByDefault() {
        lenient().when(automaticDiscountService.apply(any(CartResponseModel.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private static final String CART_ID = "cart-001";

    private StripeProductRequestModel createRequest(String cartId) {
        return StripeProductRequestModel.builder()
                .cartId(cartId)
                .build();
    }

    private CartProduct createCartProduct(
            double price, int quantity) {

        return CartProduct.builder()
                .productName("Dog Food")
                .productSalePrice(price)
                .quantityInCart(quantity)
                .build();
    }

    private Cart createCart(List<CartProduct> products) {
        return Cart.builder()
                .cartId(CART_ID)
                .products(products)
                .build();
    }

    private SessionCreateParams executeCheckout(
            String frontend,
            Double total,
            Double shipping,
            Double tvq,
            Double tvc,
            Long expectedDiscountCents) {

        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        StripeProductRequestModel request = createRequest(CART_ID);
        request.setFrontendType(frontend);

        ReflectionTestUtils.setField(
                paymentService, "customerFrontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(
                paymentService, "employeeFrontendUrl", "http://localhost:4200");

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        CartResponseModel totals = mock(CartResponseModel.class);
        lenient().when(totals.getTotal()).thenReturn(total);
        lenient().when(totals.getShippingCost()).thenReturn(shipping);
        lenient().when(totals.getTvq()).thenReturn(tvq);
        lenient().when(totals.getTvc()).thenReturn(tvc);

        Session stripeSession = new Session();
        stripeSession.setId("cs_test_123");
        stripeSession.setUrl("https://checkout.stripe.com/test");

        AtomicReference<SessionCreateParams> captured =
                new AtomicReference<>();

        // Obtain this before mocking Schedulers.
        Scheduler immediate = Schedulers.immediate();

        try (MockedStatic<EntityModelUtil> entityMock =
                     mockStatic(EntityModelUtil.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<Schedulers> schedulerMock =
                     mockStatic(Schedulers.class);
             MockedStatic<Session> sessionMock =
                     mockStatic(Session.class);
             MockedStatic<Coupon> couponMock =
                     mockStatic(Coupon.class)) {

            entityMock.when(() ->
                            EntityModelUtil.toCartResponseModel(
                                    cart, cart.getProducts()))
                    .thenReturn(totals);

            schedulerMock.when(Schedulers::boundedElastic)
                    .thenReturn(immediate);

            couponMock.when(() ->
                            Coupon.create(any(CouponCreateParams.class)))
                    .thenAnswer(invocation -> {
                        Coupon coupon = new Coupon();
                        coupon.setId("coupon_test");
                        return coupon;
                    });

            sessionMock.when(() ->
                            Session.create(any(SessionCreateParams.class)))
                    .thenAnswer(invocation -> {
                        captured.set(invocation.getArgument(0));
                        return stripeSession;
                    });

            StepVerifier.create(
                            paymentService.createStripeCheckoutSession(
                                    request, CART_ID))
                    .assertNext(response ->
                            assertEquals(
                                    new StripeResponseModel(
                                            stripeSession.getId(),
                                            stripeSession.getUrl()),
                                    response))
                    .verifyComplete();

            sessionMock.verify(() ->
                    Session.create(any(SessionCreateParams.class)));

            if (expectedDiscountCents == null) {
                couponMock.verifyNoInteractions();
            } else {
                couponMock.verify(() ->
                        Coupon.create(argThat(
                                (CouponCreateParams params) ->
                                        expectedDiscountCents.equals(
                                                params.getAmountOff())
                                                && "cad".equals(
                                                params.getCurrency())
                                                && params.getDuration()
                                                == CouponCreateParams.Duration.ONCE)));
            }
        }

        return captured.get();
    }

    @Test
    void createStripeCheckoutSession_withMismatchedCartIds_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest("cart-999");

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && error.getMessage().equals(
                                "Cart ids does not match."))
                .verify();

        verifyNoInteractions(cartRepository);
    }

    @Test
    void createStripeCheckoutSession_withCartNotFound_returnsNotFoundException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest(CART_ID);

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.empty());

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof NotFoundException
                                && error.getMessage().equals(
                                "Cart not found."))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withEmptyCart_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest(CART_ID);

        Cart cart = createCart(List.of());

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && error.getMessage().equals(
                                "No products found in cart."))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }


    @Test
    void createStripeCheckoutSession_withInvalidPrice_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request = createRequest(CART_ID);

        CartProduct product = createCartProduct(0.0, 2);

        Cart cart = createCart(List.of(product));

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && "Invalid total price amount."
                                .equals(error.getMessage()))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }


    @Test
    void createStripeCheckoutSession_withInvalidQuantity_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request = createRequest(CART_ID);

        CartProduct product = createCartProduct(19.99, 0);

        Cart cart = createCart(List.of(product));

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && "Invalid total price amount."
                                .equals(error.getMessage()))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withMismatchedCartIds_returnsError() {
        // Arrange
        String cartId = "cart-123";

        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId("cart-456");

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(request, cartId);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(cartRepository);
    }

    @Test
    void createStripeCheckoutSession_withCartNotFound_returnsError() {
        // Arrange
        String cartId = "cart-123";

        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(cartId);

        when(cartRepository.findCartByCartId(cartId))
                .thenReturn(Mono.empty());

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(request, cartId);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();

        verify(cartRepository).findCartByCartId(cartId);
    }

    @Test
    void createStripeCheckoutSession_withEmptyCart_returnsError() {
        // Arrange
        String cartId = "cart-123";

        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(cartId);

        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setProducts(Collections.emptyList());

        when(cartRepository.findCartByCartId(cartId))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(request, cartId);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidInputException.class)
                .verify();

        verify(cartRepository).findCartByCartId(cartId);
    }

    @Test
    void createStripeCheckoutSession_withInvalidPromoPercent_returnsError() {
        // Arrange
        String cartId = "cart-123";

        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(cartId);

        CartProduct product = new CartProduct();
        product.setProductName("Dog Food");
        product.setProductSalePrice(25.00);
        product.setQuantityInCart(2);

        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setProducts(List.of(product));
        cart.setPromoPercent(150.0);

        when(cartRepository.findCartByCartId(cartId))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(request, cartId);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidInputException.class)
                .verify();

        verify(cartRepository).findCartByCartId(cartId);
    }

    @Test
    void createStripeCheckoutSession_withInvalidProductPrice_returnsError() {
        // Arrange
        String cartId = "cart-123";

        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(cartId);

        CartProduct product = new CartProduct();
        product.setProductName("Dog Food");
        product.setProductSalePrice(0.0);
        product.setQuantityInCart(2);

        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setProducts(List.of(product));
        cart.setPromoPercent(0.0);

        when(cartRepository.findCartByCartId(cartId))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(request, cartId);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidInputException.class)
                .verify();

        verify(cartRepository).findCartByCartId(cartId);
    }

    @Test
    void processSuccessfulPayment_withUnpaidSession_returnsError() {
        // Arrange
        Session session = new Session();
        session.setPaymentStatus("unpaid");

        // Act
        Mono<Void> result = paymentService.processSuccessfulPayment(session);

        // Assert
        StepVerifier.create(result)
                .expectError(InvalidInputException.class)
                .verify();

        verifyNoInteractions(orderRepository, cartRepository, cartService);
    }

    @Test
    void processSuccessfulPayment_withAlreadyProcessedSession_doesNotCreateDuplicateOrder() {
        // Arrange
        String stripeSessionId = "cs_test_123";

        Session session = new Session();
        session.setPaymentStatus("paid");
        session.setId(stripeSessionId);
        session.setMetadata(Map.of("cartId", CART_ID));

        when(orderRepository.existsByStripeSessionId(stripeSessionId))
                .thenReturn(Mono.just(true));

        // Act
        Mono<Void> result = paymentService.processSuccessfulPayment(session);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(orderRepository).existsByStripeSessionId(stripeSessionId);
        verifyNoInteractions(cartRepository, cartService);
    }

    @Test
    void processSuccessfulPayment_withValidPayment_checkoutsCart() {
        // Arrange
        String stripeSessionId = "cs_test_123";

        CartProduct product = createCartProduct(20.00, 2);
        Cart cart = createCart(List.of(product));

        double expectedTotal = EntityModelUtil
                .toCartResponseModel(cart, cart.getProducts())
                .getTotal();

        long expectedAmount = Math.round(expectedTotal * 100);

        Session session = new Session();
        session.setPaymentStatus("paid");
        session.setId(stripeSessionId);
        session.setMetadata(Map.of(
                "cartId", CART_ID,
                "expectedAmountCents", String.valueOf(expectedAmount)));
        session.setAmountTotal(expectedAmount);
        session.setCurrency("cad");

        Address address = new Address();
        address.setLine1("123 Main St");
        address.setCity("Montreal");
        address.setState("QC");
        address.setPostalCode("H1H 1H1");
        Session.CustomerDetails details = new Session.CustomerDetails();
        details.setName("Jane Doe");
        details.setEmail("jane@example.com");
        details.setAddress(address);
        session.setCustomerDetails(details);

        when(orderRepository.existsByStripeSessionId(stripeSessionId))
                .thenReturn(Mono.just(false));

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        when(cartService.checkoutCart(eq(CART_ID), eq(stripeSessionId), any(BillingRequestModel.class)))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = paymentService.processSuccessfulPayment(session);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        ArgumentCaptor<BillingRequestModel> billingCaptor =
                ArgumentCaptor.forClass(BillingRequestModel.class);
        verify(cartService).checkoutCart(eq(CART_ID), eq(stripeSessionId), billingCaptor.capture());

        BillingRequestModel billing = billingCaptor.getValue();
        assertEquals("Jane Doe", billing.getCustomerName());
        assertEquals("jane@example.com", billing.getEmail());
        assertEquals("123 Main St", billing.getAddress());
        assertEquals("Montreal", billing.getCity());
        assertEquals("QC", billing.getProvince());
        assertEquals("H1H 1H1", billing.getPostalCode());
    }

    @Test
    void processSuccessfulPayment_whenCartChangedAfterSessionCreated_returnsError() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        Session session = createPaidSession();
        session.setAmountTotal(standardCartAmountCents());

        // the customer added another item in a different tab after going to Stripe
        cart.setProducts(List.of(createCartProduct(20.00, 3)));

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        StepVerifier.create(paymentService.processSuccessfulPayment(session))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && "Cart changed after payment was started."
                                .equals(error.getMessage()))
                .verify();

        verifyNoInteractions(cartService);
    }

    @Test
    void processSuccessfulPayment_whenSameSessionIsSavedTwice_completesWithoutError() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        Session session = createPaidSession();
        session.setAmountTotal(standardCartAmountCents());

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));
        when(cartService.checkoutCart(eq(CART_ID), eq(session.getId()), any(BillingRequestModel.class)))
                .thenReturn(Mono.error(new org.springframework.dao.DuplicateKeyException("duplicate stripeSessionId")));

        StepVerifier.create(paymentService.processSuccessfulPayment(session))
                .verifyComplete();
    }

    @Test
    void processSuccessfulPayment_withMissingCartMetadata_returnsError() {
        Session session = new Session();
        session.setPaymentStatus("paid");
        session.setId("cs_test_123");

        // Covers null metadata, missing cartId, and empty cartId.
        List<Map<String, String>> metadataCases = new java.util.ArrayList<>();
        metadataCases.add(null);
        metadataCases.add(Map.of());
        metadataCases.add(Map.of("cartId", ""));

        for (Map<String, String> metadata : metadataCases) {
            session.setMetadata(metadata);

            StepVerifier.create(paymentService.processSuccessfulPayment(session))
                    .expectErrorMatches(error ->
                            error instanceof InvalidInputException
                                    && "Cart id is empty.".equals(error.getMessage()))
                    .verify();
        }

        verifyNoInteractions(orderRepository, cartRepository, cartService);
    }

    @Test
    void processSuccessfulPayment_withMissingSessionId_returnsError() {
        Session session = new Session();
        session.setPaymentStatus("paid");
        session.setMetadata(Map.of("cartId", CART_ID));

        for (String sessionId : new String[]{null, ""}) {
            session.setId(sessionId);

            StepVerifier.create(paymentService.processSuccessfulPayment(session))
                    .expectErrorMatches(error ->
                            error instanceof InvalidInputException
                                    && "Stripe session id is empty."
                                    .equals(error.getMessage()))
                    .verify();
        }

        verifyNoInteractions(orderRepository, cartRepository, cartService);
    }

    @Test
    void processSuccessfulPayment_withMissingCart_returnsNotFoundException() {
        Session session = createPaidSession();

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.empty());

        StepVerifier.create(paymentService.processSuccessfulPayment(session))
                .expectErrorMatches(error ->
                        error instanceof NotFoundException
                                && "Cart not found.".equals(error.getMessage()))
                .verify();

        verifyNoInteractions(cartService);
    }

    @Test
    void processSuccessfulPayment_withMissingOrIncorrectAmount_returnsError() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        Session session = createPaidSession();

        long expectedAmount = Math.round(
                EntityModelUtil.toCartResponseModel(cart, cart.getProducts())
                        .getTotal() * 100);

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        for (Long amount : new Long[]{
                null, expectedAmount - 1, expectedAmount + 1
        }) {
            session.setAmountTotal(amount);

            StepVerifier.create(paymentService.processSuccessfulPayment(session))
                    .expectErrorMatches(error ->
                            error instanceof InvalidInputException
                                    && "Total amount is invalid."
                                    .equals(error.getMessage()))
                    .verify();
        }

        verifyNoInteractions(cartService);
    }

    @Test
    void processSuccessfulPayment_withMissingOrIncorrectCurrency_returnsError() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        Session session = createPaidSession();
        session.setAmountTotal(Math.round(
                EntityModelUtil.toCartResponseModel(cart, cart.getProducts())
                        .getTotal() * 100));

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        for (String currency : new String[]{null, "", "usd"}) {
            session.setCurrency(currency);

            StepVerifier.create(paymentService.processSuccessfulPayment(session))
                    .expectErrorMatches(error ->
                            error instanceof InvalidInputException
                                    && "Currency is invalid."
                                    .equals(error.getMessage()))
                    .verify();
        }

        verifyNoInteractions(cartService);
    }

    @Test
    void processSuccessfulPayment_whenCheckoutFails_propagatesError() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        Session session = createPaidSession();
        session.setAmountTotal(Math.round(
                EntityModelUtil.toCartResponseModel(cart, cart.getProducts())
                        .getTotal() * 100));

        RuntimeException failure = new RuntimeException("Checkout failed");

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));
        when(cartService.checkoutCart(eq(CART_ID), eq(session.getId()), any(BillingRequestModel.class)))
                .thenReturn(Mono.error(failure));

        StepVerifier.create(paymentService.processSuccessfulPayment(session))
                .expectErrorMatches(error -> error == failure)
                .verify();

        verify(cartService).checkoutCart(eq(CART_ID), eq(session.getId()), any(BillingRequestModel.class));
    }

    private Session createPaidSession() {
        Session session = new Session();
        session.setPaymentStatus("paid");
        session.setId("cs_test_123");
        session.setMetadata(Map.of(
                "cartId", CART_ID,
                "expectedAmountCents", String.valueOf(standardCartAmountCents())));
        session.setCurrency("cad");
        return session;
    }

    // total of the 2 x $20.00 cart most tests use
    private long standardCartAmountCents() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        return Math.round(
                EntityModelUtil.toCartResponseModel(cart, cart.getProducts()).getTotal() * 100);
    }

    @Test
    void processSuccessfulPayment_withMissingExpectedAmountMetadata_returnsError() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        Session session = createPaidSession();
        session.setMetadata(Map.of("cartId", CART_ID));
        session.setAmountTotal(standardCartAmountCents());

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        StepVerifier.create(paymentService.processSuccessfulPayment(session))
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && "Total amount is invalid."
                                .equals(error.getMessage()))
                .verify();

        verifyNoInteractions(cartService);
    }

    @Test
    void createStripeCheckoutSession_buildsProductShippingAndTaxItems() {
        // Products: $40 + shipping: $5 + taxes: $4 + $2 = $51
        SessionCreateParams params =
                executeCheckout("react", 51.0, 5.0, 4.0, 2.0, null);

        assertEquals(SessionCreateParams.Mode.PAYMENT, params.getMode());
        assertEquals(4, params.getLineItems().size());

        assertLineItem(params.getLineItems().get(0), "Dog Food", 2000L, 2L);
        assertLineItem(params.getLineItems().get(1), "Shipping", 500L, 1L);
        assertLineItem(params.getLineItems().get(2), "TVQ Taxes", 400L, 1L);
        assertLineItem(params.getLineItems().get(3), "TVC Taxes", 200L, 1L);

        assertEquals(CART_ID, params.getMetadata().get("cartId"));
        assertEquals("5100",
                params.getMetadata().get("expectedAmountCents"));
    }

    @Test
    void createStripeCheckoutSession_discount_createsCoupon() {
        // Products: $40, expected total: $35 => $5 coupon
        SessionCreateParams params =
                executeCheckout("react", 35.0, 0.0, 0.0, 0.0, 500L);

        assertEquals(1, params.getLineItems().size());
        assertEquals(1, params.getDiscounts().size());
        assertEquals("coupon_test",
                params.getDiscounts().get(0).getCoupon());
    }

    @Test
    void createStripeCheckoutSession_automaticDiscount_chargesDiscountedTotal() {
        // Products: $40, the automatic discount brings the cart total to $30 => $10 coupon
        CartResponseModel discounted = new CartResponseModel();
        discounted.setTotal(30.0);
        when(automaticDiscountService.apply(any(CartResponseModel.class)))
                .thenReturn(Mono.just(discounted));

        SessionCreateParams params =
                executeCheckout("react", 40.0, 0.0, 0.0, 0.0, 1000L);

        assertEquals("3000", params.getMetadata().get("expectedAmountCents"));
        assertEquals(1, params.getDiscounts().size());
    }

    @Test
    void processSuccessfulPayment_withAutomaticDiscount_acceptsDiscountedAmount() {
        Cart cart = createCart(List.of(createCartProduct(20.00, 2)));
        Session session = createPaidSession();
        session.setMetadata(Map.of("cartId", CART_ID, "expectedAmountCents", "3000"));
        session.setAmountTotal(3000L);

        CartResponseModel discounted = new CartResponseModel();
        discounted.setTotal(30.0);

        when(orderRepository.existsByStripeSessionId(session.getId()))
                .thenReturn(Mono.just(false));
        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));
        when(automaticDiscountService.apply(any(CartResponseModel.class)))
                .thenReturn(Mono.just(discounted));
        when(cartService.checkoutCart(eq(CART_ID), eq(session.getId()), any(BillingRequestModel.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(paymentService.processSuccessfulPayment(session))
                .verifyComplete();

        verify(cartService).checkoutCart(eq(CART_ID), eq(session.getId()), any(BillingRequestModel.class));
    }

    @Test
    void createStripeCheckoutSession_higherExpectedTotal_addsAdjustment() {
        // Products: $40, expected total: $40.01 => 1-cent adjustment
        SessionCreateParams params =
                executeCheckout("react", 40.01, 0.0, 0.0, 0.0, null);

        assertEquals(2, params.getLineItems().size());
        assertLineItem(
                params.getLineItems().get(1),
                "Rounding adjustment", 1L, 1L);
    }

    @Test
    void createStripeCheckoutSession_frontendSelection_buildsCorrectUrls() {
        for (String frontend :
                new String[]{null, "", "   ", "react", "REACT",
                        "angular", "ANGULAR"}) {

            SessionCreateParams params =
                    executeCheckout(frontend, 40.0, 0.0, 0.0, 0.0, null);

            String baseUrl = "angular".equalsIgnoreCase(frontend)
                    ? "http://localhost:4200"
                    : "http://localhost:3000";

            assertEquals(
                    baseUrl + "/carts/" + CART_ID
                            + "?payment=success&session_id={CHECKOUT_SESSION_ID}",
                    params.getSuccessUrl());

            assertEquals(
                    baseUrl + "/carts/" + CART_ID + "?payment=cancelled",
                    params.getCancelUrl());
        }
    }

    private void assertLineItem(SessionCreateParams.LineItem item, String name, long amount, long quantity) {

        assertEquals(Long.valueOf(quantity), item.getQuantity());
        assertEquals("cad", item.getPriceData().getCurrency());
        assertEquals(Long.valueOf(amount),
                item.getPriceData().getUnitAmount());
        assertEquals(name,
                item.getPriceData().getProductData().getName());
    }



}