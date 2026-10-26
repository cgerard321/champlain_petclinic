package com.petclinic.cartsservice.businesslayer.Payment;

import com.petclinic.cartsservice.presentationlayer.Payment.StripeProductRequestModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeResponseModel;
import reactor.core.publisher.Mono;

public interface PaymentService {

    public Mono<StripeResponseModel> createStripeBill(StripeProductRequestModel sessionRequestModel, String cartId);
}