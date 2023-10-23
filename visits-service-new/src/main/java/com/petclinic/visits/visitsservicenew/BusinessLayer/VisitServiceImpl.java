package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.AuthServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.UserDetails;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.Mail;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.MailService;
import com.petclinic.visits.visitsservicenew.Exceptions.BadRequestException;
import com.petclinic.visits.visitsservicenew.Exceptions.DuplicateTimeException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

import static java.lang.String.format;


@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {
    private final VisitRepo repo;
    private final VetsClient vetsClient;
    private final PetsClient petsClient;
    private final EntityDtoUtil entityDtoUtil;

    private final AuthServiceClient authServiceClient;
    private final MailService mailService;
    @Override
    public Flux<VisitResponseDTO> getAllVisits() {
        return repo.findAll().flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit));
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForPet(String petId) {
        return validatePetId(petId)
                .thenMany(repo.findByPetId(petId)
                        .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit)));
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForStatus(String statusString) {
        Status status;
        switch (statusString){

            case("UPCOMING"):
                status = Status.UPCOMING;

            case("CONFIRMED"):
                status = Status.CONFIRMED;

            case("CANCELLED"):
                status = Status.CANCELLED;

            default:
                status = Status.COMPLETED;
        }
        return repo.findAllByStatus(statusString)
                .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit));
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForPractitioner(String vetId) {
        return validateVetId(vetId)
                .thenMany(repo.findVisitsByPractitionerId(vetId))
                .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit));
    }

    @Override
    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
        return repo.findByVisitId(visitId)
                .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit));
    }

    @Override
    public Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono) {
        return visitRequestDTOMono
                .flatMap(visitRequestDTO -> validateVisitRequest(visitRequestDTO)
                        .then(validatePetId(visitRequestDTO.getPetId()))
                        .then(validateVetId(visitRequestDTO.getPractitionerId()))
                        .then(Mono.just(visitRequestDTO))
                        .doOnNext(s->{

                            authServiceClient.getUserById(visitRequestDTO.getJwtToken(), visitRequestDTO.getOwnerId()).subscribe(user->mailService.sendMail(generateVisitRequestEmail(user, visitRequestDTO.getPetId(), visitRequestDTO.getVisitDate())));

        //                    Mono<UserDetails> user = getUserById(auth, ownerId);
        //                    try{
        //                        simpleJavaMailClient.sendMail(emailBuilder("test@email.com"));
        //                    }catch(Exception e){System.out.println("Email failed to send: "+e.getMessage());}
                        })
                )
//                .doOnNext(v -> System.out.println("Request Date: " + v.getVisitDate())) // Debugging
                .map(visitRequestDTO -> entityDtoUtil.toVisitEntity(visitRequestDTO))
//                .doOnNext(x -> x.setVisitId(entityDtoUtil.generateVisitIdString()))
//                .doOnNext(v -> System.out.println("Entity Date: " + v.getVisitDate())) // Debugging
                .flatMap(visit ->
                        repo.findByVisitDateAndPractitionerId(visit.getVisitDate(), visit.getPractitionerId()) // FindVisits method in repository
                                .collectList()
                                .flatMap(existingVisits -> {
                                    if(existingVisits.isEmpty()) {// If there are no existing visits
                                        return repo.insert(visit); // Insert the visit
                                    } else {
                                        //return exception if a visits already exists at the specific day and time for a specific practitioner
                                        return Mono.error(new DuplicateTimeException("A visit with the same time and practitioner already exists."));
                                    }
                                })
                )
                .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit));
    }

    @Override
    public Mono<Void> deleteVisit(String visitId) {
        return repo.existsByVisitId(visitId)
                .flatMap(visitExists -> {
                    if (!visitExists) {
                        return Mono.error(new NotFoundException("No visit was found with visitId: " + visitId));
                    } else {
                        return repo.deleteByVisitId(visitId);
                    }
                });
    }

    @Override
    public Mono<Void> deleteAllCancelledVisits() {
        return repo.findAllByStatus("CANCELLED")
                .collectList()
                .flatMap(canceledVisits ->{
                    if(canceledVisits.isEmpty()){
                        return Mono.empty();
                    } else{
                        return repo.deleteAll(canceledVisits);
                    }
                });
    }


