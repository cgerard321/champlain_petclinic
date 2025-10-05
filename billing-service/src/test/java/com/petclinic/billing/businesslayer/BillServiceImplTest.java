package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.exceptions.NotFoundException;
import com.petclinic.billing.util.EntityDtoUtil;
import com.petclinic.billing.util.InterestCalculationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public void test_getBillsByOwnerName() {
        // Arrange
        String ownerFirstName = "John";
        String ownerLastName = "Doe";

        Bill billEntity = buildBill();
        billEntity.setOwnerFirstName(ownerFirstName);
        billEntity.setOwnerLastName(ownerLastName);

        when(repo.findAll()).thenReturn(Flux.just(billEntity));

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByOwnerName(ownerFirstName, ownerLastName);

        // Assert
        StepVerifier.create(result)
                .consumeNextWith(bill -> {
                    assertNotNull(bill);
                    assertEquals(ownerFirstName, bill.getOwnerFirstName());
                    assertEquals(ownerLastName, bill.getOwnerLastName());
                })
                .verifyComplete();
    }

    @Test
    public void test_getBillsByVetName() {
        // Arrange
        String vetFirstName = "Alice";
        String vetLastName = "Smith";

        Bill billEntity = buildBill();
        billEntity.setVetFirstName(vetFirstName);
        billEntity.setVetLastName(vetLastName);

        when(repo.findAll()).thenReturn(Flux.just(billEntity));

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByVetName(vetFirstName, vetLastName);

        // Assert
        StepVerifier.create(result)
                .consumeNextWith(bill -> {
                    assertNotNull(bill);
                    assertEquals(vetFirstName, bill.getVetFirstName());
                    assertEquals(vetLastName, bill.getVetLastName());
                })
                .verifyComplete();
    }

    @Test
    public void test_getBillsByVisitType() {
        // Arrange
        String visitType = "Regular";

        Bill billEntity = buildBill();
        billEntity.setVisitType(visitType);

        when(repo.findAll()).thenReturn(Flux.just(billEntity));

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByVisitType(visitType);

        // Assert
        StepVerifier.create(result)
                .consumeNextWith(bill -> {
                    assertNotNull(bill);
                    assertEquals(visitType, bill.getVisitType());
                })
                .verifyComplete();
    }

    @Test
    public void test_getBillsByOwnerName_notFound() {
        // Arrange
        String ownerFirstName = "Nonexistent";
        String ownerLastName = "Person";

        when(repo.findAll()).thenReturn(Flux.empty());

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByOwnerName(ownerFirstName, ownerLastName);

        // Assert
        StepVerifier.create(result)
                .consumeErrorWith(error -> {
                    assertNotNull(error);
                    assertTrue(error instanceof NotFoundException);
                    assertEquals("No bills found for the given owner name", error.getMessage());
                })
                .verify();
    }

    @Test
    public void test_getBillsByVetName_notFound() {
        // Arrange
        String vetFirstName = "Nonexistent";
        String vetLastName = "Vet";

        when(repo.findAll()).thenReturn(Flux.empty());

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByVetName(vetFirstName, vetLastName);

        // Assert
        StepVerifier.create(result)
                .consumeErrorWith(error -> {
                    assertNotNull(error);
                    assertTrue(error instanceof NotFoundException);
                    assertEquals("No bills found for the given vet name", error.getMessage());
                })
                .verify();
    }

    @Test
    public void test_getBillsByVisitType_notFound() {
        // Arrange
        String visitType = "ImaginaryType";

        when(repo.findAll()).thenReturn(Flux.empty());

        // Act
        Flux<BillResponseDTO> result = billService.getAllBillsByVisitType(visitType);

        // Assert
        StepVerifier.create(result)
                .consumeErrorWith(error -> {
                    assertNotNull(error);
                    assertTrue(error instanceof NotFoundException);
                    assertEquals("No bills found for the given visit type", error.getMessage());
                })
                .verify();
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

        BigDecimal updatedAmount = new BigDecimal(20.00);
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
        BigDecimal updatedAmount = new BigDecimal(20.0);
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
    public void test_updateBillWithInvalidRequest() {
        String billId = "validBillId";
        BigDecimal updatedAmount = new BigDecimal (-5.0); // Negative amount, which is invalid
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
        Bill mockBill = Bill.builder()
                .billId("billId-1")
                .customerId("customerId-1")
                .ownerFirstName("John")
                .ownerLastName("Doe")
                .visitType("General")
                .vetId("vetId-1")
                .amount(new BigDecimal(100.0))
                .billStatus(BillStatus.PAID)
                .date(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .build();

        String customerId = mockBill.getCustomerId();
        String billId = mockBill.getBillId();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(mockBill));

        Mono<byte[]> pdfBytesMono = billService.generateBillPdf(customerId, billId);

        StepVerifier.create(pdfBytesMono)
                .assertNext(pdfBytes -> {
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                })
                .verifyComplete();
    }

    @Test
    public void testGenerateBillPdf_BillNotFound() {
        when(repo.findByBillId(anyString())).thenReturn(Mono.empty());

        Mono<byte[]> pdfMono = billService.generateBillPdf("nonexistentCustomerId", "nonexistentBillId");

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
                .amount(new BigDecimal(100.0))
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

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.PAID).dueDate(dueDate).build();
    }

    private Bill buildUnpaidBill(){

        VetResponseDTO vetDTO = buildVetDTO();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 5);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.UNPAID).dueDate(dueDate).build();

    }

    private Bill buildOverdueBill(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.OVERDUE).dueDate(dueDate).build();
    }



    private BillRequestDTO buildBillRequestDTO(){

        VetResponseDTO vetDTO = buildVetDTO();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate =LocalDate.of(2022, Month.OCTOBER, 10);

        return BillRequestDTO.builder().customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.PAID).dueDate(dueDate).build();

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
        bill2.setBillId("BillUUID2");
        int year = 2022;
        int month = 9;

        when(repo.findByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Flux.just(bill1, bill2));

        // Act
        Flux<BillResponseDTO> result = billService.getBillsByMonth(year, month);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void calculateCurrentBalance_ShouldReturnCorrectBalance() {

        String customerId = "valid-customer-id";
        Bill unpaidBill = Bill.builder().amount(new BigDecimal(100.0)).billStatus(BillStatus.UNPAID).customerId(customerId).build();
        Bill overdueBill = Bill.builder().amount(new BigDecimal(50.0)).billStatus(BillStatus.OVERDUE).customerId(customerId).build();

        when(repo.findByCustomerIdAndBillStatus(customerId, BillStatus.UNPAID))
                .thenReturn(Flux.just(unpaidBill));
        when(repo.findByCustomerIdAndBillStatus(customerId, BillStatus.OVERDUE))
                .thenReturn(Flux.just(overdueBill));

        Mono<BigDecimal> result = billService.calculateCurrentBalance(customerId);

        StepVerifier.create(result)
                .expectNext(new BigDecimal(150.0)) 
                .verifyComplete();
    }

    @Test
    void calculateCurrentBalance_InvalidCustomer_ShouldReturnNotFound() {

        String invalidCustomerId = "non-existent-id";
        when(repo.findByCustomerIdAndBillStatus(invalidCustomerId, BillStatus.UNPAID))
                .thenReturn(Flux.empty());
        when(repo.findByCustomerIdAndBillStatus(invalidCustomerId, BillStatus.OVERDUE))
                .thenReturn(Flux.empty());

        Mono<BigDecimal> result = billService.calculateCurrentBalance(invalidCustomerId);

        StepVerifier.create(result)
                .expectNext(new BigDecimal(0.0)) 
                .verifyComplete();
    }


    @Test
    void processPayment_Success() {
        // Arrange
        String customerId = "customerId-1";
        String billId = "billId-1";
        Bill bill = buildBill();
        bill.setBillStatus(BillStatus.UNPAID);

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        when(repo.findByCustomerIdAndBillId(customerId, billId)).thenReturn(Mono.just(bill));
        when(repo.save(any(Bill.class))).thenReturn(Mono.just(bill));

        // Act
        Mono<BillResponseDTO> result = billService.processPayment(customerId, billId, paymentRequest);

        // Assert
        StepVerifier.create(result)
                .consumeNextWith(updatedBillDto -> {
                    assertEquals(BillStatus.PAID, updatedBillDto.getBillStatus());
                    verify(repo, times(1)).save(any(Bill.class));
                })
                .verifyComplete();
    }

    @Test
    void processPayment_InvalidCardNumber_Failure() {
        String customerId = "customerId-1";
        String billId = "billId-1";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("12345678", "123", "12/23");

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest))
                .expectErrorMatches(throwable -> throwable instanceof InvalidPaymentException &&
                        throwable.getMessage().contains("Invalid payment details"))
                .verify();
    }


    @Test
    void processPayment_InvalidCVV_Failure() {
        String customerId = "customerId-1";
        String billId = "billId-1";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "12", "12/23");

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest))
                .expectErrorMatches(throwable -> throwable instanceof InvalidPaymentException &&
                        throwable.getMessage().contains("Invalid payment details"))
                .verify();
    }


    @Test
    void processPayment_InvalidExpirationDate_Failure() {
        String customerId = "customerId-1";
        String billId = "billId-1";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "1223");

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest))
                .expectErrorMatches(throwable -> throwable instanceof InvalidPaymentException &&
                        throwable.getMessage().contains("Invalid payment details"))
                .verify();
    }

    @Test
    void processPayment_BillNotFound_Failure() {
        String customerId = "customerId-1";
        String billId = "billId-1";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        when(repo.findByCustomerIdAndBillId(customerId, billId)).thenReturn(Mono.empty());

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest))
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(ResponseStatusException.class);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).isEqualTo("Bill not found");
                })
                .verify();
    }

    @Test
    void getNumberOfBillsWithFilters_Positive_ShouldCountMatchingBills() {

        Bill b1 = buildBill();
        b1.setBillId("B-1");
        b1.setCustomerId("C-1");
        b1.setOwnerFirstName("Alice");
        b1.setOwnerLastName("Smith");
        b1.setVisitType("ANNUAL");
        b1.setVetId("V-1");
        b1.setVetFirstName("Jenny");
        b1.setVetLastName("Doe");

        Bill b2 = buildBill();
        b2.setBillId("B-2");
        b2.setCustomerId("C-1");
        b2.setOwnerFirstName("Alice");
        b2.setOwnerLastName("Smith");
        b2.setVisitType("ANNUAL");
        b2.setVetId("V-1");
        b2.setVetFirstName("Jenny");
        b2.setVetLastName("Doe");

        Bill b3 = buildBill();
        b3.setBillId("B-3");
        b3.setCustomerId("C-2");
        b3.setOwnerFirstName("Bob");
        b3.setOwnerLastName("Jones");
        b3.setVisitType("SURGERY");
        b3.setVetId("V-2");
        b3.setVetFirstName("Tom");
        b3.setVetLastName("Lee");

        when(repo.findAll()).thenReturn(Flux.just(b1, b2, b3));

        Mono<Long> result = billService.getNumberOfBillsWithFilters(
                null,
                "C-1",
                "Alice",
                "Smith",
                "ANNUAL",
                "V-1",
                "Jenny",
                "Doe"
        );

        StepVerifier.create(result)
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void getNumberOfBillsWithFilters_Negative_NoMatches_ShouldReturnZero() {
        Bill b1 = buildBill();
        b1.setBillId("B-10");
        b1.setCustomerId("C-10");
        b1.setOwnerFirstName("Alice");
        b1.setOwnerLastName("Smith");
        b1.setVisitType("ANNUAL");
        b1.setVetId("V-10");
        b1.setVetFirstName("Jenny");
        b1.setVetLastName("Doe");

        Bill b2 = buildBill();
        b2.setBillId("B-11");
        b2.setCustomerId("C-11");
        b2.setOwnerFirstName("Bob");
        b2.setOwnerLastName("Jones");
        b2.setVisitType("SURGERY");
        b2.setVetId("V-11");
        b2.setVetFirstName("Tom");
        b2.setVetLastName("Lee");

        when(repo.findAll()).thenReturn(Flux.just(b1, b2));

        Mono<Long> result = billService.getNumberOfBillsWithFilters(
                "NO-SUCH-BILL",
                "C-999",
                "Nonexistent",
                "Person",
                "DENTAL",
                "V-999",
                "Nobody",
                "Nowhere"
        );

        StepVerifier.create(result)
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void getBillByCustomerIdAndBillId_Positive_ShouldReturnDtoWhenCustomerMatches() {
        Bill bill = buildBill();
        bill.setBillId("B-42");
        bill.setCustomerId("C-123");

        when(repo.findByBillId("B-42")).thenReturn(Mono.just(bill));

        Mono<BillResponseDTO> result =
                billService.getBillByCustomerIdAndBillId("C-123", "B-42");

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals("B-42", dto.getBillId());
                    assertEquals("C-123", dto.getCustomerId());
                })
                .verifyComplete();
    }

    @Test
    void getBillByCustomerIdAndBillId_Negative_NoMatchOnCustomer_ShouldBeEmpty() {

        Bill bill = buildBill();
        bill.setBillId("B-42");
        bill.setCustomerId("C-123");

        when(repo.findByBillId("B-42")).thenReturn(Mono.just(bill));

        Mono<BillResponseDTO> result =
                billService.getBillByCustomerIdAndBillId("C-999", "B-42");

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void test_getBillById_OverdueBill_ShouldCalculateInterest() {
        // Arrange
        LocalDate dueDate = LocalDate.now().minusMonths(2); // 2 months overdue
        BigDecimal amount = new BigDecimal("100.00");
        Bill overdueBill = Bill.builder()
            .billId("overdue-bill-id")
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(dueDate)
            .build();

        when(repo.findByBillId("overdue-bill-id")).thenReturn(Mono.just(overdueBill));

        // Act
        Mono<BillResponseDTO> result = billService.getBillByBillId("overdue-bill-id");

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(dto -> {
                assertEquals(amount, dto.getAmount());
                assertEquals(BillStatus.OVERDUE, dto.getBillStatus());
                // Use centralized utility for compound interest calculation
                BigDecimal expectedInterest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, LocalDate.now());
                assertEquals(expectedInterest, dto.getInterest());
            })
            .verifyComplete();
    }

    @Test
    void test_getBillById_NotOverdueBill_ShouldHaveZeroInterest() {
        // Arrange
        LocalDate dueDate = LocalDate.now().plusDays(10); // Not overdue
        BigDecimal amount = new BigDecimal("200.00");
        Bill unpaidBill = Bill.builder()
            .billId("unpaid-bill-id")
            .amount(amount)
            .billStatus(BillStatus.UNPAID)
            .dueDate(dueDate)
            .build();

        when(repo.findByBillId("unpaid-bill-id")).thenReturn(Mono.just(unpaidBill));

        // Act
        Mono<BillResponseDTO> result = billService.getBillByBillId("unpaid-bill-id");

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(dto -> {
                assertEquals(amount, dto.getAmount());
                assertEquals(BillStatus.UNPAID, dto.getBillStatus());
                assertEquals(BigDecimal.ZERO.setScale(2), dto.getInterest().setScale(2));
            })
            .verifyComplete();
    }

    @Test
    void test_calculateCurrentBalance_ShouldIncludeInterestForOverdueBills() {
        // Arrange
        String customerId = "customer-interest";
        LocalDate overdueDate = LocalDate.now().minusMonths(1);
        BigDecimal unpaidAmount = new BigDecimal("100.00");
        BigDecimal overdueAmount = new BigDecimal("50.00");

        Bill unpaidBill = Bill.builder()
            .amount(unpaidAmount)
            .billStatus(BillStatus.UNPAID)
            .customerId(customerId)
            .build();

        Bill overdueBill = Bill.builder()
            .amount(overdueAmount)
            .billStatus(BillStatus.OVERDUE)
            .customerId(customerId)
            .dueDate(overdueDate)
            .build();

        when(repo.findByCustomerIdAndBillStatus(customerId, BillStatus.UNPAID))
            .thenReturn(Flux.just(unpaidBill));
        when(repo.findByCustomerIdAndBillStatus(customerId, BillStatus.OVERDUE))
            .thenReturn(Flux.just(overdueBill));

        // Act
        Mono<BigDecimal> result = billService.calculateCurrentBalance(customerId);

        // Assert
        StepVerifier.create(result)
            .consumeNextWith(balance -> {
                BigDecimal expectedInterest = overdueAmount
                        .multiply(new BigDecimal("0.015"))
                        .multiply(BigDecimal.valueOf(1))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal expectedTotal = unpaidAmount.add(overdueAmount).add(expectedInterest);
                assertEquals(expectedTotal, balance.setScale(2, RoundingMode.HALF_UP));
            })
            .verifyComplete();
    }

    @Test
    void setInterestExempt_Positive_ShouldUpdateFlagAndClearInterestWhenTrue() {
        // Arrange
        Bill bill = buildBill();
        bill.setBillId("B-300");
        bill.setInterestExempt(false);
        bill.setInterest(new BigDecimal("33.33"));

        when(repo.findByBillId("B-300")).thenReturn(Mono.just(bill));
        when(repo.save(any(Bill.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        Mono<Void> result = billService.setInterestExempt("B-300", true);

        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<Bill> captor = ArgumentCaptor.forClass(Bill.class);
        verify(repo).save(captor.capture());
        Bill saved = captor.getValue();

        assertTrue(saved.isInterestExempt(), "exempt flag should be true");
        assertEquals(BigDecimal.ZERO, saved.getInterest(), "interest should be cleared when exempted");
    }

    @Test
    void setInterestExempt_Negative_ShouldNotSaveWhenBillNotFound() {
        when(repo.findByBillId("B-301")).thenReturn(Mono.empty());

        Mono<Void> result = billService.setInterestExempt("B-301", true);

        StepVerifier.create(result).verifyComplete();
        verify(repo, never()).save(any(Bill.class));
    }

    @Test
    void getBillsByCustomerIdAndStatus_Positive_ShouldReturnMappedDtos() {
        String customerId = "C-007";
        BillStatus status = BillStatus.UNPAID;

        Bill b1 = buildBill();
        b1.setBillId("B-1");
        b1.setCustomerId(customerId);
        b1.setBillStatus(status);
        b1.setDueDate(LocalDate.now().plusDays(10)); // ensure non-null

        Bill b2 = buildBill();
        b2.setBillId("B-2");
        b2.setCustomerId(customerId);
        b2.setBillStatus(status);
        b2.setDueDate(LocalDate.now().plusDays(20)); // ensure non-null

        when(repo.findByCustomerIdAndBillStatus(customerId, status))
                .thenReturn(Flux.just(b1, b2));

        Flux<BillResponseDTO> result =
                billService.getBillsByCustomerIdAndStatus(customerId, status);

        StepVerifier.create(result)
                .assertNext(dto -> {
                    assertEquals("B-1", dto.getBillId());
                    assertEquals(customerId, dto.getCustomerId());
                    assertEquals(BillStatus.UNPAID, dto.getBillStatus());
                })
                .assertNext(dto -> {
                    assertEquals("B-2", dto.getBillId());
                    assertEquals(customerId, dto.getCustomerId());
                    assertEquals(BillStatus.UNPAID, dto.getBillStatus());
                })
                .verifyComplete();

        verify(repo, times(1)).findByCustomerIdAndBillStatus(customerId, status);
    }

    @Test
    void getBillsByCustomerIdAndStatus_Negative_NoMatches_ShouldBeEmpty() {
        String customerId = "NO-SUCH-CUSTOMER";
        BillStatus status = BillStatus.PAID;

        when(repo.findByCustomerIdAndBillStatus(customerId, status))
                .thenReturn(Flux.empty());

        Flux<BillResponseDTO> result =
                billService.getBillsByCustomerIdAndStatus(customerId, status);

        StepVerifier.create(result)
                .verifyComplete();

        verify(repo, times(1)).findByCustomerIdAndBillStatus(customerId, status);
    }

    @Test
    void deleteBill_Negative_UnpaidOrOverdue_ShouldReturn422AndNotDelete() {
        String billId = "B-99";
        Bill bill = buildBill();
        bill.setBillId(billId);
        bill.setBillStatus(BillStatus.UNPAID); // or OVERDUE

        when(repo.findByBillId(billId)).thenReturn(Mono.just(bill));

        Mono<Void> result = billService.deleteBill(billId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof ResponseStatusException);
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, rse.getStatus()); // Spring 5.x
                    assertEquals("Cannot delete a bill that is unpaid or overdue.", rse.getReason());
                })
                .verify();

        verify(repo, times(1)).findByBillId(billId);
        verify(repo, never()).deleteBillByBillId(anyString());
    }

    @Test
    void generateBillPdf_Negative_BillNotForCustomer_ShouldErrorAndSkipPdf() {
        String billId = "B-42";
        String repoCustomer = "C-123";
        String requestedCustomer = "C-999";

        Bill bill = buildBill();
        bill.setBillId(billId);
        bill.setCustomerId(repoCustomer);

        when(repo.findByBillId(billId)).thenReturn(Mono.just(bill));

        Mono<byte[]> result = billService.generateBillPdf(requestedCustomer, billId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof RuntimeException);
                    assertEquals("Bill not found for given customer", ex.getMessage());
                })
                .verify();

        verify(repo, times(1)).findByBillId(billId);
    }
}