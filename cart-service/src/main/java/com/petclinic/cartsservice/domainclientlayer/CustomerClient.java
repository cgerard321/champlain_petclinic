package com.petclinic.cartsservice.domainclientlayer;

import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CustomerClient {

    private final WebClient webClient;
    private final String customersBaseURL;

    public CustomerClient(
            @Value("customers-service") String customersServiceHost,
            @Value("8080") String customersServicePort
    ) {
        this.customersBaseURL = "http://" + customersServiceHost + ":" + customersServicePort + "/customers";
        this.webClient = WebClient.builder()
                .baseUrl(customersBaseURL)
                .build();
    }

    public Mono<CustomerResponseModel> getCustomerById(String customerId) {
        return webClient.get()
                .uri("/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, error -> switch (error.statusCode().value()) {
                    case 404 -> Mono.error(new NotFoundException("CustomerId not found: " + customerId));
                    case 422 -> Mono.error(new InvalidInputException("CustomerId invalid: " + customerId));
                    default -> Mono.error(new IllegalArgumentException("Unexpected error contacting customers-service"));
                })
                .bodyToMono(CustomerResponseModel.class);
    }
}
