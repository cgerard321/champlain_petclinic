package com.petclinic.customers.datalayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class PhotoPersistenceTest {

    @Autowired
    private PhotoRepository photoRepository;

//    @BeforeEach
//    public void setUpDB()
//    {
//        photoRepository.deleteAll();
//    }

    @Test
    public void findById(){
//        int photoId = 1;
//        final String test = "Test photo";
//        final byte[] testBytes = test.getBytes();
//        //Arrange
//        Photo photo = new Photo(1,"image","jpeg",testBytes);
//        Photo savedPhoto = photoRepository.save(photo);
//
//
//        //Act
//        Photo foundPhoto = photoRepository.findPhotoById(savedPhoto.getId());
//
//        //Assert
//        assert foundPhoto != null;
//        assertThat(foundPhoto, samePropertyValuesAs(savedPhoto));
    }

    @Test
    public void findByName(){}

    @Test
    public void deleteById(){}

    @Test
    public void save(){}
}