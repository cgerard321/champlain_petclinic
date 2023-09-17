package com.petclinic.customersservice.util;

import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.business.PetDTO;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import lombok.Generated;
import org.springframework.beans.BeanUtils;

public class EntityDTOUtil {

    @Generated
    public EntityDTOUtil(){};

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

    public static PetDTO toPetDTO(Pet pet) {
        PetDTO petDTO = new PetDTO();
        BeanUtils.copyProperties(pet, petDTO);
        return petDTO;
    }

    public static Pet toPet(PetDTO petDTO) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(petDTO, pet);
        return pet;
    }

}
