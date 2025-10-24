package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gateway/customers")
@RequiredArgsConstructor
@Validated
public class CustomerCartController {

    private final CartServiceClient cartServiceClient;

    private <T> Mono<ResponseEntity<T>> mapToOkOrNotFound(Mono<T> mono) {
        return mono.map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping("/{customerId}/cart")
    public Mono<ResponseEntity<CartResponseDTO>> getCartForCustomer(@PathVariable String customerId) {
        return mapToOkOrNotFound(cartServiceClient.getCartByCustomerId(customerId));
    }
}
