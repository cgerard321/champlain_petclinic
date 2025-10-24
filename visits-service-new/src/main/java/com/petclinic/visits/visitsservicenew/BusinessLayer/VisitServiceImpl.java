package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions.PrescriptionService;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
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
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FilesServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
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

    private final PrescriptionService prescriptionService;
    private final FilesServiceClient filesServiceClient;

    /**
     * Get all visits from the repo
     *
     * @return all Visits as Flux
     */
    @Override
    public Flux<VisitResponseDTO> getAllVisits(String description) {
        Flux<Visit> visits;

        if (description != null && !description.isBlank()) {
            visits = repo.findVisitsByDescriptionContainingIgnoreCase(description);
        } else {
            visits = repo.findAll();
        }
        return visits.flatMap(entityDtoUtil::toVisitResponseDTO);
        //return repo.findAll().flatMap(entityDtoUtil::toVisitResponseDTO);
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
                break;

            case ("CONFIRMED"):
                status = Status.CONFIRMED;
                break;

            case ("CANCELLED"):
                status = Status.CANCELLED;
                break;

            case ("ARCHIVED"):
                status = Status.ARCHIVED;
                break;

            default:
                status = Status.COMPLETED;
                break;
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
     * @param visitId             The visit ID we search with
     * @return Return a single visit with the corresponding ID
     */
//    @Override
//    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId) {
//        return repo.findByVisitId(visitId)
//                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No visit was found with visitId: " + visitId))))
//                .doOnNext(visit -> log.debug("The visit entity is: " + visit.toString()))
//                .flatMap(entityDtoUtil::toVisitResponseDTO);
//    }


    @Override
    public Mono<VisitResponseDTO> getVisitByVisitId(String visitId, boolean includePrescription) {
        return repo.findByVisitId(visitId)
                .switchIfEmpty(Mono.error(new NotFoundException("Visit not found: " + visitId)))
                .flatMap(visit -> entityDtoUtil.toVisitResponseDTO(visit)
                        .flatMap(dto -> {
                            if (!includePrescription || visit.getPrescriptionFileId() == null) {
                                return Mono.just(dto);
                            }

                            return filesServiceClient.getFile(visit.getPrescriptionFileId())
                                    .map(file -> {
                                        dto.setPrescription(file);
                                        return dto;
                                    })
                                    .onErrorResume(ex -> Mono.just(dto))
                                    .defaultIfEmpty(dto);
                        })
                );
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
                                            user -> mailService
                                                    .sendMail(
                                                            generateVisitRequestEmail(
                                                                    user,
                                                                    visitRequestDTO
                                                                            .getPetId(),
                                                                    visitRequestDTO
                                                                            .getVisitDate()
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
                        repo.findByVisitDateAndPractitionerId(visit.getVisitDate(), visit.getPractitionerId()) // FindVisits method in repository
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
     *
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

    @Override
    public Mono<VisitResponseDTO> archiveCompletedVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTO) {
        return repo.findByVisitId(visitId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No completed visit was found with visitId: " + visitId))))
                .filter(visit -> visit.getStatus() == Status.COMPLETED)
                .switchIfEmpty(Mono.error(new BadRequestException("Cannot archive a visit that is not completed.")))
                .doOnNext(visit -> {
                    visit.setStatus(Status.ARCHIVED);
                })
                .flatMap(repo::save)

                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Flux<VisitResponseDTO> getAllArchivedVisits() {
        return repo.findAllByStatus("ARCHIVED")
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("No archived visits were found"))))
                .flatMap(entityDtoUtil::toVisitResponseDTO);
    }

    @Override
    public Mono<InputStreamResource> exportVisitsToCSV() {
            return repo.findAll()
                    .collectList()
                    .map(visits -> {
                        // Create a ByteArrayOutputStream to store CSV data
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        PrintWriter writer = new PrintWriter(out);

                        // Write CSV header
                        writer.println("VisitId,Description,VisitDate,PetId,PractitionerId,Status");

                        // Write rows for each visit
                        visits.forEach(visit -> {
                            writer.println(String.join(",",
                                    visit.getVisitId(),
                                    "\"" + visit.getDescription().replace("\"", "\"\"") + "\"", // Escape quotes for CSV
                                    visit.getVisitDate().toString(),
                                    visit.getPetId(),
                                    visit.getPractitionerId(),
                                    visit.getStatus().toString()
                            ));
                        });

                        writer.flush(); // Ensure all data is written
                        writer.close(); // Close writer to release resources

                        // Convert ByteArrayOutputStream to InputStreamResource
                        return new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
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

    /**
     * Modify a Visit by its ID. Does verification for existing Vet and pets before replacing
     *
     * @param visitId             The visit ID to modify
     * @param visitRequestDTOMono Visit Request DTO
     * @return Updated visit
     */
    @Override
    public Mono<VisitResponseDTO> updateVisit(String visitId, Mono<VisitRequestDTO> visitRequestDTOMono) {
        return repo.findByVisitId(visitId)
                .switchIfEmpty(Mono.error(new NotFoundException("No visit was found with visitId: " + visitId)))
                .flatMap(existingVisit ->
                        visitRequestDTOMono
                                .flatMap(dto -> validatePetId(dto.getPetId())
                                        .then(validateVetId(dto.getPractitionerId()))
                                        .thenReturn(dto))
                                .map(entityDtoUtil::toVisitEntity)
                                .doOnNext(updatedVisit -> {
                                    updatedVisit.setId(existingVisit.getId());
                                    updatedVisit.setVisitId(existingVisit.getVisitId());

                                    updatedVisit.setPrescriptionFileId(existingVisit.getPrescriptionFileId());

                                    if (updatedVisit.getStatus() == null) {
                                        updatedVisit.setStatus(existingVisit.getStatus());
                                    }

                                    if (updatedVisit.getVisitDate() == null) {
                                        updatedVisit.setVisitDate(existingVisit.getVisitDate());
                                    }
                                })
                )
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

            case "ARCHIVED":
                newStatus1 = Status.ARCHIVED;
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
        } else if (dto.getVisitDate() == null) {
            return Mono.error(new BadRequestException("Please choose a date for your appointment"));
        }
        if (dto.getVisitDate().isBefore(LocalDateTime.now())) {
            return Mono.error(new BadRequestException("Appointment cannot be scheduled in the past"));
        } else if (dto.getPetId() == null || dto.getPetId().isBlank()) {
            return Mono.error(new BadRequestException("PetId cannot be null or blank"));
        } else if (dto.getPractitionerId() == null || dto.getPractitionerId().isBlank()) {
            return Mono.error(new BadRequestException("VetId cannot be null or blank"));
        } else if (dto.getStatus() == null) {
            return Mono.error(new BadRequestException("Status cannot be null"));
        } else {
            return Mono.just(dto);
        }
    }

    /**
     * Generates an email through an already defined template. Uses the mailer Service through
     *
     * @param user      UserDetails from DomainClientLayer/Auth/UserDetails
     * @param petName   The Pet name
     * @param visitDate The Date of the visit
     * @return The email built from the message
     */
    private Mail generateVisitRequestEmail(UserDetails user, String petName, LocalDateTime visitDate) {


        return new Mail(
                user.getEmail(), "PetClinic Visit request", "Default", "PetClinic Visit request",
                "Dear " + user.getUsername() + ",\n" +
                        "We have received a request to schedule a visit for your pet with id: "+ petName +" on the following date and time: "+ visitDate.toString() +"." +"\n"+
                        "If you do not wish to create an account, please disregard this email.",
                "Thank you for choosing Pet Clinic.", user.getUsername(), "ChamplainPetClinic@gmail.com");
        //Old way of doing it with old Mail entity in case of need to revert
//        return Mail.builder()
//                .message(
//                        format("""
//                                <!DOCTYPE html>
//                                <html lang="en">
//                                <head>
//                                    <meta charset="UTF-8">
//                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
//                                    <title>Email Verification</title>
//                                    <style>
//                                        body {
//                                            font-family: Arial, sans-serif;
//                                            background-color: #f4f4f4;
//                                            margin: 0;
//                                            padding: 0;
//                                        }
//                                        .container {
//                                            max-width: 600px;
//                                            margin: 0 auto;
//                                            padding: 20px;
//                                            background-color: #fff;
//                                            border-radius: 5px;
//                                            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
//                                        }
//                                        h1 {
//                                            color: #333;
//                                        }
//                                        p {
//                                            color: #555;
//                                        }
//                                        a {
//                                            color: #007BFF;
//                                        }
//                                    </style>
//                                </head>
//                                <body>
//                                    <div class="container">
//                                        <h1>Dear %s,</h1>
//                                        <h3>We have received a request to schedule a visit for your pet with id: %s on the following date and time: %s.</h3>
//                                        \s
//                                        <p>If you do not wish to create an account, please disregard this email.</p>
//                                        \s
//                                        <p>Thank you for choosing Pet Clinic.</p>
//                                    </div>
//                                </body>
//                                </html>
//                                """, user.getUsername(), petName, visitDate.toString()))
//                .subject("PetClinic Visit request")
//                .to(user.getEmail())
//                .build();
    }


    @Override
    public Mono<VisitResponseDTO> patchVisitStatusInVisit(String visitId, String status) {
        // Find the visit by the ID
        log.info("Attempting to update visit {} to status {}", visitId, status);
        return repo.findByVisitId(visitId)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new NotFoundException("Cannot find visit with id: " + visitId))
                ))
                // Update the status of the found Visit entity
                .doOnNext(visit -> {
                    log.info("Current status: {}, New status: {}", visit.getStatus(), status);
                    visit.setStatus(Status.valueOf(status));
                }) // Update status reactively
                // Save the updated visit
                .flatMap(repo::save)
                .doOnNext(saved -> log.info("Saved visit with new status: {}", saved.getStatus()))
                // Convert to VisitResponseDTO
                .flatMap(entityDtoUtil::toVisitResponseDTO)
                .doOnNext(dto -> log.info("Converted to DTO with status: {}", dto.getStatus()));
    }


}