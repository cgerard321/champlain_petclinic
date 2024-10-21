package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Notifications.NotificationRequestModel;
import com.petclinic.bffapigateway.dtos.Notifications.NotificationResponseModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class NotificationsServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String baseURL;

    public NotificationsServiceClient(
            WebClient.Builder wcb,
            @Value("${app.products-service.host}") String host,
            @Value("${app.products-service.port}") String port
    ){
        this.webClientBuilder = wcb;
        this.baseURL = "http://" + host + ":" + port + "/api/v1/notifications";
    }

    public Flux<String> getSubscriptionTypes(){
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/types")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(String.class);
    }

    public Flux<NotificationResponseModel> getNotificationsByCustomerId(final String customerId){
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/" + customerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(NotificationResponseModel.class);
    }

    public Mono<NotificationResponseModel> getNotificationByCustomerIdAndProductId(final String customerId, final String productId){
        return webClientBuilder.build()
                .get()
                .uri(baseURL + "/" + customerId + "/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(NotificationResponseModel.class);
    }

    public Mono<NotificationResponseModel> addNotificationSubscription(final String customerId, final String productId, final Mono<NotificationRequestModel> requestModel){
        return webClientBuilder.build()
                .post()
                .uri(baseURL + "/" + customerId + "/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestModel, NotificationRequestModel.class)
                .retrieve()
                .bodyToMono(NotificationResponseModel.class);
    }

    public Mono<NotificationResponseModel> updateNotificationSubscription(final String customerId, final String productId, final Mono<NotificationRequestModel> requestModel){
        return webClientBuilder.build()
                .put()
                .uri(baseURL + "/" + customerId + "/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .body(requestModel, NotificationRequestModel.class)
                .retrieve()
                .bodyToMono(NotificationResponseModel.class);
    }

    public Mono<Void> deleteNotificationSubscription(final String customerId, final String productId){
        return webClientBuilder.build()
                .delete()
                .uri(baseURL + "/" + customerId + "/" + productId)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
