package com.petclinic.customersservice.util;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.presentationlayer.PetTypeResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityDTOUtilTest {

    @Test
    void toOwnerResponseDTO_WithValidOwner_ShouldConvertSuccessfully() {
        Owner owner = new Owner();
        owner.setOwnerId("ownerId123");
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setProvince("IL");
        owner.setTelephone("555-1234");
        owner.setPhotoId("photo123");

        OwnerResponseDTO result = EntityDTOUtil.toOwnerResponseDTO(owner);

        assertNotNull(result);
        assertEquals("ownerId123", result.getOwnerId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("Springfield", result.getCity());
        assertEquals("IL", result.getProvince());
        assertEquals("555-1234", result.getTelephone());
    }

    @Test
    void toOwner_WithValidOwnerResponseDTO_ShouldConvertSuccessfully() {
        OwnerResponseDTO ownerResponseDTO = new OwnerResponseDTO();
        ownerResponseDTO.setOwnerId("ownerId456");
        ownerResponseDTO.setFirstName("Jane");
        ownerResponseDTO.setLastName("Smith");
        ownerResponseDTO.setAddress("456 Oak Ave");
        ownerResponseDTO.setCity("Chicago");
        ownerResponseDTO.setProvince("IL");
        ownerResponseDTO.setTelephone("555-5678");

        Owner result = EntityDTOUtil.toOwner(ownerResponseDTO);

        assertNotNull(result);
        assertEquals("ownerId456", result.getOwnerId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("456 Oak Ave", result.getAddress());
        assertEquals("Chicago", result.getCity());
        assertEquals("IL", result.getProvince());
        assertEquals("555-5678", result.getTelephone());
    }

    @Test
    void toPetResponseDTO_WithValidPet_ShouldConvertSuccessfully() {
        Pet pet = new Pet();
        pet.setPetId("petId123");
        pet.setName("Fluffy");
        pet.setBirthDate(new java.util.Date());
        pet.setPetTypeId("typeId123");
        pet.setOwnerId("ownerId123");
        pet.setIsActive("true");
        pet.setPhotoId("photo456");
        pet.setWeight("10.5");

        PetResponseDTO result = EntityDTOUtil.toPetResponseDTO(pet);

        assertNotNull(result);
        assertEquals("petId123", result.getPetId());
        assertEquals("Fluffy", result.getName());
        assertNotNull(result.getBirthDate());
        assertEquals("typeId123", result.getPetTypeId());
        assertEquals("ownerId123", result.getOwnerId());
        assertEquals("true", result.getIsActive());
    }

    @Test
    void toPet_WithValidPetResponseDTO_ShouldConvertSuccessfully() {
        PetResponseDTO petResponseDTO = new PetResponseDTO();
        petResponseDTO.setPetId("petId456");
        petResponseDTO.setName("Spot");
        petResponseDTO.setBirthDate(new java.util.Date());
        petResponseDTO.setPetTypeId("typeId456");
        petResponseDTO.setOwnerId("ownerId456");
        petResponseDTO.setIsActive("false");
        petResponseDTO.setWeight("8.2");

        Pet result = EntityDTOUtil.toPet(petResponseDTO);

        assertNotNull(result);
        assertEquals("petId456", result.getPetId());
        assertEquals("Spot", result.getName());
        assertNotNull(result.getBirthDate());
        assertEquals("typeId456", result.getPetTypeId());
        assertEquals("ownerId456", result.getOwnerId());
        assertEquals("false", result.getIsActive());
    }

    @Test
    void toPetTypeResponseDTO_WithValidPetType_ShouldConvertSuccessfully() {
        PetType petType = new PetType();
        petType.setPetTypeId("typeId789");
        petType.setName("Dog");
        petType.setPetTypeDescription("Man's best friend");

        PetTypeResponseDTO result = EntityDTOUtil.toPetTypeResponseDTO(petType);

        assertNotNull(result);
        assertEquals("typeId789", result.getPetTypeId());
        assertEquals("Dog", result.getName());
    }
}
