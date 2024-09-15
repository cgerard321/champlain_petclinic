package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.AuthServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.UserDetails;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.Mail;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.MailService;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.Exceptions.BadRequestException;
import com.petclinic.visits.visitsservicenew.Exceptions.DuplicateTimeException;
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

import static java.lang.String.format;


@Service
@RequiredArgsConstructor
@Slf4j
public class VisitServiceImpl implements VisitService {
    /**
     * Access the hardcoded values created in DataSetupService. We have access to VisitRepo commands. LOOK AT DATALAYER
     */
    private final VisitRepo repo;
    /**
     * We can use this to access externally the vets Service and get information from it through http request
     */
    private final VetsClient vetsClient;
    /**
     * We can use this to access externally the pets Service and get information from it through http request
     */
    private final PetsClient petsClient;
    /**
     * Access to the util class to change one datatype to another
     */
    private final EntityDtoUtil entityDtoUtil;
    private final AuthServiceClient authServiceClient;
    private final MailService mailService;

    /**
     * Get all visits from the repo
     *
     * @return all Visits as Flux
     */
    @Override
    public Flux<VisitResponseDTO> getAllVisits() {
        return repo.findAll().flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    /**
     * Gets all the visits that a single pet ever had or will have
     *
     * @param petId
     * @return
     */
    @Override
    public Flux<VisitResponseDTO> getVisitsForPet(String petId) {
        return validatePetId(petId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No pet was found with petId: " + petId))))
                .thenMany(repo.findByPetId(petId)
                        .flatMap(entityDtoUtil::toVisitResponseDTO));
    }

    /**
     * Returns all the visit with the corresponding status
     *
     * @param statusString Accept a Status Enumerator ( See DataLayer/Status )
     * @return Flux VisitResponseDTO
     */
    @Override
    public Flux<VisitResponseDTO> getVisitsForStatus(String statusString) {
        Status status;

        switch (statusString) { // Transform string back into enumerator
            case ("UPCOMING"):
                status = Status.UPCOMING;

            case ("CONFIRMED"):
                status = Status.CONFIRMED;

            case ("CANCELLED"):
                status = Status.CANCELLED;

            default:
                status = Status.COMPLETED;
        }
        return repo.findAllByStatus(statusString)
                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    /**
     * Gets all the visits my their corresponding Vets
     *
     * @param vetId The id of the vet we want to get all their visits
     * @return Returns all visit who have the same vet
     */
    @Override
    public Flux<VisitResponseDTO> getVisitsForPractitioner(String vetId) {
        return validateVetId(vetId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No vet was found with vetId: " + vetId))))
                .thenMany(repo.findVisitsByPractitionerId(vetId))
                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    /**
     * We get a single visit by its VisitId
     *
     * @param visitId The visit ID we search with
     * @return Return a single visit with the corresponding ID
     */
    @Override
    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
        return repo.findByVisitId(visitId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No visit was found with visitId: " + visitId))))
                .doOnNext(visit -> log.debug("The visit entity is: " + visit.toString()))
                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    /**
     * Safe add visit. Need authentication to work and uses JwtToken
     *
     * @param visitRequestDTOMono The visit request model DTO
     * @return Added visit
     */
    @Override
    public Mono<VisitResponseDTO> addVisit(Mono<VisitRequestDTO> visitRequestDTOMono) {
        return visitRequestDTOMono
                .flatMap(visitRequestDTO -> validateVisitRequest(visitRequestDTO)
                        .then(validatePetId(visitRequestDTO.getPetId()))//Validate the pet
                        .then(validateVetId(visitRequestDTO.getPractitionerId()))// Validate the Vet
                        .then(Mono.just(visitRequestDTO)) // Used to say we are continuing work with the RequestDTO
                        .doOnNext(s -> {

                            authServiceClient.getUserById(
                                    visitRequestDTO.getJwtToken(),
                                    visitRequestDTO.getOwnerId())
                                        .subscribe(
                                                //WILL HAVE TO BET MODIFIED IN ORDER TO USE THE NEW MAIL SERVICE
                                                user->mailService
                                                        .sendMail(
                                                                generateVisitRequestEmail(
                                                                        user,
                                                                        visitRequestDTO
                                                                                .getPetId(),
                                                                        visitRequestDTO
                                                                                .getVisitStartDate()
                                                                )
                                                        )
                                        );

                            //                    Mono<UserDetails> user = getUserById(auth, ownerId);
                            //                    try{
                            //                        simpleJavaMailClient.sendMail(emailBuilder("test@email.com"));
                            //                    }catch(Exception e){System.out.println("Email failed to send: "+e.getMessage());}
                        })
                )
//                .doOnNext(v -> System.out.println("Request Date: " + v.getVisitDate())) // Debugging

                //Converts Request DTO ( JSON ) into an entity
                .map(entityDtoUtil::toVisitEntity)
                //Creating a new ID for the visit
                .doOnNext(x -> x.setVisitId(entityDtoUtil.generateVisitIdString()))
//                .doOnNext(v -> System.out.println("Entity Date: " + v.getVisitDate())) // Debugging
                //FLATENS THE MONO
                .flatMap(visit ->
                        repo.findByVisitStartDateAndPractitionerId(visit.getVisitStartDate(), visit.getPractitionerId()) // FindVisits method in repository
                                .collectList()
                                .flatMap(existingVisits -> {
                                    if (existingVisits.isEmpty()) {// If there are no existing visits
                                        return repo.insert(visit); // Insert the visit
                                    } else {
                                        //return exception if a visits already exists at the specific day and time for a specific practitioner
                                        return Mono.error(new DuplicateTimeException("A visit with the same time and practitioner already exists."));
                                    }
                                })
                )
                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    /**
     * Delete a visit with the visit ID.
     * InvalidException implemented
     *
     * @param visitId The string VisitID
     * @return Deleted Visit
     */
    @Override
    public Mono<Void> deleteVisit(String visitId) {

        return repo.findByVisitId(visitId)
                .switchIfEmpty(Mono.error(new NotFoundException("No visit was found with visitId: " + visitId)))
                .flatMap(foundVisit -> repo.deleteByVisitId(foundVisit.getVisitId()));
    }

    /**
     * Delete all visits who are Cancelled in the DataLayer.Status.
     * @return Returns all the deleted Visits
     */
    @Override
    public Mono<Void> deleteAllCancelledVisits() {
        return repo.findAllByStatus("CANCELLED")
                .collectList()
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No cancelled visits were found")))
                )
                .flatMap(repo::deleteAll);
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

    /**
     * Modify a Visit by its ID. Does verification for existing Vet and pets before replacing
     * @param visitId The visit ID to modify
     * @param visitRequestDTOMono Visit Request DTO
     * @return Updated visit
     */
    @Override
    public Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono) {
        //Find the visit by the ID
        return repo.findByVisitId(visitId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No visit was found with visitId: " + visitId)))
                )
                //VisitEntity becomes a reference to the found Visit
                //Removes nested Structure  ( Mono<Mono<(...)>> )
                .flatMap(visitEntity -> visitRequestDTOMono
                        //Validate Pet and Vet
                        .flatMap(visitRequestDTO -> validatePetId(visitRequestDTO.getPetId())
                                .then(validateVetId(visitRequestDTO.getPractitionerId()))
                                .then(Mono.just(visitRequestDTO)))
                        .map(entityDtoUtil::toVisitEntity)
                        .doOnNext(visitEntityToUpdate -> {
                            visitEntityToUpdate.setVisitId(visitEntity.getVisitId());
                            visitEntityToUpdate.setId(visitEntity.getId());
                        }))
                //Save
                .flatMap(repo::save)
                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    /**
     * Change the status of any saved Visits by their ID
     *
     * @param visitId The ID of the visit we want to change the status
     * @param status  The new Status ( Enum : DataLayer/Status )
     * @return Updated Visit
     */
    @Override
    public Mono<VisitResponseDTO> updateStatusForVisitByVisitId(String visitId, String status) {
        Status newStatus;
        Status newStatus1;
        switch (status) {

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
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No visit was found with visitId: " + visitId)))
                )
                .doOnNext(v -> v.setStatus(newStatus))
                .flatMap(repo::save)
                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }


    /**
     * Validate if a pet exist. If it doesn't exist, returns 404 NotFound
     *
     * @param petId Pet Id to verify
     * @return That specific Pet
     */
    private Mono<PetResponseDTO> validatePetId(String petId) {
        return petsClient.getPetById(petId)
                .switchIfEmpty(Mono.error(new NotFoundException("No pet was found with petId: " + petId)));
    }

    /**
     * Validate if a vet exist. If it doesn't exist, returns 404 NotFound
     *
     * @param vetId Vet Id to verify
     * @return That specific Pet
     */
    private Mono<VetDTO> validateVetId(String vetId) {
        return vetsClient.getVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet was found with vetId: " + vetId)));
    }

    /**
     * Validates if the content of a message is usable to create a new Visit or to modify one
     *
     * @param dto The VisitRequest DTO that will be validated
     * @return The DTO as Mono or BadRequestException if it doesn't respect the needed format
     */
    private Mono<VisitRequestDTO> validateVisitRequest(VisitRequestDTO dto) {
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            return Mono.error(new BadRequestException("Please enter a description for this visit"));
        } else if (dto.getVisitStartDate() == null) {
            return Mono.error(new BadRequestException("Please choose a date for your appointment"));
        }
        if(dto.getVisitStartDate().isBefore(LocalDateTime.now())) {
            return Mono.error(new BadRequestException("Appointment cannot be scheduled in the past"));
        } else if (dto.getPetId() == null || dto.getPetId().isBlank()) {
            return Mono.error(new BadRequestException("PetId cannot be null or blank"));
        } else if (dto.getPractitionerId() == null || dto.getPractitionerId().isBlank()) {
            return Mono.error(new BadRequestException("VetId cannot be null or blank"));
        } else if (dto.getStatus() != Status.UPCOMING) {
            return Mono.error(new BadRequestException("Status is being set wrong!"));
        } else {
            return Mono.just(dto);
        }
    }

    /**
     * Generates an email through an already defined template. Uses the mailer Service through
     * @param user UserDetails from DomainClientLayer/Auth/UserDetails
     * @param petName The Pet name
     * @param visitStartDate The Date of the visit
     * @return The email built from the message
     */
    private Mail generateVisitRequestEmail(UserDetails user, String petName, LocalDateTime visitStartDate) {
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
                                    <h3>We have received a request to schedule a visit for your pet with id: %s on the following date and time: %s.</h3>
                                    \s
                                    <p>If you do not wish to create an account, please disregard this email.</p>
                                    \s
                                    <p>Thank you for choosing Pet Clinic.</p>
                                </div>
                            </body>
                            </html>
                            """, user.getUsername(), petName, visitStartDate.toString()))
                .subject("PetClinic Visit request")
                .to(user.getEmail())
                .build();
    }
}