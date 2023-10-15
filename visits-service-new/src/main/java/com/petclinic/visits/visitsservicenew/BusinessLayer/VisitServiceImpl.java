package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mail.Mail;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mail.MailClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mail.MailService;
import com.petclinic.visits.visitsservicenew.Exceptions.BadRequestException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {
    private final VisitRepo repo;
    private final VetsClient vetsClient;
    private final MailService mailService;
    private final PetsClient petsClient;
    private final MailClient mailClient;
    @Override
    public Flux<VisitResponseDTO> getAllVisits() {
        return repo.findAll().map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForPet(String petId) {
        return validatePetId(petId)
                .thenMany(repo.findByPetId(petId)
                        .map(EntityDtoUtil::toVisitResponseDTO));
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
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Flux<VisitResponseDTO> getVisitsForPractitioner(String vetId) {
        return validateVetId(vetId)
                .thenMany(repo.findVisitsByPractitionerId(vetId))
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
        return repo.findByVisitId(visitId)
                .map(EntityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono) {
        return visitRequestDTOMono
                .flatMap(visitRequestDTO -> validateVisitRequest(visitRequestDTO)
                        .then(validatePetId(visitRequestDTO.getPetId()))
                        .then(validateVetId(visitRequestDTO.getPractitionerId()))
                        .then(Mono.just(visitRequestDTO))
                )
                .doOnNext(v -> System.out.println("Request Date: " + v.getVisitDate())) // Debugging
                .map(EntityDtoUtil::toVisitEntity)
                .doOnNext(x -> x.setVisitId(EntityDtoUtil.generateVisitIdString()))
                .doOnNext(v -> System.out.println("Entity Date: " + v.getVisitDate())) // Debugging
                .flatMap((repo::insert))
                .doOnNext(v -> log.info(mailService.sendMail(generateMail())))
                .map(EntityDtoUtil::toVisitResponseDTO);
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
                        .map(EntityDtoUtil::toVisitEntity)
                        .doOnNext(visitEntityToUpdate -> {
                            visitEntityToUpdate.setVisitId(visitEntity.getVisitId());
                            visitEntityToUpdate.setId(visitEntity.getId());
                        }))
                .flatMap(repo::save)
                .map(EntityDtoUtil::toVisitResponseDTO);
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
                .map(EntityDtoUtil::toVisitResponseDTO);
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


    private Mail generateMail() {
        return Mail.builder()
                .message("""
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
                            h1 {color: #333;}
                            p {color: #555;}
                            a {color: #007BFF;}
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>Thank you for Testing Up with us - Verify your email address</h1>
                            <h3>We have received a request to create an account for Pet Clinic from this email.</h3>
                            \s
                            <ol>
                                
                            </ol>
                            \s
                            <p>If you do not wish to create an account, please disregard this email.</p>
                            \s
                            <p>Thank you for choosing Pet Clinic.</p>
                        </div>
                    </body>
                    </html>
                    """)
                .subject("PetClinic e-mail test")
//                .to(user.getEmail())
                .to("william.chalifoux@gmail.com")
                .build();

//                .message(format("Your verification link: %s://%s%s/verification/%s",
//                gatewayProtocol, niceSub, gatewayOrigin, base64Token))
    }
}