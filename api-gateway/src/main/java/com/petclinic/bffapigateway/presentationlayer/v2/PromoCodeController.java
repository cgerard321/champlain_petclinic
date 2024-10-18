package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.PromoCodeResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/gateway/promos")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class PromoCodeController {

    private final CartServiceClient cartServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PromoCodeResponseDTO> getAllPromos() {
        return cartServiceClient.getAllPromoCodes();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseDTO>> createPromoCode(@RequestBody PromoCodeRequestDTO promoCodeRequestDTO
    ) {
        return cartServiceClient.createPromoCode(promoCodeRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping(value = "/{promoCodeId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseDTO>> updatePromoCode(@RequestBody PromoCodeRequestDTO promoCodeRequestDTO,
                                                                      @PathVariable String promoCodeId) {
        return cartServiceClient.updatePromoCode(promoCodeId, promoCodeRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/{promoCodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseDTO>> getPromoCodeById(@PathVariable String promoCodeId) {
        return cartServiceClient.getPromoCodeById(promoCodeId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{promoCodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PromoCodeResponseDTO>> deletePromoById(@PathVariable String promoCodeId) {
        return cartServiceClient.deletePromoCode(promoCodeId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }

    @GetMapping(value = "/actives", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PromoCodeResponseDTO> getActivePromos() {
        return cartServiceClient.getActivePromos();
    }


}
