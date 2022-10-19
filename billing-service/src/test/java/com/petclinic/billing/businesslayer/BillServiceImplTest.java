package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.exceptions.InvalidInputException;
import com.petclinic.billing.exceptions.NotFoundException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class BillServiceImplTest {

    @MockBean
    BillRepository repo;


    @Autowired
    BillService billService;

    @Test
    public void test_GetBill(){
        Bill billEntity = buildBill();

        String BILL_ID = billEntity.getBillId();

        when(repo.findByBillId(anyString())).thenReturn(Mono.just(billEntity));

        Mono<BillDTO> billDTOMono = billService.GetBill(BILL_ID);

        StepVerifier.create(billDTOMono)
                .consumeNextWith(foundBill -> {
                    assertEquals(billEntity.getBillId(), foundBill.getBillId());
                    assertEquals(billEntity.getAmount(), foundBill.getAmount());
                    assertEquals(billEntity.getVisitType(), foundBill.getVisitType());
                })
                .verifyComplete();


    }

    @Test
    public void test_GetAllBills() {
       Bill billEntity = buildBill();

       when(repo.findAll()).thenReturn(Flux.just(billEntity));

       Flux<BillDTO> billDTOFlux = billService.GetAllBills();

       StepVerifier.create(billDTOFlux)
               .consumeNextWith(foundBill -> {
                   assertNotNull(foundBill);
               })
               .verifyComplete();
    }


    @Test
    public void test_CreateBill(){

        Bill billEntity = buildBill();

        Mono<Bill> billMono = Mono.just(billEntity);
        BillDTO billDTO = buildBillDTO();

        when(repo.insert(any(Bill.class))).thenReturn(billMono);

        Mono<BillDTO> returnedBill = billService.CreateBill(Mono.just(billDTO));

        StepVerifier.create(returnedBill)
                .consumeNextWith(monoDTO -> {
                    assertEquals(billEntity.getCustomerId(), monoDTO.getCustomerId());
                    assertEquals(billEntity.getAmount(), monoDTO.getAmount());
                })
                .verifyComplete();

    }



    @Test
    public void test_DeleteBill(){

        Bill billEntity = buildBill();

        when(repo.deleteBillByBillId(anyString())).thenReturn(Mono.empty());

        Mono<Void> deletedObj = billService.DeleteBill(billEntity.getBillId());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    public void test_DeleteBillsByCustomerId(){
        Bill billEntity = buildBill();
        when(repo.deleteBillsByCustomerId(anyInt())).thenReturn(Flux.empty());

        Flux<Void> deletedObjs = billService.DeleteBillsByCustomerId(billEntity.getCustomerId());

        StepVerifier.create(deletedObjs)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    public void test_GetBillByCustomerId(){

        Bill billEntity = buildBill();

        int CUSTOMER_ID = billEntity.getCustomerId();

        when(repo.findByCustomerId(anyInt())).thenReturn(Flux.just(billEntity));

        Flux<BillDTO> billDTOMono = billService.GetBillsByCustomerId(CUSTOMER_ID);

        StepVerifier.create(billDTOMono)
                .consumeNextWith(foundBill -> {
                    assertEquals(billEntity.getBillId(), foundBill.getBillId());
                    assertEquals(billEntity.getAmount(), foundBill.getAmount());
                    assertEquals(billEntity.getVisitType(), foundBill.getVisitType());
                })
                .verifyComplete();
    }


    private Bill buildBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        Date date = calendar.getTime();


        return Bill.builder().id("Id").billId("BillUUID").customerId(1).visitType("Test Type").visitDate(date).amount(13.37).build();
    }

    private BillDTO buildBillDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        Date date = calendar.getTime();


        return BillDTO.builder().billId("BillUUID").customerId(1).visitType("Test Type").date(date).amount(13.37).build();
    }

}

