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
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class VetServiceImpl implements VetService {

    @Autowired
    VetRepository vetRepository;

    @Override
    public Flux<VetDTO> getAll() {
        return vetRepository.findAll()
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<VetDTO> insertVet(Mono<VetDTO> vetDTOMono) {
        return vetDTOMono
                .map(EntityDtoUtil::toEntity)
                .doOnNext(e -> e.setVetId(EntityDtoUtil.generateVetId()))
                .flatMap((vetRepository::save))
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<VetDTO> updateVet(String vetId, Mono<VetDTO> vetDTOMono) {
        return vetRepository.findVetByVetId(vetId)
                .flatMap(p -> vetDTOMono
                        .map(EntityDtoUtil::toEntity)
                        .doOnNext(e -> e.setVetId(p.getVetId()))
                        .doOnNext(e -> e.setId(p.getId()))
                )
                .flatMap(vetRepository::save)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<VetDTO> getVetByVetId(String vetId) {
        return vetRepository.findVetByVetId(vetId)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<VetDTO> getVetByVetBillId(String vetBillId) {
       return  vetRepository.findVetByVetBillId(vetBillId)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Flux<VetDTO> getVetByIsActive(boolean isActive) {
        return vetRepository.findVetsByIsActive(isActive)
                .map(EntityDtoUtil::toDTO);
    }

    @Override
    public Mono<Void> deleteVetByVetId(String vetId) {
        return vetRepository.deleteVetByVetId(vetId);
    }



}
