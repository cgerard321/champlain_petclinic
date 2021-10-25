package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.AggregateAllCombinedDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AggregateServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String aggregateUrl;



    public AggregateServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.customers-service.host}") String customersServiceHost,
            @Value("${app.customers-service.port}") String customersServicePort
    )
    {
        this.webClientBuilder = webClientBuilder;
        aggregateUrl = "http://" + customersServiceHost + ":" + customersServicePort + "/aggregate" ;

    }

   public Mono<AggregateAllCombinedDetails> getOneAggregate(final int id){
        return webClientBuilder.build().get()
                .uri(aggregateUrl + "/{id}", id)
                .retrieve()
                .bodyToMono(AggregateAllCombinedDetails.class);
   }
}
