package com.petclinic.customersservice.util;

import com.petclinic.customersservice.data.*;
import com.petclinic.customersservice.presentationlayer.*;
import lombok.Generated;

import java.util.UUID;

public class EntityDTOUtil {

    @Generated
    public EntityDTOUtil(){}

    public static CustomerResponseDTO toCustomerReponseDTO(Customer customer) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setCity(customer.getCity());
        dto.setAddress(customer.getAddress());
        dto.setProvince(customer.getProvince());
        dto.setTelephone(customer.getTelephone());
        return dto;
    }

    public static Customer toCustomer(CustomerRequestDTO customerRequestDTO) {
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID().toString());
        customer.setFirstName(customerRequestDTO.getFirstName());
        customer.setLastName(customerRequestDTO.getLastName());
        customer.setCity(customerRequestDTO.getCity());
        customer.setAddress(customerRequestDTO.getAddress());
        customer.setProvince(customerRequestDTO.getProvince());
        customer.setTelephone(customerRequestDTO.getTelephone());
        return customer;
    }

    public static PetResponseDTO toPetResponseDTO(Pet pet) {
        PetResponseDTO dto = new PetResponseDTO();
        dto.setPetId(pet.getPetId());
        dto.setCustomerId(pet.getCustomerId());
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
        pet.setCustomerId(petRequestDTO.getCustomerId());
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