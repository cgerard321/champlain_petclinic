package com.petclinic.bffapigateway.dtos.apis;

import com.petclinic.bffapigateway.dtos.VisitDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VisitsServiceAPI{
    @GetMapping(
        value = "/visit",
        produces = "application/json"
    )
    Mono<VisitDetails> getVisitDetails(@RequestParam(value = "petId", required = true) int petId);
}