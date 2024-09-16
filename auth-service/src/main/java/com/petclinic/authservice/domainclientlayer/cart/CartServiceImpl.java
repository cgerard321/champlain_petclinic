package com.petclinic.authservice.domainclientlayer.cart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Service
public class CartServiceImpl implements CartService{

    private WebClient webClient;

    private String cartServiceBaseURL;

    public CartServiceImpl(@Value("${cart-service.host}") String cartServiceHost,
                           @Value("${cart-service.port}") String cartServicePort) {
        cartServiceBaseURL  = "http://" + cartServiceHost + ":" + cartServicePort + "/api/v1/carts";

        this.webClient = WebClient.builder()
                .baseUrl(cartServiceBaseURL)
                .build();
    }

    @Override
    public CartResponse createCart(CartRequest cartRequest) {
        return webClient.post()
                .bodyValue(cartRequest)
                .retrieve()
                .bodyToMono(CartResponse.class)
                .block();
    }



}
