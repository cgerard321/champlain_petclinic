package com.petclinic.customers.datalayer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PhotoTest {

    @Test
    public void test_setPhoto()
    {
        //Arrange
        Photo photo = new Photo(1,"photoName","jpeg","testPhoto");

        String expected = "ID: 1, Name: photoName, Type: jpeg, Image: testPhoto";

        //Act
        String result = photo.toString();

        //Assert
        assertEquals(expected, result);
    }
}