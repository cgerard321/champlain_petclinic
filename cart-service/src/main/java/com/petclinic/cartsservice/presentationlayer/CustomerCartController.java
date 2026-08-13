package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerCartController {

    private final CartService cartService;

    @GetMapping(value = "/{customerId}/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<CartResponseModel>> getCartForCustomer(@PathVariable String customerId) {
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided customer id is invalid: " + customerId)))
                .flatMap(cartService::findCartByCustomerId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{customerId}/cart/recent-purchases", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<CartProduct>>> getRecentPurchasesForCustomer(@PathVariable String customerId) {
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided customer id is invalid: " + customerId)))
                .flatMap(cartService::getRecentPurchasesByCustomerId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{customerId}/cart/recommendation-purchases", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<CartProduct>>> getRecommendationPurchasesForCustomer(@PathVariable String customerId) {
        return Mono.just(customerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided customer id is invalid: " + customerId)))
                .flatMap(cartService::getRecommendationPurchasesByCustomerId)
                .map(ResponseEntity::ok);
    }
}
