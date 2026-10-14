package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.OrderResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway/orders")
public class OrderControllerV1 {

    private final CartServiceClient cartServiceClient;
    @SecuredEndpoint(allowedRoles = {Roles.OWNER, Roles.ADMIN})
    @GetMapping(
            value = "/customer/{customerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Flux<OrderResponseDTO> getOrdersByCustomerId(
            @PathVariable String customerId
    ) {
        return cartServiceClient.getOrdersByCustomerId(customerId);
    }
}