package com.petclinic.vets.businesslayer;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tymofiy Bun: vetDTOToVet, vetToVetDTO, vetDTOListToVetList, vetListToVetDTOList
 * <p>
 * User: @BunTymofiy
 * Date: 2021-10-11
 * Ticket: feat(VETS-CPC-228): add dto and vet mapper
 */

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VetMapper {
    @Mappings(
            {
                    @Mapping(target = "id", ignore = true),
            }
    )
    Vet vetDTOToVet(VetDTO vetDTO);

    VetDTO vetToVetDTO(Vet vet);

    List<Vet> vetDTOListToVetList(List<VetDTO> vetDTOList);

    List<VetDTO> vetListToVetDTOList(List<Vet> vetList);
}
