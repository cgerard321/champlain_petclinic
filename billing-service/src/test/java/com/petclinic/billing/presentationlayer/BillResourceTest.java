package com.petclinic.billing.presentationlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.exceptions.InvalidInputException;
import com.petclinic.billing.exceptions.NotFoundException;
import com.petclinic.billing.http.BillControllerExceptionHandler;
import com.petclinic.billing.http.HttpErrorInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillResource.class)
@ExtendWith(SpringExtension.class)
class BillResourceTest {

    @MockBean
    BillService service;

    @MockBean
    BillRepository repository;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BillControllerExceptionHandler exceptionHandler;

    @Test
    void createBillNotFound() throws Exception {
        BillDTO newDTO = new BillDTO(1, 1, "type1", new Date(), 1.0);

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
    void createBillInvalidInputNegativeNumber() throws Exception {
        BillDTO newDTO = new BillDTO(-1, 1, "type1", new Date(), 1.0);

        when(service.CreateBill(any())).thenThrow(new InvalidInputException("That bill id does not exist"));

        mvc.perform(post("/bills", 1)
                        .content(objectMapper.writeValueAsString(newDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
                .andExpect(result -> assertEquals("That bill id does not exist", result.getResolvedException().getMessage()));
    }

    @Test
    void createBillInvalidInputZero() throws Exception {
        BillDTO newDTO = new BillDTO(0, 1, "type1", new Date(), 1.0);

        when(service.CreateBill(any())).thenThrow(new InvalidInputException("That bill id does not exist"));

        mvc.perform(post("/bills", 1)
                        .content(objectMapper.writeValueAsString(newDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
                .andExpect(result -> assertEquals("That bill id does not exist", result.getResolvedException().getMessage()));
    }

    @Test
    void findBill() {

    }

    @Test
    void deleteBill() {

    }

    @Test
    void test_FindAllBills() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, 9, 21);
        Date date = calendar.getTime();

        Bill bill = new Bill(1, 1, "general", date, 59.99);

        given(repository.findAll()).willReturn(asList(bill));

        mvc.perform(get("/bills").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testEmptyHttpErrorInfo() {
        HttpErrorInfo httpErrorInfo = new HttpErrorInfo();

        assertEquals(httpErrorInfo.getHttpStatus(), null);
        assertEquals(httpErrorInfo.getPath(), null);
        assertEquals(httpErrorInfo.getTimestamp(), null);
        assertEquals(httpErrorInfo.getMessage(), null);
    }

    @Test
    void testHttpErrorInfoConstructor() {
        HttpErrorInfo httpErrorInfo = new HttpErrorInfo("timestamp1", HttpStatus.NOT_FOUND, "Bill not found");
    }

    @Test
    void testInvalidInputExceptionHandler() throws JsonProcessingException {
        BillDTO newDTO = new BillDTO(1, 1, "type1", new Date(), 1.0);

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleInvalidInputException(MockServerHttpRequest.post("/bills", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(newDTO)), new InvalidInputException("That bill id does not exist"));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.UNPROCESSABLE_ENTITY);
        assertEquals(httpErrorInfo.getPath(), "/bills");
        assertEquals(httpErrorInfo.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
    }

    @Test
    void testNotFoundExceptionHandler() throws JsonProcessingException {
        BillDTO newDTO = new BillDTO(1, 1, "type1", new Date(), 1.0);

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleNotFoundException(MockServerHttpRequest.post("/bills", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(newDTO)), new NotFoundException("Bill not found"));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.NOT_FOUND);
        assertEquals(httpErrorInfo.getMessage(), "Bill not found");
    }
}