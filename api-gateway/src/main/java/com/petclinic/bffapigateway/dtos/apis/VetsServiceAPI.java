package com.petclinic.bffapigateway.dtos.apis;

import com.petclinic.bffapigateway.dtos.VetDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VetsServiceAPI {
    @GetMapping(
        value = "",
        produces = "application/json"
    )
    Mono<VetDetails> getVetDetails(@RequestParam(value = "vetId", required = true) int vetId);
}