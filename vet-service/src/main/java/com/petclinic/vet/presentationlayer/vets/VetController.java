package com.petclinic.vet.presentationlayer.vets;
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


import com.petclinic.vet.businesslayer.albums.AlbumService;
import com.petclinic.vet.businesslayer.badges.BadgeService;
import com.petclinic.vet.businesslayer.education.EducationService;
import com.petclinic.vet.businesslayer.photos.PhotoService;
import com.petclinic.vet.businesslayer.ratings.RatingService;
import com.petclinic.vet.businesslayer.vets.VetService;
import com.petclinic.vet.dataaccesslayer.albums.Album;
import com.petclinic.vet.presentationlayer.badges.BadgeResponseDTO;
import com.petclinic.vet.presentationlayer.files.FileRequestDTO;
import com.petclinic.vet.presentationlayer.photos.PhotoRequestDTO;
import com.petclinic.vet.presentationlayer.photos.PhotoResponseDTO;
import com.petclinic.vet.presentationlayer.education.EducationRequestDTO;
import com.petclinic.vet.presentationlayer.education.EducationResponseDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingRequestDTO;
import com.petclinic.vet.presentationlayer.ratings.RatingResponseDTO;
import com.petclinic.vet.utils.EntityDtoUtil;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("/vets")
@Slf4j
public class VetController {
    private final VetService vetService;
    private final RatingService ratingService;
    private final PhotoService photoService;
    private final EducationService educationService;
    private final BadgeService badgeService;
    private final AlbumService albumService;


