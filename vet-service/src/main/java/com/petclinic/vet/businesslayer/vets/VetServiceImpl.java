package com.petclinic.vet.businesslayer.vets;
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

import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.dataaccesslayer.vets.Specialty;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.domainclientlayer.FilesServiceClient;
import com.petclinic.vet.presentationlayer.files.FileRequestDTO;
import com.petclinic.vet.presentationlayer.files.FileResponseDTO;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.presentationlayer.vets.VetResponseDTO;
import com.petclinic.vet.presentationlayer.vets.SpecialtyDTO;
import com.petclinic.vet.utils.EntityDtoUtil;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VetServiceImpl implements VetService {

    private final VetRepository vetRepository;
    private final BadgeRepository badgeRepository;
    private final PhotoRepository photoRepository;
    private final RatingRepository ratingRepository;
    private final EducationRepository educationRepository;
    private final FilesServiceClient filesServiceClient;

    @Override
    public Flux<VetResponseDTO> getAll() {
        return vetRepository.findAll()
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }

    @Override
    public Mono<VetResponseDTO> addVet(Mono<VetRequestDTO> vetDTOMono) {
        return vetDTOMono
                .flatMap(this::validateVetRequestDTO)
                .flatMap(vetRequest -> {
                    Vet vetEntity = EntityDtoUtil.vetRequestDtoToEntity(vetRequest);
                    
                    Mono<FileResponseDTO> photoMono;
                    if (vetRequest.getPhoto() != null && vetRequest.getPhoto().getFileData() != null) {
                        photoMono = filesServiceClient.addFile(vetRequest.getPhoto());
                    } else if (vetRequest.isPhotoDefault()) {
                        FileRequestDTO defaultPhoto = FileRequestDTO.builder()
                                .fileName("vet_default.jpg")
                                .fileType("image/jpeg")
                                .build();
                        defaultPhoto.setFileDataFromBytes(loadImage("images/vet_default.jpg"));
                        photoMono = filesServiceClient.addFile(defaultPhoto);
                    } else {
                        photoMono = Mono.empty();
                    }
                    
                    return photoMono
                            .defaultIfEmpty(null)
                            .flatMap(photo -> {
                                if (photo != null) {
                                    vetEntity.setImageId(photo.getFileId());
                                }
                                return assignBadgeAndSaveBadgeAndVet(vetEntity)
                                        .map(savedVet -> {
                                            VetResponseDTO dto = EntityDtoUtil.vetEntityToResponseDTO(savedVet);
                                            dto.setPhoto(photo);
                                            return dto;
                                        });
                            });
                });
    }

    @Override
    public Mono<VetResponseDTO> updateVet(String vetId, Mono<VetRequestDTO> vetDTOMono) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this vetId was found: " + vetId)))
                .flatMap(p -> vetDTOMono
                        .flatMap(requestDTO->{
                            if(requestDTO.getFirstName().length()>30||requestDTO.getFirstName().length()<2)
                                return Mono.error(new InvalidInputException("firstName length should be between 2 and 20 characters: "+requestDTO.getFirstName()));
                            if(requestDTO.getLastName().length()>30||requestDTO.getLastName().length()<2)
                                return Mono.error(new InvalidInputException("lastName length should be between 2 and 20 characters: "+requestDTO.getLastName()));
                            if(requestDTO.getPhoneNumber().length()!=20)
                                return Mono.error(new InvalidInputException("phoneNumber length not equal to 20 characters: "+requestDTO.getPhoneNumber()));
                            if(requestDTO.getEmail().length()<6||requestDTO.getEmail().length()>320)
                                return Mono.error(new InvalidInputException("email length should be between 6 and 320 characters: "+requestDTO.getEmail()));
                            if(requestDTO.getResume().length()<10)
                                return Mono.error(new InvalidInputException("resume length should be more than 10 characters: "+requestDTO.getResume()));
                            if(requestDTO.getSpecialties()==null)
                                return Mono.error(new InvalidInputException("invalid specialties"));
                            return Mono.just(requestDTO);
                        })
                        .map(EntityDtoUtil::vetRequestDtoToEntity)
                        .doOnNext(e -> e.setVetId(p.getVetId()))
                        .doOnNext(e -> e.setId(p.getId()))
                )
                .flatMap(vetRepository::save)
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }

    @Override
    public Mono<VetResponseDTO> getVetByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this vetId was found: " + vetId)))
                .doOnNext(i -> log.debug("The vet entity is: " + i.toString()))
                .map(EntityDtoUtil::vetEntityToResponseDTO)
                .log();
    }


    @Override
    public Flux<VetResponseDTO> getVetByIsActive(boolean isActive) {
        return vetRepository.findVetsByActive(isActive)
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }

    @Override
    public Mono<VetResponseDTO> getVetByVetBillId(String vetBillId) {
        return  vetRepository.findVetByVetBillId(vetBillId)
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }

    @Override
    public Mono<VetResponseDTO> addSpecialtiesByVetId(String vetId, Mono<SpecialtyDTO> specialtyDTO) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Vet not found with id: " + vetId)))
                .flatMap(vet -> specialtyDTO
                        .map(this::specialtyDtoToEntity) // Convert DTO to entity
                        .doOnNext(specialty -> vet.getSpecialties().add(specialty)) // Add specialty to the vet's specialties
                        .then(vetRepository.save(vet)) // Save updated vet
                )
                .map(EntityDtoUtil::vetEntityToResponseDTO); // Convert the vet entity back to DTO
    }
    private Specialty specialtyDtoToEntity(SpecialtyDTO specialtyDTO) {
        return Specialty.builder()
                .specialtyId(EntityDtoUtil.generateSpecialtyId())  // Using the utility method for consistency
                .name(specialtyDTO.getName())
                .build();
    }

    @Override
    public Mono<Void> deleteSpecialtyBySpecialtyId(String vetId, String specialtyId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet found with vetId: " + vetId)))
                .flatMap(vet -> {
                    Set<Specialty> specialties = vet.getSpecialties().stream()
                            .filter(specialty -> !specialty.getSpecialtyId().equals(specialtyId))
                            .collect(Collectors.toSet());
                    
                    if (specialties.size() == vet.getSpecialties().size()) {
                        return Mono.error(new NotFoundException("No specialty found with specialtyId: " + specialtyId));
                    }
                    
                    vet.setSpecialties(specialties);
                    return vetRepository.save(vet);
                })
                .then();
    }


    @Transactional
    @Override
    public Mono<Void> deleteVetByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this vetId was found: " + vetId)))
                .flatMap(vet -> {
                    log.info("Deleting associated data for vetId: {}", vetId);

                    Mono<Void> deletePhoto = Mono.justOrEmpty(vet.getImageId())
                            .flatMap(filesServiceClient::deleteFileById)
                            .onErrorResume(e -> {
                                log.warn("Could not delete file for vet {}: {}", vetId, e.getMessage());
                                return Mono.empty();
                            });

                    Mono<Void> deleteOldPhotos = photoRepository.findByVetId(vetId)
                            .flatMap(photoRepository::delete)
                            .then()
                            .onErrorResume(e -> {
                                log.warn("Could not delete old photo for vet {}: {}", vetId, e.getMessage());
                                return Mono.empty();
                            });

                    Mono<String> deleteRatings = ratingRepository.deleteByVetId(vetId);

                    Mono<String> deleteEducations = educationRepository.deleteByVetId(vetId);

                    return Mono.when(deletePhoto, deleteOldPhotos, deleteRatings, deleteEducations)
                            .then(vetRepository.delete(vet))
                            .doOnSuccess(unused -> log.info("Successfully deleted vetId: {}", vetId))
                            .doOnError(error -> log.error("Error deleting vetId: {}", vetId, error));
                });
    }


    private byte[] loadImage(String imagePath) {
        try {
            ClassPathResource cpr = new ClassPathResource(imagePath);
            return StreamUtils.copyToByteArray(cpr.getInputStream());
        } catch (IOException io) {
            throw new InvalidInputException("Picture does not exist: " + io.getMessage());
        }
    }

    private Mono<VetRequestDTO> validateVetRequestDTO(VetRequestDTO requestDTO) {
        if(requestDTO.getFirstName().length()>30||requestDTO.getFirstName().length()<2)
            return Mono.error(new InvalidInputException("firstName length should be between 2 and 20 characters: "+requestDTO.getFirstName()));
        if(requestDTO.getLastName().length()>30||requestDTO.getLastName().length()<2)
            return Mono.error(new InvalidInputException("lastName length should be between 2 and 20 characters: "+requestDTO.getLastName()));
        if(requestDTO.getPhoneNumber().length()!=20)
            return Mono.error(new InvalidInputException("phoneNumber length not equal to 20 characters: "+requestDTO.getPhoneNumber()));
        if(requestDTO.getEmail().length()<6||requestDTO.getEmail().length()>320)
            return Mono.error(new InvalidInputException("email length should be between 6 and 320 characters: "+requestDTO.getEmail()));
        if(requestDTO.getResume().length()<10)
            return Mono.error(new InvalidInputException("resume length should be more than 10 characters: "+requestDTO.getResume()));
        if(requestDTO.getSpecialties()==null)
            return Mono.error(new InvalidInputException("invalid specialties"));
        return Mono.just(requestDTO);
    }

    private Mono<Vet> assignBadgeAndSaveBadgeAndVet(Vet vetEntity) {
        Badge assignedBadge = Badge.builder()
                .vetId(vetEntity.getVetId())
                .badgeTitle(BadgeTitle.VALUED)
                .badgeDate(String.valueOf(LocalDate.now().getYear()))
                .data(loadImage("images/empty_food_bowl.png"))
                .build();

        return badgeRepository.save(assignedBadge)
                .zipWith(vetRepository.save(vetEntity))
                .map(tuple -> tuple.getT2());
    }

    @Override
    public Mono<VetResponseDTO> getVetByVetId(String vetId, boolean includePhoto) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this vetId was found: " + vetId)))
                .doOnNext(i -> log.debug("The vet entity is: " + i.toString()))
                .flatMap(vet -> {
                    VetResponseDTO dto = EntityDtoUtil.vetEntityToResponseDTO(vet);
                    
                    if (includePhoto && vet.getImageId() != null) {
                        return filesServiceClient.getFileById(vet.getImageId())
                                .map(fileDetails -> {
                                    dto.setPhoto(fileDetails);
                                    return dto;
                                })
                                .onErrorReturn(dto);
                    } else {
                        dto.setPhoto(null);
                        return Mono.just(dto);
                    }
                })
                .log();
    }

    @Override
    public Mono<VetResponseDTO> updateVetPhoto(String vetId, FileRequestDTO photo) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("Vet not found with id: " + vetId)))
                .flatMap(existingVet -> {
                    Mono<FileResponseDTO> fileOperation;

                    if (existingVet.getImageId() != null && !existingVet.getImageId().isEmpty()) {
                        fileOperation = filesServiceClient.updateFile(existingVet.getImageId(), photo)
                                .onErrorResume(e -> {
                                    log.warn("Photo file {} not found or error updating, creating new file instead: {}", 
                                            existingVet.getImageId(), e.getMessage());
                                    return filesServiceClient.addFile(photo);
                                });
                    } else {
                        fileOperation = filesServiceClient.addFile(photo);
                    }

                    return fileOperation
                            .flatMap(fileResp -> {
                                existingVet.setImageId(fileResp.getFileId());
                                return vetRepository.save(existingVet)
                                        .map(savedVet -> {
                                            VetResponseDTO dto = EntityDtoUtil.vetEntityToResponseDTO(savedVet);
                                            dto.setPhoto(fileResp);
                                            return dto;
                                        });
                            });
                });
    }


}
