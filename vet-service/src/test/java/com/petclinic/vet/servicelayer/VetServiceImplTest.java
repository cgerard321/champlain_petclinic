package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.VetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class VetServiceImplTest {

//    @Autowired
//    private VetService vetService;
//
//    @MockBean
//    private VetRepository vetRepository;


    @Test
    void getAll() {
    }

    @Test
    void insertVet() {
    }

    @Test
    void updateVet() {
    }

    @Test
    void getVetByVetId() {
    }

    @Test
    void getVetByIsActive() {
    }

    @Test
    void deleteVet() {
    }
}