package com.petclinic.vet.presentationlayer;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.petclinic.vet.servicelayer.*;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("vets")
public class VetController {
    private final VetService vetService;
    private final RatingService ratingService;
    private final PhotoService photoService;
    private final EducationService educationService;

    //Ratings
    @GetMapping("{vetId}/ratings")
    public Flux<RatingResponseDTO> getAllRatingsByVetId(@PathVariable String vetId) {
        return ratingService.getAllRatingsByVetId(EntityDtoUtil.verifyId(vetId));

    }

    @GetMapping("{vetId}/ratings/count")
    public Mono<ResponseEntity<Integer>> getNumberOfRatingsByVetId(@PathVariable String vetId){
        return ratingService.getNumberOfRatingsByVetId(vetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{vetId}/ratings")
    public Mono<ResponseEntity<RatingResponseDTO>> addRatingToVet(@PathVariable String vetId, @RequestBody Mono<RatingRequestDTO> ratingRequest) {
        return ratingService.addRatingToVet(vetId, ratingRequest)
                .map(r->ResponseEntity.status(HttpStatus.CREATED).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("{vetId}/ratings/{ratingId}")
    public Mono<ResponseEntity<Void>> deleteRatingByRatingId(@PathVariable String vetId,
                                             @PathVariable String ratingId){
        return ratingService.deleteRatingByRatingId(vetId, ratingId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("{vetId}/ratings/average")
    public Mono<ResponseEntity<Double>> getAverageRatingByVetId(@PathVariable String vetId){
        return ratingService.getAverageRatingByVetId(vetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("{vetId}/ratings/{ratingId}")
    public Mono<ResponseEntity<RatingResponseDTO>> updateRatingByVetIdAndRatingId(@PathVariable String vetId,
                                                                                  @PathVariable String ratingId,
                                                                                  @RequestBody Mono<RatingRequestDTO> ratingRequestDTOMono){
        return ratingService.updateRatingByVetIdAndRatingId(vetId, ratingId, ratingRequestDTOMono)
                .map(r->ResponseEntity.status(HttpStatus.OK).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    /*@PutMapping("{vetId}/ratings/{ratingId}")
    public Mono<RatingResponseDTO> updateRatingByVetIdAndRatingId(@PathVariable String vetId, @PathVariable String ratingId, @RequestBody Mono<RatingRequestDTO> ratingRequestDTOMono){
        return ratingService.updateRating(vetId, ratingId, ratingRequestDTOMono);
    }*/

    @GetMapping("{vetId}/ratings/percentages")
    public Mono<ResponseEntity<String>> getPercentageOfRatingsByVetId(@PathVariable String vetId){
        return ratingService.getRatingPercentagesByVetId(EntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    //Vets
    @GetMapping()
    public Flux<VetDTO> getAllVets() {
        return vetService.getAll();
    }

    @GetMapping("{vetId}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetService.getVetByVetId(EntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/vetBillId/{vetBillId}")
    public Mono<ResponseEntity<VetDTO>> getVetByBillId(@PathVariable String vetBillId) {
        return vetService.getVetByVetBillId(EntityDtoUtil.verifyId(vetBillId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public Flux<VetDTO> getActiveVets() {
        return vetService.getVetByIsActive(true);
    }

    @GetMapping("/inactive")
    public Flux<VetDTO> getInactiveVets() {
        return vetService.getVetByIsActive(false);
    }

    @PostMapping
    public Mono<ResponseEntity<VetDTO>> insertVet(@RequestBody Mono<VetDTO> vetDTOMono) {
        return vetService.insertVet(vetDTOMono)
                .map(v->ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("{vetId}")
    public Mono<ResponseEntity<VetDTO>> updateVetByVetId(@PathVariable String vetId, @RequestBody Mono<VetDTO> vetDTOMono) {
        return vetService.updateVet(EntityDtoUtil.verifyId(vetId), vetDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{vetId}")
    public Mono<ResponseEntity<Void>> deleteVet(@PathVariable String vetId) {
        return vetService.deleteVetByVetId(EntityDtoUtil.verifyId(vetId))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    //Education
    @GetMapping("{vetId}/educations")
    public Flux<EducationResponseDTO> getAllEducationsByVetId(@PathVariable String vetId) {
        return educationService.getAllEducationsByVetId(EntityDtoUtil.verifyId(vetId));
    }

    @DeleteMapping("{vetId}/educations/{educationId}")
    public Mono<Void> deleteEducationByEducationId(@PathVariable String vetId,
                                                   @PathVariable String educationId){
        return educationService.deleteEducationByEducationId(vetId, educationId);

    }


    //Photo
    @GetMapping("{vetId}/photo")
    public Mono<ResponseEntity<Resource>> getPhotoByVetId(@PathVariable String vetId){
        return photoService.getPhotoByVetId(vetId)
                .map(r -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
