package com.petclinic.vet.servicelayer;
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

import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.exceptions.NotFoundException;
import com.petclinic.vet.presentationlayer.VetRequestDTO;
import com.petclinic.vet.presentationlayer.VetResponseDTO;
import com.petclinic.vet.util.DatabaseInitializer;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class VetServiceImpl implements VetService {

    private final VetRepository vetRepository;
    private final BadgeRepository badgeRepository;
    private final PhotoRepository photoRepository;
    private final RatingRepository ratingRepository;
    private final EducationRepository educationRepository;

    @Override
    public Flux<VetResponseDTO> getAll() {
        return vetRepository.findAll()
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }

    @Override
    public Mono<VetResponseDTO> addVet(Mono<VetRequestDTO> vetDTOMono) {
        return vetDTOMono
                .flatMap(this::validateVetRequestDTO)
                .flatMap(this::handleDefaultPhoto)
                .map(EntityDtoUtil::vetRequestDtoToEntity)
                .flatMap(this::assignBadgeAndSaveBadgeAndVet)
                .map(EntityDtoUtil::vetEntityToResponseDTO);
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
                .map(EntityDtoUtil::vetEntityToResponseDTO);
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

    @Transactional
    @Override
    public Mono<VetResponseDTO> getVetByFirstName(String firstName) {
        return vetRepository.findVetByFirstName(firstName)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this first name was found: " + firstName)))
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }

    @Override
    public Mono<VetResponseDTO> getVetByLastName(String lastName) {
        return vetRepository.findVetByLastName(lastName)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this last name was found: " + lastName)))
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }


    @Override
    public Mono<Void> deleteVetByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this vetId was found: " + vetId)))
                .flatMap(vet -> {
                    log.info("Deleting associated data for vetId: {}", vetId);
                    Mono<Long> deleteBadges = badgeRepository.deleteByVetId(vetId);
                    Mono<Long> deletePhotos = photoRepository.deleteByVetId(vetId);
                    Mono<Long> deleteRatings = ratingRepository.deleteByVetId(vetId);
                    Mono<Long> deleteEducations = educationRepository.deleteByVetId(vetId);

                    return Mono.when(deleteBadges, deletePhotos, deleteRatings, deleteEducations)
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

    private Mono<VetRequestDTO> handleDefaultPhoto(VetRequestDTO vet) {
        if (vet.isPhotoDefault()) {
            Photo photo = Photo.builder()
                    .vetId(vet.getVetId())
                    .filename("vet_default.jpg")
                    .imgType("image/jpeg")
                    .data(loadImage("images/vet_default.jpg"))
                    .build();
            return photoRepository.save(photo)
                    .thenReturn(vet);
        }
        return Mono.just(vet);
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

}
