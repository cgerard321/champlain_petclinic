package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.presentationlayer.OrderResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;




@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerClient customerClient;


    public OrderServiceImpl(OrderRepository orderRepository, CustomerClient customerClient ) {
        this.orderRepository = orderRepository;
        this.customerClient = customerClient;
    }


    @Override
    public Mono<OrderResponseModel> createOrder(Cart cart, String stripeSessionId) {
        return customerClient.getCustomerById(cart.getCustomerId())
                        .switchIfEmpty(Mono.error(
                                new NotFoundException("Customer not found")))
                        .map(customer ->
                                EntityModelUtil.toOrder(cart, customer, stripeSessionId))
                        .flatMap(orderRepository::save)
                        .map(EntityModelUtil::toOrderResponseModel);
    }


    @Override
    public Mono<OrderResponseModel> getOrderByOrderId(String orderId) {
        return orderRepository.findOrderByOrderId(orderId)
                .map(EntityModelUtil::toOrderResponseModel)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")));
    }

    @Override
    public Flux<OrderResponseModel> getAllOrders() {
        return orderRepository.findAll()
                .map(EntityModelUtil::toOrderResponseModel);
        }


    @Override
    public Flux<OrderResponseModel> findOrdersByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Flux.error(new InvalidInputException("customerId must not be null or empty"));
        }
        return orderRepository.findAllByCustomerId(customerId)
                .sort((order1, order2) ->
                        order2.getOrderDate().compareTo(order1.getOrderDate())
                )
                .map(EntityModelUtil::toOrderResponseModel);
    }


}
