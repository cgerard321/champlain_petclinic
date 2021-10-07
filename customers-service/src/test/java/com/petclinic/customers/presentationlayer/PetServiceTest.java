package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.PetService;
import com.petclinic.customers.datalayer.*;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.swing.text.html.Option;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
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

    public Pet setupPet() {

        Owner owner = setupOwner();

        Pet pet = new Pet();
        pet.setName("Daisy");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    // TEST FOR FINDING PET BY ID
    @DisplayName("petService_FindByPetId")
    @Test
    public void test_findByPetId() {
        //Arrange
        Pet petTest = setupPet();
        when(repository.findById(2)).thenReturn(Optional.of(petTest));

        //Act
        Optional<Pet> returnedPetOpt = service.findByPetId(2);
        Pet returnedPet = returnedPetOpt.get();

        //Assert
        assertThat(returnedPet.getId()).isEqualTo(petTest.getId());
    }


    // TEST FOR FINDING ALL PETS
    @DisplayName("petService_FindAll")
    @Test
    public void test_findAll() {
        //Arrange
        int expectedLength = 4;
        List<Pet> petList = new ArrayList<>();
        Pet newPet = setupPet();

        newPet.setId(1);
        newPet.setName("John");
        petList.add(newPet);

        newPet.setId(2);
        newPet.setName("Joseph");
        petList.add(newPet);

        newPet.setId(3);
        newPet.setName("Jill");
        petList.add(newPet);

        newPet.setId(4);
        newPet.setName("Jojo");
        petList.add(newPet);

        when(repository.findAll()).thenReturn(petList);

        //Act
        List<Pet> returnedList = service.findAll();

        //Assert
        assertThat(expectedLength).isEqualTo(returnedList.size());
    }

    /**
     * ------------------------ TEST_DELETE ------------------------
     * Testing the method deleteOwner()
     */
    @DisplayName("ownerService_DeleteOwner")
    @Test
    public void test_deletePet()
    {

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



    /**
     * ------------------------ TEST_CREATE ------------------------
     * Testing the method createOwner()
     */
    /*

    --WORK IN PROGRESS--

    @DisplayName("ownerService_CreateOwner")
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

        PetRequest petRequest = new PetRequest("John", birthDate);

        //Act
        service.CreatePet(petRequest, 1);
        Optional<Owner> retrievedOwner = ownerRepository.findById(ownerTest.getId());

        //Assert
        MatcherAssert.assertThat(retrievedOwner.get(), samePropertyValuesAs(ownerTest));
    }

     */


}
