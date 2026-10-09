package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway/customers")
public class CustomersLookupController {

    private final CustomersServiceClient customersServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.OWNER, Roles.VET})
    @GetMapping("/{customerId}")
    public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerById(@PathVariable String customerId,
                                                                     @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return customersServiceClient.getCustomer(customerId, includePhoto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
