package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.Order;
import com.petclinic.cartsservice.dataaccesslayer.OrderRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.CustomerClient;
import com.petclinic.cartsservice.domainclientlayer.CustomerResponseModel;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.presentationlayer.OrderRequestModel;
import com.petclinic.cartsservice.presentationlayer.OrderResponseModel;
import com.petclinic.cartsservice.utils.EntityModelUtil;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final CustomerClient customerClient;


    public OrderServiceImpl(OrderRepository orderRepository, ProductClient productClient, CustomerClient customerClient ) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.customerClient = customerClient;
    }

    public Mono<OrderResponseModel> createOrder(Cart cart) {
        return customerClient.getCustomerById(cart.getCustomerId())
                .flatMap(customer -> {
                    Order order = new Order();
                    order.setOrderId(UUID.randomUUID().toString());
                    order.setCustomerId(customer.getCustomerId());
                    order.setCustomerName(customer.getFullName());
                    order.setCustomerAddress(customer.getAddress());
                    order.setProducts(cart.getProducts());
                    order.setSubtotal(cart.getSubtotal());
                    order.setPromoPercent(cart.getPromoPercent());
                    order.setTvq(cart.getTvq());
                    order.setTvc(cart.getTvc());
                    order.setTotal(cart.getTotal());
                    order.setInvoiceId(cart.getInvoiceId());
                    order.setOrderDate(LocalDateTime.now());

                    return orderRepository.save(order)
                            .map(EntityModelUtil::toOrderResponseModel);
                });
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
    @Override
    public Order toOrder(OrderResponseModel orderResponse){
        return Order.builder()
                .products(orderResponse.getProducts())
                .orderId(orderResponse.getOrderId())
                .customerId(orderResponse.getCustomerId())
                .customerName(orderResponse.getCustomerName())
                .customerAddress(orderResponse.getCustomerAddress())
                .subtotal(orderResponse.getSubtotal())
                .tvq(orderResponse.getTvq())
                .tvc(orderResponse.getTvc())
                .total(orderResponse.getTotal())
                .invoiceId(orderResponse.getInvoiceId())
                .orderDate(orderResponse.getOrderDate())
                .build();
    }

    @Override
    public Cart storeCartOrder(Cart cart) {
        Cart orderCart = new Cart();
        BeanUtils.copyProperties(cart, orderCart);
        List<CartProduct> products = cart.getProducts().stream()
                .map(product -> {
                    CartProduct copy = new CartProduct();
                    BeanUtils.copyProperties(product, copy);
                    return copy;
                })
                .toList();

        orderCart.setProducts(products);
        CartResponseModel calculatedCart =
                EntityModelUtil.toCartResponseModel(orderCart, products);
        orderCart.setSubtotal(calculatedCart.getSubtotal());
        orderCart.setPromoPercent(calculatedCart.getPromoPercent());
        orderCart.setTvq(calculatedCart.getTvq());
        orderCart.setTvc(calculatedCart.getTvc());
        orderCart.setTotal(calculatedCart.getTotal());

        return orderCart;
    }



}
