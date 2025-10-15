package com.petclinic.visits.visitsservicenew.Utils;


import com.petclinic.visits.visitsservicenew.DataLayer.Review.Review;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Change one type to another
 */
@Component
@RequiredArgsConstructor
public class EntityDtoUtil {

    private final VetsClient vetsClient;
    private final PetsClient petsClient;

    /**
     * Transform a visit into a Mono<VisitResponseDTO>
     * @param visit The visit to transform
     */
    public Mono<VisitResponseDTO> toVisitResponseDTO(Visit visit) {
       // System.out.println("Entity Date in Mapping: " + visit.getVisitDate()); // Debugging

        Mono<PetResponseDTO> petResponseDTOMono = petsClient.getPetById(visit.getPetId());
        Mono<VetDTO> vetResponseDTOMono = vetsClient.getVetByVetId(visit.getPractitionerId());

        return Mono.zip(petResponseDTOMono, vetResponseDTOMono)
                .flatMap(tuple -> {
                    PetResponseDTO petResponseDTO = tuple.getT1();
                    VetDTO vetResponseDTO = tuple.getT2();

                    return Mono.just(VisitResponseDTO.builder()
                            .visitId(visit.getVisitId())
                            .visitDate(visit.getVisitDate())
                            .description(visit.getDescription())
                            .petId(visit.getPetId())
                            .petName(petResponseDTO.getName())
                            .petBirthDate(petResponseDTO.getBirthDate())
                            .practitionerId(visit.getPractitionerId())
                            .vetFirstName(vetResponseDTO.getFirstName())
                            .vetLastName(vetResponseDTO.getLastName())
                            .vetEmail(vetResponseDTO.getEmail())
                            .vetPhoneNumber(vetResponseDTO.getPhoneNumber())
                            .status(visit.getStatus())
                            .visitEndDate(visit.getVisitDate().plusHours(1))
                            .isEmergency(visit.getIsEmergency())
                            .build());
                });
    }

    /**
     * Transform a Request DTO into a Visit
     * @param visitRequestDTO The DTO to transform
     * @return The transformed DTO into a Visit
     */
    public Visit toVisitEntity(VisitRequestDTO visitRequestDTO) {
        Visit visit = new Visit();
        BeanUtils.copyProperties(visitRequestDTO, visit);
        visit.setStatus(Status.UPCOMING);
        return visit;
    }

    public static ReviewResponseDTO toReviewResponseDTO(Review review) {
        ReviewResponseDTO reviewResponseDTO  = new ReviewResponseDTO ();
        BeanUtils.copyProperties(review, reviewResponseDTO);
        return reviewResponseDTO;
    }

    public static Review toReviewEntity(ReviewRequestDTO reviewRequestDTO){
        return Review.builder()
                .reviewId(generateReviewIdString())
                .rating(reviewRequestDTO.getRating())
                .reviewerName(reviewRequestDTO.getReviewerName())
                .review(reviewRequestDTO.getReview())
                .dateSubmitted(reviewRequestDTO.getDateSubmitted())
                .build();
    }

    /**
     * Generate a random UUID and returns it. IS NOT ERROR FREE
     * @return The UUID as a string
     */
    public String generateVisitIdString() {
        return UUID.randomUUID().toString();
    }

    public static String generateReviewIdString() {
        return UUID.randomUUID().toString();
    }

    public static String generateEmergencyIdString() {
        return UUID.randomUUID().toString();
    }


}