//    @Override
//    public Mono<VetDTO> testingGetVetDTO(String vetId) {
//        return vetsClient.getVetByVetId(vetId);
//    }
//
//    @Override
//    public Mono<PetResponseDTO> testingGetPetDTO(int petId) {
//        return petsClient.getPetById(petId);
//    }
//

    @Override
    public Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono) {
        return repo.findByVisitId(visitId)
                .flatMap(visitEntity -> visitRequestDTOMono
                        .flatMap(visitRequestDTO -> validatePetId(visitRequestDTO.getPetId())
                                .then(validateVetId(visitRequestDTO.getPractitionerId()))
                                .then(Mono.just(visitRequestDTO)))
                        .map(visitRequestDTO -> entityDtoUtil.toVisitEntity(visitRequestDTO))
                        .doOnNext(visitEntityToUpdate -> {
                            visitEntityToUpdate.setVisitId(visitEntity.getVisitId());
                            visitEntityToUpdate.setId(visitEntity.getId());
                        }))
                .flatMap(repo::save)
                .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit));
    }

    @Override
    public Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status) {
        Status newStatus;
        Status newStatus1;
        switch (status){

            case "UPCOMING":
                newStatus1 = Status.UPCOMING;
                break;

            case "CONFIRMED":
                newStatus1 = Status.CONFIRMED;
                break;

            case "COMPLETED":
                newStatus1 = Status.COMPLETED;
                break;

            default:
                newStatus1 = Status.CANCELLED;
                break;
        }
        newStatus = newStatus1;
        return repo.findByVisitId(visitId)
                .doOnNext(v -> v.setStatus(newStatus))
                .flatMap(repo::save)
                .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit));
    }


    private Mono<PetResponseDTO> validatePetId(String petId) {
        return petsClient.getPetById(petId)
                .switchIfEmpty(Mono.error(new NotFoundException("No pet was found with petId: " + petId)));
    }

    private Mono<VetDTO> validateVetId(String vetId) {
        return vetsClient.getVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet was found with vetId: " + vetId)));
    }

    private Mono<VisitRequestDTO> validateVisitRequest(VisitRequestDTO dto) {
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            return Mono.error(new BadRequestException("Please enter a description for this visit"));
        } else if (dto.getVisitDate() == null) {
            return Mono.error(new BadRequestException("Please choose a date for your appointment"));
        }
        if(dto.getVisitDate().isBefore(LocalDateTime.now())) {
            return Mono.error(new BadRequestException("Appointment cannot be scheduled in the past"));
        } else if (dto.getPetId() == null || dto.getPetId().isBlank()) {
            return Mono.error(new BadRequestException("PetId cannot be null or blank"));
        } else if ( dto.getPractitionerId() == null || dto.getPractitionerId().isBlank()) {
            return Mono.error(new BadRequestException("VetId cannot be null or blank"));
        }
        else if (dto.getStatus() != Status.UPCOMING){
            return Mono.error(new BadRequestException("Status is being set wrong!"));
        }
        else {
            return Mono.just(dto);
        }
    }

    private Mail generateVisitRequestEmail(UserDetails user, String petName, LocalDateTime visitDate) {
        return Mail.builder()
                .message(
                    format("""
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <title>Email Verification</title>
                                <style>
                                    body {
                                        font-family: Arial, sans-serif;
                                        background-color: #f4f4f4;
                                        margin: 0;
                                        padding: 0;
                                    }
                                    .container {
                                        max-width: 600px;
                                        margin: 0 auto;
                                        padding: 20px;
                                        background-color: #fff;
                                        border-radius: 5px;
                                        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                                    }
                                    h1 {
                                        color: #333;
                                    }
                                    p {
                                        color: #555;
                                    }
                                    a {
                                        color: #007BFF;
                                    }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <h1>Dear %s,</h1>
                                    <h3>We have received a request to schedule a visit for one of your pet: %s at the following date and time: %s.</h3>
                                    \s
                                    <p>If you do not wish to create an account, please disregard this email.</p>
                                    \s
                                    <p>Thank you for choosing Pet Clinic.</p>
                                </div>
                            </body>
                            </html>
                            """, user.getUsername(), petName, visitDate.toString()))
                .subject("PetClinic Visit request")
                .to(user.getEmail())
                .build();
    }
}