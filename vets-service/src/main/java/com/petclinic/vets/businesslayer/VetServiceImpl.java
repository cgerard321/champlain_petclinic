package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
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
        if(!updateVet.getEmail().isEmpty()) {vet.setEmail(updateVet.getEmail());}
        if(!updateVet.getEmail().isEmpty()) {vet.setFirstName(updateVet.getFirstName());}
        if(!updateVet.getEmail().isEmpty()) {vet.setLastName(updateVet.getLastName());}
        if(!updateVet.getEmail().isEmpty()) {vet.setPhoneNumber(updateVet.getPostNumber());}
        if(!updateVet.getEmail().isEmpty()) {vet.setResume(updateVet.getResume());}
        if(!updateVet.getEmail().isEmpty()) {vet.setWorkday(updateVet.getWorkday());}
        vetRepository.save(vet);

        return vet;
    }

    @Override
    public Vet createVet(Vet vet)
    {
        return vetRepository.save(vet);
    }

    @Override
    public List<Vet> getAllDisabledVets()
    {
        return vetRepository.findAllDisabledVets();
    }

    @Override
    public List<Vet> getAllEnabledVets()
    {
        return vetRepository.findAllEnabledVets();
    }


    public Vet disableVet(Vet vet, Vet vetFound){
       vet.setIsActive(vetFound.getIsActive());
       vetRepository.save(vet);
       return vet;
    }

    @Override
    public Vet enableVet(Vet vet, Vet vetFound) {
        vet.setIsActive(1);
        vetRepository.save(vet);
        return vet;
    }

}
