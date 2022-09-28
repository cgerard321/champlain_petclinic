package com.petclinic.customers.presentationlayer;

import com.petclinic.customers.datalayer.Owner;
import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.datalayer.Photo;
import com.petclinic.customers.datalayer.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class PhotoTests {

    @Autowired
    PhotoRepository photoRepository;

    @Autowired
    OwnerRepository ownerRepository;

    private final String test = "Test byte";
    private final byte[] testBytes = test.getBytes();

    @Test
    void setPhoto(){

        Owner owner = new Owner();
        owner.setId(1);
        owner.setTelephone("1234567890");
        owner.setFirstName("John");
        owner.setLastName("Smith");
        owner.setCity("MTL");
        owner.setAddress("9 rue des oiseaux");
        owner.setImageId(1);

        Photo photo = new Photo();
        photo.setId(1);
        photo.setName("Test photo");
        photo.setType("jpeg");
        photo.setPhoto(testBytes);


        assertEquals(photo.getId(),1);
        assertEquals(photo.getName(),"Test photo");
        assertEquals(photo.getType(),"jpeg");
        assertEquals(photo.getPhoto(),testBytes);

    }


}