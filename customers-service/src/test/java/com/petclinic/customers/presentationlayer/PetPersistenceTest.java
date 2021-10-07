package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetRepository;
import com.petclinic.customers.datalayer.PetType;
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

    @BeforeEach
    public void setUpDB()
    {
        repository.deleteAll();
    }


    public Pet setupPet() {
        Owner owner = new Owner();
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


    /**
     * ------------------------ TEST_FIND ------------------------
     * Testing the find by id method
     */
    @Test
    public void findPetById() {

        //Arrange
        Pet newPet = setupPet();
        Pet savedPet = repository.save(newPet);

        //Act
        Pet foundPet = repository.findById(savedPet.getId()).orElse(null);

        //Assert
        assert foundPet != null;
        MatcherAssert.assertThat(foundPet, samePropertyValuesAs(savedPet));

    }


    /**
     * ------------------------ TEST_FIND_ALL ------------------------
     * Testing the find_all() method
     */
    @Test
    public void findAll() {

        //Expect 4 entities
        int expectedLength = 4;

        //Arrange
        Pet newPet = setupPet();
        repository.deleteAll();

        newPet.setId(1);
        newPet.setName("John");
        Pet savedPet1 = repository.save(newPet);
        Pet foundPet1 = repository.findById(savedPet1.getId()).orElse(null);
        assert foundPet1 != null;
        MatcherAssert.assertThat(foundPet1, samePropertyValuesAs(savedPet1));

        newPet.setId(2);
        newPet.setName("Joseph");
        Pet savedPet2 = repository.save(newPet);
        Pet foundPet2 = repository.findById(savedPet2.getId()).orElse(null);
        assert foundPet2 != null;
        MatcherAssert.assertThat(foundPet2, samePropertyValuesAs(savedPet2));


        newPet.setId(3);
        newPet.setName("Jill");
        Pet savedPet3 = repository.save(newPet);
        Pet foundPet3 = repository.findById(savedPet3.getId()).orElse(null);
        assert foundPet3 != null;
        MatcherAssert.assertThat(foundPet3, samePropertyValuesAs(savedPet3));

        newPet.setId(4);
        newPet.setName("Jojo");
        Pet savedPet4 = repository.save(newPet);
        Pet foundPet4 = repository.findById(savedPet4.getId()).orElse(null);
        assert foundPet4 != null;
        MatcherAssert.assertThat(foundPet4, samePropertyValuesAs(savedPet4));


        //Act
        List<Pet> petList = repository.findAll();

        //Assert
        assertEquals(expectedLength, petList.size());
    }



    /**
     * ------------------------ TEST_CREATE ------------------------
     * Testing the delete owner method
     */
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


    /**
     * ------------------------ TEST_DELETE ------------------------
     * Testing the delete owner method
     */
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
