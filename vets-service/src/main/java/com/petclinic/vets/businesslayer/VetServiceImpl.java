package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Specialty;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VetServiceImpl implements VetService {
    private final VetRepository vetRepository;
    private final VetMapper vetMapper;
//    public VetServiceImpl(VetRepository vetRepository,VetMapper vetMapper) {
//        this.vetMapper = vetMapper;
//        this.vetRepository = vetRepository;
//    }

    @Override
    public List<Vet> getAllVets() {
        return vetRepository.findAll();
    }

    @Override
    public Vet getVetByVetId(int vetId) {
        Vet foundVet = vetRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException("No vet found for vetId: " + vetId));
        return foundVet;
    }

    @Override
    public Vet updateVet(Vet vet, Vet updateVet) {
        if (!updateVet.getEmail().isEmpty() && updateVet.getEmail() != null) {
            vet.setEmail(updateVet.getEmail());
        }
        if (!updateVet.getFirstName().isEmpty() && updateVet.getFirstName() != null) {
            vet.setFirstName(updateVet.getFirstName());
        }
        if (!updateVet.getLastName().isEmpty() && updateVet.getLastName() != null) {
            vet.setLastName(updateVet.getLastName());
        }
        if (!updateVet.getPhoneNumber().isEmpty() && updateVet.getPhoneNumber() != null) {
            vet.setPhoneNumber(updateVet.getPostNumber());
        }
        if (!updateVet.getResume().isEmpty() && updateVet.getResume() != null) {
            vet.setResume(updateVet.getResume());
        }
        if (!updateVet.getWorkday().isEmpty() && updateVet.getWorkday() != null) {
            vet.setWorkday(updateVet.getWorkday());
        }
        if(updateVet.getImage() != null){
            vet.setImage(updateVet.getImage());
        }
        if(!updateVet.getSpecialties().isEmpty() && updateVet.getSpecialties() != null){
            Set<Specialty> specialties = new HashSet<>();
            specialties.addAll(updateVet.getSpecialties());
            vet.setSpecialties(specialties);
        }
        vetRepository.save(vet);

        return vet;
    }

    @Override
    public Vet createVet(Vet vet) {
        return vetRepository.save(vet);
    }

    @Override
    public List<Vet> getAllDisabledVets() {
        return vetRepository.findAllDisabledVets();
    }

    @Override
    public List<Vet> getAllEnabledVets() {
        return vetRepository.findAllEnabledVets();
    }


    public Vet disableVet(Vet vet, Vet vetFound) {
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
    public void deleteVetByVetId(int vetId) {
        Vet foundVet = vetRepository.findByVetId(vetId)
                .orElseThrow(() -> new NotFoundException(String.format("No vet found for vetId: {vetId} ", vetId)));
        vetRepository.delete(foundVet);
        //return "redirect:/vets"; <-- redirect to list after ??
    }

    @Override
    public List<VetDTO> getAllVetDTOs() {
        List<Vet> vetList = getAllVets();
        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);
        for(int i = 0; i < vetDTOList.size(); i++)
        {
            Set<Specialty> specialties = new HashSet<>();
            specialties.addAll(vetList.get(i).getSpecialties());
            vetDTOList.get(i).setSpecialties(specialties);
        }
        return vetDTOList;
    }

    @Override
    public VetDTO getVetDTOByVetId(int vetId) {
        return vetMapper.vetToVetDTO(getVetByVetId(vetId));
    }

    @Override
    public VetDTO updateVetWithDTO(int vetId, VetDTO vetDTO) {
        Vet vet = getVetByVetId(vetId);
        Vet vetUpdate = vetMapper.vetDTOToVet(vetDTO);
        if(vetDTO.getSpecialties() != null && !vetDTO.getSpecialties().isEmpty()) {
            Set<Specialty> specialties = new HashSet<>();
            specialties.addAll(vetDTO.getSpecialties());
            vetUpdate.setSpecialties(specialties);
        }
        updateVet(vet,vetUpdate);
        return getVetDTOByVetId(vetId);
    }

    @Override
    public VetDTO createVetFromDTO(VetDTO vetDTO) {
        Vet vet = vetMapper.vetDTOToVet(vetDTO);
        Set<Specialty> specialties = new HashSet<>();
        specialties.addAll(vetDTO.getSpecialties());
        vet.setSpecialties(specialties);
        createVet(vet);
        return vetDTO;
    }

    @Override
    public List<VetDTO> getAllDisabledVetDTOs() {
        List<Vet> vetList = getAllDisabledVets();
        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);
        for(int i = 0; i < vetDTOList.size(); i++)
        {
            Set<Specialty> specialties = new HashSet<>();
            specialties.addAll(vetList.get(i).getSpecialties());
            vetDTOList.get(i).setSpecialties(specialties);
        }
        return vetDTOList;
    }

    @Override
    public List<VetDTO> getAllEnabledVetDTOs() {
        List<Vet> vetList = getAllEnabledVets();
        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);
        for(int i = 0; i < vetDTOList.size(); i++)
        {
            Set<Specialty> specialties = new HashSet<>();
            specialties.addAll(vetList.get(i).getSpecialties());
            vetDTOList.get(i).setSpecialties(specialties);
        }
        return vetDTOList;
    }

    @Override
    public VetDTO disableVetFromDTO(int vetId, VetDTO vetDTOFound) {
        Vet fromVetId = getVetByVetId(vetId);
        Vet vetFound = vetMapper.vetDTOToVet(vetDTOFound);
        VetDTO vetDTO = vetMapper.vetToVetDTO(disableVet(fromVetId,vetFound));
        return vetDTO;
    }

    @Override
    public VetDTO enableVetFromDTO(int vetId, VetDTO vetDTOFound) {
        Vet fromVetId = getVetByVetId(vetId);
        Vet vetFound = vetMapper.vetDTOToVet(vetDTOFound);
        VetDTO vetDTO = vetMapper.vetToVetDTO(enableVet(fromVetId,vetFound));
        return vetDTO;
    }

    @Override
    public void deleteVetByVetIdFromVetDTO(int vetId) {
        deleteVetByVetId(vetId);
    }
}
