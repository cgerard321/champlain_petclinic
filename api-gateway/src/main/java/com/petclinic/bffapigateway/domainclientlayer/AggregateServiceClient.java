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
            @Value("${app.api-gateway.host}") String aggregateHost,
            @Value("${app.api-gateway.port}") String aggregatePort
    )
    {
        this.webClientBuilder = webClientBuilder;
        aggregateUrl = "http://" + aggregateHost + ":" + aggregatePort + "/aggregate" ;

    }

   public Mono<AggregateAllCombinedDetails> getOneAggregate(final int id){
        return webClientBuilder.build().get()
                .uri(aggregateUrl + "/{id}", id)
                .retrieve()
                .bodyToMono(AggregateAllCombinedDetails.class);
   }
}
