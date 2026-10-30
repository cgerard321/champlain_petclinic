
package com.petclinic.cartsservice.presentationlayer.Payment;

import com.petclinic.cartsservice.businesslayer.Payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WebhookControllerUnitTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private WebhookController webhookController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                webhookController,
                "webhookSecret",
                "whsec_test"
        );
    }

    private final String stripeHookKey = "stripe-test-" + UUID.randomUUID();

    @AfterEach
    void cleanUpSchedulerHook() {
        Schedulers.resetOnScheduleHook(stripeHookKey);
    }

    private Event checkoutEvent(String type) {
        Session eventSession = new Session();
        eventSession.setId("cs_test_123");

        Event event = mock(Event.class, RETURNS_DEEP_STUBS);
        when(event.getType()).thenReturn(type);
        when(event.getDataObjectDeserializer().getObject())
                .thenReturn(Optional.of(eventSession));

        return event;
    }

    private void mockStripeOnWorkerThreads(Event event, Session session) {
        Schedulers.onScheduleHook(stripeHookKey, task -> () -> {
            try (MockedStatic<Webhook> webhookMock = mockStatic(Webhook.class);
                 MockedStatic<Session> sessionMock = mockStatic(Session.class)) {

                webhookMock.when(() ->
                                Webhook.constructEvent(
                                        "test-payload", "test-signature", "whsec_test"))
                        .thenReturn(event);

                sessionMock.when(() -> Session.retrieve("cs_test_123"))
                        .thenReturn(session);

                task.run();
            }
        });
    }

    private Session retrievedSession(String paymentStatus) {
        Session session = new Session();
        session.setId("cs_test_123");
        session.setPaymentStatus(paymentStatus);
        return session;
    }

    @Test
    void webhook_withMissingSignature_returnsBadRequest() {
        StepVerifier.create(webhookController.webhook("{\"id\":\"evt_test\"}", null))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_withEmptySignature_returnsBadRequest() {
        StepVerifier.create(webhookController.webhook("{\"id\":\"evt_test\"}", ""))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_withMissingSecret_returnsBadRequest() {
        ReflectionTestUtils.setField(webhookController, "webhookSecret", "");

        StepVerifier.create(
                        webhookController.webhook(
                                "{\"id\":\"evt_test\"}",
                                "t=1,v1=test"
                        )
                )
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_withNullSecret_returnsBadRequest() {
        ReflectionTestUtils.setField(webhookController, "webhookSecret", null);

        StepVerifier.create(
                        webhookController.webhook(
                                "{\"id\":\"evt_test\"}",
                                "t=1,v1=test"
                        )
                )
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_withMissingPayload_returnsBadRequest() {
        StepVerifier.create(webhookController.webhook(null, "t=1,v1=test"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_withEmptyPayload_returnsBadRequest() {
        StepVerifier.create(webhookController.webhook("", "t=1,v1=test"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_withBlankPayload_returnsBadRequest() {
        StepVerifier.create(webhookController.webhook("   ", "t=1,v1=test"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_withInvalidSignature_returnsBadRequest() {
        StepVerifier.create(webhookController.webhook("{\"id\":\"evt_test\"}","t=1,v1=invalid"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals("Invalid Stripe signature", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_completedAndPaid_processesPayment() {
        Event event = checkoutEvent("checkout.session.completed");
        Session session = retrievedSession("paid");

        mockStripeOnWorkerThreads(event, session);
        when(paymentService.processSuccessfulPayment(session))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        webhookController.webhook("test-payload", "test-signature"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("Payment processed", response.getBody());
                })
                .verifyComplete();

        verify(paymentService).processSuccessfulPayment(session);
    }

    @Test
    void webhook_completedButUnpaid_doesNotProcessPayment() {
        Event event = checkoutEvent("checkout.session.completed");
        Session session = retrievedSession("unpaid");

        mockStripeOnWorkerThreads(event, session);

        StepVerifier.create(
                        webhookController.webhook("test-payload", "test-signature"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("Checkout completed", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_asyncPaymentSucceeded_processesPayment() {
        Event event = checkoutEvent(
                "checkout.session.async_payment_succeeded");
        Session session = retrievedSession("paid");

        mockStripeOnWorkerThreads(event, session);
        when(paymentService.processSuccessfulPayment(session))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        webhookController.webhook("test-payload", "test-signature"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("Payment processed", response.getBody());
                })
                .verifyComplete();

        verify(paymentService).processSuccessfulPayment(session);
    }

    @Test
    void webhook_unrelatedEvent_returnsWebhookReceived() {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn("customer.created");

        mockStripeOnWorkerThreads(event, null);

        StepVerifier.create(
                        webhookController.webhook("test-payload", "test-signature"))
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertEquals("Webhook received", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }

    @Test
    void webhook_paymentServiceFails_returnsInternalServerError() {
        Event event = checkoutEvent("checkout.session.completed");
        Session session = retrievedSession("paid");

        mockStripeOnWorkerThreads(event, session);
        when(paymentService.processSuccessfulPayment(session))
                .thenReturn(Mono.error(
                        new RuntimeException("Payment processing failed")));

        StepVerifier.create(
                        webhookController.webhook("test-payload", "test-signature"))
                .assertNext(response -> {
                    assertEquals(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            response.getStatusCode());
                    assertEquals(
                            "Payment processing failed", response.getBody());
                })
                .verifyComplete();

        verify(paymentService).processSuccessfulPayment(session);
    }

    @Test
    void webhook_sessionIdMissing_returnsInternalServerError() {
        Session eventSession = new Session(); // No ID

        Event event = mock(Event.class, RETURNS_DEEP_STUBS);
        when(event.getType()).thenReturn("checkout.session.completed");
        when(event.getDataObjectDeserializer().getObject())
                .thenReturn(Optional.of(eventSession));

        mockStripeOnWorkerThreads(event, null);

        StepVerifier.create(
                        webhookController.webhook("test-payload", "test-signature"))
                .assertNext(response -> {
                    assertEquals(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            response.getStatusCode());
                    assertEquals(
                            "Payment processing failed", response.getBody());
                })
                .verifyComplete();

        verifyNoInteractions(paymentService);
    }


}