    //Ratings
    @GetMapping("{vetId}/ratings")
    public Flux<RatingResponseDTO> getAllRatingsByVetId(@PathVariable String vetId) {
        return ratingService.getAllRatingsByVetId(EntityDtoUtil.verifyId(vetId))
                .doOnNext(rating -> log.info("Rating ID: {}, Vet ID: {}, Rating: {}, Customer Name: {}, Experience: {}",
                        rating.getRatingId(), rating.getVetId(), rating.getRateScore(), rating.getCustomerName(), rating.getPredefinedDescription()))
                .doOnComplete(() -> log.info("Successfully fetched all ratings for vetId: {}", vetId))
                .doOnError(error -> log.error("Error fetching ratings for vet {}", vetId, error));
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

    @DeleteMapping("{vetId}/ratings/customer/{customerName}")
    public Mono<ResponseEntity<Void>> deleteRatingByCustomerName(@PathVariable String vetId,
                                                                 @PathVariable String customerName){
        return ratingService.deleteRatingByVetIdAndCustomerName(vetId, customerName)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("{vetId}/ratings/average")
    public Mono<ResponseEntity<Double>> getAverageRatingByVetId(@PathVariable String vetId){
        return ratingService.getAverageRatingByVetId(vetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("{vetId}/ratings/date")
    public Flux<RatingResponseDTO> getRatingsOfAVetBasedOnDate(@PathVariable String vetId, @RequestParam Map<String,String> queryParams) {
        if (queryParams.containsKey("year")) {
            String year = queryParams.get("year");


            //This regex signifies that it required 4 numbers input for the year
            if (!year.matches("^\\d{4}$")) {
                throw new NotFoundException("Invalid year format. Please enter a valid year.");
            }
        }
        return ratingService.getRatingsOfAVetBasedOnDate(vetId, queryParams)
                .switchIfEmpty(Mono.error(new NotFoundException("No valid ratings were found for " + vetId)));
    }




    @GetMapping("topVets")
    public Flux<VetAverageRatingDTO> getTopThreeVetsWithHighestAverageRating() {
        return ratingService.getTopThreeVetsWithHighestAverageRating();
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


  /*@GetMapping("{vetId}/ratings/{predefinedDescription}/count")
  public Mono<ResponseEntity<Integer>> getCountOfRatingsByVetIdAndPredefinedDescription(@PathVariable String vetId, @PathVariable PredefinedDescription predefinedDescription){
      return ratingService.getCountOfRatingsByVetIdAndPredefinedDescription(EntityDtoUtil.verifyId(vetId), predefinedDescription)
              .map(ResponseEntity::ok)
              .defaultIfEmpty(ResponseEntity.notFound().build());
  }*/


    //Vets
    @GetMapping()
    public Flux<VetResponseDTO> getAllVets() {
        return vetService.getAll();
    }


    @GetMapping(value = "/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> getVetByVetId(@PathVariable String vetId, @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return Mono.just(vetId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided vet id is invalid:" + vetId)))
                .flatMap(id -> vetService.getVetByVetId(id, includePhoto))
                .map(ResponseEntity::ok);
    }

    //bills

    @GetMapping("/vetBillId/{vetBillId}")
    public Mono<ResponseEntity<VetResponseDTO>> getVetByBillId(@PathVariable String vetBillId) {
        return vetService.getVetByVetBillId(EntityDtoUtil.verifyId(vetBillId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @GetMapping("/active")
    public Flux<VetResponseDTO> getActiveVets() {
        return vetService.getVetByIsActive(true);
    }


    @GetMapping("/inactive")
    public Flux<VetResponseDTO> getInactiveVets() {
        return vetService.getVetByIsActive(false);
    }


    @PostMapping
    public Mono<ResponseEntity<VetResponseDTO>> insertVet(@RequestBody Mono<VetRequestDTO> vetRequestDTOMono) {
        return vetService.addVet(vetRequestDTOMono)
                .map(v->ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }



    @PutMapping("{vetId}")
    public Mono<ResponseEntity<VetResponseDTO>> updateVetByVetId(@PathVariable String vetId, @RequestBody Mono<VetRequestDTO> vetRequestDTOMono) {
    return vetService.updateVet(EntityDtoUtil.verifyId(vetId), vetRequestDTOMono)
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
    public Mono<ResponseEntity<Void>> deleteEducationByEducationId(@PathVariable String vetId,
                                                                   @PathVariable String educationId){
        return educationService.deleteEducationByEducationId(vetId, educationId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @PutMapping("{vetId}/educations/{educationId}")
    public Mono<ResponseEntity<EducationResponseDTO>> updateEducationByVetIdAndEducationId(@PathVariable String vetId,
                                                                                           @PathVariable String educationId,
                                                                                           @RequestBody Mono<EducationRequestDTO> educationRequestDTOMono){
        return educationService.updateEducationByVetIdAndEducationId(vetId, educationId, educationRequestDTOMono)
                .map(e->ResponseEntity.status(HttpStatus.OK).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @PostMapping("{vetId}/educations")
    public Mono<ResponseEntity<EducationResponseDTO>> addEducationToVet(@PathVariable String vetId,
                                                                        @RequestBody Mono<EducationRequestDTO> educationRequestDTOMono) {
        return educationService.addEducationToVet(vetId, educationRequestDTOMono)
                .map(education -> ResponseEntity.status(HttpStatus.CREATED).body(education))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }







    //Photo
    @GetMapping("{vetId}/photo")
    public Mono<ResponseEntity<PhotoResponseDTO>> getPhotoByVetId(@PathVariable String vetId){
        return photoService.getPhotoByVetId(vetId)
                .map(photo -> ResponseEntity.ok().body(photo))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @GetMapping("{vetId}/default-photo")
    public Mono<ResponseEntity<PhotoResponseDTO>> getDefaultPhotoByVetId(@PathVariable String vetId){
        return photoService.getDefaultPhotoByVetId(vetId)
                .map(r -> ResponseEntity.ok().body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @PostMapping(value = "{vetId}/photos",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PhotoResponseDTO>> addPhotoByVetId(
            @PathVariable String vetId,
            @RequestBody Mono<PhotoRequestDTO> photoRequestDTO) {
        return photoService.insertPhotoOfVet(vetId, photoRequestDTO)
                .map(photo -> ResponseEntity.status(HttpStatus.CREATED).body(photo))
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
    }


    @DeleteMapping("{vetId}/photo")
    public Mono<ResponseEntity<Void>> deletePhotoByVetId(@PathVariable String vetId) {
        return photoService.deletePhotoByVetId(vetId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(NotFoundException.class, e -> Mono.just(ResponseEntity.notFound().build()));
    }

 @PostMapping(
        value = "{vetId}/albums/photos/{photoName}",
        consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Album>> addAlbumPhotoOctet(
            @PathVariable String vetId,
            @PathVariable String photoName,
            @RequestBody Mono<byte[]> fileData
    ) {
        return albumService.insertAlbumPhoto(vetId, photoName, fileData)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PostMapping(
        value = "{vetId}/albums/photos",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<Album>> addAlbumPhotoMultipart(
            @PathVariable String vetId,
            @RequestPart("photoName") String photoName,
            @RequestPart("file") FilePart file
    ) {
        return albumService.insertAlbumPhoto(vetId, photoName, file)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("{vetId}/photo")
    public Mono<ResponseEntity<PhotoResponseDTO>> updatePhotoByVetId(@PathVariable String vetId, @RequestBody Mono<PhotoRequestDTO> photoRequestDTO){
        return photoService.updatePhotoByVetId(vetId, photoRequestDTO)
                .map(photo -> ResponseEntity.ok().body(photo))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PatchMapping("/{vetId}/photo")
    public Mono<ResponseEntity<VetResponseDTO>> updateVetPhoto(@PathVariable String vetId, @RequestBody Mono<FileRequestDTO> photoMono) {
        return photoMono
                .flatMap(photo -> vetService.updateVetPhoto(vetId, photo))
                .map(updatedVet -> ResponseEntity.ok().body(updatedVet))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    //Badge
    @GetMapping("{vetId}/badge")
    public Mono<ResponseEntity<BadgeResponseDTO>> getBadgeByVetId(@PathVariable String vetId){
        return badgeService.getBadgeByVetId(vetId)
                .map(r->ResponseEntity.status(HttpStatus.OK).body(r))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    //specialty
    @PostMapping("{vetId}/specialties")
    public Mono<VetResponseDTO> addSpecialtiesByVetId(
            @PathVariable String vetId,
            @RequestBody Mono<SpecialtyDTO> specialties) {

        return vetService.addSpecialtiesByVetId(vetId, specialties);
    }

    @DeleteMapping("{vetId}/specialties/{specialtyId}")
    public Mono<ResponseEntity<Void>> deleteSpecialtyBySpecialtyId(
            @PathVariable String vetId,
            @PathVariable String specialtyId) {
        return vetService.deleteSpecialtyBySpecialtyId(vetId, specialtyId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("{vetId}/albums")
    public Flux<Album> getAllAlbumsByVetId(@PathVariable String vetId) {
        return albumService.getAllAlbumsByVetId(vetId)
                .doOnNext(album -> log.info("Album ID: {}, Vet ID: {}, Filename: {}, ImgType: {}",
                        album.getId(), album.getVetId(), album.getFilename(), album.getImgType()))
                .doOnComplete(() -> log.info("Successfully fetched all albums for vetId: {}", vetId))
                .doOnError(error -> log.error("Error fetching photos for vet {}", vetId, error));
    }

    @DeleteMapping("/{vetId}/albums/{Id}")
    public Mono<ResponseEntity<Void>> deleteAlbumPhoto(@PathVariable String vetId,@PathVariable Integer Id) {
        return albumService.deleteAlbumPhotoById(vetId, Id)
                .then(Mono.defer(() -> Mono.just(ResponseEntity.noContent().<Void>build())))
                .onErrorResume(NotFoundException.class, e -> Mono.defer(() -> Mono.just(ResponseEntity.notFound().build())));
    }

}
