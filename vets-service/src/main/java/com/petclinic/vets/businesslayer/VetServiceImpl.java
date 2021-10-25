package com.petclinic.vets.businesslayer;

import com.petclinic.vets.datalayer.Specialty;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@Service
@RequiredArgsConstructor
@Slf4j
public class VetServiceImpl implements VetService {
    private final VetRepository vetRepository;
    private final VetMapper vetMapper;

    @Override
    public List<Vet> getAllVets() {
        return vetRepository.findAll();
    }

    @Override
    public Vet getVetByVetId(int vetId) {
        return vetRepository.findByVetId(vetId).orElseThrow(() -> new NotFoundException("No vet found for vetId: " + vetId));
    }

    @Override
    public Vet updateVet(Vet vet, Vet updateVet) {
        if (updateVet.getEmail() != null && !updateVet.getEmail().isEmpty()) {
            vet.setEmail(updateVet.getEmail());
        }
        if (updateVet.getFirstName() != null && !updateVet.getFirstName().isEmpty()) {
            vet.setFirstName(updateVet.getFirstName());
        }
        if (updateVet.getLastName() != null && !updateVet.getLastName().isEmpty()) {
            vet.setLastName(updateVet.getLastName());
        }
        if (updateVet.getPhoneNumber() != null && !updateVet.getPhoneNumber().isEmpty()) {
            vet.setPhoneNumber(updateVet.getPostNumber());
        }
        if (updateVet.getResume() != null && !updateVet.getResume().isEmpty()) {
            vet.setResume(updateVet.getResume());
        }
        if (updateVet.getWorkday() != null && !updateVet.getWorkday().isEmpty()) {
            vet.setWorkday(updateVet.getWorkday());
        }
        if (updateVet.getImage() != null) {
            vet.setImage(updateVet.getImage());
        }
        Set<Specialty> specialties = new HashSet<>();
        specialties.addAll(updateVet.getSpecialties());
        vet.setSpecialties(specialties);

        return vetRepository.save(vet);
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
    }

    @Override
    public List<VetDTO> getAllVetDTOs() {
        List<Vet> vetList = getAllVets();
        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);
        return vetDTOList;
    }

    @Override
    public VetDTO getVetDTOByVetId(int vetId) {
        Vet vet = getVetByVetId(vetId);
        VetDTO vetDTO = vetMapper.vetToVetDTO(vet);
        return vetDTO;
    }

    @Override
    public VetDTO updateVetWithDTO(int vetId, VetDTO vetDTO) {
        vetDTO.setVetId(vetId);
        Vet vet = getVetByVetId(vetId);
        Vet vetUpdate = vetMapper.vetDTOToVet(vetDTO);
        updateVet(vet, vetUpdate);
        return vetMapper.vetToVetDTO(updateVet(vet, vetUpdate));
    }

    @Override
    public VetDTO createVetFromDTO(VetDTO vetDTO) {
        Vet vet = vetMapper.vetDTOToVet(vetDTO);
        if (vetDTO.getSpecialties() != null)
            vetDTO.getSpecialties().forEach(vet::addSpecialty);
        return vetMapper.vetToVetDTO(createVet(vet));
    }

    @Override
    public List<VetDTO> getAllDisabledVetDTOs() {
        List<Vet> vetList = getAllDisabledVets();
        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);
        return vetDTOList;
    }

    @Override
    public List<VetDTO> getAllEnabledVetDTOs() {
        List<Vet> vetList = getAllEnabledVets();
        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);
        return vetDTOList;
    }

    @Override
    public VetDTO disableVetFromDTO(int vetId, VetDTO vetDTOFound) {
        Vet fromVetId = getVetByVetId(vetId);
        Vet vetFound = vetMapper.vetDTOToVet(vetDTOFound);
        VetDTO vetDTO = vetMapper.vetToVetDTO(disableVet(fromVetId, vetFound));
        return vetDTO;
    }

    @Override
    public VetDTO enableVetFromDTO(int vetId, VetDTO vetDTOFound) {
        Vet fromVetId = getVetByVetId(vetId);
        Vet vetFound = vetMapper.vetDTOToVet(vetDTOFound);
        VetDTO vetDTO = vetMapper.vetToVetDTO(enableVet(fromVetId, vetFound));
        return vetDTO;
    }

    @Override
    public void deleteVetByVetIdFromVetDTO(int vetId) {
        deleteVetByVetId(vetId);
    }
}
