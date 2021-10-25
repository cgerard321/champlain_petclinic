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
}
