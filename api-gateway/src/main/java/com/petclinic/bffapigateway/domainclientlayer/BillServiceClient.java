package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.Optional;


@Component
@Slf4j
public class BillServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String billServiceUrl;


    public BillServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.billing-service.host}") String billingServiceHost,
            @Value("${app.billing-service.port}") String billingServicePort
    ) {
        this.webClientBuilder = webClientBuilder;

        billServiceUrl = "http://" + billingServiceHost + ":" + billingServicePort + "/bills";

    }

    public Mono<BillResponseDTO> getBilling(final String billId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/{billId}", billId)
                .retrieve()
                .bodyToMono(BillResponseDTO.class)
                .doOnNext(t -> t.setTaxedAmount(((t.getAmount() * 15)/100)+ t.getAmount()))
                .doOnNext(t -> t.setTaxedAmount(Math.round(t.getTaxedAmount() * 100.0) / 100.0));
    }
    public Flux<BillResponseDTO> getBillsByOwnerId(final String customerId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }
    public Flux<BillResponseDTO> getBillsByVetId(final String vetId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/vet/{vetId}", vetId)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }
    public Flux<BillResponseDTO> getAllBilling() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    //to be changed
//    public Flux<BillResponseDTO> getAllBillsByPage(Optional<Integer> page, Optional<Integer> size) {
//        return webClientBuilder.build().get()
//                .uri(billServiceUrl + "/bills-pagination?page="+page.orElse(0)+"&size="+size.orElse(5))
//                .retrieve()
//                .bodyToFlux(BillResponseDTO.class);
//    }

//    public Flux<BillResponseDTO> getAllBillsByPage(Optional<Integer> page, Optional<Integer> size, String billId, String customerId,
//                                                    String ownerFirstName, String ownerLastName, String visitType,
//                                                    String vetId, String vetFirstName, String vetLastName) {
//
//        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billServiceUrl + "/bills-pagination");
//
//        builder.queryParam("page", page);
//        builder.queryParam("size",size);
//
//        // Add query parameters conditionally if they are not null or empty
//        if (billId != null && !billId.isEmpty()) {
//            builder.queryParam("billId", billId);
//        }
//        if (customerId != null && !customerId.isEmpty()) {
//            builder.queryParam("customerId", customerId);
//        }
//        if (ownerFirstName != null && !ownerFirstName.isEmpty()) {
//            builder.queryParam("ownerFirstName", ownerFirstName);
//        }
//        if (ownerLastName != null && !ownerLastName.isEmpty()) {
//            builder.queryParam("ownerLastName", ownerLastName);
//        }
//        if (visitType != null && !visitType.isEmpty()) {
//            builder.queryParam("visitType", visitType);
//        }
//        if (vetId != null && !vetId.isEmpty()) {
//            builder.queryParam("vetId", vetId);
//        }
//        if (vetFirstName != null && !vetFirstName.isEmpty()) {
//            builder.queryParam("vetFirstName", vetFirstName);
//        }
//        if (vetLastName != null && !vetLastName.isEmpty()) {
//            builder.queryParam("vetLastName", vetLastName);
//        }
//
//        return webClientBuilder.build()
//                .get()
//                .uri(builder.build().toUri())
//                .retrieve()
//                .bodyToFlux(BillResponseDTO.class);
//    }


    //to be changed
    public Mono<Long> getTotalNumberOfBills() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/bills-count")
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Mono<Long> getTotalNumberOfBillsWithFilters(String billId, String customerId,
                                                       String ownerFirstName, String ownerLastName, String visitType,
                                                       String vetId, String vetFirstName, String vetLastName){
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billServiceUrl + "/bills-filtered-count");

        // Add query parameters conditionally if they are not null or empty
        if (billId != null && !billId.isEmpty()) {
            builder.queryParam("billId", billId);
        }
        if (customerId != null && !customerId.isEmpty()) {
            builder.queryParam("customerId", customerId);
        }
        if (ownerFirstName != null && !ownerFirstName.isEmpty()) {
            builder.queryParam("ownerFirstName", ownerFirstName);
        }
        if (ownerLastName != null && !ownerLastName.isEmpty()) {
            builder.queryParam("ownerLastName", ownerLastName);
        }
        if (visitType != null && !visitType.isEmpty()) {
            builder.queryParam("visitType", visitType);
        }
        if (vetId != null && !vetId.isEmpty()) {
            builder.queryParam("vetId", vetId);
        }
        if (vetFirstName != null && !vetFirstName.isEmpty()) {
            builder.queryParam("vetFirstName", vetFirstName);
        }
        if (vetLastName != null && !vetLastName.isEmpty()) {
            builder.queryParam("vetLastName", vetLastName);
        }

        return webClientBuilder.build()
                .get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(Long.class);
    }

    public Flux<BillResponseDTO> getAllPaidBilling() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/paid")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    public Flux<BillResponseDTO> getAllUnpaidBilling() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/unpaid")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    public Flux<BillResponseDTO> getAllOverdueBilling() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/overdue")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }
    public Mono<BillResponseDTO> createBill(final BillRequestDTO model){
        return webClientBuilder.build().post()
                .uri(billServiceUrl)
                .body(Mono.just(model), BillRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BillResponseDTO.class);
    }

    public Mono<BillResponseDTO> updateBill(String billId, Mono<BillRequestDTO> billRequestDTO){
        return webClientBuilder
                .build()
                .put()
                .uri(billServiceUrl + "/{billId}", billId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(billRequestDTO, BillResponseDTO.class)
                .retrieve()
                .bodyToMono(BillResponseDTO.class);
    }

    public Mono<Void> deleteAllBills() {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteBill(final String billId) {
        return getBilling(billId)
                .flatMap(bill -> {
                    if (bill.getBillStatus() == BillStatus.UNPAID || bill.getBillStatus() == BillStatus.OVERDUE) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot delete a bill that is unpaid or overdue."));
                    }
                    return webClientBuilder.build()
                            .delete()
                            .uri(billServiceUrl + "/{billId}", billId)
                            .retrieve()
                            .bodyToMono(Void.class);
                });
    }

    public Flux<Void> deleteBillsByVetId(final String vetId) {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl + "/vet/{vetId}", vetId)
                .retrieve()
                .bodyToFlux(Void.class);
    }

    public Flux<Void> deleteBillsByCustomerId(final String customerId) {
        return webClientBuilder.build()
                .delete()
                .uri(billServiceUrl + "/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(Void.class);
    }

//    public Flux<BillResponseDTO> getAllBillsByPage(Optional<Integer> page, Optional<Integer> size,
//                                                   String billId, String customerId,
//                                                   String ownerFirstName, String ownerLastName,
//                                                   String visitType, String vetId,
//                                                   String vetFirstName, String vetLastName) {
//        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billServiceUrl + "/bills-pagination");
//
//        builder.queryParam("page", page.orElse(0));
//        builder.queryParam("size", size.orElse(5));
//
//        if (billId != null && !billId.isEmpty()) builder.queryParam("billId", billId);
//        if (customerId != null && !customerId.isEmpty()) builder.queryParam("customerId", customerId);
//        if (ownerFirstName != null && !ownerFirstName.isEmpty()) builder.queryParam("ownerFirstName", ownerFirstName);
//        if (ownerLastName != null && !ownerLastName.isEmpty()) builder.queryParam("ownerLastName", ownerLastName);
//        if (visitType != null && !visitType.isEmpty()) builder.queryParam("visitType", visitType);
//        if (vetId != null && !vetId.isEmpty()) builder.queryParam("vetId", vetId);
//        if (vetFirstName != null && !vetFirstName.isEmpty()) builder.queryParam("vetFirstName", vetFirstName);
//        if (vetLastName != null && !vetLastName.isEmpty()) builder.queryParam("vetLastName", vetLastName);
//
//        return webClientBuilder.build()
//                .get()
//                .uri(builder.build().toUri())
//                .retrieve()
//                .bodyToFlux(BillResponseDTO.class);
//    }

    public Flux<BillResponseDTO> getAllBillsByPage(Optional<Integer> page, Optional<Integer> size,
                                                   String billId, String customerId,
                                                   String ownerFirstName, String ownerLastName,
                                                   String visitType, String vetId,
                                                   String vetFirstName, String vetLastName) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billServiceUrl)
                .queryParam("page", page.orElse(0))
                .queryParam("size", size.orElse(10))
                .queryParamIfPresent("billId", Optional.ofNullable(billId))
                .queryParamIfPresent("customerId", Optional.ofNullable(customerId))
                .queryParamIfPresent("ownerFirstName", Optional.ofNullable(ownerFirstName))
                .queryParamIfPresent("ownerLastName", Optional.ofNullable(ownerLastName))
                .queryParamIfPresent("visitType", Optional.ofNullable(visitType))
                .queryParamIfPresent("vetId", Optional.ofNullable(vetId))
                .queryParamIfPresent("vetFirstName", Optional.ofNullable(vetFirstName))
                .queryParamIfPresent("vetLastName", Optional.ofNullable(vetLastName));

        return webClientBuilder.build()
                .get()
                .uri(builder.build().toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }





    public Flux<BillResponseDTO> getBillsByCustomerIdPaginated(final String customerId, Optional<Integer> page, Optional<Integer> size) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/customer/" + customerId + "/paginated?page=" + page.orElse(0) + "&size=" + size.orElse(10))
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }


    public Mono<byte[]> downloadBillPdf(String customerId, String billId) {
        return webClientBuilder.build()
                .get()
                .uri(billServiceUrl + "/customer/{customerId}/bills/{billId}/pdf", customerId, billId)
                .accept(MediaType.APPLICATION_PDF)
                .retrieve()
                .bodyToMono(byte[].class);
    }

}


