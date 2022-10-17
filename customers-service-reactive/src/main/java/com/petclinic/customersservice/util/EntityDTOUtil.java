package com.petclinic.customersservice.util;

import com.petclinic.customersservice.business.OwnerDTO;
import com.petclinic.customersservice.business.PetDTO;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import org.springframework.beans.BeanUtils;

public class EntityDTOUtil {

    public static OwnerDTO toOwnerDTO(Owner owner) {
        OwnerDTO ownerDTO = new OwnerDTO();
        BeanUtils.copyProperties(owner, ownerDTO);
        return ownerDTO;
    }

    public static Owner toOwner(OwnerDTO ownerDTO) {
        Owner owner = new Owner();
        BeanUtils.copyProperties(ownerDTO, owner);
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
