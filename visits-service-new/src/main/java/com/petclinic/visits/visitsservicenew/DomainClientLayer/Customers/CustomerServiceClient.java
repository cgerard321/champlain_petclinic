package com.petclinic.visits.visitsservicenew.DomainClientLayer.Customers;

import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomerServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String customerServiceUrl;

    public CustomerServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.customers-service-reactive.host}") String customerServiceHost,
            @Value("${app.customers-service-reactive.port}") String customerServicePort) {

        this.webClientBuilder = webClientBuilder;
        this.customerServiceUrl =
                "http://" + customerServiceHost + ":" + customerServicePort;
    }


    public Mono<CustomerResponseDTO> getCustomerByCustomerId(String customerId) {
        // A blank id would hit GET /customers/ and return the whole customer list
        if (customerId == null || customerId.isBlank()) {
            return Mono.error(new NotFoundException("Customer id is not found: " + customerId));
        }
        String url = customerServiceUrl + "/customers/{customerId}";
        return webClientBuilder.build()
                .get()
                .uri(url, customerId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(
                                new NotFoundException(
                                        "Customer id is not found: " + customerId))
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> response.createException()
                )
                .bodyToMono(CustomerResponseDTO.class);
    }
}