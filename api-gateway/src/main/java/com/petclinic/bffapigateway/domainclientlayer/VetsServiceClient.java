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
import org.springframework.web.reactive.function.client.WebClient;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        Mono<Resource> photo = webClientBuilder.build()
                .get()
                .uri(vetsServiceUrl + "/" + vetId + "/photo")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("Photo for vet "+vetId + " not found", NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong"))
                )
                .bodyToMono(Resource.class);
        return photo;
    }



    //Ratings
    public Flux<RatingResponseDTO> getRatingsByVetId(String vetId) {
        Flux<RatingResponseDTO> ratingResponseDTOFlux =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/" + vetId + "/ratings")
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, error->{
                            HttpStatusCode statusCode = error.statusCode();
                            if(statusCode.equals(HttpStatus.NOT_FOUND))
                                return Mono.error(new NotFoundException("vetId not found: "+vetId));
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToFlux(RatingResponseDTO.class);

        return ratingResponseDTOFlux;
    }

    public Mono<Integer> getNumberOfRatingsByVetId(String vetId) {
        Mono<Integer> numberOfRatings =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/{vetId}/ratings/count", vetId)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, error->{
                            HttpStatusCode statusCode = error.statusCode();
                            if(statusCode.equals(NOT_FOUND))
                                return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError,error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(Integer.class);

        return numberOfRatings;
    }

    public Mono<String> getPercentageOfRatingsByVetId(String vetId) {
        Mono<String> percentageOfRatings =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/{vetId}/ratings/percentages", vetId)
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, error->{
                            HttpStatusCode statusCode = error.statusCode();
                            if(statusCode.equals(NOT_FOUND))
                                return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError,error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(String.class);
        return percentageOfRatings;
    }

    public Mono<RatingResponseDTO> addRatingToVet(String vetId, Mono<RatingRequestDTO> ratingRequestDTO) {
        Mono<RatingResponseDTO> ratingResponseDTOMono =
                webClientBuilder
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
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError,error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(RatingResponseDTO.class);

        return ratingResponseDTOMono;
    }
    public Mono<RatingResponseDTO> updateRatingByVetIdAndByRatingId(String vetId, String ratingId, Mono<RatingRequestDTO> ratingRequestDTOMono){
        Mono<RatingResponseDTO> ratingResponseDTOMono =
                webClientBuilder
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
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, error ->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(RatingResponseDTO.class);

        return ratingResponseDTOMono;
    }

    public Mono<Void> deleteRating(String vetId, String ratingId) {
        Mono<Void> result = webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/ratings" + "/" + ratingId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error -> {
                    HttpStatusCode statusCode = error.statusCode();
                    if (statusCode.equals(HttpStatus.NOT_FOUND))
                        return Mono.error(new NotFoundException("vetId not found "+vetId+" or ratingId not found: " + ratingId));
                    return Mono.error(new IllegalArgumentException("Something went wrong"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, error ->
                        Mono.error(new IllegalArgumentException("Something went wrong"))
                )
                .bodyToMono(Void.class);
        return result;
    }
    public Flux<VetAverageRatingDTO> getTopThreeVetsWithHighestAverageRating() {
        Flux<VetAverageRatingDTO> averageRatingDTOFlux =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/topVets" )
                        .retrieve()
                        .bodyToFlux(VetAverageRatingDTO.class);

        return averageRatingDTOFlux;
    }
    public Mono<Double> getAverageRatingByVetId(String vetId) {
        Mono<Double> averageRating =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/" + vetId + "/ratings" + "/average")
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, error->{
                            HttpStatusCode statusCode = error.statusCode();
                            if(statusCode.equals(NOT_FOUND))
                                return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError,error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(Double.class);
        return averageRating;
    }



    //Vets
    public Flux<VetDTO> getVets() {
        Flux<VetDTO> vetDTOFlux =
               webClientBuilder
                 .build()
                 .get()
                 .uri(vetsServiceUrl)
                 .retrieve()
                 .onStatus(HttpStatusCode::is5xxServerError,error->
                         Mono.error(new IllegalArgumentException("Something went wrong"))
                 )
                 .bodyToFlux(VetDTO.class);

        return  vetDTOFlux;
    }

    public Mono<VetDTO> getVetByVetId(String vetId) {
        Mono<VetDTO> vetDTOMono =
                webClientBuilder
                  .build()
                  .get()
                  .uri(vetsServiceUrl + "/{vetId}", vetId)
                  .retrieve()
                  .onStatus(HttpStatusCode::is4xxClientError, error->{
                      HttpStatusCode statusCode = error.statusCode();
                      if(statusCode.equals(NOT_FOUND))
                          return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                      return Mono.error(new IllegalArgumentException("Something went wrong"));
                  })
                  .onStatus(HttpStatusCode::is5xxServerError,error->
                          Mono.error(new IllegalArgumentException("Something went wrong"))
                  )
                  .bodyToMono(VetDTO.class);

        return vetDTOMono;
    }

    public Mono<VetDTO> getVetByVetBillId(String vetBillId) {
        Mono<VetDTO> vetDTOMono =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/vetBillId/{vetBillId}", vetBillId)
                        .retrieve()
                        .bodyToMono(VetDTO.class);

        return vetDTOMono;
    }

    public Flux<VetDTO> getInactiveVets() {
        Flux<VetDTO> vetDTOFlux =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/inactive")
                        .retrieve()
                        .onStatus(HttpStatusCode::is5xxServerError,error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToFlux(VetDTO.class);

        return  vetDTOFlux;
    }

    public Flux<VetDTO> getActiveVets() {
        Flux<VetDTO> vetDTOFlux =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/active")
                        .retrieve()
                        .onStatus(HttpStatusCode::is5xxServerError,error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToFlux(VetDTO.class);

        return  vetDTOFlux;
    }


    public Mono<VetDTO> createVet(Mono<VetDTO> model) {
        Mono<VetDTO> vetDTO =
                webClientBuilder
                        .build()
                        .post()
                .uri(vetsServiceUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(model, VetDTO.class)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong"))
                )
                .bodyToMono(VetDTO.class);

        return vetDTO;
    }

    public Mono<Void> deleteVet(String vetId) {
        Mono<Void> response = webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong"))
                )
                .bodyToMono(Void.class);

        return response;
    }

    public Mono<VetDTO> updateVet(String vetId,Mono<VetDTO> model) {
        Mono<VetDTO> vetDTOMono = webClientBuilder
                .build()
                .put()
                .uri(vetsServiceUrl + "/{vetId}", vetId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(model, VetDTO.class)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, error->{
                    HttpStatusCode statusCode = error.statusCode();
                    if(statusCode.equals(NOT_FOUND))
                        return Mono.error(new ExistingVetNotFoundException("vetId not found: "+vetId, NOT_FOUND));
                    return Mono.error(new IllegalArgumentException("Something went wrong"));
                })
                .onStatus(HttpStatusCode::is5xxServerError,error->
                        Mono.error(new IllegalArgumentException("Something went wrong"))
                )
                .bodyToMono(VetDTO.class);

        return vetDTOMono;
    }


    //Education
    public Flux<EducationResponseDTO> getEducationsByVetId(String vetId) {
        Flux<EducationResponseDTO> educationResponseDTOFlux =
                webClientBuilder
                        .build()
                        .get()
                        .uri(vetsServiceUrl + "/" + vetId + "/educations")
                        .retrieve()
                        .bodyToFlux(EducationResponseDTO.class);

        return educationResponseDTOFlux;
    }

    public Mono<Void> deleteEducation(String vetId, String educationId) {
        Mono<Void> result = webClientBuilder
                .build()
                .delete()
                .uri(vetsServiceUrl + "/" + vetId + "/educations" + "/" + educationId)
                .retrieve()
                .bodyToMono(Void.class);
        return result;
    }

    public Mono<EducationResponseDTO> addEducationToAVet(String vetId, Mono<EducationRequestDTO> educationRequestDTOMono){
        Mono<EducationResponseDTO> educationResponseDTOMono =
                webClientBuilder
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
                            return Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                        .onStatus(HttpStatusCode::is5xxServerError,error->
                                Mono.error(new IllegalArgumentException("Something went wrong"))
                        )
                        .bodyToMono(EducationResponseDTO.class);

        return educationResponseDTOMono;
    }

}