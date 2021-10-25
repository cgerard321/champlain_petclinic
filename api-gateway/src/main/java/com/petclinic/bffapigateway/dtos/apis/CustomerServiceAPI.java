package com.petclinic.bffapigateway.dtos.apis;

import com.petclinic.bffapigateway.dtos.OwnerDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CustomerServiceAPI {
    @GetMapping(
            value = "/owners/",
            produces = "application/json"
    )
    Mono<OwnerDetails> getCustomerDetails(@RequestParam(value = "ownerId", required = true) int ownerId);
}
