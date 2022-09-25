package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.*;

import com.petclinic.bffapigateway.utils.PhotoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

import static reactor.core.publisher.Mono.just;

/**
 * @author Maciej Szarlinski
 * @author Christine Gerard
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * Modified to remove circuitbreaker
 */

@Component
public class CustomersServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final String customersServiceUrl;

    public CustomersServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.customers-service.host}") String customersServiceHost,
            @Value("${app.customers-service.port}") String customersServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        customersServiceUrl = "http://" + customersServiceHost + ":" + customersServicePort + "/owners/";
    }


    public Mono<OwnerDetails> getOwner(final int ownerId) {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
    }

    public Flux<OwnerDetails> getOwners() {
        return webClientBuilder.build().get()
                .uri(customersServiceUrl)
                .retrieve()
                .bodyToFlux(OwnerDetails.class);
    }

    public Mono<OwnerDetails> updateOwner(int ownerId, OwnerDetails od){

            return webClientBuilder.build().put()
                    .uri(customersServiceUrl + ownerId)
                    .body(Mono.just(od), OwnerDetails.class)
                    .retrieve().bodyToMono(OwnerDetails.class);
    }




    public Flux<OwnerDetails> createOwners (){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
            .retrieve().bodyToFlux(OwnerDetails.class);

    }

    public Mono<OwnerDetails> createOwner (OwnerDetails model){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(model), OwnerDetails.class)
                .retrieve().bodyToMono(OwnerDetails.class);

    }

    public Flux<PetType> getPetTypes (){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + "/petTypes")
                .retrieve()
                .bodyToFlux(PetType.class);
    }

    public Mono<PetDetails> getPet(final int ownerId, final int petId){
        return webClientBuilder.build().get()
                .uri(customersServiceUrl + ownerId + "/pets/" + petId)
                .retrieve()
                .bodyToMono(PetDetails.class);
    }

    public Mono<PetDetails> createPet(PetDetails model, final int ownerId){
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"{ownerId}/pets", ownerId)
                .body(just(model), PetDetails.class)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PetDetails.class);
    }

    public Mono<PetDetails> deletePet(final int ownerId, final int petId){
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + "{ownerId}/pets/{petId}", ownerId ,petId)
                .retrieve()
                .bodyToMono(PetDetails.class);
    }


    public Mono<OwnerDetails> deleteOwner (final long ownerId) {
        return webClientBuilder.build().delete()
                .uri(customersServiceUrl + ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
    }

//    public Mono<ResponseMessage> setPhoto (MultipartFile file){
//        try {
//            MultipartBodyBuilder builder = new MultipartBodyBuilder();
//            builder.part("file", new ByteArrayResource(file.getBytes())).filename(file.getName());
//            return webClientBuilder.build().post()
//                    .uri(customersServiceUrl + "/upload/photo")
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA.toString())
//                    .accept(MediaType.APPLICATION_JSON)
//                    .body(BodyInserters.fromMultipartData(builder.build()))
//                    .retrieve()
//                    .bodyToMono(ResponseMessage.class);
//        } catch(Exception e) {
//            return null;
//        }
//    }

    public Mono<ResponseMessage> setPhoto(MultipartFile file)
            throws IOException {

        PhotoDetails photo = new PhotoDetails().builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .photo(PhotoUtil.compressImage(file.getBytes())).build();
        return webClientBuilder.build().post()
                .uri(customersServiceUrl +"/upload/photo")
                .body(just(photo), PhotoDetails.class)
                .retrieve().bodyToMono(ResponseMessage.class);
    }

//    public Mono<PhotoDetails> getPhoto(final int photoId) {
//        return webClientBuilder.build().get()
//                .uri(customersServiceUrl + ownerId)
//                .retrieve()
//                .bodyToMono(OwnerDetails.class);
//    }



}
