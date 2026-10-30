package com.petclinic.cartsservice.businesslayer.Payment;

import com.petclinic.cartsservice.presentationlayer.Payment.StripeProductRequestModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeResponseModel;
import com.stripe.model.checkout.Session;
import reactor.core.publisher.Mono;

public interface PaymentService {

    public Mono<StripeResponseModel> createStripeCheckoutSession(StripeProductRequestModel sessionRequestModel, String cartId);
    public Mono<Void> processSuccessfulPayment(Session session);
}