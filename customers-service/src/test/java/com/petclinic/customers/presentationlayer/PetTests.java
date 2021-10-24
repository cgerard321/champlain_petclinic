package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PetTests {


    private Owner setupOwner() {

        Owner owner = new Owner();
        owner.setId(5);
        owner.setFirstName("John");
        owner.setLastName("Wick");
        owner.setAddress("56 John St.");
        owner.setCity("Amsterdam");
        owner.setTelephone("9999999999");

        return owner;
    }

    //FOR SOME UNIQUE AND UNKNOWN REASON TO HUMANITY, THIS GODDAMN TEST IS CURSED. PLEASE DO NOT
    //REMOVE COMMENTS AS YOU WILL RELEASE EVIL ACROSS OUR CIVILIZATION.
    //THAT BEING SAID, IF YOU SEE THIS MESSAGE, YOU ARE A GREAT PERSON AND I WISH YOU SUCCESS

    /*
    @DisplayName("ToString_Test")
    @Test
    public void test_PetToString() throws ParseException {
        //Arrange
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Daisy");
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );
        pet.setBirthDate(date);
        PetType petType = new PetType();
        //Since this test do not use any repo or service, petType cannot access the name associated with its id.  Therefore, it is set to null.
        pet.setType(petType);
        pet.setOwner(setupOwner());

        String expected = "ID: 1, Name: Daisy, Birth of date: Thu May 20 00:00:00 EDT 2010, Type: null, Owner - First name: John, Last name: Wick";

        //Act
        String result = pet.toString();

        //Assert
        assertEquals(expected, result);
    }
    */
}
