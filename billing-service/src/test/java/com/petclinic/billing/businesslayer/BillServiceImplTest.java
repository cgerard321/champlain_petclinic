package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
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
import org.springframework.beans.BeanUtils;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.time.ZoneId;
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
    public void test_getBillById(){
        Bill billEntity = buildBill();


        String BILL_ID = billEntity.getBillId();

        when(repo.findByBillId(anyString())).thenReturn(Mono.just(billEntity));

        Mono<BillResponseDTO> billDTOMono = billService.getBillByBillId(BILL_ID);

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

       Flux<BillResponseDTO> billDTOFlux = billService.GetAllBills();

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
        BillRequestDTO billDTO = buildBillRequestDTO();

        when(repo.insert(any(Bill.class))).thenReturn(billMono);

        Mono<BillResponseDTO> returnedBill = billService.CreateBill(Mono.just(billDTO));

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
    public void test_DeleteBillByVetId(){

        Bill billEntity = buildBill();

        when(repo.deleteBillsByVetId(anyString())).thenReturn(Flux.empty());

        Flux<Void> deletedObj = billService.DeleteBillsByVetId(billEntity.getVetId());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    public void test_DeleteBillsByCustomerId(){
        Bill billEntity = buildBill();
        when(repo.deleteBillsByCustomerId(anyString())).thenReturn(Flux.empty());
        Flux<Void> deletedObj = billService.DeleteBillsByCustomerId(billEntity.getCustomerId());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    public void test_GetBillByCustomerId(){

        Bill billEntity = buildBill();

        String CUSTOMER_ID = billEntity.getCustomerId();

        when(repo.findByCustomerId(anyString())).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOMono = billService.GetBillsByCustomerId(CUSTOMER_ID);

        StepVerifier.create(billDTOMono)
                .consumeNextWith(foundBill -> {
                    assertEquals(billEntity.getBillId(), foundBill.getBillId());
                    assertEquals(billEntity.getAmount(), foundBill.getAmount());
                    assertEquals(billEntity.getVisitType(), foundBill.getVisitType());
                })
                .verifyComplete();
    }

    @Test
    public void test_GetBillByVetId(){

        Bill billEntity = buildBill();

        String VET_ID = billEntity.getVetId();

        when(repo.findByVetId(anyString())).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOMono = billService.GetBillsByVetId(VET_ID);

        StepVerifier.create(billDTOMono)
                .consumeNextWith(foundBill -> {
                    assertEquals(billEntity.getBillId(), foundBill.getBillId());
                    assertEquals(billEntity.getAmount(), foundBill.getAmount());
                    assertEquals(billEntity.getVisitType(), foundBill.getVisitType());
                })
                .verifyComplete();
    }

    @Test
    public void test_UpdateBill() {

        Bill originalBill = buildBill();


        double updatedAmount = 20.0;
        originalBill.setAmount(updatedAmount);


        BillRequestDTO updatedBillRequestDTO = buildBillRequestDTO();
        updatedBillRequestDTO.setAmount(updatedAmount);


        Mono<BillRequestDTO> updatedBillRequestMono = Mono.just(updatedBillRequestDTO);


        when(repo.findByBillId(anyString())).thenReturn(Mono.just(originalBill));
        when(repo.save(any(Bill.class))).thenReturn(Mono.just(originalBill));


        Mono<BillResponseDTO> updatedBillMono = billService.updateBill(originalBill.getBillId(), updatedBillRequestMono);


        StepVerifier.create(updatedBillMono)
                .consumeNextWith(updatedBill -> {
                    assertEquals(originalBill.getBillId(), updatedBill.getBillId());
                    assertEquals(updatedAmount, updatedBill.getAmount());

                })
                .verifyComplete();
    }

    private Bill buildBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();;


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).build();
    }

    private BillDTO buildBillDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();;


        return BillDTO.builder().billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).build();
    }

    private BillRequestDTO buildBillRequestDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();;


        return BillRequestDTO.builder().customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).build();
    }



}

