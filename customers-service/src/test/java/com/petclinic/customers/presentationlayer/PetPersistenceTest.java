package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetRepository;
import com.petclinic.customers.datalayer.PetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class PetPersistenceTest {
    @Autowired
    private PetRepository repository;
//
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
//
//    // TEST FOR FINDING PET BY ID
//    @Test
//    public void find() {
//        repository.save(setupPet());
//        assertEquals(repository.findById(2).get().getName(), setupPet().getName());
//    }
//
//    // TEST FOR FINDING ALL PETS
//    @Test
//    public void findAll() {
//        int expectedLength = 4;
//        Pet newPet = setupPet();
//        repository.deleteAll(); //otherwise the active profile pets will be counted
//
//        newPet.setId(1);
//        newPet.setName("John");
//        repository.save(newPet);
//
//        newPet.setId(2);
//        newPet.setName("Joseph");
//        repository.save(newPet);
//
//        newPet.setId(3);
//        newPet.setName("Jill");
//        repository.save(newPet);
//
//        newPet.setId(4);
//        newPet.setName("Jojo");
//        repository.save(newPet);
//
//        assertEquals(expectedLength, repository.findAll().size());
//    }
//
//    // TEST FOR CREATING A PET
//    @Test
//    public void create() {
//        Pet newPet = setupPet();
//        Pet savedPet = repository.save(newPet);
//
//        assertEquals(newPet.getId(), savedPet.getId());
//        assertEquals(newPet.getName(), savedPet.getName());
//    }
//

    @Test
    public void shouldDeletePet(){
        Pet newPet = setupPet();
        Pet savedPet = repository.save(newPet);

        assertThat(repository.findById(savedPet.getId()).isPresent());
        repository.delete(savedPet);

        Pet foundPet = repository.findById(savedPet.getId()).orElse(null);
        assertNull(foundPet);
    }
}
