package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.
http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/gateway/bills")
public class BillControllerV1 {

    private final BillServiceClient billServiceClient;

    // Define endpoints to interact with the BillServiceClient here

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBills() {
        return billServiceClient.getAllBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/admin", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsAdmin() {
        return billServiceClient.getAllBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/{billId}")
    public Mono<ResponseEntity<BillResponseDTO>> getBillById(final @PathVariable String billId)
    {
        return billServiceClient.getBillById(billId)
                .map(updated -> ResponseEntity.status(HttpStatus.OK).body(updated))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.VET, Roles.ADMIN})
    @PostMapping(value = "",
            consumes = "application/json",
            produces = "application/json")
    public Mono<ResponseEntity<BillResponseDTO>> createBill(@RequestBody BillRequestDTO model) {
        return billServiceClient.createBill(model).map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BillResponseDTO> getAllBillsByPage(
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

        if(page.isEmpty()){
            page = Optional.of(0);
        }

        if (size.isEmpty()) {
            size = Optional.of(10);
        }

        return billServiceClient.getAllBillsByPage(page, size, billId, customerId, ownerFirstName, ownerLastName,
                visitType, vetId, vetFirstName, vetLastName);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/bills-count")
    public Mono<Long> getTotalNumberOfBills(){
        return billServiceClient.getTotalNumberOfBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @GetMapping(value = "/bills-filtered-count")
    public Mono<Long> getTotalNumberOfBillsWithFilters (@RequestParam(required = false) String billId,
                                                        @RequestParam(required = false) String customerId,
                                                        @RequestParam(required = false) String ownerFirstName,
                                                        @RequestParam(required = false) String ownerLastName,
                                                        @RequestParam(required = false) String visitType,
                                                        @RequestParam(required = false) String vetId,
                                                        @RequestParam(required = false) String vetFirstName,
                                                        @RequestParam(required = false) String vetLastName)
    {
        return billServiceClient.getTotalNumberOfBillsWithFilters(billId, customerId, ownerFirstName, ownerLastName, visitType,
                vetId, vetFirstName, vetLastName);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/paid", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllPaidBills() {
        return billServiceClient.getAllPaidBills();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/unpaid", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllUnpaidBills() {
        return billServiceClient.getAllUnpaidBills();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/overdue", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllOverdueBills() {
        return billServiceClient.getAllOverdueBills();
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "/vets/{vetId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByVetId(final @PathVariable String vetId)
    {
        return billServiceClient.getBillsByVetId(vetId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/vet/{vetFirstName}/{vetLastName}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByVetName(@PathVariable String vetFirstName, @PathVariable String vetLastName) {
        return billServiceClient.getBillsByVetName(vetFirstName, vetLastName);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/visitType/{visitType}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getAllBillsByVisitType(@PathVariable String visitType) {
        return billServiceClient.getBillsByVisitType(visitType);
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping("/{billId}")
    public Mono<ResponseEntity<BillResponseDTO>> updateBill(@PathVariable String billId, @RequestBody Mono<BillRequestDTO> billRequestDTO){
        return billServiceClient.updateBill(billId, billRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "")
    public Mono<ResponseEntity<Void>> deleteAllBills() {
        return billServiceClient.deleteAllBills()
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{billId}")
    public Mono<ResponseEntity<Void>> deleteBill(final @PathVariable String billId) {
        return billServiceClient.deleteBill(billId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error deleting bill with ID {}: {}", billId, e.getMessage());
                    return Mono.just(ResponseEntity.unprocessableEntity().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/vets/{vetId}")
    public Mono<ResponseEntity<Void>> deleteBillsByVetId(final @PathVariable String vetId){
        return billServiceClient.deleteBillsByVetId(vetId).then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping(value = "/month", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return billServiceClient.getBillsByMonth(year, month);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PatchMapping("/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Object>> archiveBill() {
        return billServiceClient.archiveBill()
                .then(Mono.just(ResponseEntity.noContent().build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
