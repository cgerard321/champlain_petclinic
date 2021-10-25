package com.petclinic.vets.businesslayer;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tymofiy Bun: Added getAllVets, getVetByVetId,
 * updateVet, createVet, getAllDisabledVets, getAllEnabledVets,
 * getAllVetDTOs, getVetDTOByVetId, updateVetWithDTO, createVetFromDTO, getAllDisabledVetDTOs,getAllEnabledVetDTOs,
 * disableVetFromDTO, enableVetFromDTO, deleteVetByVetIdFromVetDTO
 * <p>
 * User: @BunTymofiy
 * Date: 2021-9-27
 * Ticket: feat(vets-cpc-40): modify vet info
 * <p>
 * User: @BunTymofiy
 * Date: 2021-9-28
 * Ticket: feat(VETS-CPC-65): disabled vet list
 * <p>
 * User: @BunTymofiy
 * Date: 2021-10-11
 * Ticket: feat(VETS-CPC-228): add dto and vet mapper
 */

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;

import java.util.List;

public interface VetService {
    List<Vet> getAllVets();

    Vet getVetByVetId(int vetId);

    Vet updateVet(Vet vet, Vet updateVet);

    Vet createVet(Vet vet);

    List<Vet> getAllDisabledVets();

    List<Vet> getAllEnabledVets();

    Vet disableVet(Vet vet, Vet vetFound);

    Vet enableVet(Vet vet, Vet vetFound);

    void deleteVetByVetId(int vetId);

    List<VetDTO> getAllVetDTOs();

    VetDTO getVetDTOByVetId(int vetId);

    VetDTO updateVetWithDTO(int vetId, VetDTO vetDTO);

    VetDTO createVetFromDTO(VetDTO vetDTO);

    List<VetDTO> getAllDisabledVetDTOs();

    List<VetDTO> getAllEnabledVetDTOs();

    VetDTO disableVetFromDTO(int vetId, VetDTO vetFound);

    VetDTO enableVetFromDTO(int vetId, VetDTO vetFound);

    void deleteVetByVetIdFromVetDTO(int vetId);


}
