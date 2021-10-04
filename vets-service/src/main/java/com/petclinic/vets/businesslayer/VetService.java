package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;

import java.util.List;

public interface VetService
{
    public List<Vet> getAllVets();
    public Vet getVetByVetId(int vetId);
    public VetDTO updateVet(int vetId,VetDTO vetDTO);
    public Vet createVet(VetDTO vet);
    public List<Vet> getAllDisabledVets();
    public List<Vet> getAllEnabledVets();
    public Vet disableVet(Vet vet, Vet vetFound);
    public Vet enableVet(Vet vet, Vet vetFound);

    public VetDTO getVetDTOByVetId(int vetDtoId);
    public List<VetDTO> getAllDisabledVetDTOs();
    public List<VetDTO> getAllEnabledVetDTOs();


}
