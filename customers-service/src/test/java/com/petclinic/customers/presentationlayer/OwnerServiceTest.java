package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.businesslayer.OwnerService;
import com.petclinic.customers.customerExceptions.exceptions.NotFoundException;
import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class OwnerServiceTest {

    @MockBean
    OwnerRepository ownerRepository;

    @Autowired
    OwnerService ownerService;


    @DisplayName("ownerService_FindByOwnerId")
    @Test
    public void test_findByOwnerId()
    {
        //Arrange
        Owner ownerTest = new Owner(1, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        when(ownerRepository.findById(1)).thenReturn(Optional.of(ownerTest));

        //Act
        Optional<Owner> returnedOwnerOpt = ownerService.findByOwnerId(1);
        Owner returnedOwner = returnedOwnerOpt.get();

        //Assert
        assertThat(returnedOwner.getId()).isEqualTo(ownerTest.getId());
    }

    @DisplayName("ownerService_FindByOwnerId_NotFoundException")
    @Test
    public void test_findByOwnerId_NotFoundException()
    {
        //Arrange
        int ownerId = 1;
        when(ownerRepository.findById(ownerId)).thenThrow(new NotFoundException("Owner "+ ownerId +" not found"));

        //Act
        ownerService.findByOwnerId(1);

    }

    @DisplayName("ownerService_FindAll")
    @Test
    public void test_findAll()
    {
        //Arrange
        int expectedLength = 4;
        List<Owner> ownerList = new ArrayList<>();
        ownerList.add(new Owner(1, "firstname1", "lastname1", "address1","city1","1111111111"));
        ownerList.add(new Owner(2, "firstname2", "lastname2", "address2","city2","2222222222"));
        ownerList.add(new Owner(3, "firstname3", "lastname3", "address3","city3","3333333333"));
        ownerList.add(new Owner(4, "firstname4", "lastname4", "address4","city4","4444444444"));
        when(ownerRepository.findAll()).thenReturn(ownerList);

        //Act
        List<Owner> returnedList = ownerService.findAll();

        //Assert
        assertThat(expectedLength).isEqualTo(returnedList.size());

    }

    /**
     * ------------------------ TEST_DELETE ------------------------
     * Testing the method deleteOwner()
     */
    @DisplayName("ownerService_DeleteOwner")
    @Test
    public void test_deleteOwner()
    {

        //Arrange
        int OwnerId = 1;
        Owner ownerTest = new Owner(OwnerId, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        when(ownerRepository.findById(OwnerId)).thenReturn(Optional.of(ownerTest));

        //Act
        ownerService.deleteOwner(OwnerId);

        //Assert
        verify(ownerRepository, times(1)).delete(ownerTest);

    }
    /**
     * ------------------------ TEST_CREATE ------------------------
     * Testing the method createOwner()
     */
    @DisplayName("ownerService_CreateOwner")
    @Test
    public void test_CreateOwner()
    {
        //Arrange
        int OwnerId = 1;
        Owner ownerTest = new Owner(OwnerId, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        when(ownerRepository.findById(OwnerId)).thenReturn(Optional.of(ownerTest));

        //Act
        ownerService.createOwner(ownerTest);
        Optional<Owner> retrievedOwner = ownerRepository.findById(OwnerId);

        //Assert
        MatcherAssert.assertThat(retrievedOwner.get(), samePropertyValuesAs(ownerTest));
    }

    @DisplayName("ownerService_CreateOwner")
    @Test
    public void test_UpdateOwner()
    {
        //Arrange
        int OwnerId = 1;
        Owner newOwner1 = new Owner(OwnerId, "Michel", "Lebrie", "56 Yeet St.", "Longueuil", "1234567890");
        when(ownerRepository.findById(OwnerId)).thenReturn(Optional.of(newOwner1));

        //Act
        ownerService.updateOwner(1, newOwner1);

        assertEquals(ownerService.findByOwnerId(1).get().getFirstName(), "Michel");
        assertEquals(ownerService.findByOwnerId(1).get().getLastName(), "Lebrie");
        assertEquals(ownerService.findByOwnerId(1).get().getAddress(), "56 Yeet St.");
        assertEquals(ownerService.findByOwnerId(1).get().getCity(), "Longueuil");
        assertEquals(ownerService.findByOwnerId(1).get().getTelephone(), "1234567890");
    }




}
