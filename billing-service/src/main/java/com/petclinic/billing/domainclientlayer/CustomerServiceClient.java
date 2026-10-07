package com.petclinic.billing.domainclientlayer;

import com.petclinic.billing.datalayer.CustomerResponseDTO;
import com.petclinic.billing.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.rmi.ServerException;

@Service
public class CustomerServiceClient {
    private final WebClient webClient;
    private final String customerServiceClientBaseURL;

    CustomerServiceClient(@Value("${app.customers-service.host}") String customerServiceHost,
                          @Value("${app.customers-service.port}") String customerServicePort) {
        customerServiceClientBaseURL = "http://" + customerServiceHost + ":" + customerServicePort + "/customers";
        this.webClient = WebClient.builder()
                .baseUrl(customerServiceClientBaseURL).build();
    }
    public Mono<CustomerResponseDTO> getCustomerByCustomerId(final String customerId) {
        return this.webClient
                .get()
                .uri("/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new NotFoundException("Customer not found with customerId: " + customerId));
                    } else {
                        return Mono.error(new IllegalArgumentException("Client error for customerId: " + customerId));
                    }
                })
                .onStatus(HttpStatus::is5xxServerError, serverResponse ->
                        Mono.error(new ServerException("Server error for customerId: " + customerId))
                )
                .bodyToMono(CustomerResponseDTO.class);
    }
}
