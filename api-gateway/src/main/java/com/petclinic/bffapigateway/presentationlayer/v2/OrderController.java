package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.OrderResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/gateway/orders")
public class OrderController {

    private final CartServiceClient cartServiceClient;

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