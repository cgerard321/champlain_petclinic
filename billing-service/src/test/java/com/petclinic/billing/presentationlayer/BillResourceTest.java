package com.petclinic.billing.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillResource.class)
@ExtendWith(SpringExtension.class)
class BillResourceTest {

    @MockBean
    BillService service;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createBillNotFound() throws Exception {
        BillDTO newDTO = new BillDTO();

        when(service.CreateBill(any())).thenThrow(new NotFoundException("Bill not found"));

        mvc.perform(post("/bills", 65)
                .content(objectMapper.writeValueAsString(newDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8")
                .accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
                .andExpect(result -> assertEquals("Bill not found", result.getResolvedException().getMessage()));
    }

    @Test
    void findBill() {

    }

    @Test
    void deleteBill() {

    }
}