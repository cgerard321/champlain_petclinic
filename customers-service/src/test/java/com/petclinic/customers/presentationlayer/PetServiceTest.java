package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.OwnerService;
import com.petclinic.customers.businesslayer.PetService;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.datalayer.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PetServiceTest {

    @MockBean
    OwnerRepository ownerRepository;

    @MockBean
    PetRepository repository;

    @Autowired
    OwnerService ownerService;

    @Autowired
    PetService service;

    public Owner setupOwner()
    {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Wick");
        owner.setTelephone("5144041234");
        owner.setCity("Montreal");
        owner.setAddress("420 Avenue");

        return owner;
    }

    public Pet setupPet() throws ParseException {

        Owner owner = setupOwner();

        Pet pet = new Pet();
        pet.setName("Daisy");
        pet.setId(2);

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date birthDate = simpleDateFormat.parse("2018-09-09");

        pet.setBirthDate(birthDate);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    @DisplayName("ownerService_FindByPetId_NotFoundException")
    @Test
    public void test_findByPetId_NotFoundException()
    {
        int petId = 1;
        String expectedErrorMsg = "Pet with ID: " + petId + " not found!";
        Mockito.when(repository.findById(Mockito.anyInt())).thenThrow(new NotFoundException());
        try {
            service.findByPetId(petId);
        } catch(NotFoundException ex) {
            assertEquals(ex.getMessage(), expectedErrorMsg);
        }

    }

    // TEST FOR FINDING ALL PET TYPES
    @DisplayName("petService_FindAll_PetTypes")
    @Test
    public void test_findAll_PetTypes() {
        //Arrange
        int expectedLength = 4;
        List<PetType> petTypeList = new ArrayList<>();

        PetType p1 = new PetType();
        p1.setId(1);
        petTypeList.add(p1);

        PetType p2 = new PetType();
        p1.setId(2);
        petTypeList.add(p2);

        PetType p3 = new PetType();
        p1.setId(3);
        petTypeList.add(p3);

        PetType p4 = new PetType();
        p1.setId(4);
        petTypeList.add(p4);

        when(repository.findPetTypes()).thenReturn(petTypeList);

        //Act
        List<PetType> returnedList = service.getAllPetTypes();

        //Assert
        assertThat(expectedLength).isEqualTo(returnedList.size());
    }


    @DisplayName("PetService_DeletePet")
    @Test
    public void test_deletePet() throws ParseException {

        //Arrange
        Pet petTest = setupPet();
        when(repository.findById(petTest.getId())).thenReturn(Optional.of(petTest));

        Owner ownerTest = setupOwner();
        when(ownerRepository.findById(ownerTest.getId())).thenReturn(Optional.of(ownerTest));

        //Act
        service.deletePet(petTest.getId(), 1);

        //Assert
        verify(repository, times(1)).delete(petTest);

    }

    @DisplayName("ownerService_UpdateOwner_NotFoundExceptionForOwner")
    @Test
    public void test_deletePet_NotFoundException() throws ParseException {
        int ownerId = 1;
        Pet pet = setupPet();
        String expectedErrorMsg = "Owner or pet is not valid. Please standby for assistance. A specialized support team will shortly make contact with you.";
        Mockito.when(ownerRepository.findById(Mockito.anyInt())).thenThrow(new NotFoundException());
        try {
            service.deletePet(pet.getId(),ownerId);
        } catch(NotFoundException ex) {
            assertEquals(expectedErrorMsg, ex.getMessage());
        }
    }

    @DisplayName("PetService_CreatePet")
    @Test
    public void test_CreatePet() throws ParseException {
        //Arrange
        Owner ownerTest = setupOwner();
        when(ownerRepository.findById(ownerTest.getId())).thenReturn(Optional.of(ownerTest));

        Pet petTest = setupPet();
        when(repository.findById(petTest.getId())).thenReturn(Optional.of(petTest));


        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date birthDate = simpleDateFormat.parse("2018-09-09");

        PetRequest petRequest = new PetRequest("Daisy", birthDate);

        //Act
        service.CreatePet(petRequest, 1);

        Optional<Pet> retrievedPet = repository.findById(2);

        //Assert
        assertEquals(petRequest.getName(), retrievedPet.get().getName());
    }

    @DisplayName("ownerService_UpdateOwner_NotFoundException")
    @Test
    public void test_createPet_NotFoundException()
    {
        int ownerId = 1;
        PetRequest petrequest = new PetRequest();
        String expectedErrorMsg = "Owner with ID : " + ownerId+ " is not found";
        Mockito.when(ownerRepository.findById(Mockito.anyInt())).thenThrow(new NotFoundException());
        try {
            service.CreatePet(petrequest, ownerId);
        } catch(NotFoundException ex) {
            assertEquals(ex.getMessage(), expectedErrorMsg);
        }
    }
}
