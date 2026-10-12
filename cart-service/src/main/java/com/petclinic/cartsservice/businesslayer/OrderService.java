package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CartItemRequestModel;
import com.petclinic.cartsservice.presentationlayer.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Mono<OrderResponseModel> createOrder(Cart cart);
    Mono<OrderResponseModel> getOrderByOrderId(String orderId);
    Flux<OrderResponseModel> getAllOrders();
    Flux<OrderResponseModel>  findOrdersByCustomerId(String customerId);
    Order toOrder(OrderResponseModel orderResponseModel);
}
