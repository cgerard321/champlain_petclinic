package com.petclinic.customers.businesslayer;

import com.petclinic.customers.datalayer.OwnerRepository;
import com.petclinic.customers.datalayer.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PhotoServiceTest {

    @MockBean
    PhotoRepository photoRepository;

    @Autowired
    PhotoService photoService;

    @Test
    void setPhotoOwner() {
    }

    @Test
    void setPhotoPet() {
    }

    @Test
    void getPhotoOwner() {
    }

    @Test
    void getPhotoPet() {
    }

    @Test
    void deletePhoto() {
    }
}