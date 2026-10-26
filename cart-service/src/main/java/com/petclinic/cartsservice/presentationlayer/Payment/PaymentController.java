package com.petclinic.cartsservice.presentationlayer.Payment;

import com.petclinic.cartsservice.businesslayer.Payment.PaymentServiceImpl;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/carts")
@CrossOrigin(origins = {"http://localhost:3000", "https://petclinic.benmusicgeek.synology.me"})
public class PaymentController {


    private final PaymentServiceImpl paymentService;

    public PaymentController(PaymentServiceImpl paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{cartId}/transaction")
    public Mono<ResponseEntity<StripeResponseModel>> createStripeBill(@RequestBody StripeProductRequestModel sessionRequestModel, @PathVariable String cartId) {

        System.out.println("CARTS PAYMENT CONTROLLER REACHED");

        return paymentService.createStripeBill(sessionRequestModel, cartId)
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(InvalidInputException.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }

}