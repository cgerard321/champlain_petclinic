package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;

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

//    @Override
//    public Optional getVetByVetId(int vetId) {
//        return vetRepository.findById(vetId);
//    }

    @Override
    public Vet getVetByVetId(int vetId)
    {
        Vet foundVet = vetRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("No vet found for vetId: " + vetId));
        return foundVet;
    }

    @Override
    public Vet updateVet(Vet vet, Vet updateVet)
    {
        vet.setEmail(updateVet.getEmail());
        vet.setFirstName(updateVet.getFirstName());
        vet.setLastName(updateVet.getLastName());
        vet.setPhoneNumber(updateVet.getPhoneNumber());
        vet.setResume(updateVet.getResume());
        vet.setWorkday(updateVet.getWorkday());
        vetRepository.save(vet);

        return vet;
    }

    @Override
    public void createVet(Vet vet)
    {
        vetRepository.save(vet);
    }

}
