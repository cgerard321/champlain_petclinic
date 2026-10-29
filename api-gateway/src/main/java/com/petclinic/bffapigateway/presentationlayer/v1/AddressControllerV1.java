package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.GeoapifyClient;
import com.petclinic.bffapigateway.dtos.Addresses.AddressSuggestionDTO;
import com.petclinic.bffapigateway.exceptions.BadRequestException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway/addresses")
public class AddressControllerV1 {

    private static final int MIN_QUERY_LENGTH = 3;

    private final GeoapifyClient geoapifyClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.CUST, Roles.RECEPTIONIST})
    @GetMapping(value = "/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AddressSuggestionDTO> autocomplete(@RequestParam String query) {
        String trimmed = query.trim();
        if (trimmed.length() < MIN_QUERY_LENGTH) {
            return Flux.error(new BadRequestException("Query must contain at least 3 characters"));
        }
        return geoapifyClient.autocomplete(trimmed);
    }
}