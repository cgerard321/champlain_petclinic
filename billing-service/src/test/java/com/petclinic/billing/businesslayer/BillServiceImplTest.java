package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
//       Flux<BillResponseDTO> billDTOFlux = billService.getAllBills();
//
//       StepVerifier.create(billDTOFlux)
//               .consumeNextWith(foundBill -> {
//                   assertNotNull(foundBill);
//               })
//               .verifyComplete();
//    }

    @Test
    void getBillsByPage_ShouldSucceed(){

        Bill bill1 = Bill.builder()
                .billId("billId-1")
                .customerId("customerId-1")
                .ownerFirstName("ownerFirstName1")
                .ownerLastName("ownerLastName1")
                .visitType("operation")
                .vetId("vetId1")
                .vetFirstName("vetFirstName1")
                .vetLastName("vetLastName1")
                .date(LocalDate.of(2024,10,1))
                .dueDate(LocalDate.of(2024,10,30))
                .build();
        Bill bill2 = Bill.builder()
                .billId("billId-2")
                .customerId("customerId-2")
                .ownerFirstName("ownerFirstName2")
                .ownerLastName("ownerLastName2")
                .visitType("general")
                .vetId("vetId2")
                .vetFirstName("vetFirstName2")
                .vetLastName("vetLastName2")
                .date(LocalDate.of(2024,10,1))
                .dueDate(LocalDate.of(2024,10,30))
                .build();
        Bill bill3 = Bill.builder()
                .billId("billId-3")
                .customerId("customerId-3")
                .ownerFirstName("ownerFirstName3")
                .ownerLastName("ownerLastName3")
                .visitType("injury")
                .vetId("vetId3")
                .vetFirstName("vetFirstName3")
                .vetLastName("vetLastName3")
                .date(LocalDate.of(2024,10,1))
                .dueDate(LocalDate.of(2024,10,30))
                .build();

        Pageable pageable = PageRequest.of(0, 2);

        // Mock the repository to return a Flux of owners
        when(repo.findAll()).thenReturn(Flux.just(bill1, bill2, bill3));

        // Call the method under test
        Flux<BillResponseDTO> bills = billService.getAllBillsByPage(pageable,null,null,
                null,null,null, null, null, null);

        // Verify the behavior using StepVerifier
        StepVerifier.create(bills)
                .expectNextMatches(billDto1 -> billDto1.getBillId().equals(bill1.getBillId()))
                .expectNextMatches(billDto2 -> billDto2.getBillId().equals(bill2.getBillId()))
                .expectComplete()
                .verify();

    }

    @Test
    public void test_getAllBillsByPaidStatus() {
        BillStatus status = BillStatus.PAID; // Change this to the desired status

        Bill billEntity = buildBill(); // Create a sample bill entity
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.getAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1) // Adjust this count according to the number of expected results
                .verifyComplete();
    }

    @Test
    public void test_getAllBillsByUnpaidStatus() {
        BillStatus status = BillStatus.UNPAID; // Change this to the desired status

        Bill billEntity = buildUnpaidBill(); // Create a sample bill entity
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.getAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1) // Adjust this count according to the number of expected results
                .verifyComplete();
    }

    @Test
    public void test_getAllBillsByOverdueStatus() {
        BillStatus status = BillStatus.OVERDUE; // Change this to the desired status

        Bill billEntity = buildOverdueBill(); // Create a sample bill entity
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.getAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1) // Adjust this count according to the number of expected results
                .verifyComplete();
    }


    @Test
    public void test_createBill(){

        Bill billEntity = buildBill();

        Mono<Bill> billMono = Mono.just(billEntity);
        BillRequestDTO billDTO = buildBillRequestDTO();

        when(repo.insert(any(Bill.class))).thenReturn(billMono);

        Mono<BillResponseDTO> returnedBill = billService.createBill(Mono.just(billDTO));

        StepVerifier.create(returnedBill)
                .consumeNextWith(monoDTO -> {
                    assertEquals(billEntity.getCustomerId(), monoDTO.getCustomerId());
                    assertEquals(billEntity.getAmount(), monoDTO.getAmount());
                })
                .verifyComplete();

    }

    @Test
    public void test_deleteAllBills(){
        
        when(repo.deleteAll()).thenReturn(Mono.empty());

        Mono<Void> deleteObj = billService.deleteAllBills();

        StepVerifier.create(deleteObj)
                .expectNextCount(0)
                .verifyComplete();
    }

