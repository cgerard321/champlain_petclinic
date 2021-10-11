package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VetMapper
{
    @Mappings(
            {
                    @Mapping(target = "id", ignore = true),
                    @Mapping(target = "specialties", ignore = true)
            }
    )
    Vet vetDTOToVet(VetDTO vetDTO);
    VetDTO vetToVetDTO(Vet vet);

    List<Vet> vetDTOListToVetList(List<VetDTO> vetDTOList);
    List<VetDTO> vetListToVetDTOList(List<Vet> vetList);
}
