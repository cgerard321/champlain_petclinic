package com.petclinic.customersservice.util;

import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.presentationlayer.*;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import lombok.Generated;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

public class EntityDTOUtil {

    @Generated
    public EntityDTOUtil(){}

    public static OwnerResponseDTO toOwnerResponseDTO(Owner owner) {
        OwnerResponseDTO dto = new OwnerResponseDTO();
        dto.setOwnerId(owner.getOwnerId());
        dto.setFirstName(owner.getFirstName());
        dto.setLastName(owner.getLastName());
        dto.setCity(owner.getCity());
        dto.setAddress(owner.getAddress());
        dto.setProvince(owner.getProvince());
        dto.setTelephone(owner.getTelephone());
        return dto;
    }

    public static Owner toOwner(OwnerRequestDTO ownerRequestDTO) {
        Owner owner = new Owner();
        owner.setOwnerId(UUID.randomUUID().toString());
        owner.setFirstName(ownerRequestDTO.getFirstName());
        owner.setLastName(ownerRequestDTO.getLastName());
        owner.setCity(ownerRequestDTO.getCity());
        owner.setAddress(ownerRequestDTO.getAddress());
        owner.setProvince(ownerRequestDTO.getProvince());
        owner.setTelephone(ownerRequestDTO.getTelephone());
        return owner;
    }

    public static PetResponseDTO toPetResponseDTO(Pet pet) {
        PetResponseDTO dto = new PetResponseDTO();
        dto.setPetId(pet.getPetId());
        dto.setOwnerId(pet.getOwnerId());
        dto.setPetTypeId(pet.getPetTypeId());
        dto.setName(pet.getName());
        dto.setBirthDate(pet.getBirthDate());
        dto.setWeight(pet.getWeight());
        dto.setIsActive(pet.getIsActive());
        return dto;
    }

    public static Pet toPet(PetRequestDTO petRequestDTO) {
        Pet pet = new Pet();
        pet.setPetId(UUID.randomUUID().toString());
        pet.setOwnerId(petRequestDTO.getOwnerId());
        pet.setPetTypeId(petRequestDTO.getPetTypeId());
        pet.setName(petRequestDTO.getName());
        pet.setBirthDate(petRequestDTO.getBirthDate());
        pet.setWeight(petRequestDTO.getWeight());
        pet.setIsActive(petRequestDTO.getIsActive() != null ? petRequestDTO.getIsActive() : "true");
        return pet;
    }

    public static PetTypeResponseDTO toPetTypeResponseDTO(PetType petType) {
        PetTypeResponseDTO dto = new PetTypeResponseDTO();
        dto.setPetTypeId(petType.getPetTypeId());
        dto.setName(petType.getName());
        dto.setPetTypeDescription(petType.getPetTypeDescription());
        return dto;
    }

    public static PetType toPetType(PetTypeRequestDTO petTypeRequestDTO) {
        PetType petType = new PetType();
        petType.setPetTypeId(UUID.randomUUID().toString());
        petType.setName(petTypeRequestDTO.getName());
        petType.setPetTypeDescription(petTypeRequestDTO.getPetTypeDescription());
        return petType;
    }
}