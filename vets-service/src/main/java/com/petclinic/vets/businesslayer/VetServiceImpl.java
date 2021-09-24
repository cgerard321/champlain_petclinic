package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import java.util.List;

@Service
public class VetServiceImpl implements VetService
{
    private final VetRepository vetRepository;

    public VetServiceImpl(VetRepository vetRepository) {
        this.vetRepository = vetRepository;
    }

    @Override
    public List<Vet> getAllVets()
    {
        return vetRepository.findAll();
    }

    @Override
    public Vet getVetByVetId(int vetId)
    {

        Vet foundVet = vetRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("No vet found for vetId: " + vetId));
        return foundVet;
    }

    @Override
    public Vet updateVet(Vet vet)
    {

        vetRepository.save(vet);
        return vet;
    }

}
