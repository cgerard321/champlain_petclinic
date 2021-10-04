package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VetMapper
{
//    @Mapping(target = "id", ignore = true)
    VetDTO VetToVetDto(Vet vet);

    @Mapping(target = "id", ignore = true)
    Vet VetDtoToVet(VetDTO vet);

    List<VetDTO>  VetListToVetDTOList(List<Vet> vetList);

    List<Vet> VetDTOListToVetList(List<VetDTO> vetDTOList);
}
