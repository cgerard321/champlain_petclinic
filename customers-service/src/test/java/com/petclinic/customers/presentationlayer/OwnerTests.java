package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OwnerTests {

    @DisplayName("ToString_Test")
    @Test
    public void test_setOwner()
    {
        //Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setTelephone("1234567890");
        owner.setFirstName("John");
        owner.setLastName("Smith");
        owner.setCity("MTL");
        owner.setAddress("9 rue des oiseaux");

        String expected = "ID: 1, First Name: John, Last Name: Smith, Address: 9 rue des oiseaux, City: MTL, Telephone: 1234567890";

        //Act
        String result = owner.toString();

        //Assert
        assertEquals(expected, result);
    }
}

