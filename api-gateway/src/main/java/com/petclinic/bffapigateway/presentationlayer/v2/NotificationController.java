package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.NotificationsServiceClient;
import com.petclinic.bffapigateway.dtos.Notifications.NotificationRequestModel;
import com.petclinic.bffapigateway.dtos.Notifications.NotificationResponseModel;
import com.petclinic.bffapigateway.exceptions.InvalidCredentialsException;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@RestController
@RequestMapping("/api/v2/gateway/notifications")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class NotificationController {
    private final NotificationsServiceClient notificationsServiceClient;
    private final AuthServiceClient authServiceClient;

    private class UserCache{
        private String userId;
        private long lastUsed;

        public UserCache(String userId){
            this.userId = userId;
            this.lastUsed = System.currentTimeMillis();
        }

        public String getUserId() {
            this.lastUsed = System.currentTimeMillis();
            return userId;
        }

        public long getLastUsed() {
            return lastUsed;
        }
    }

    // Ghetto User Cache
    private static HashMap<String, NotificationController.UserCache> jwtUserCache = new HashMap<>();

    // Caches JWT to a UserID so we don't have to call Auth all the time
    private Mono<String> getFromJWTUserId(String jwt){
        if(jwtUserCache.containsKey(jwt)){
            return Mono.just(jwtUserCache.get(jwt).getUserId());
        }else{
            return authServiceClient.validateToken(jwt)
                    .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid credentials")))
                    .doOnNext(user -> jwtUserCache.put(jwt, new NotificationController.UserCache(user.getBody().getUserId())))
                    .flatMap(u -> Mono.just(u.getBody().getUserId()));
        }
    }

    // Goes through HashMap and cleans if was not used within 5 minutes.
    private void cleanCache(){
        jwtUserCache.entrySet().iterator().forEachRemaining(entry -> {
            if(System.currentTimeMillis() - entry.getValue().getLastUsed() > 300000){
                jwtUserCache.remove(entry.getKey());
            }
        });
    }

    public static void clearCache(){
        jwtUserCache.clear();
    }

    public NotificationController(NotificationsServiceClient notificationsServiceClient, AuthServiceClient authServiceClient) {
        this.notificationsServiceClient = notificationsServiceClient;
        this.authServiceClient = authServiceClient;
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("/types")
    public Flux<String> getSubscriptionTypes(){
        return notificationsServiceClient.getSubscriptionTypes();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping("/for-customer")
    public Flux<NotificationResponseModel> getNotificationsByCustomerId(@CookieValue("Bearer") String jwt){
        return getFromJWTUserId(jwt)
                .flatMapMany(notificationsServiceClient::getNotificationsByCustomerId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<NotificationResponseModel>> getNotificationByCustomerIdAndProductId(
            @CookieValue("Bearer") String jwt,
            @PathVariable String productId
            ){
        return getFromJWTUserId(jwt)
                .flatMap(userId -> notificationsServiceClient.getNotificationByCustomerIdAndProductId(userId, productId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @PostMapping("/{productId}")
    public Mono<ResponseEntity<NotificationResponseModel>> addNotificationSubscription(
            @CookieValue("Bearer") String jwt,
            @PathVariable String productId,
            @RequestBody Mono<NotificationRequestModel> notificationRequestModel
    ){
        return getFromJWTUserId(jwt)
                .flatMap(userId -> notificationsServiceClient.addNotificationSubscription(userId, productId, notificationRequestModel))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @PutMapping("/{productId}")
    public Mono<ResponseEntity<NotificationResponseModel>> updateNotificationSubscription(
            @CookieValue("Bearer") String jwt,
            @PathVariable String productId,
            @RequestBody Mono<NotificationRequestModel> notificationRequestModel
    ){
        return getFromJWTUserId(jwt)
                .flatMap(userId -> notificationsServiceClient.updateNotificationSubscription(userId, productId, notificationRequestModel))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ALL})
    @DeleteMapping("/{productId}")
    public Mono<ResponseEntity<Void>> deleteNotificationSubscription(
            @CookieValue("Bearer") String jwt,
            @PathVariable String productId
    ){
        return getFromJWTUserId(jwt)
                .flatMap(userId -> notificationsServiceClient.deleteNotificationSubscription(userId, productId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
