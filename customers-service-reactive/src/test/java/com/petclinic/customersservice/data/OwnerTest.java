package com.petclinic.customersservice.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OwnerTest {

    @Test
    void test_setOwner() {
        //Arrange
        Owner owner = new Owner();
        owner.setId("1");
        owner.setOwnerId("ownerId-123");
        owner.setTelephone("1234567890");
        owner.setFirstName("John");
        owner.setLastName("Smith");
        owner.setCity("MTL");
        owner.setAddress("9 rue des oiseaux");
        //owner.setPhotoId("0");

        String expected = "Owner(id=1, ownerId=ownerId-123, firstName=John, lastName=Smith, address=9 rue des oiseaux, city=MTL, telephone=1234567890, pets=null)";

        //Act
        String result = owner.toString();

        //Assert
        assertEquals(expected, result);
    }

}
