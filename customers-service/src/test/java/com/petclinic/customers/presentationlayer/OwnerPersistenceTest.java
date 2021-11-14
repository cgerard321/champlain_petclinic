package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class OwnerPersistenceTest {

    @Autowired
    private OwnerRepository repository;

    @BeforeEach
    public void setUpDB()
    {
        repository.deleteAll();
    }

    @DisplayName("ownerPersistence_FindOwner")
    @Test
    public void findById()
    {
        //Set ID
        int OwnerID = 1;

        //Arrange
        Owner newOwner = new Owner(OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        Owner savedOwner = repository.save(newOwner);

        //Act
        Owner foundOwner = repository.findById(savedOwner.getId()).orElse(null);

        //Assert
        assert foundOwner != null;
        assertThat(foundOwner, samePropertyValuesAs(savedOwner));
    }

    @DisplayName("ownerPersistence_FindAllOwner")
    @Test
    public void findAll()
    {
        //Expect 4 entities
        int expectedLength = 4;

        //Arrange
        Owner owner1 = new Owner (1, "Brian1", "Smith1", "940 Rue des Oiseaux", "Montreal", "1111111111");
        Owner savedOwner1 = repository.save(owner1);
        Owner foundOwner1 = repository.findById(savedOwner1.getId()).orElse(null);
        assert foundOwner1 != null;
        assertThat(foundOwner1, samePropertyValuesAs(savedOwner1));

        Owner owner2 = new Owner (2, "Brian2", "Smith2", "940 Rue des Oiseaux", "Montreal", "1111111111");
        Owner savedOwner2 = repository.save(owner2);
        Owner foundOwner2 = repository.findById(savedOwner2.getId()).orElse(null);
        assert foundOwner2 != null;
        assertThat(foundOwner2, samePropertyValuesAs(savedOwner2));


        Owner owner3 = new Owner (3, "Brian3", "Smith3", "940 Rue des Oiseaux", "Montreal", "1111111111");
        Owner savedOwner3 = repository.save(owner3);
        Owner foundOwner3 = repository.findById(savedOwner3.getId()).orElse(null);
        assert foundOwner3 != null;
        assertThat(foundOwner3, samePropertyValuesAs(savedOwner3));

        Owner owner4 = new Owner (4, "Brian4", "Smith4", "940 Rue des Oiseaux", "Montreal", "1111111111");
        Owner savedOwner4 = repository.save(owner4);
        Owner foundOwner4 = repository.findById(savedOwner4.getId()).orElse(null);
        assert foundOwner4 != null;
        assertThat(foundOwner4, samePropertyValuesAs(savedOwner4));

        //Act
        List<Owner> ownerList = repository.findAll();

        //Assert
        assertEquals(expectedLength, ownerList.size());
    }

    @DisplayName("ownerPersistence_DeleteOwner")
    @Test
    public void deleteOwner()
    {
        //Arrange
        int OwnerID = 1;
        Owner newOwner = new Owner (OwnerID, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        Owner savedOwner = repository.save(newOwner);

        //Act
        repository.deleteById(savedOwner.getId());

        //Assert
        assertFalse(repository.existsById(savedOwner.getId()));

    }

    @DisplayName("ownerPersistence_CreateOwner")
    @Test
    public void create_owner_test()
    {
        //Arrange
        int OwnerId = 1;
        Owner newOwner = new Owner (OwnerId, "Brian", "Smith", "940 Rue des Oiseaux", "Montreal", "1111111111");
        Owner savedOwner = repository.save(newOwner);;

        //Act
        Owner foundSaved = repository.findById(savedOwner.getId()).orElse(null);

        //Assert
        assert foundSaved != null;
        assertThat(foundSaved, samePropertyValuesAs(savedOwner));
        assertEquals(1,repository.findAll().size());
    }

    @DisplayName("ownerPersistence_UpdateOwner")
    @Test
    public void update_owner_test()
    {
        // Arrange
        int ownerId = 11;
        Owner newOwner = new Owner (ownerId,
                "Brian",
                "Smith",
                "940 Rue des Oiseaux",
                "Montreal",
                "1111111111");
        Owner savedOwner = repository.save(newOwner);;
        Owner foundSaved = repository.findById(savedOwner.getId()).orElse(null);
        assert foundSaved != null;
        assertThat(foundSaved, samePropertyValuesAs(newOwner));

        // Act
        foundSaved.setFirstName("Kevin");
        foundSaved.setLastName("Laplace");
        foundSaved.setAddress("670 Fort Kerton");
        foundSaved.setCity("Montreal");
        foundSaved.setTelephone("2222222222");
        Owner savedUpdate = repository.save(foundSaved);

        // Assert
        assertEquals(savedUpdate, foundSaved);
    }

}
