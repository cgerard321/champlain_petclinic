package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.dtos.Bills.PaymentRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.net.URI;
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
        if (billingServiceHost == null || billingServiceHost.isBlank()) {
            throw new IllegalArgumentException("Configuration property 'app.billing-service.host' must be set and non-blank");
        }
        if (billingServicePort == null || billingServicePort.isBlank()) {
            throw new IllegalArgumentException("Configuration property 'app.billing-service.port' must be set and non-blank");
        }
        billServiceUrl = "http://" + billingServiceHost + ":" + billingServicePort + "/bills";
    }

    public Mono<BillResponseDTO> getBillById(final String billId) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/{billId}", billId)
                .retrieve()
                .bodyToMono(BillResponseDTO.class);
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
    public Flux<BillResponseDTO> getAllBills() {
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

    public Flux<BillResponseDTO> getAllPaidBills() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/paid")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    public Flux<BillResponseDTO> getAllUnpaidBills() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/unpaid")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    public Flux<BillResponseDTO> getAllOverdueBills() {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/overdue")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    public Flux<BillResponseDTO> getBillsByOwnerName(final String ownerFirstName, final String ownerLastName) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/owner/{ownerFirstName}/{ownerLastName}", ownerFirstName, ownerLastName)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class)
                .switchIfEmpty(Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No bills found for owner: " + ownerFirstName + " " + ownerLastName)));
    }

    public Flux<BillResponseDTO> getBillsByVetName(final String vetFirstName, final String vetLastName) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/vet/{vetFirstName}/{vetLastName}", vetFirstName, vetLastName)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class)
                .switchIfEmpty(Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No bills found for vet: " + vetFirstName + " " + vetLastName)));
    }

    public Flux<BillResponseDTO> getBillsByVisitType(final String visitType) {
        return webClientBuilder.build().get()
                .uri(billServiceUrl + "/visitType/{visitType}", visitType)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class)
                .switchIfEmpty(Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No bills found for visit type: " + visitType)));
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
        return getBillById(billId)
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

    // Example PDF download method (replace with your actual method name/signature)
    public Mono<byte[]> downloadBillPdf(String customerId, String billId, String currency) {
        String cur = (currency == null || currency.isBlank()) ? "CAD" : currency;
        String url = UriComponentsBuilder
                .fromHttpUrl(billServiceUrl)
                .path("/customer/{customerId}/bills/{billId}/pdf")
                .queryParam("currency", cur)
                .buildAndExpand(customerId, billId)
                .toUriString();

        return webClientBuilder.build()
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_PDF)
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Flux<BillResponseDTO> getBillsByMonth(int year, int month) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(billServiceUrl + "/month")
                .queryParam("year", year)
                .queryParam("month", month);

        return webClientBuilder.build()
                .get()
                .uri(builder.build().toUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }

    public Mono<Double> getCurrentBalance(String customerId) {
        return webClientBuilder.build()
                .get()
                .uri(billServiceUrl + "/customer/{customerId}/bills/current-balance", customerId)
                .retrieve()
                .bodyToMono(Double.class);
    }

    public Mono<BillResponseDTO> payBill(String customerId, String billId, PaymentRequestDTO paymentRequestDTO,String jwtToken) {
        return webClientBuilder.build()
                .post()
                .uri(billServiceUrl + "/customer/{customerId}/bills/{billId}/pay", customerId, billId)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", jwtToken)
                .bodyValue(paymentRequestDTO)
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToMono(BillResponseDTO.class);
                    }
                    if (resp.statusCode().value() == 400) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment details"));
                    }
                    if (resp.statusCode().value() == 404) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
                    }
                    if (resp.statusCode().is5xxServerError()) {
                        return Mono.error(new ResponseStatusException(
                                resp.statusCode(),
                                "Upstream billing service error: " + resp.statusCode().value()
                        ));
                    }
                    // Fallback: let other statuses bubble up
                    return resp.createException().flatMap(Mono::error);
                });
    }

    public Mono<Double> getInterest(String billId) {
        return webClientBuilder.build()
                .get()
                .uri(billServiceUrl + "/{billId}/interest", billId)
                .retrieve()
                .bodyToMono(Double.class);
    }

    public Mono<Double> getTotalWithInterest(String billId) {
        return webClientBuilder.build()
                .get()
                .uri(billServiceUrl + "/{billId}/total", billId)
                .retrieve()
                .bodyToMono(Double.class);
    }

    public Mono<Void> setInterestExempt(String billId, boolean exempt) {
        URI uri = UriComponentsBuilder
                .fromUriString(billServiceUrl + "/{billId}/exempt-interest")
                .queryParam("exempt", exempt)
                .build(billId);

        return webClientBuilder.build()
                .patch()
                .uri(uri)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<BillResponseDTO> archiveBill() {
        return webClientBuilder.build().patch()
                .uri(billServiceUrl + "/archive")
                .retrieve()
                .bodyToFlux(BillResponseDTO.class);
    }
}