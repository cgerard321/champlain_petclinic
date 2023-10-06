package com.petclinic.customersservice.util;

import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeResponseDTO;
import lombok.Generated;
import org.springframework.beans.BeanUtils;

public class EntityDTOUtil {

    @Generated
    public EntityDTOUtil(){}

    public static OwnerResponseDTO toOwnerResponseDTO(Owner owner) {
        OwnerResponseDTO ownerResponseDTO = new OwnerResponseDTO();
        BeanUtils.copyProperties(owner, ownerResponseDTO);
        return ownerResponseDTO;
    }

    public static Owner toOwner(OwnerResponseDTO ownerResponseDTO) {
        Owner owner = new Owner();
        BeanUtils.copyProperties(ownerResponseDTO, owner);
        return owner;
    }

    public static PetResponseDTO toPetResponseDTO(Pet pet) {
        PetResponseDTO petDTO = new PetResponseDTO();
        BeanUtils.copyProperties(pet, petDTO);
        return petDTO;
    }

    public static Pet toPet(PetResponseDTO petResponseDTO) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(petResponseDTO, pet);
        return pet;
    }

    public static PetTypeResponseDTO toPetTypeResponseDTO(PetType petType) {
        PetTypeResponseDTO petTypeResponseDTO = new PetTypeResponseDTO();
        BeanUtils.copyProperties(petType, petTypeResponseDTO);
        return petTypeResponseDTO;
    }

}
