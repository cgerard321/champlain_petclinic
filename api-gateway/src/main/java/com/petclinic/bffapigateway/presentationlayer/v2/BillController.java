package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.print.Pageable;
import java.util.Optional;

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

    @GetMapping(value = "/customer/{customerId}/paginated", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByCustomerIdPaginated(
            @PathVariable String customerId,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size) {
        return billService.getBillsByCustomerIdPaginated(customerId, page, size);
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/admin",
            consumes = "application/json",
            produces = "application/json")
    public Mono<ResponseEntity<BillResponseDTO>> createBill(@RequestBody BillRequestDTO model) {
        return billService.createBill(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBills()
    {
        return billService.getAllBilling();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/{billId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BillResponseDTO> getBillById(@PathVariable String billId)
    {
        return billService.getBilling(billId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping()
    public ResponseEntity<Flux<BillResponseDTO>> getAllBillingByPage(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) String billId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String ownerFirstName,
            @RequestParam(required = false) String ownerLastName,
            @RequestParam(required = false) String visitType,
            @RequestParam(required = false) String vetId,
            @RequestParam(required = false) String vetFirstName,
            @RequestParam(required = false) String vetLastName) {

        if (page.isEmpty()) {
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(10);
        }
        return ResponseEntity.ok().body(billService.getAllBillsByPage(page, size, billId, customerId, ownerFirstName,
                ownerLastName, visitType, vetId, vetFirstName, vetLastName));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{billId}")
    public Mono<ResponseEntity<Void>> deleteBill(final @PathVariable String billId) {
        return billService.deleteBill(billId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
