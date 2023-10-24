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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.beans.BeanUtils;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.time.Month;
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

//    @Test
//    public void test_GetAllBills() {
//       Bill billEntity = buildBill();
//
//       when(repo.findAll()).thenReturn(Flux.just(billEntity));
//
//       Flux<BillResponseDTO> billDTOFlux = billService.GetAllBills();
//
//       StepVerifier.create(billDTOFlux)
//               .consumeNextWith(foundBill -> {
//                   assertNotNull(foundBill);
//               })
//               .verifyComplete();
//    }

    @Test
    void getBillsByPage_ShouldSucceed(){
        // Create a sample Pageable object
        Pageable pageable = PageRequest.of(0, 5);

        // Create a sample list of Bill entities
        List<Bill> billList = Arrays.asList(buildBill(), buildBill(), buildBill());

        // Mock the repository call to return the list of Bill entities
        when(repo.findAll()).thenReturn(Flux.fromIterable(billList));

        // Test the service method
        Flux<BillResponseDTO> billDTOFlux = billService.getAllBillsByPage(pageable);

        // Verify the results
        StepVerifier.create(billDTOFlux)
                .expectNextCount(3) // Adjust this count according to the number of expected results
                .verifyComplete();
    }

    @Test
    public void test_GetAllBillsByPaidStatus() {
        BillStatus status = BillStatus.PAID; // Change this to the desired status

        Bill billEntity = buildBill(); // Create a sample bill entity
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.GetAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1) // Adjust this count according to the number of expected results
                .verifyComplete();
    }

    @Test
    public void test_GetAllBillsByUnpaidStatus() {
        BillStatus status = BillStatus.UNPAID; // Change this to the desired status

        Bill billEntity = buildUnpaidBill(); // Create a sample bill entity
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.GetAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1) // Adjust this count according to the number of expected results
                .verifyComplete();
    }

    @Test
    public void test_GetAllBillsByOverdueStatus() {
        BillStatus status = BillStatus.OVERDUE; // Change this to the desired status

        Bill billEntity = buildOverdueBill(); // Create a sample bill entity
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.GetAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1) // Adjust this count according to the number of expected results
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
    public void test_DeleteAllBills(){
        
        when(repo.deleteAll()).thenReturn(Mono.empty());

        Mono<Void> deleteObj = billService.DeleteAllBills();

        StepVerifier.create(deleteObj)
                .expectNextCount(0)
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

    @Test
    public void test_getBillByNonExistentBillId() {
        String nonExistentBillId = "nonExistentId";

        when(repo.findByBillId(nonExistentBillId)).thenReturn(Mono.empty());

        Mono<BillResponseDTO> billDTOMono = billService.getBillByBillId(nonExistentBillId);

        StepVerifier.create(billDTOMono)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_updateNonExistentBillId() {
        String nonExistentBillId = "nonExistentId";
        double updatedAmount = 20.0;
        BillRequestDTO updatedBillRequestDTO = buildBillRequestDTO();
        updatedBillRequestDTO.setAmount(updatedAmount);
        Mono<BillRequestDTO> updatedBillRequestMono = Mono.just(updatedBillRequestDTO);

        when(repo.findByBillId(nonExistentBillId)).thenReturn(Mono.empty());

        Mono<BillResponseDTO> updatedBillMono = billService.updateBill(nonExistentBillId, updatedBillRequestMono);

        StepVerifier.create(updatedBillMono)
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    public void test_deleteNonExistentBillId() {
        String nonExistentBillId = "nonExistentId";

        when(repo.deleteBillByBillId(nonExistentBillId)).thenReturn(Mono.empty());

        Mono<Void> deletedObj = billService.DeleteBill(nonExistentBillId);

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_updateBillWithInvalidRequest() {
        String billId = "validBillId";
        double updatedAmount = -5.0; // Negative amount, which is invalid
        BillRequestDTO updatedBillRequestDTO = buildBillRequestDTO();
        updatedBillRequestDTO.setAmount(updatedAmount);
        Mono<BillRequestDTO> updatedBillRequestMono = Mono.just(updatedBillRequestDTO);

        when(repo.findByBillId(billId)).thenReturn(Mono.just(buildBill()));

        Mono<BillResponseDTO> updatedBillMono = billService.updateBill(billId, updatedBillRequestMono);

        StepVerifier.create(updatedBillMono)
                .expectError()
                .verify();
    }


    @Test
    public void test_GetBillByNonExistentCustomerId() {
        String nonExistentCustomerId = "nonExistentId";


        when(repo.findByCustomerId(nonExistentCustomerId)).thenReturn(Flux.empty());

        Flux<BillResponseDTO> billDTOMono = billService.GetBillsByCustomerId(nonExistentCustomerId);

        StepVerifier.create(billDTOMono)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_CreateBillWithInvalidData() {
        BillRequestDTO billDTO = buildInvalidBillRequestDTO(); // Create a BillRequestDTO with invalid data

        Mono<BillRequestDTO> billRequestMono = Mono.just(billDTO);

        when(repo.insert(any(Bill.class))).thenReturn(Mono.error(new RuntimeException("Invalid data")));

        Mono<BillResponseDTO> returnedBill = billService.CreateBill(billRequestMono);

        StepVerifier.create(returnedBill)
                .expectError()
                .verify();
    }

    private BillRequestDTO buildInvalidBillRequestDTO() {
        LocalDate date = LocalDate.now();

        return BillRequestDTO.builder()
                .customerId("1")
                .vetId("2")
                .visitType("") // Empty visitType, which is considered invalid
                .date(date)
                .amount(100.0)
                .billStatus(BillStatus.PAID)
                .dueDate(date)
                .build();
    }

    private Bill buildBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022,Month.OCTOBER,15);


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.PAID).dueDate(dueDate).build();
    }

    private Bill buildUnpaidBill(){

        VetResponseDTO vetDTO = buildVetDTO();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 5);


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.UNPAID).dueDate(dueDate).build();

    }

    private Bill buildOverdueBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);


        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.OVERDUE).dueDate(dueDate).build();
    }



    private BillRequestDTO buildBillRequestDTO(){



        VetResponseDTO vetDTO = buildVetDTO();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate =LocalDate.of(2022, Month.OCTOBER, 10);



        return BillRequestDTO.builder().customerId("1").vetId("1").visitType("Test Type").date(date).amount(13.37).billStatus(BillStatus.PAID).dueDate(dueDate).build();

    }

    private VetResponseDTO buildVetDTO() {
        return VetResponseDTO.builder()
                .vetId("d9d3a7ac-6817-4c13-9a09-c09da74fb65f")
                .vetBillId("53c2d16e-1ba3-4dbc-8e31-6decd2eaa99a")
                .firstName("Pauline")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .specialties(new HashSet<>())
                .active(false)
                .build();

    }



}