//    @Test
//    public void test_DeleteBill(){
//
//        Bill billEntity = buildBill();
//
//        when(repo.deleteBillByBillId(anyString())).thenReturn(Mono.empty());
//
//        Mono<Void> deletedObj = billService.deleteBill(billEntity.getBillId());
//
//        StepVerifier.create(deletedObj)
//                .expectNextCount(0)
//                .verifyComplete();
//    }

    @Test
    public void test_deleteBillByVetId(){

        Bill billEntity = buildBill();

        when(repo.deleteBillsByVetId(anyString())).thenReturn(Flux.empty());

        Flux<Void> deletedObj = billService.deleteBillsByVetId(billEntity.getVetId());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    public void test_deleteBillsByCustomerId(){
        Bill billEntity = buildBill();
        when(repo.deleteBillsByCustomerId(anyString())).thenReturn(Flux.empty());
        Flux<Void> deletedObj = billService.deleteBillsByCustomerId(billEntity.getCustomerId());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }
    @Test
    public void test_getBillByCustomerId(){

        Bill billEntity = buildBill();

        String CUSTOMER_ID = billEntity.getCustomerId();

        when(repo.findByCustomerId(anyString())).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOMono = billService.getBillsByCustomerId(CUSTOMER_ID);

        StepVerifier.create(billDTOMono)
                .consumeNextWith(foundBill -> {
                    assertEquals(billEntity.getBillId(), foundBill.getBillId());
                    assertEquals(billEntity.getAmount(), foundBill.getAmount());
                    assertEquals(billEntity.getVisitType(), foundBill.getVisitType());
                })
                .verifyComplete();
    }

    @Test
    public void test_getBillByVetId(){

        Bill billEntity = buildBill();

        String VET_ID = billEntity.getVetId();

        when(repo.findByVetId(anyString())).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOMono = billService.getBillsByVetId(VET_ID);

        StepVerifier.create(billDTOMono)
                .consumeNextWith(foundBill -> {
                    assertEquals(billEntity.getBillId(), foundBill.getBillId());
                    assertEquals(billEntity.getAmount(), foundBill.getAmount());
                    assertEquals(billEntity.getVisitType(), foundBill.getVisitType());
                })
                .verifyComplete();
    }

    @Test
    public void test_updateBill() {

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


//    @Test
//    public void test_deleteNonExistentBillId() {
//        String nonExistentBillId = "nonExistentId";
//
//        when(repo.deleteBillByBillId(nonExistentBillId)).thenReturn(Mono.empty());
//
//        Mono<Void> deletedObj = billService.deleteBill(nonExistentBillId);
//
//        StepVerifier.create(deletedObj)
//                .expectNextCount(0)
//                .verifyComplete();
//    }

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
    public void test_getBillByNonExistentCustomerId() {
        String nonExistentCustomerId = "nonExistentId";


        when(repo.findByCustomerId(nonExistentCustomerId)).thenReturn(Flux.empty());

        Flux<BillResponseDTO> billDTOMono = billService.getBillsByCustomerId(nonExistentCustomerId);

        StepVerifier.create(billDTOMono)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_createBillWithInvalidData() {
        BillRequestDTO billDTO = buildInvalidBillRequestDTO();

        Mono<BillRequestDTO> billRequestMono = Mono.just(billDTO);

        when(repo.insert(any(Bill.class))).thenReturn(Mono.error(new RuntimeException("Invalid data")));

        Mono<BillResponseDTO> returnedBill = billService.createBill(billRequestMono);

        StepVerifier.create(returnedBill)
                .expectError()
                .verify();
    }

        @Test
    public void testGenerateBillPdf() {
        // Step 1: Mocking Bill entity with first and last name
        Bill mockBill = Bill.builder()
                .billId("billId-1")
                .customerId("customerId-1")
                .ownerFirstName("John")
                .ownerLastName("Doe")
                .visitType("General")
                .vetId("vetId-1")
                .amount(100.0)
                .billStatus(BillStatus.PAID)
                .date(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .build();

        String customerId = mockBill.getCustomerId();
        String billId = mockBill.getBillId();

        // Step 2: Mocking the repository to return the mock Bill
        when(repo.findByBillId(billId)).thenReturn(Mono.just(mockBill));

        // Step 3: Calling the method under test
        Mono<byte[]> pdfBytesMono = billService.generateBillPdf(customerId, billId);

        // Step 4: Verifying the result using StepVerifier
        StepVerifier.create(pdfBytesMono)
                .assertNext(pdfBytes -> {
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                })
                .verifyComplete();
    }

    @Test
    public void testGenerateBillPdf_BillNotFound() {
        // Step 1: Mocking the repository to return Mono.empty() when a bill is not found
        when(repo.findByBillId(anyString())).thenReturn(Mono.empty());

        // Step 2: Calling the method under test
        Mono<byte[]> pdfMono = billService.generateBillPdf("nonexistentCustomerId", "nonexistentBillId");

        // Step 3: Verifying that the Mono emits an error with the expected message
        StepVerifier.create(pdfMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Bill not found for given customer"))
                .verify();
    }

    private BillRequestDTO buildInvalidBillRequestDTO() {
        LocalDate date = LocalDate.now();

        return BillRequestDTO.builder()
                .customerId("1")
                .vetId("2")
                .visitType("")
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


    @Test
    void getAllBillsByPage_ShouldReturnPaginatedResults() {
        // Arrange
        Bill bill1 = buildBill();
        Bill bill2 = buildBill();
        bill2.setBillId("BillUUID2");
        Pageable pageable = PageRequest.of(0, 1);

        when(repo.findAll()).thenReturn(Flux.just(bill1, bill2));

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByPage(pageable, null, null,
                null, null, null, null, null, null);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(bill -> bill.getBillId().equals("BillUUID"))
                .expectComplete()
                .verify();
    }


    @Test
    void getAllBillsByPage_WhenNoBills_ShouldReturnEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(repo.findAll()).thenReturn(Flux.empty());

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByPage(pageable, null, null,
                null, null, null, null, null, null);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getBillsByMonth_ShouldReturnBillsForGivenMonth() {
        // Arrange
        Bill bill1 = buildBill();
        Bill bill2 = buildBill();
        bill2.setBillId("BillUUID2"); // Different ID for distinct objects
        int year = 2022;
        int month = 9;

        when(repo.findByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Flux.just(bill1, bill2));

        // Act
        Flux<BillResponseDTO> result = billService.getBillsByMonth(year, month);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2) // Expect both bills
                .verifyComplete();
    }


}