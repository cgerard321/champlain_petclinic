package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/bills")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class BillController {
    private final BillServiceClient billService;

    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "/customer/{customerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByOwnerId(final @PathVariable String customerId)
    {
        return billService.getBillsByOwnerId(customerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBills()
    {
        return billService.getAllBilling();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/{billId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BillResponseDTO> getBilling(final @PathVariable String billId)
    {
        return billService.getBilling(billId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{billId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBill(@PathVariable String billId) {
        return billService.deleteBill(billId);
    }
}
