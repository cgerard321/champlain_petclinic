package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.*;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class PetPersistenceTest {

    @Autowired
    private PetRepository repository;

    @Autowired
    private OwnerRepository ownerRepository;

    @BeforeEach
    public void setUpDB()
    {
        repository.deleteAll();
    }


    public Pet setupPet() {
        Owner owner = new Owner();
        owner.setId(11);
        owner.setFirstName("John");
        owner.setLastName("Wick");
        owner.setTelephone("5144041234");
        owner.setCity("Montreal");
        owner.setAddress("420 Avenue");

        Pet pet = new Pet();
        pet.setName("Daisy");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    @DisplayName("ownerPersistence_CreateOwner")
    @Test
    public void create_owner_test()
    {
        //Arrange
        Pet newPet = setupPet();
        Pet savedPet = repository.save(newPet);;

        //Act
        Pet foundSaved = repository.findById(savedPet.getId()).orElse(null);

        //Assert
        assert foundSaved != null;
        MatcherAssert.assertThat(foundSaved, samePropertyValuesAs(savedPet));
        assertEquals(1,repository.findAll().size());
    }
    
    @Test
    public void shouldDeletePet(){

        //Arrange
        Pet newPet = setupPet();
        Pet savedPet = repository.save(newPet);

        //Act
        assertThat(repository.findById(savedPet.getId()).isPresent());
        repository.delete(savedPet);

        //Assert
        Pet foundPet = repository.findById(savedPet.getId()).orElse(null);
        assertNull(foundPet);
    }
}
