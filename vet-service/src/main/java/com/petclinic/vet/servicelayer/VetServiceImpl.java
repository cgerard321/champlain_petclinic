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

import com.petclinic.vet.dataaccesslayer.VetRepository;
import com.petclinic.vet.exceptions.InvalidInputException;
import com.petclinic.vet.exceptions.NotFoundException;
import com.petclinic.vet.presentationlayer.VetRequestDTO;
import com.petclinic.vet.presentationlayer.VetResponseDTO;
import com.petclinic.vet.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class VetServiceImpl implements VetService {

    private final VetRepository vetRepository;

    @Override
    public Flux<VetResponseDTO> getAll() {
        return vetRepository.findAll()
                .map(EntityDtoUtil::vetEntityToResponseDTO);
    }

    @Override
    public Mono<VetResponseDTO> insertVet(Mono<VetRequestDTO> vetDTOMono) {
        return vetDTOMono
                .flatMap(requestDTO->{
                    if(requestDTO.getFirstName().length()>30||requestDTO.getFirstName().length()<2)
                        return Mono.error(new InvalidInputException("firstName length should be between 2 and 30 characters: "+requestDTO.getFirstName()));
                    if(requestDTO.getLastName().length()>30||requestDTO.getLastName().length()<2)
                        return Mono.error(new InvalidInputException("lastName length should be between 2 and 30 characters: "+requestDTO.getLastName()));
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
                .map(vetDTO -> EntityDtoUtil.vetRequestDtoToEntity(vetDTO))
                .flatMap((vetRepository::save))
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

    @Override
    public Mono<Void> deleteVetByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .switchIfEmpty(Mono.error(new NotFoundException("No vet with this vetId was found: " + vetId)))
                .flatMap(vetRepository::delete);
    }



}
