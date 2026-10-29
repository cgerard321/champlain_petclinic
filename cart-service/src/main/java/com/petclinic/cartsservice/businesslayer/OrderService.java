package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.presentationlayer.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Mono<OrderResponseModel> createOrder(Cart cart, BillingRequestModel billing);
    Mono<OrderResponseModel> getOrderByOrderId(String orderId);
    Flux<OrderResponseModel> getAllOrders();
    Flux<OrderResponseModel>  findOrdersByCustomerId(String customerId);

    Mono<OrderReportResponseModel> createOrderReport(
            String customerId,
            String orderId,
            CreateOrderReportRequestModel request
    );

    Flux<OrderReportResponseModel> findReportsByCustomerId(String customerId);

    Flux<OrderReportResponseModel> getAllReports();

    Mono<OrderReportResponseModel> updateReportStatus(
            String reportId,
            UpdateOrderReportStatusRequestModel request
    );
}
