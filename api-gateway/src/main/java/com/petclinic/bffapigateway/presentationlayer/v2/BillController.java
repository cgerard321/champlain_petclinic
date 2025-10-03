package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.PaymentRequestDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
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

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/bills")
@Validated
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
        return billService.getAllBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/{billId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<BillResponseDTO> getBillById(@PathVariable String billId)
    {
        return billService.getBillById(billId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping()
    public ResponseEntity<Flux<BillResponseDTO>> getAllBillsByPage(
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
    
    @PutMapping(value = "/admin/{billId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BillResponseDTO>> updateBill(@PathVariable String billId, @RequestBody Mono<BillRequestDTO> billRequestDTO) {

        return Mono.just(billId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided bill ID is invalid: " + billId)))
                .flatMap(id -> billService.updateBill(id, billRequestDTO))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());

    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/paid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllPaidBills()
    {
        return billService.getAllPaidBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/unpaid", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllUnpaidBills()
    {
        return billService.getAllUnpaidBills();
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/overdue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllOverdueBills()
    {
        return billService.getAllOverdueBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/month", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        if (year < 0 || month < 1 || month > 12) {
            throw new InvalidInputException("Invalid year or month: year=" + year + ", month=" + month);
        }

        return billService.getBillsByMonth(year, month);
    }

    //moved to CustomerBillController
//    @IsUserSpecific(idToMatch = {"customerId"})
//    @PostMapping("/customer/{customerId}/bills/{billId}/pay")
//    public Mono<ResponseEntity<String>> payBill(
//            @PathVariable("customerId") String customerId,
//            @PathVariable("billId") String billId,
//            @RequestBody PaymentRequestDTO paymentRequestDTO) {
//        return billService.payBill(customerId, billId, paymentRequestDTO)
//                .map(response -> ResponseEntity.ok(response))
//                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("Payment failed: " + e.getMessage())));
//    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/{billId}/interest")
    public Mono<ResponseEntity<Double>> getInterest(@PathVariable String billId) {
        return billService.getInterest(billId).map(ResponseEntity::ok);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin/{billId}/total")
    public Mono<ResponseEntity<Double>> getTotal(@PathVariable String billId) {
        return billService.getTotalWithInterest(billId).map(ResponseEntity::ok);
    }

    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping("/customer/{customerId}/bills/{billId}/interest")
    public Mono<ResponseEntity<Double>> getInterestForCustomer(@PathVariable String customerId,
                                                               @PathVariable String billId) {
        return billService.getInterest(billId).map(ResponseEntity::ok);
    }

    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping("/customer/{customerId}/bills/{billId}/total")
    public Mono<ResponseEntity<Double>> getTotalForCustomer(@PathVariable String customerId,
                                                            @PathVariable String billId) {
        return billService.getTotalWithInterest(billId).map(ResponseEntity::ok);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PatchMapping(value = "/{billId}/exempt-interest")
    public Mono<ResponseEntity<Void>> setInterestExempt(
            @PathVariable String billId,
            @RequestParam boolean exempt) {

        return billService.setInterestExempt(billId, exempt)
                .thenReturn(ResponseEntity.noContent().build());
    }



}
