package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.Pet;
import com.petclinic.customers.datalayer.PetType;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class PetDetailsTests {

    //Located in PresentationLayer

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

    @Test
    public void test_PetDetails_CanEqual_Empty()
    {
        //ARRANGE - ACT
        PetDetails pd = new PetDetails();

        //ASSERT
        assertTrue(pd.canEqual(pd));
    }

    @Test
    public void test_PetDetails_NotEquals() throws ParseException {
        //ARRANGE - ACT
        Owner owner = setupOwner();

        //PetType
        PetType petType = new PetType();
        petType.setId(1);

        //Pet
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Marcus");
        pet.setOwner(owner);
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );
        pet.setBirthDate(date);
        pet.setType(petType);

        //ACT
        PetDetails pd1 = new PetDetails(pet);
        PetDetails pd2 = new PetDetails();
        pd2.setId(2);

        //ASSERT
        assertNotEquals(pd1,pd2);
    }



    @Test
    public void test_PetDetails_HashCode_NotEmpty() throws ParseException {
        //ARRANGE
        //Owner
        Owner owner = setupOwner();

        //PetType
        PetType petType = new PetType();
        petType.setId(1);

        //Pet
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Marcus");
        pet.setOwner(owner);
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );
        pet.setBirthDate(date);
        pet.setType(petType);

        //ACT
        PetDetails pd1 = new PetDetails(pet);
        PetDetails pd2 = new PetDetails(pet);

        //ASSERT
        assertTrue(pd1.equals(pd2) && pd1.equals(pd2));
        assertTrue(pd1.hashCode() == pd2.hashCode());
    }

    @Test
    public void test_PetDetails_HashCode_Empty()  {
        //ARRANGE - ACT
        PetDetails pd1 = new PetDetails();
        PetDetails pd2 = new PetDetails();

        //ASSERT
        assertTrue(pd1.equals(pd2) && pd1.equals(pd2));
        assertTrue(pd1.hashCode() == pd2.hashCode());
    }


    @Test
    public void testToString()
    {
        //ARRANGE - ACT
        PetDetails pd = new PetDetails();
        String expected = "PetDetails(id=0, name=null, owner=null, birthDate=null, type=null)";

        //ASSERT
        assertEquals(expected, pd.toString());
    }

    @Test
    public void test_set_petDetails() throws ParseException {

        //ARRANGE

        //Owner
        Owner owner = setupOwner();
        String ownerToString = owner.getFirstName() + " " + owner.getLastName();

        //PetType
        PetType petType = new PetType();
        petType.setId(1);

        //Pet
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Marcus");
        pet.setOwner(owner);
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );
        pet.setBirthDate(date);
        pet.setType(petType);

        //ACT
        PetDetails pd = new PetDetails(pet);

        //ASSERT
        assertEquals(1, pd.getId());
        assertEquals("Marcus", pd.getName());
        assertEquals("John Wick", pd.getOwner());
        assertEquals(date, pd.getBirthDate());
        assertEquals(petType, pd.getType());
    }

    @Test
    public void test_PetDetails_set_Id()
    {
        //ARRANGE
        PetDetails pd = new PetDetails();
        long test_id = 100;

        //ACT
        pd.setId(test_id);

        //ASSERT
        assertEquals(test_id, pd.getId());
    }

    @Test
    public void test_PetDetails_set_Name()
    {
        //ARRANGE
        PetDetails pd = new PetDetails();
        String pd_test_name = "Marcus";

        //ACT
        pd.setName(pd_test_name);

        //ASSERT
        assertEquals(pd_test_name, pd.getName());
    }

    @Test
    public void test_PetDetails_set_Owner()
    {
        //ARRANGE
        PetDetails pd = new PetDetails();

        //Owner
        Owner owner = setupOwner();
        String ownerToString = owner.getFirstName() + " " + owner.getLastName();

        //ACT
        pd.setOwner(ownerToString);

        //ASSERT
        assertEquals(ownerToString, pd.getOwner());
    }

    @Test
    public void test_set_Date() throws ParseException {
        //ARRANGE
        PetDetails pd = new PetDetails();
        Date date = new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" );

        //ACT
        pd.setBirthDate(date);

        //ASSERT
        assertEquals(date, pd.getBirthDate());
    }

    @Test
    public void test_set_Type()
    {
        //ARRANGE
        PetDetails pd = new PetDetails();

        //PetType
        PetType petType = new PetType();
        petType.setId(1);

        //ACT
        pd.setType(petType);

        //ASSERT
        assertEquals(petType, pd.getType());
    }

}
