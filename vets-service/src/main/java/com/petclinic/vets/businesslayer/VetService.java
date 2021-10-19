package com.petclinic.vets.businesslayer;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Tymofiy Bun: Added getAllVets, getVetByVetId,
 * updateVet, createVet, getAllDisabledVets, getAllEnabledVets,
 * getAllVetDTOs, getVetDTOByVetId, updateVetWithDTO, createVetFromDTO, getAllDisabledVetDTOs,getAllEnabledVetDTOs,
 * disableVetFromDTO, enableVetFromDTO, deleteVetByVetIdFromVetDTO
 *
 * User: @BunTymofiy
 * Date: 2021-9-27
 * Ticket: feat(vets-cpc-40): modify vet info
 *
 * User: @BunTymofiy
 * Date: 2021-9-28
 * Ticket: feat(VETS-CPC-65): disabled vet list
 *
 * User: @BunTymofiy
 * Date: 2021-10-11
 * Ticket: feat(VETS-CPC-228): add dto and vet mapper
 */
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;

import java.util.List;

public interface VetService
{
    public List<Vet> getAllVets();
    public Vet getVetByVetId(int vetId);
    public Vet updateVet(Vet vet,Vet updateVet);
    public Vet createVet(Vet vet);
    public List<Vet> getAllDisabledVets();
    public List<Vet> getAllEnabledVets();
    public Vet disableVet(Vet vet, Vet vetFound);
    public Vet enableVet(Vet vet, Vet vetFound);
    public void deleteVetByVetId(int vetId);

    public List<VetDTO> getAllVetDTOs();
    public VetDTO getVetDTOByVetId(int vetId);
    public VetDTO updateVetWithDTO(int vetId, VetDTO vetDTO);
    public VetDTO createVetFromDTO(VetDTO vetDTO);
    public List<VetDTO> getAllDisabledVetDTOs();
    public List<VetDTO> getAllEnabledVetDTOs();
    public VetDTO disableVetFromDTO(int vetId, VetDTO vetFound);
    public VetDTO enableVetFromDTO(int vetId, VetDTO vetFound);
    public void deleteVetByVetIdFromVetDTO(int vetId);


}
