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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
class PhotoPersistenceTest {

    @Autowired
    private PhotoRepository photoRepository;

    @BeforeEach
    public void setUpDB()
    {
        photoRepository.deleteAll();
    }

    private Photo buildPhoto(){
        return Photo.builder()
                .id(1)
                .name("test photo")
                .type("jpeg")
                .photo("testBytes")
                .build();
    }

    @Test
    public void findById(){

        Photo photo = buildPhoto();
        Photo savedPhoto = photoRepository.save(photo);

        //Act
        Photo foundPhoto = photoRepository.findPhotoById(savedPhoto.getId());

        //Assert
        assert foundPhoto != null;
        assertThat(foundPhoto, samePropertyValuesAs(savedPhoto));
    }

    @Test
    public void findByName(){
        Photo photo = buildPhoto();
        Photo savedPhoto = photoRepository.save(photo);

        //Act
        Photo foundPhoto = photoRepository.findPhotoByName(photo.getName());

        //Assert
        assert foundPhoto != null;
        assertThat(foundPhoto, samePropertyValuesAs(savedPhoto));
    }

    @Test
    public void delete(){
        Photo photo = buildPhoto();
        photoRepository.save(photo);

        //Act
        photoRepository.delete(photo);

        //Assert
        assertFalse(photoRepository.existsById(photo.getId()));
    }

    @Test
    public void save(){
        Photo photo = buildPhoto();
        Photo savedPhoto = photoRepository.save(photo);

        //Act
        Photo foundPhoto = photoRepository.findPhotoById(savedPhoto.getId());

        //Assert
        assert foundPhoto != null;
        assertThat(foundPhoto, samePropertyValuesAs(savedPhoto));
        assertEquals(1,photoRepository.findAll().size());
    }
}