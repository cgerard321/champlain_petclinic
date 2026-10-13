package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.RatingsServiceClient;
import com.petclinic.bffapigateway.dtos.Ratings.ReviewSettingsModel;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/gateway/review-settings")
public class ReviewSettingsControllerV1 {

    private final RatingsServiceClient ratingsServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @GetMapping
    public Mono<ReviewSettingsModel> getSettings(){
        return ratingsServiceClient.getReviewSettings();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping
    public Mono<ReviewSettingsModel> updateSettings(@RequestBody ReviewSettingsModel settings){
        return ratingsServiceClient.updateReviewSettings(settings);
    }
}
