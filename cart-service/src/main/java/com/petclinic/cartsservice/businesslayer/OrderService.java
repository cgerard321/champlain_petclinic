package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.presentationlayer.*;
import com.stripe.model.checkout.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Mono<OrderResponseModel> createOrder(Cart cart, String stripeSessionId);
    Mono<OrderResponseModel> getOrderByOrderId(String orderId);
    Flux<OrderResponseModel> getAllOrders();
    Flux<OrderResponseModel>  findOrdersByCustomerId(String customerId);
}
