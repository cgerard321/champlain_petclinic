package com.petclinic.cartsservice.presentationlayer.Payment;

import com.petclinic.cartsservice.businesslayer.Payment.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/api/v1/carts")
public class WebhookController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret.key:}")
    private String webhookSecret;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/webhook")
    public Mono<ResponseEntity<String>> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {

        if (payload == null || payload.isBlank()
                || signature == null || signature.isBlank()
                || webhookSecret == null || webhookSecret.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Stripe signature"));
        }

        return Mono.fromCallable(() -> Webhook.constructEvent(payload, signature, webhookSecret))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::handleEvent)
                .onErrorResume(SignatureVerificationException.class, e -> {
                    log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body("Invalid Stripe signature"));
                })
                .onErrorResume(StripeException.class, e -> {
                    log.error("Stripe webhook processing failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body("Invalid Stripe event"));
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("Stripe webhook payload could not be read: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body("Invalid Stripe event"));
                });
    }

    private Mono<ResponseEntity<String>> handleEvent(Event event) {
        String eventType = event.getType();
        if (!"checkout.session.completed".equals(eventType)
                && !"checkout.session.async_payment_succeeded".equals(eventType)) {
            return Mono.just(ResponseEntity.ok("Webhook received"));
        }

        return Mono.fromCallable(() -> Session.retrieve(extractCheckoutSessionId(event)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(session -> {
                    // checkout.session.completed also fires for delayed payment methods that are not paid yet
                    if (!"paid".equals(session.getPaymentStatus())) {
                        return Mono.just(ResponseEntity.ok().body("Checkout completed"));
                    }
                    return paymentService.processSuccessfulPayment(session)
                            .thenReturn(ResponseEntity.ok().body("Payment processed"));
                })
                .onErrorResume(e -> {
                    log.error("Stripe checkout webhook failed: {}", e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().body("Payment processing failed"));
                });
    }

    private String extractCheckoutSessionId(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseGet(() -> {
                    try {
                        return event.getDataObjectDeserializer().deserializeUnsafe();
                    } catch (Exception e) {
                        return null;
                    }
                });

        if (stripeObject instanceof Session session && session.getId() != null) {
            return session.getId();
        }

        throw new IllegalArgumentException("Unable to read Stripe checkout session id.");
    }
}
