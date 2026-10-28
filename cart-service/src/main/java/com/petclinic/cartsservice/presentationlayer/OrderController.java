package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.OrderService;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderResponseModel> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OrderResponseModel>> getOrderByOrderId(@PathVariable String orderId) {
        return Mono.just(orderId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided order ID is invalid: " + orderId)))
                .flatMap(orderService::getOrderByOrderId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<OrderResponseModel> getOrdersByCustomerId(@PathVariable String customerId) {
        return orderService.findOrdersByCustomerId(customerId);
    }

    @PostMapping(
            value = "/customer/{customerId}/{orderId}/reports",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<OrderReportResponseModel>> createOrderReport(
            @PathVariable String customerId,
            @PathVariable String orderId,
            @RequestBody CreateOrderReportRequestModel request
    ) {
        return orderService.createOrderReport(customerId, orderId, request)
                .map(report -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(report));
    }

    @GetMapping(
            value = "/customer/{customerId}/reports",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Flux<OrderReportResponseModel> getCustomerOrderReports(
            @PathVariable String customerId
    ) {
        return orderService.findReportsByCustomerId(customerId);
    }

    @GetMapping(
            value = "/reports",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Flux<OrderReportResponseModel> getAllOrderReports() {
        return orderService.getAllReports();
    }

}
