package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PhotoServiceTest {

//    @MockBean
//    PhotoRepository photoRepository;
//    @MockBean
//    VetRepository vetRepository;
//    @Autowired
//    PhotoService photoService;
//
//    @Autowired
//    VetService vetService;
//
//    VetDTO vetDTO = buildVetDTO();
//    Vet vet = buildVet();
//    String VET_ID = vet.getVetId();
//
//    @Test
//    void insertPhoto() {
//    }
//
//    @Test
//    void getPhotoByPhotoId() {
//    }
//
//    @Test
//    void deletePhoto() {
//    }
//
//    private Photo buildPhoto(){
//        return Photo.builder()
//                .id("2")
//                .photoName("test photo")
//                .type("jpeg")
//                .photo("testPhoto")
//                .build();
//    }
////
//    @Test
//    void setVetPhoto() {
//        Vet vet = buildVet();
//        Photo photo = buildPhoto();
//
//        when(vetRepository.findVetByVetId("1")).thenReturn(vet);
//        when(photoRepository.findPhotoByName(photo.getPhotoName())).thenReturn(photo);
//
//        vetService.insertVet(vet);
////        photoService.setVetPhoto(photo,vet.getId());
//
//        final String photoResult = photoService.setVetPhoto(photo,vet.getId());
//        assertEquals("Image uploaded successfully: " + photo.getPhotoName(), photoResult);
//
//    }


//    @Test
//    void getVetPhoto() {
//
//        Vet vet = buildVet();
//        Photo photo = buildPhoto();
//
//        when(vetRepository.findVetByVetId(vet.getId())).thenReturn(vet);
//        when(photoRepository.findPhotoById(vet.getImageId())).thenReturn(photo);
//
//        Photo returnedPhoto = photoService.getVetPhoto(1);
//
//        assertThat(returnedPhoto.getPhotoName()).isEqualTo(photo.getPhotoName());
//    }


    private Vet buildVet() {
        return Vet.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("")
                .workday("Monday")
                .specialties(new HashSet<>())
                .isActive(false)
                .build();
    }
    private VetDTO buildVetDTO() {
        return VetDTO.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("kjd")
                .workday("Monday")
                .specialties(new HashSet<>())
                .isActive(false)
                .build();
    }
    private Vet buildVet2() {
        return Vet.builder()
                .vetId("678910")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .imageId("")
                .workday("Monday")
                .specialties(new HashSet<>())
                .isActive(true)
                .build();
    }
}