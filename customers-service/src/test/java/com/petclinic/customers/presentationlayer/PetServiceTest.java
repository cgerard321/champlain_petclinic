package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.PetService;
import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PetServiceTest {
    @MockBean
    PetRepository repository;

    @Qualifier("petServiceImpl")
    @Autowired
    PetService service;

    // TEST FOR FINDING PET BY ID
    @DisplayName("petService_FindByPetId")
    @Test
    public void test_findByPetId() {
        //Arrange
        Pet petTest = new PetPersistenceTest().setupPet();
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
        Pet newPet = new PetPersistenceTest().setupPet();

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

    // TEST FOR CREATING A PET
    @DisplayName("petService_CreatePet")
    @Test
    public void test_createPet() {
        //Arrange
        Pet petTest = new PetPersistenceTest().setupPet();
        service.CreatePet(petTest);

        //Assert
        verify(repository, times(1)).save(petTest);
    }
}
