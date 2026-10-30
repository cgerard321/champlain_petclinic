package com.petclinic.cartsservice.presentationlayer.Payment;

import com.petclinic.cartsservice.businesslayer.Payment.PaymentService;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/carts")
public class PaymentController {


    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{cartId}/checkout/sessions")
    public Mono<ResponseEntity<StripeResponseModel>> createStripeCheckoutSession(@RequestBody StripeProductRequestModel sessionRequestModel, @PathVariable String cartId) {

        return paymentService.createStripeCheckoutSession(sessionRequestModel, cartId)
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(InvalidInputException.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }

}