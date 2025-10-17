package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.PromoCodeService;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeRequestModel;
import com.petclinic.cartsservice.domainclientlayer.PromoCodeResponseModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:4200"}, allowCredentials = "true")
@RequestMapping("/api/v1/promos")
@Slf4j
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PromoCodeResponseModel> getAllPromoCodes() {
        return promoCodeService.getAllPromoCodes();
    }

    @GetMapping(value = "/{promoCodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseModel>> getPromoCodeById(@PathVariable String promoCodeId) {
        return Mono.just(promoCodeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided promo code ID is invalid: " + promoCodeId)))
                .flatMap(promoCodeService::getPromoCodeById)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Promo code not found")));  // Handle not found case
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseModel>> createPromoCode(@RequestBody PromoCodeRequestModel promoCodeRequestModel) {
        return promoCodeService.createPromo(promoCodeRequestModel)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping(value = "/{promoCodeId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseModel>> updatePromoCodeById(
            @RequestBody PromoCodeRequestModel promoCodeRequestModel,
            @PathVariable String promoCodeId) {
        return Mono.just(promoCodeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided promo code ID is invalid: " + promoCodeId)))
                .flatMap(id -> promoCodeService.updatePromoCodeById(promoCodeRequestModel, id))  // Call service to update the promo code
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @DeleteMapping("/{promoCodeId}")
    public Mono<ResponseEntity<PromoCodeResponseModel>> deletePromoCodeById(@PathVariable String promoCodeId) {
        return Mono.just(promoCodeId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided promo code ID is invalid: " + promoCodeId)))
                .flatMap(promoCodeService::deletePromoCode)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());  // Handle case where no promo code is found
    }


    @GetMapping(value = "/validate/{promoCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseModel>> validatePromoCode(@PathVariable String promoCode) {
        return promoCodeService.getPromoCodeByCode(promoCode)
                .filter(PromoCodeResponseModel::isActive)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promo code is not valid")));
    }

    @GetMapping(value = "/actives", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PromoCodeResponseModel> getActivePromos() {
        return promoCodeService.getActivePromos()
                .doOnNext(promo -> log.debug("Active Promo: " + promo));
    }


}
