package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetType;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PetRequestTests {


    @Test
    public void test_PetDetails_CanEqual_Empty()
    {
        //ARRANGE - ACT
        PetRequest pr = new PetRequest();

        //ASSERT
        assertTrue(pr.canEqual(pr));
    }

    @Test
    public void test_PetDetails_HashCode_NotEmpty() throws ParseException {
        //ARRANGE
        String petName = "Marcus";
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );

        //ACT
        PetRequest pr1 = new PetRequest(petName, date);
        PetRequest pr2 = new PetRequest(petName, date);

        //ASSERT
        assertTrue(pr1.equals(pr2) && pr1.equals(pr2));
        assertTrue(pr1.hashCode() == pr2.hashCode());
    }

    @Test
    public void test_PetDetails_HashCode_Empty() {
        //ARRANGE - ACT
        PetRequest pr1 = new PetRequest();
        PetRequest pr2 = new PetRequest();

        //ASSERT
        assertTrue(pr1.equals(pr2) && pr1.equals(pr2));
        assertTrue(pr1.hashCode() == pr2.hashCode());
    }

    @Test
    public void testToString()
    {
        //ARRANGE - ACT
        PetRequest pr = new PetRequest();
        String expected = "PetRequest(id=0, birthDate=null, name=null, typeId=0)";

        //ASSERT
        assertEquals(expected, pr.toString());
    }

    @Test
    public void test_PetRequest_set_PetRequest() throws ParseException {
        //Arrange
        String petName = "Marcus";
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );

        //Act
        PetRequest pr = new PetRequest(petName, date);

        //Assert
        assertEquals(petName, pr.getName());
        assertEquals(date, pr.getBirthDate());
    }

    @Test
    public void test_PetRequest_set_Id() {
        //Arrange
        int petId = 100;
        PetRequest pr = new PetRequest();

        //Act
        pr.setId(petId);

        //Assert
        assertEquals(petId, pr.getId());
    }
    @Test
    public void test_PetRequest_set_Name() {
        //Arrange
        String petName = "Marcus";
        PetRequest pr = new PetRequest();

        //Act
        pr.setName(petName);

        //Assert
        assertEquals(petName, pr.getName());
    }
    @Test
    public void test_PetRequest_set_Date() throws ParseException {
        //Arrange
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );
        PetRequest pr = new PetRequest();

        //Act
        pr.setBirthDate(date);

        //Assert
        assertEquals(date, pr.getBirthDate());
    }
    @Test
    public void test_PetRequest_set_PetType() {
        //Arrange
        //PetType
        PetType petType = new PetType();
        petType.setId(1);
        PetRequest pr = new PetRequest();

        //Act
        pr.setTypeId(petType.getId());

        //Assert
        assertEquals(petType.getId(), pr.getTypeId());
    }
}
