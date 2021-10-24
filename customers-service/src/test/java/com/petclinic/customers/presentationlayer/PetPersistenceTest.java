package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.*;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
/**
 * @author lpsim
 * @author Christopher
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class PetPersistenceTest {

    @Autowired
    private PetRepository repository;

    @Autowired
    private OwnerRepository ownerRepository;

    public Owner setupOwner()
    {
        Owner owner = new Owner();
        owner.setId(11);
        owner.setFirstName("John");
        owner.setLastName("Wick");
        owner.setTelephone("5144041234");
        owner.setCity("Montreal");
        owner.setAddress("420 Avenue");

        return owner;
    }

    public Pet setupPet() {


        Pet pet = new Pet();
        pet.setName("Daisy");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        return pet;
    }

    public Pet setupFakePet() {
        Owner owner = new Owner();
        owner.setId(12);
        owner.setFirstName("Sam");
        owner.setLastName("Brian");
        owner.setTelephone("5144041234");
        owner.setCity("Montreal");
        owner.setAddress("420 Avenue");
        PetType petType = new PetType();
        petType.setId(6);

        Pet pet = new Pet();
        pet.setName("Daisy");
        pet.setId(2);
        pet.setType(petType);
        pet.setOwner(owner);

        owner.addPet(pet);

        return pet;
    }

    @DisplayName("PetPersistence_findPetByOwner_test")
    @Test
    public void findPetByOwner_test() {

        //Arrange
        Owner owner = setupOwner();
        Owner savedOwner = ownerRepository.save(owner);

        Pet newPet = setupPet();
        newPet.setOwner(ownerRepository.findById(savedOwner.getId()).get());
        savedOwner.addPet(newPet);
        Pet fakePet = setupFakePet();
        Pet savedPet = repository.save(newPet);

        //Act
        Pet foundPet = repository.findPetByOwner(ownerRepository.findById(savedOwner.getId()).get(), savedPet.getId()).orElse(null);

        //Assert
        assert foundPet != null;
        assertEquals(foundPet, savedPet);
        assertNotEquals(foundPet, fakePet);
    }

    @DisplayName("PetPersistence_findAll_test")
    @Test
    public void findAllPetByOwner_test() {

        /*
         HOW IT WORKS?
         ownerRepository.findById(1).get() already exist and he has one pet. Here we are adding a new one to test if
         the repository can find two pets
        */

        //Expect 4 entities
        int expectedLength = 2;

        //Arrange
        Pet newPet = setupPet();

        newPet.setName("John");
        newPet.setOwner(ownerRepository.findById(1).get());
        Pet savedPet1 = repository.save(newPet);
        Pet foundPet1 = repository.findPetByOwner(savedPet1.getOwner(), savedPet1.getId()).orElse(null);

        assert foundPet1 != null;
        assertEquals(foundPet1, savedPet1);

        //Act
        List<Pet> petList = repository.findAllPetByOwner(ownerRepository.findById(1).get());

        //Assert
        assertEquals(expectedLength, petList.size());
    }



    @DisplayName("PetPersistence_createPet")
    @Test
    public void create_pet_test()
    {
        //Here we delete the repo to make sure that there is not pet inside
        //Therefore, when creating a pet, there should be only 1
        repository.deleteAll();
        int expectedPetInDB = 1;
        Pet newPet = setupPet();
        newPet.setOwner(setupOwner());
        setupOwner().addPet(newPet);
        Pet savedPet = repository.save(newPet);

        //Act
        Pet foundSaved = repository.findById(savedPet.getId()).orElse(null);

        //Assert
        assert foundSaved != null;
        MatcherAssert.assertThat(foundSaved, samePropertyValuesAs(savedPet));
        assertEquals(expectedPetInDB, repository.findAll().size());
    }


    @DisplayName("PetPersistence_shouldDeletePet")
    @Test
    public void shouldDeletePet(){

        //Arrange
        Pet newPet = setupPet();
        newPet.setOwner(setupOwner());
        setupOwner().addPet(newPet);
        Pet savedPet = repository.save(newPet);

        //Act
        assertThat(repository.findById(savedPet.getId()).isPresent());
        repository.delete(savedPet);

        //Assert
        Pet foundPet = repository.findById(savedPet.getId()).orElse(null);
        assertNull(foundPet);
    }
}
