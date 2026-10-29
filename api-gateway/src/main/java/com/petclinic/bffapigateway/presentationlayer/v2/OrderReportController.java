package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.OrderReportResponseDTO;
import com.petclinic.bffapigateway.dtos.Cart.OrderReportStatusRequestDTO;
import com.petclinic.bffapigateway.dtos.Cart.OrderResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v2/gateway/orders")
public class OrderReportController {

    private final CartServiceClient cartServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @GetMapping(value = "/reports", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<OrderReportResponseDTO> getAllOrderReports() {
        return cartServiceClient.getAllOrderReports();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OrderResponseDTO>> getOrderByOrderId(@PathVariable String orderId) {
        return cartServiceClient.getOrderByOrderId(orderId)
                .map(ResponseEntity::ok);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.RECEPTIONIST})
    @PatchMapping(
            value = "/reports/{reportId}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<OrderReportResponseDTO>> updateOrderReportStatus(
            @PathVariable String reportId,
            @RequestBody OrderReportStatusRequestDTO request
    ) {
        return cartServiceClient.updateOrderReportStatus(reportId, request)
                .map(ResponseEntity::ok);
    }
}
