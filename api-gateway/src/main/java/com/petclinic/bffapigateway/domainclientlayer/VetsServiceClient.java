package com.petclinic.bffapigateway.domainclientlayer;

import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.dtos.Files.FileRequestDTO;
import com.petclinic.bffapigateway.exceptions.ExistingRatingNotFoundException;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
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
                .accept(MediaType.APPLICATION_JSON)
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
                .bodyToMono(PhotoResponseDTO.class)
                .map(dto -> {
                    byte[] data = dto.getResource() != null ? dto.getResource() : 
                                  (dto.getResourceBase64() != null ? java.util.Base64.getDecoder().decode(dto.getResourceBase64()) : new byte[0]);
                    return (Resource) new org.springframework.core.io.ByteArrayResource(data);
                });
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

    public Mono<Resource> addPhotoToVetFromBytes(String vetId, String photoName, byte[] fileData) {
        PhotoRequestDTO photoRequest = PhotoRequestDTO.builder()
                .vetId(vetId)
                .filename(photoName)
                .imgType(determineContentType(photoName))
                .data(fileData)
                .build();
                
        return webClientBuilder.build()
                .post()
                .uri(vetsServiceUrl + "/" + vetId + "/photos")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(photoRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    if (error.statusCode().equals(NOT_FOUND)) {
                        return Mono.error(new NotFoundException("Photo for vet " + vetId + " not found"));
                    }
                    return Mono.error(new IllegalArgumentException("Client error"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,
                        error -> Mono.error(new IllegalArgumentException("Server error")))
                .bodyToMono(PhotoResponseDTO.class)
                .map(dto -> {
                    byte[] data = dto.getResource() != null ? dto.getResource() : 
                                  (dto.getResourceBase64() != null ? java.util.Base64.getDecoder().decode(dto.getResourceBase64()) : new byte[0]);
                    return (Resource) new org.springframework.core.io.ByteArrayResource(data);
                });
    }
    
    private String determineContentType(String filename) {
        if (filename == null) {
            return "image/jpeg";
        }
        String lowerCase = filename.toLowerCase();
        if (lowerCase.endsWith(".png")) {
            return "image/png";
        } else if (lowerCase.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerCase.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "image/jpeg";
        }
    }

    public Mono<Resource> addPhotoToVet(String vetId, String photoName, FilePart filePart) {
        return filePart.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .reduce((byte[] a, byte[] b) -> {
                    byte[] combined = new byte[a.length + b.length];
                    System.arraycopy(a, 0, combined, 0, a.length);
                    System.arraycopy(b, 0, combined, a.length, b.length);
                    return combined;
                })
                .flatMap(bytes -> {
                    String contentType = filePart.headers().getContentType() != null 
                            ? filePart.headers().getContentType().toString() : "image/jpeg";
                    PhotoRequestDTO photoRequest = PhotoRequestDTO.builder()
                            .vetId(vetId)
                            .filename(photoName)
                            .imgType(contentType)
                            .data(bytes)
                            .build();
                    return webClientBuilder.build()
                            .post()
                            .uri(vetsServiceUrl + "/" + vetId + "/photos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .bodyValue(photoRequest)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, error -> {
                                if (error.statusCode().equals(NOT_FOUND)) {
                                    return Mono.error(new NotFoundException("Photo for vet " + vetId + " not found"));
                                }
                                return Mono.error(new IllegalArgumentException("Client error"));
                            })
                            .onStatus(HttpStatusCode::is5xxServerError,
                                    error -> Mono.error(new IllegalArgumentException("Server error")))
                            .bodyToMono(PhotoResponseDTO.class)
                            .map(dto -> {
                                byte[] data = dto.getResource() != null ? dto.getResource() : 
                                              (dto.getResourceBase64() != null ? java.util.Base64.getDecoder().decode(dto.getResourceBase64()) : new byte[0]);
                                return (Resource) new org.springframework.core.io.ByteArrayResource(data);
                            });
                });
    }


    public Mono<Resource> updatePhotoOfVet(String vetId, String photoName, Mono<Resource> image){
        return image.flatMap(resource -> {
            try {
                byte[] data = resource.getInputStream().readAllBytes();
                PhotoRequestDTO photoRequest = PhotoRequestDTO.builder()
                        .vetId(vetId)
                        .filename(photoName)
                        .imgType(determineContentType(photoName))
                        .data(data)
                        .build();
                        
                return webClientBuilder
                        .build()
                        .put()
                        .uri(vetsServiceUrl+"/"+vetId+"/photo")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(photoRequest)
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
                        .bodyToMono(PhotoResponseDTO.class)
                        .map(dto -> {
                            byte[] responseData = dto.getResource() != null ? dto.getResource() : 
                                          (dto.getResourceBase64() != null ? java.util.Base64.getDecoder().decode(dto.getResourceBase64()) : new byte[0]);
                            return (Resource) new org.springframework.core.io.ByteArrayResource(responseData);
                        });
            } catch (java.io.IOException e) {
                return Mono.error(new IllegalArgumentException("Failed to read resource: " + e.getMessage()));
            }
        });
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

    public Mono<Void> deleteRatingByCustomerName(String vetId, String customerName) {
        return webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/ratings/customer/" + customerName)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("vetId not found "+vetId+" or no rating found for customer: " + customerName));
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
                .accept(MediaType.TEXT_EVENT_STREAM)
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

    public Mono<VetResponseDTO> deleteVet(String vetId) {

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
                .bodyToMono(VetResponseDTO.class);
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
                .uri(vetsServiceUrl + "/" + vetId + "/educations/" + educationId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("Education not found: " + educationId));
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

    public Mono<Void> deleteSpecialtyBySpecialtyId(String vetId, String specialtyId) {
        return webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/specialties/" + specialtyId)
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
                .bodyToMono(Void.class);
    }

    public Flux<Album> getAllAlbumsByVetId(String vetId) {
        return webClientBuilder.build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/albums")
                .accept(MediaType.TEXT_EVENT_STREAM)
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
    public Mono<Void> deleteAlbumPhotoById(String vetId, Integer Id) {
        return webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/albums/" + Id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new NotFoundException("Album photo not found: " + Id));
                    }
                    return Mono.error(new IllegalArgumentException("Client error occurred while deleting album photo with ID: " + Id));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Server error occurred while deleting album photo with ID: " + Id))
                )
                .bodyToMono(Void.class);
    }


public Mono<Album> addAlbumPhotoFromBytes(String vetId, String photoName, byte[] fileData) {
    return webClientBuilder.build()
            .post()
            .uri(vetsServiceUrl + "/" + vetId + "/albums/photos/" + photoName)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(fileData)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, error -> {
                if (error.statusCode().equals(NOT_FOUND)) {
                    return Mono.error(new NotFoundException("Album source not found for vet " + vetId));
                }
                return Mono.error(new IllegalArgumentException("Client error while adding album photo"));
            })
            .onStatus(HttpStatusCode::is5xxServerError,
                    error -> Mono.error(new IllegalArgumentException("Server error while adding album photo")))
            .bodyToMono(Album.class);
}

public Mono<Album> addAlbumPhoto(String vetId, String photoName, FilePart filePart) {
    return DataBufferUtils.join(filePart.content())
            .map(buf -> {
                byte[] bytes = new byte[buf.readableByteCount()];
                buf.read(bytes);
                DataBufferUtils.release(buf);
                return bytes;
            })
            .flatMap(bytes -> webClientBuilder.build()
                    .post()
                    .uri(vetsServiceUrl + "/" + vetId + "/albums/photos/" + photoName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(bytes)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, error -> {
                        if (error.statusCode().equals(NOT_FOUND)) {
                            return Mono.error(new NotFoundException("Album source not found for vet " + vetId));
                        }
                        return Mono.error(new IllegalArgumentException("Client error while adding album photo"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            error -> Mono.error(new IllegalArgumentException("Server error while adding album photo")))
                    .bodyToMono(Album.class));
}

    public Mono<VetResponseDTO> getVet(final String vetId, boolean includePhoto) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(vetsServiceUrl + "/" + vetId);
        builder.queryParam("includePhoto", includePhoto);
    
        return webClientBuilder.build().get()
                .uri(builder.build().toUri())
                .retrieve()
                .bodyToMono(VetResponseDTO.class);
    }

    public Mono<VetResponseDTO> updateVetPhoto(String vetId, Mono<FileRequestDTO> photoMono) {
        return photoMono.flatMap(photo ->
            webClientBuilder.build()
                    .patch()
                    .uri(vetsServiceUrl + "/" + vetId + "/photo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(photo)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, error -> {
                        if (error.statusCode().equals(NOT_FOUND)) {
                            return Mono.error(new NotFoundException("Vet not found with id: " + vetId));
                        }
                        return Mono.error(new IllegalArgumentException("Client error"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            error -> Mono.error(new IllegalArgumentException("Server error")))
                    .bodyToMono(VetResponseDTO.class)
        );
    }

}
