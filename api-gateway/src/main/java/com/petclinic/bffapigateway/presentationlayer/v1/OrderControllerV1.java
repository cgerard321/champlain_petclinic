package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.OrderReportRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.OrderReportResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.OrderResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gateway/orders")
public class OrderControllerV1 {

    private final CartServiceClient cartServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.CUST, Roles.ADMIN})
    @IsUserSpecific(
            idToMatch = {"customerId"},
            bypassRoles = {Roles.ADMIN}
    )
    @GetMapping(
            value = "/customer/{customerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Flux<OrderResponseDTO> getOrdersByCustomerId(
            @PathVariable String customerId
    ) {
        return cartServiceClient.getOrdersByCustomerId(customerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.CUST, Roles.ADMIN})
    @IsUserSpecific(
            idToMatch = {"customerId"},
            bypassRoles = {Roles.ADMIN}
    )
    @PostMapping(
            value = "/customer/{customerId}/{orderId}/reports",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<OrderReportResponseDTO>> createOrderReport(
            @PathVariable String customerId,
            @PathVariable String orderId,
            @RequestBody OrderReportRequestDTO request
    ) {
        return cartServiceClient.createOrderReport(
                        customerId,
                        orderId,
                        request
                )
                .map(report -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(report));
    }

    @SecuredEndpoint(allowedRoles = {Roles.CUST, Roles.ADMIN})
    @IsUserSpecific(
            idToMatch = {"customerId"},
            bypassRoles = {Roles.ADMIN}
    )
    @GetMapping(
            value = "/customer/{customerId}/reports",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Flux<OrderReportResponseDTO> getCustomerOrderReports(
            @PathVariable String customerId
    ) {
        return cartServiceClient.getCustomerOrderReports(customerId);
    }
}
