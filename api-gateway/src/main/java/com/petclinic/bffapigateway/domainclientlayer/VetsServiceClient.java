package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.exceptions.ExistingRatingNotFoundException;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Christine Gerard
 */

@Component
@Slf4j
public class VetsServiceClient {
    private final WebClient.Builder webClientBuilder;
    private String vetsServiceUrl;

    public void setVetsServiceUrl(String vetsServiceUrl) {
        this.vetsServiceUrl = vetsServiceUrl;
    }

    public VetsServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.vet-service.host}") String vetsServiceHost,
            @Value("${app.vet-service.port}") String vetsServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        vetsServiceUrl = "http://" + vetsServiceHost + ":" + vetsServicePort + "/vets";
    }

    //Photo
    public Mono<Resource> getPhotoByVetId(String vetId){
        return webClientBuilder.build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/photo")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("Photo for vet "+vetId + " not found", NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Resource.class);
    }
    public Mono<PhotoResponseDTO> getDefaultPhotoByVetId(String vetId){
        return webClientBuilder.build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/default-photo")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("Photo for vet "+vetId + " not found", NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(PhotoResponseDTO.class);
    }

    public Mono<Resource> addPhotoToVet(String vetId, String photoName, Mono<Resource> image) {
        return webClientBuilder
                .build()
                .post()
                .uri(vetsServiceUrl + "/" + vetId + "/photos/" + photoName)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(image, Resource.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(NOT_FOUND))
                        return Mono.error(new NotFoundException("Photo for vet " + vetId + " not found"));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Resource.class);
    }

    public Mono<Resource> updatePhotoOfVet(String vetId, String photoName, Mono<Resource> image){
        return webClientBuilder
                .build()
                .put()
                .uri(vetsServiceUrl+"/"+vetId+"/photos/"+photoName)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(image, Resource.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new NotFoundException("Photo for vet "+vetId + " not found"));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Resource.class);
    }

    //Badge
    public Mono<BadgeResponseDTO> getBadgeByVetId(String vetId){
        return webClientBuilder.build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId+ "/badge")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("vetId not found: "+vetId));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(BadgeResponseDTO.class);
    }

    //Ratings
    public Flux<RatingResponseDTO> getRatingsByVetId(String vetId) {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/ratings")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("vetId not found: "+vetId));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToFlux(RatingResponseDTO.class);
    }

    public Mono<Integer> getNumberOfRatingsByVetId(String vetId) {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/{vetId}/ratings/count", vetId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Integer.class);
    }

    public Mono<String> getPercentageOfRatingsByVetId(String vetId) {
        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/{vetId}/ratings/percentages", vetId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(String.class);
    }

    public Mono<RatingResponseDTO> addRatingToVet(String vetId, Mono<RatingRequestDTO> ratingRequestDTO) {

        return webClientBuilder
                .build()
                .post()
                .uri(vetsServiceUrl + "/" + vetId + "/ratings")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(ratingRequestDTO, RatingResponseDTO.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(RatingResponseDTO.class);
    }
    public Mono<RatingResponseDTO> updateRatingByVetIdAndByRatingId(String vetId, String ratingId, Mono<RatingRequestDTO> ratingRequestDTOMono){

        return webClientBuilder
                .build()
                .put()
                .uri(vetsServiceUrl+"/"+vetId+"/ratings/"+ratingId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(ratingRequestDTOMono, RatingResponseDTO.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("Rating not found for vetId: " + vetId + " and ratingId: " + ratingId));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(RatingResponseDTO.class);
    }

    public Mono<Void> deleteRating(String vetId, String ratingId) {
        return webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/ratings" + "/" + ratingId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("vetId not found "+vetId+" or ratingId not found: " + ratingId));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Void.class);
    }
    public Flux<VetAverageRatingDTO> getTopThreeVetsWithHighestAverageRating() {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/topVets" )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("No vets found"));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToFlux(VetAverageRatingDTO.class);
    }
    public Flux<RatingResponseDTO> getRatingsOfAVetBasedOnDate(String vetId, Map<String,String> queryParams){
        return webClientBuilder
                .baseUrl(vetsServiceUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + vetId + "/ratings/date")
                        .queryParam("year",queryParams.get("year"))
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("No ratings found for vetId: " + vetId));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToFlux(RatingResponseDTO.class);
    }

    public Mono<Double> getAverageRatingByVetId(String vetId) {
        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/ratings" + "/average")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Double.class);
    }
    //Vets
    public Flux<VetResponseDTO> getVets() {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("No vets found", NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToFlux(VetResponseDTO.class);
    }

    public Mono<VetResponseDTO> getVetByVetId(String vetId) {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(VetResponseDTO.class);
    }

    public Mono<VetResponseDTO> getVetByVetBillId(String vetBillId) {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/vetBillId/{vetBillId}", vetBillId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vet with this vetBillId not found: "+vetBillId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(VetResponseDTO.class);
    }

    public Flux<VetResponseDTO> getInactiveVets() {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/inactive")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("No inactive vets found", NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToFlux(VetResponseDTO.class);
    }

    public Flux<VetResponseDTO> getActiveVets() {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/active")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("No active vets found", NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToFlux(VetResponseDTO.class);
    }

    public Mono<VetResponseDTO> addVet(Mono<VetRequestDTO> vetRequestDTO){
        String vetId = UUID.randomUUID().toString();
        return vetRequestDTO.flatMap(request ->{
            request.setVetId(vetId);
            return webClientBuilder
                    .build()
                    .post()
                    .uri(vetsServiceUrl)
                    .body(BodyInserters.fromValue(request))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, error->{
                        HttpStatusCode statusCode = error.statusCode();
                        if(statusCode.equals(NOT_FOUND))
                            return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                        return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError,error->
                            Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                    )
                    .bodyToMono(VetResponseDTO.class);
        });
    }


    public Mono<VetResponseDTO> createVet(Mono<VetRequestDTO> model) {

        return webClientBuilder
                .build()
                .post()
                .uri(vetsServiceUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(model, VetRequestDTO.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found", NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(VetResponseDTO.class);
    }

    public Mono<Void> deleteVet(String vetId) {

        return webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Void.class);
    }

    public Mono<VetResponseDTO> updateVet(String vetId,Mono<VetRequestDTO> model) {

        return webClientBuilder
                .build()
                .put()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(model, VetRequestDTO.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(VetResponseDTO.class);
    }


    //Education
    public Flux<EducationResponseDTO> getEducationsByVetId(String vetId) {

        return webClientBuilder
                .build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/educations")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("Education not found for vetId: " + vetId));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToFlux(EducationResponseDTO.class);
    }

    public Mono<Void> deleteEducation(String vetId, String educationId) {
        return webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/educations" + "/" + educationId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("vetId not found "+vetId+" or educationId not found: " + educationId));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(Void.class);
    }
    public Mono<EducationResponseDTO> updateEducationByVetIdAndByEducationId(String vetId, String educationId, Mono<EducationRequestDTO> educationRequestDTOMono){
        Mono<EducationResponseDTO> educationResponseDTOMono =
                webClientBuilder
                        .build()
                        .put()
                        .uri(vetsServiceUrl+"/"+vetId+"/educations/"+educationId)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(educationRequestDTOMono, EducationResponseDTO.class)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, error -> {
                            HttpStatusCode statusCode = error.statusCode();
                            if (statusCode.equals(HttpStatus.NOT_FOUND))
                                return Mono.error(new NotFoundException("Education not found for vetId: " + vetId + " and educationId: " + educationId));
                            return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, error ->
                                Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                        )
                        .bodyToMono(EducationResponseDTO.class);

        return educationResponseDTOMono;
    }

    public Mono<EducationResponseDTO> addEducationToAVet(String vetId, Mono<EducationRequestDTO> educationRequestDTOMono){

        return webClientBuilder
                .build()
                .post()
                .uri(vetsServiceUrl + "/" + vetId + "/educations")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(educationRequestDTOMono, EducationResponseDTO.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+ vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(EducationResponseDTO.class);
    }
    //specialties
    public Mono<VetResponseDTO> addSpecialtiesByVetId(String vetId, Mono<SpecialtyDTO> specialties) {
        return webClientBuilder
                .build()
                .post()  // POST for adding new resources
                .uri(vetsServiceUrl + "/" + vetId + "/specialties")  // Ensure you're pointing to the correct POST endpoint
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(BodyInserters.fromPublisher(specialties, SpecialtyDTO.class))  // Use fromPublisher to handle the Mono
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new ExistingVetNotFoundException("Vet not found: " + vetId, HttpStatus.NOT_FOUND));
                    }
                    return Mono.error(new IllegalArgumentException("Something went wrong with the client"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong with the server"))
                )
                .bodyToMono(VetResponseDTO.class);
    }

    public Flux<Album> getAllAlbumsByVetId(String vetId) {
        return webClientBuilder.build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/albums")
                .accept(MediaType.APPLICATION_JSON) // Set Content-Type to application/json
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new ExistingVetNotFoundException("Albums for vet " + vetId + " not found", NOT_FOUND));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToFlux(Album.class);
    }

    public Mono<Void> deletePhotoByVetId(String vetId) {
        return webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/photo")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new ExistingVetNotFoundException("Photo not found for vetId: " + vetId, HttpStatus.NOT_FOUND));
                    }
                    return Mono.error(new IllegalArgumentException("Client error occurred while deleting photo for vetId: " + vetId));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Server error occurred while deleting photo for vetId: " + vetId))
                )
                .bodyToMono(Void.class);
    }

}