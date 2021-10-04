package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import com.petclinic.vets.utils.http.HttpErrorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import retrofit2.HttpException;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class VetServiceImpl implements VetService
{
    private final VetRepository vetRepository;
    private final VetMapper vetMapper;



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
    public VetDTO updateVet(int vetId, VetDTO updateVet)
    {
        Vet vet = getVetByVetId(vetId);
        if(!updateVet.getEmail().isEmpty()) {vet.setEmail(updateVet.getEmail());}
        if(!updateVet.getFirstName().isEmpty()) {vet.setFirstName(updateVet.getFirstName());}
        if(!updateVet.getLastName().isEmpty()) {vet.setLastName(updateVet.getLastName());}
        if(!updateVet.getPhoneNumber().isEmpty()) {vet.setPhoneNumber(updateVet.getPostNumber());}
        if(!updateVet.getResume().isEmpty()) {vet.setResume(updateVet.getResume());}
        if(!updateVet.getWorkday().isEmpty()) {vet.setWorkday(updateVet.getWorkday());}
        vetRepository.save(vet);
        VetDTO dtoReturn = vetMapper.VetToVetDto(getVetByVetId(vet.getVetId()));
        return dtoReturn;
    }

    @Override
    public Vet createVet(VetDTO vet)
    {
            Vet vetSave = vetMapper.VetDtoToVet(vet);
            return vetRepository.save(vetSave);
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

    @Override
    public VetDTO getVetDTOByVetId(int vetDtoId) {
        VetDTO vetDTO = vetMapper.VetToVetDto(getVetByVetId(vetDtoId));
        return vetDTO;
    }

    @Override
    public List<VetDTO> getAllDisabledVetDTOs() {
        List<VetDTO> vetDTOList = vetMapper.VetListToVetDTOList(getAllDisabledVets());
        return vetDTOList;
    }

    @Override
    public List<VetDTO> getAllEnabledVetDTOs() {
        List<VetDTO> vetDTOList = vetMapper.VetListToVetDTOList(getAllEnabledVets());
        return vetDTOList;
    }


}
