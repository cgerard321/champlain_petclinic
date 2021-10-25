package com.petclinic.bffapigateway.domainclientlayer.AggregateIntegrations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.aggregates.CustomerVisitsVetsAggregate;
import com.petclinic.bffapigateway.dtos.aggregates.CustomerVisitsVetsAggregateInterface;
import com.petclinic.bffapigateway.exceptions.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Component
public class ApiAggregateIntegration implements CustomerVisitsVetsAggregateInterface {

    private static final Logger LOG = LoggerFactory.getLogger(ApiAggregateIntegration.class);

    private final WebClient.Builder webClientBuilder;
    private final String customersServiceUrl;
    private final String vetsServiceUrl;
    private final String visitsServiceUrl;
    private final ObjectMapper mapper;

    @Autowired
    public ApiAggregateIntegration(
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            @Value("${app.customers-service.host}") String customersServiceHost,
            @Value("${app.customers-service.port}") String customersServicePort,
            @Value("${app.vets-service.host}") String vetsServiceHost,
            @Value("${app.vets-service.port}") String vetsServicePort,
            @Value("${app.visits-service.host}") String visitsServiceHost,
            @Value("${app.visits-service.port}") String visitsServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        customersServiceUrl = "http://" + customersServiceHost + ":" + customersServicePort + "/owners/";
        vetsServiceUrl = "http://" + vetsServiceHost + ":" + vetsServicePort + "/vets";
        visitsServiceUrl = "http://" + visitsServiceHost + ":" + visitsServicePort + "";

    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()) {
            case NOT_FOUND:
            return new NotFoundException(getErrorMessage(ex));
            default:
            LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
            LOG.warn("Error body: {}", ex.getResponseBodyAsString());
            return ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try{
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        }
        catch(IOException ioex){
            return ioex.getMessage();
        }
    }


    public Mono<CustomerVisitsVetsAggregate> Aggregate_Customer_Visits_Vets(int ownerId, int vetId, int visitId){
        try {

            Mono<OwnerDetails> cust1 = this.webClientBuilder.build().get()
                    .uri(customersServiceUrl + ownerId)
                    .retrieve()
                    .bodyToMono(OwnerDetails.class);

            Mono<VetDetails> vets1 = this.webClientBuilder.build().get()
                    .uri(vetsServiceUrl + "/{vetId}", vetId)
                    .retrieve()
                    .bodyToMono(VetDetails.class);

            Mono<VisitDetails> visit1 = this.webClientBuilder.build()
                    .get()
                    .uri(visitsServiceUrl + "/visits/{petId}", visitId)
                    .retrieve()
                    .bodyToMono(VisitDetails.class);

            return Mono.zip(cust1, vets1, visit1)
                    .map(objects -> {
                        CustomerVisitsVetsAggregate aggregate = new CustomerVisitsVetsAggregate();

                        aggregate.getVetsInfo();
                        aggregate.getOwnerInfo();
                        aggregate.getVisitsInfo();

                        return aggregate;
                    });
        }
        catch(HttpClientErrorException ex)
        {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Mono<CustomerVisitsVetsAggregate> getCustomer(int customerId) {
        return Aggregate_Customer_Visits_Vets(customerId, customerId, customerId);
    }
}
