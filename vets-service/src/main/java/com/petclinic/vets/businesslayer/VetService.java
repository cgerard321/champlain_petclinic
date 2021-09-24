package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;

import java.util.List;

public interface VetService
{
    public List<Vet> getAllVets();
    public Vet getVetByVetId(int vetId);
    public Vet updateVet(Vet vet);
}
