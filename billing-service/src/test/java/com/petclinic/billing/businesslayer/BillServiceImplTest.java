package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
import com.petclinic.billing.domainclientlayer.Auth.AuthServiceClient;
import com.petclinic.billing.domainclientlayer.Auth.UserDetails;
import com.petclinic.billing.domainclientlayer.Mailing.Mail;
import com.petclinic.billing.domainclientlayer.Mailing.MailService;
import com.petclinic.billing.domainclientlayer.OwnerClient;
import com.petclinic.billing.domainclientlayer.VetClient;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.exceptions.NotFoundException;
import com.petclinic.billing.util.EntityDtoUtil;
import com.petclinic.billing.util.InterestCalculationUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.lang.reflect.Method;
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

    @MockBean
    VetClient vetClient;

    @MockBean
    AuthServiceClient authClient;

    @MockBean
    MailService mailService;

    @MockBean
    OwnerClient ownerClient;

    @Autowired
    BillService billService;

    @Test
    public void test_getBillById() {
        Bill billEntity = buildBill();

        String BILL_ID = billEntity.getBillId();

        when(repo.findByBillId(anyString())).thenReturn(Mono.just(billEntity));
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());

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
    void getBillsByPage_ShouldSucceed() {

        Bill bill1 = Bill.builder()
                .billId("billId-1")
                .customerId("customerId-1")
                .ownerFirstName("ownerFirstName1")
                .ownerLastName("ownerLastName1")
                .visitType("operation")
                .vetId("vetId1")
                .vetFirstName("vetFirstName1")
                .vetLastName("vetLastName1")
                .date(LocalDate.of(2024, 10, 1))
                .dueDate(LocalDate.of(2024, 10, 30))
                .amount(new BigDecimal("100.00"))
                .billStatus(BillStatus.UNPAID)
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
                .date(LocalDate.of(2024, 10, 1))
                .dueDate(LocalDate.of(2024, 10, 30))
                .amount(new BigDecimal("150.00"))
                .billStatus(BillStatus.PAID)
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
                .date(LocalDate.of(2024, 10, 1))
                .dueDate(LocalDate.of(2024, 10, 30))
                .amount(new BigDecimal("200.00"))
                .billStatus(BillStatus.OVERDUE)
                .build();

        Pageable pageable = PageRequest.of(0, 2);

        // Mock the repository to return a Flux of owners
        when(repo.findAll()).thenReturn(Flux.just(bill1, bill2, bill3));
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());

        // Call the method under test
        Flux<BillResponseDTO> bills = billService.getAllBillsByPage(pageable, null, null,
                null, null, null, null, null, null);


        StepVerifier.create(bills)
                .expectNextMatches(billDto1 -> billDto1.getBillId().equals(bill1.getBillId()))
                .expectNextMatches(billDto2 -> billDto2.getBillId().equals(bill2.getBillId()))
                .expectComplete()
                .verify();

    }

    @Test
    public void test_getAllBillsByPaidStatus() {
        BillStatus status = BillStatus.PAID; // Change this to the desired status

        Bill billEntity = buildBill();
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.getAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void test_getAllBillsByUnpaidStatus() {
        BillStatus status = BillStatus.UNPAID; // Change this to the desired status

        Bill billEntity = buildUnpaidBill();
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.getAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void test_getAllBillsByOverdueStatus() {
        BillStatus status = BillStatus.OVERDUE; // Change this to the desired status

        Bill billEntity = buildOverdueBill();
        when(repo.findAllBillsByBillStatus(status)).thenReturn(Flux.just(billEntity));

        Flux<BillResponseDTO> billDTOFlux = billService.getAllBillsByStatus(status);

        StepVerifier.create(billDTOFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void test_getBillsByOwnerName() {

        String ownerFirstName = "John";
        String ownerLastName = "Doe";

        Bill billEntity = buildBill();
        billEntity.setOwnerFirstName(ownerFirstName);
        billEntity.setOwnerLastName(ownerLastName);

        when(repo.findAll()).thenReturn(Flux.just(billEntity));


        Flux<BillResponseDTO> result = billService.getAllBillsByOwnerName(ownerFirstName, ownerLastName);


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

        String vetFirstName = "Alice";
        String vetLastName = "Smith";

        Bill billEntity = buildBill();
        billEntity.setVetFirstName(vetFirstName);
        billEntity.setVetLastName(vetLastName);

        when(repo.findAll()).thenReturn(Flux.just(billEntity));


        Flux<BillResponseDTO> result = billService.getAllBillsByVetName(vetFirstName, vetLastName);


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

        String visitType = "Regular";

        Bill billEntity = buildBill();
        billEntity.setVisitType(visitType);

        when(repo.findAll()).thenReturn(Flux.just(billEntity));


        Flux<BillResponseDTO> result = billService.getAllBillsByVisitType(visitType);


        StepVerifier.create(result)
                .consumeNextWith(bill -> {
                    assertNotNull(bill);
                    assertEquals(visitType, bill.getVisitType());
                })
                .verifyComplete();
    }

    @Test
    public void test_getBillsByOwnerName_notFound() {

        String ownerFirstName = "Nonexistent";
        String ownerLastName = "Person";

        when(repo.findAll()).thenReturn(Flux.empty());


        Flux<BillResponseDTO> result = billService.getAllBillsByOwnerName(ownerFirstName, ownerLastName);


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

        String vetFirstName = "Nonexistent";
        String vetLastName = "Vet";

        when(repo.findAll()).thenReturn(Flux.empty());


        Flux<BillResponseDTO> result = billService.getAllBillsByVetName(vetFirstName, vetLastName);


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

        String visitType = "ImaginaryType";

        when(repo.findAll()).thenReturn(Flux.empty());


        Flux<BillResponseDTO> result = billService.getAllBillsByVisitType(visitType);


        StepVerifier.create(result)
                .consumeErrorWith(error -> {
                    assertNotNull(error);
                    assertTrue(error instanceof NotFoundException);
                    assertEquals("No bills found for the given visit type", error.getMessage());
                })
                .verify();
    }

    @Test
    void createBill_success() {
        // Arrange
        BillRequestDTO billDTO = new BillRequestDTO();
        billDTO.setBillStatus(BillStatus.PAID);
        billDTO.setVetId("vet-123");
        billDTO.setCustomerId("owner-456");
        billDTO.setDueDate(LocalDate.now().plusDays(30));

        // Mock VetClient response
        VetResponseDTO vetResponse = new VetResponseDTO();
        vetResponse.setFirstName("John");
        vetResponse.setLastName("Doe");
        Mockito.when(vetClient.getVetByVetId("vet-123"))
                .thenReturn(Mono.just(vetResponse));

        // Mock OwnerClient response
        OwnerResponseDTO ownerResponse = new OwnerResponseDTO();
        ownerResponse.setFirstName("Alice");
        ownerResponse.setLastName("Smith");
        Mockito.when(ownerClient.getOwnerByOwnerId("owner-456"))
                .thenReturn(Mono.just(ownerResponse));

        // Mock repository insert
        // We no longer fix the billId because it's generated inside service
        Mockito.when(repo.findById(Mockito.anyString()))
                .thenReturn(Mono.empty()); // no collision

        Mockito.when(repo.insert(Mockito.any(Bill.class)))
                .thenAnswer(invocation -> {
                    Bill inserted = invocation.getArgument(0);
                    return Mono.just(inserted); // return same bill to verify ID format
                });

        // Act + Assert
        StepVerifier.create(billService.createBill(Mono.just(billDTO)))
                .expectNextMatches(response ->
                        response.getBillId() != null &&
                                response.getBillId().length() == 10 &&
                                response.getVetFirstName().equals("John") &&
                                response.getOwnerFirstName().equals("Alice") &&
                                response.getBillStatus().equals(BillStatus.PAID) &&
                                response.getDueDate() != null
                )
                .verifyComplete();
    }

    @Test
    void createBill_withIdCollision_shouldRetryAndSucceed() {
        // Arrange
        BillRequestDTO billDTO = new BillRequestDTO();
        billDTO.setBillStatus(BillStatus.PAID);
        billDTO.setVetId("vet-123");
        billDTO.setCustomerId("owner-456");
        billDTO.setDueDate(LocalDate.now().plusDays(30));

        // Mock VetClient response
        VetResponseDTO vetResponse = new VetResponseDTO();
        vetResponse.setFirstName("John");
        vetResponse.setLastName("Doe");
        Mockito.when(vetClient.getVetByVetId("vet-123"))
                .thenReturn(Mono.just(vetResponse));

        // Mock OwnerClient response
        OwnerResponseDTO ownerResponse = new OwnerResponseDTO();
        ownerResponse.setFirstName("Alice");
        ownerResponse.setLastName("Smith");
        Mockito.when(ownerClient.getOwnerByOwnerId("owner-456"))
                .thenReturn(Mono.just(ownerResponse));

        // Mock a collision once, then success
        Bill existingBill = new Bill();
        existingBill.setBillId("duplicateID");

        // First call -> collision, Second call -> no collision
        Mockito.when(repo.findById(Mockito.anyString()))
                .thenReturn(Mono.just(existingBill)) // 1st attempt (collision)
                .thenReturn(Mono.empty());            // 2nd attempt (unique)

        // Mock repository insert (returns whatever Bill is passed in)
        Mockito.when(repo.insert(Mockito.any(Bill.class)))
                .thenAnswer(invocation -> {
                    Bill inserted = invocation.getArgument(0);
                    return Mono.just(inserted);
                });

        // Act + Assert
        StepVerifier.create(billService.createBill(Mono.just(billDTO)))
                .expectNextMatches(response ->
                        response.getBillId() != null &&
                                response.getBillId().length() == 10 &&
                                response.getVetFirstName().equals("John") &&
                                response.getOwnerFirstName().equals("Alice") &&
                                response.getBillStatus().equals(BillStatus.PAID) &&
                                response.getDueDate() != null
                )
                .verifyComplete();

        // Verify that findById() was called twice (1 collision + 1 success)
        Mockito.verify(repo, Mockito.times(2)).findById(Mockito.anyString());
        Mockito.verify(repo, Mockito.times(1)).insert(Mockito.any(Bill.class));
    }

    @Test
    void createBill_exceedsRetryLimit_shouldFail() {
        // Arrange
        BillRequestDTO billDTO = new BillRequestDTO();
        billDTO.setBillStatus(BillStatus.PAID);
        billDTO.setVetId("vet-123");
        billDTO.setCustomerId("owner-456");
        billDTO.setDueDate(LocalDate.now().plusDays(30));

        // Mock VetClient response
        VetResponseDTO vetResponse = new VetResponseDTO();
        vetResponse.setFirstName("John");
        vetResponse.setLastName("Doe");
        Mockito.when(vetClient.getVetByVetId("vet-123"))
                .thenReturn(Mono.just(vetResponse));

        // Mock OwnerClient response
        OwnerResponseDTO ownerResponse = new OwnerResponseDTO();
        ownerResponse.setFirstName("Alice");
        ownerResponse.setLastName("Smith");
        Mockito.when(ownerClient.getOwnerByOwnerId("owner-456"))
                .thenReturn(Mono.just(ownerResponse));

        // Always simulate a collision (never returns empty)
        Bill existingBill = new Bill();
        existingBill.setBillId("duplicateID");

        // findById() will always return an existing bill — simulating permanent collision
        Mockito.when(repo.findById(Mockito.anyString()))
                .thenReturn(Mono.just(existingBill));

        // Act + Assert
        StepVerifier.create(billService.createBill(Mono.just(billDTO)))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Failed to generate unique Bill ID"))
                .verify();

        // Verify findById() was called multiple times (up to retry limit)
        Mockito.verify(repo, Mockito.atLeast(5)).findById(Mockito.anyString());
        Mockito.verify(repo, Mockito.never()).insert(Mockito.any(Bill.class));
    }

    @Test
    void createBill_missingBillStatus_shouldReturnError() {
        // Arrange
        BillRequestDTO billDTO = new BillRequestDTO();
        billDTO.setVetId("vet-123");
        billDTO.setCustomerId("owner-456");
        billDTO.setDueDate(LocalDate.now().plusDays(30));

        // Act + Assert
        StepVerifier.create(billService.createBill(Mono.just(billDTO)))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("Bill status is required", ex.getReason());
                })
                .verify();
    }

    @Test
    void createBill_missingVetId_shouldReturnError() {
        // Arrange
        BillRequestDTO billDTO = new BillRequestDTO();
        billDTO.setCustomerId("owner-456");
        billDTO.setBillStatus(BillStatus.PAID);
        billDTO.setDueDate(LocalDate.now().plusDays(30));

        // Act + Assert
        StepVerifier.create(billService.createBill(Mono.just(billDTO)))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("Vet ID is required", ex.getReason());
                })
                .verify();
    }

    @Test
    void createBill_missingCustomerId_shouldReturnError() {
        // Arrange
        BillRequestDTO billDTO = new BillRequestDTO();
        billDTO.setVetId("vet-123");
        billDTO.setBillStatus(BillStatus.PAID);
        billDTO.setDueDate(LocalDate.now().plusDays(30));

        // Act + Assert
        StepVerifier.create(billService.createBill(Mono.just(billDTO)))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof ResponseStatusException);
                    ResponseStatusException ex = (ResponseStatusException) throwable;
                    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
                    assertEquals("Customer ID is required", ex.getReason());
                })
                .verify();
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
    public void test_deleteAllBills() {

        when(repo.deleteAll()).thenReturn(Mono.empty());

        Mono<Void> deleteObj = billService.deleteAllBills();

        StepVerifier.create(deleteObj)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_deleteBillByVetId() {

        Bill billEntity = buildBill();

        when(repo.deleteBillsByVetId(anyString())).thenReturn(Flux.empty());

        Flux<Void> deletedObj = billService.deleteBillsByVetId(billEntity.getVetId());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_deleteBillsByCustomerId() {
        Bill billEntity = buildBill();
        when(repo.deleteBillsByCustomerId(anyString())).thenReturn(Flux.empty());
        Flux<Void> deletedObj = billService.deleteBillsByCustomerId(billEntity.getCustomerId());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void test_getBillByCustomerId() {
        // Arrange
        Bill billEntity = buildBill();
        billEntity.setOwnerFirstName("John");
        billEntity.setOwnerLastName("Doe");

        String CUSTOMER_ID = billEntity.getCustomerId();

        OwnerResponseDTO mockOwner = new OwnerResponseDTO();
        mockOwner.setOwnerId(CUSTOMER_ID);
        mockOwner.setFirstName("John");
        mockOwner.setLastName("Doe");

        when(ownerClient.getOwnerByOwnerId(CUSTOMER_ID)).thenReturn(Mono.just(mockOwner));
        when(repo.findByCustomerId(CUSTOMER_ID)).thenReturn(Flux.just(billEntity));

        // Act
        Flux<BillResponseDTO> result = billService.getBillsByCustomerId(CUSTOMER_ID);

        // Assert
        StepVerifier.create(result)
                .consumeNextWith(foundBill -> {
                    assertEquals(billEntity.getBillId(), foundBill.getBillId());
                    assertEquals(billEntity.getAmount(), foundBill.getAmount());
                    assertEquals(billEntity.getVisitType(), foundBill.getVisitType());
                })
                .verifyComplete();
    }


    @Test
    public void test_getBillByVetId() {

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
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());

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
        BigDecimal updatedAmount = new BigDecimal(-5.0); // Negative amount, which is invalid
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
        // Arrange
        String nonExistentCustomerId = "nonExistentId";

        when(ownerClient.getOwnerByOwnerId(nonExistentCustomerId))
                .thenReturn(Mono.empty()); // Simulate missing owner

        // Act
        Flux<BillResponseDTO> result = billService.getBillsByCustomerId(nonExistentCustomerId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatus().equals(HttpStatus.NOT_FOUND) &&
                                throwable.getMessage().contains("Customer ID does not exist"))
                .verify();

        verify(ownerClient, times(1)).getOwnerByOwnerId(nonExistentCustomerId);
        verify(repo, never()).findByCustomerId(anyString()); // should never call repo
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
    String currency = "USD";

    when(repo.findByBillId(billId)).thenReturn(Mono.just(mockBill));

    Mono<byte[]> pdfBytesMono = billService.generateBillPdf(customerId, billId, currency);

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

    String currency = "USD";
    Mono<byte[]> pdfMono = billService.generateBillPdf("nonexistentCustomerId", "nonexistentBillId", currency);

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

    private Bill buildBill() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 15);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.PAID).dueDate(dueDate).build();
    }

    private Bill buildUnpaidBill() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 5);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.UNPAID).dueDate(dueDate).build();

    }

    private Bill buildOverdueBill() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);

        return Bill.builder().id("Id").billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.OVERDUE).dueDate(dueDate).build();
    }


    private BillRequestDTO buildBillRequestDTO() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 10);

        return BillRequestDTO.builder().customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.PAID).dueDate(dueDate).build();

    }

    @Test
    void getAllBillsByPage_ShouldReturnPaginatedResults() {

        Bill bill1 = buildBill();
        Bill bill2 = buildBill();
        bill2.setBillId("BillUUID2");
        Pageable pageable = PageRequest.of(0, 1);

        when(repo.findAll()).thenReturn(Flux.just(bill1, bill2));
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());


        Flux<BillResponseDTO> result = billService.getAllBillsByPage(pageable, null, null,
                null, null, null, null, null, null);


        StepVerifier.create(result)
                .expectNextMatches(bill -> bill.getBillId().equals("BillUUID"))
                .expectComplete()
                .verify();
    }

    @Test
    void getAllBillsByPage_WhenNoBills_ShouldReturnEmpty() {

        Pageable pageable = PageRequest.of(0, 10);

        when(repo.findAll()).thenReturn(Flux.empty());
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());


        Flux<BillResponseDTO> result = billService.getAllBillsByPage(pageable, null, null,
                null, null, null, null, null, null);


        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getBillsByMonth_ShouldReturnBillsForGivenMonth() {

        Bill bill1 = buildBill();
        Bill bill2 = buildBill();
        bill2.setBillId("BillUUID2");
        int year = 2022;
        int month = 9;

        when(repo.findByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Flux.just(bill1, bill2));


        Flux<BillResponseDTO> result = billService.getBillsByMonth(year, month);


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

        String customerId = "customerId-1";
        String billId = "billId-1";
        String jwtToken = "Bearer faketoken";
        Bill bill = buildBill();
        bill.setBillStatus(BillStatus.UNPAID);

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        UserDetails fakeUser = new UserDetails();
        fakeUser.setUserId("user-123");
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("fakeUser@example.com");

        when(authClient.getUserById(anyString(), anyString()))
                .thenReturn(Mono.just(fakeUser));
        when(mailService.sendMail(any(Mail.class)))
                .thenReturn(null);


        when(repo.findByCustomerIdAndBillId(customerId, billId)).thenReturn(Mono.just(bill));
        when(repo.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill savedBill = invocation.getArgument(0);
            return Mono.just(savedBill);
        });


        Mono<BillResponseDTO> result = billService.processPayment(customerId, billId, paymentRequest, jwtToken);


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
        String jwtToken = "Bearer faketoken";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("12345678", "123", "12/23");

        UserDetails fakeUser = new UserDetails();
        fakeUser.setUserId("user-123");
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("fakeUser@example.com");

        when(authClient.getUserById(anyString(), anyString()))
                .thenReturn(Mono.just(fakeUser));
        when(mailService.sendMail(any(Mail.class)))
                .thenReturn(null);

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest, jwtToken))
                .expectErrorMatches(throwable -> throwable instanceof InvalidPaymentException &&
                        throwable.getMessage().contains("Invalid payment details"))
                .verify();
    }


    @Test
    void processPayment_InvalidCVV_Failure() {
        String customerId = "customerId-1";
        String billId = "billId-1";
        String jwtToken = "Bearer faketoken";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "12", "12/23");

        UserDetails fakeUser = new UserDetails();
        fakeUser.setUserId("user-123");
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("fakeUser@example.com");

        when(authClient.getUserById(anyString(), anyString()))
                .thenReturn(Mono.just(fakeUser));
        when(mailService.sendMail(any(Mail.class)))
                .thenReturn(null);

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest, jwtToken))
                .expectErrorMatches(throwable -> throwable instanceof InvalidPaymentException &&
                        throwable.getMessage().contains("Invalid payment details"))
                .verify();
    }


    @Test
    void processPayment_InvalidExpirationDate_Failure() {
        String customerId = "customerId-1";
        String billId = "billId-1";
        String jwtToken = "Bearer faketoken";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "1223");

        UserDetails fakeUser = new UserDetails();
        fakeUser.setUserId("user-123");
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("fakeUser@example.com");

        when(authClient.getUserById(anyString(), anyString()))
                .thenReturn(Mono.just(fakeUser));
        when(mailService.sendMail(any(Mail.class)))
                .thenReturn(null);

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest, jwtToken))
                .expectErrorMatches(throwable -> throwable instanceof InvalidPaymentException &&
                        throwable.getMessage().contains("Invalid payment details"))
                .verify();
    }

    @Test
    void processPayment_BillNotFound_Failure() {
        String customerId = "customerId-1";
        String billId = "billId-1";
        String jwtToken = "Bearer faketoken";
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO("1234567812345678", "123", "12/23");

        UserDetails fakeUser = new UserDetails();
        fakeUser.setUserId("user-123");
        fakeUser.setUsername("fakeUser");
        fakeUser.setEmail("fakeUser@example.com");

        when(authClient.getUserById(anyString(), anyString()))
                .thenReturn(Mono.just(fakeUser));
        when(mailService.sendMail(any(Mail.class)))
                .thenReturn(null);

        when(repo.findByCustomerIdAndBillId(customerId, billId)).thenReturn(Mono.empty());

        StepVerifier.create(billService.processPayment(customerId, billId, paymentRequest, jwtToken))
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

        LocalDate dueDate = LocalDate.now().minusMonths(2);
        BigDecimal amount = new BigDecimal("100.00");
        Bill overdueBill = Bill.builder()
                .billId("overdue-bill-id")
                .amount(amount)
                .billStatus(BillStatus.OVERDUE)
                .dueDate(dueDate)
                .build();

        when(repo.findByBillId("overdue-bill-id")).thenReturn(Mono.just(overdueBill));
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());


        Mono<BillResponseDTO> result = billService.getBillByBillId("overdue-bill-id");


        StepVerifier.create(result)
            .consumeNextWith(dto -> {
                assertEquals(amount, dto.getAmount());
                assertEquals(BillStatus.OVERDUE, dto.getBillStatus());
                BigDecimal expectedInterest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, LocalDate.now());
                assertEquals(expectedInterest, dto.getInterest());
            })
            .verifyComplete();
    }

    @Test
    void test_getBillById_NotOverdueBill_ShouldHaveZeroInterest() {

        LocalDate dueDate = LocalDate.now().plusDays(10);
        BigDecimal amount = new BigDecimal("200.00");
        Bill unpaidBill = Bill.builder()
                .billId("unpaid-bill-id")
                .amount(amount)
                .billStatus(BillStatus.UNPAID)
                .dueDate(dueDate)
                .build();

        when(repo.findByBillId("unpaid-bill-id")).thenReturn(Mono.just(unpaidBill));
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());


        Mono<BillResponseDTO> result = billService.getBillByBillId("unpaid-bill-id");


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


        Mono<BigDecimal> result = billService.calculateCurrentBalance(customerId);


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
    void test_getInterest_WithOverdueBill_ShouldCalculateInterest() {

        String billId = "overdue-bill-id";
        LocalDate dueDate = LocalDate.now().minusMonths(2);
        BigDecimal amount = new BigDecimal("100.00");
        
        Bill overdueBill = Bill.builder()
            .billId(billId)
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(dueDate)
            .interestExempt(false)
            .build();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(overdueBill));


        Mono<BigDecimal> result = billService.getInterest(billId, amount, 2);


        StepVerifier.create(result)
            .consumeNextWith(interest -> {
                BigDecimal expectedInterest = InterestCalculationUtil.calculateInterest(overdueBill);
                assertEquals(expectedInterest, interest);
                assertTrue(interest.compareTo(BigDecimal.ZERO) > 0);
            })
            .verifyComplete();
    }

    @Test
    void test_getInterest_WithInterestExemptBill_ShouldReturnZero() {

        String billId = "exempt-bill-id";
        BigDecimal amount = new BigDecimal("100.00");
        
        Bill exemptBill = Bill.builder()
            .billId(billId)
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .interestExempt(true)
            .build();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(exemptBill));


        Mono<BigDecimal> result = billService.getInterest(billId, amount, 2);


        StepVerifier.create(result)
            .expectNext(BigDecimal.ZERO)
            .verifyComplete();
    }

    @Test
    void test_getInterest_WithUnpaidBill_ShouldReturnZero() {

        String billId = "unpaid-bill-id";
        BigDecimal amount = new BigDecimal("100.00");
        
        Bill unpaidBill = Bill.builder()
            .billId(billId)
            .amount(amount)
            .billStatus(BillStatus.UNPAID)
            .interestExempt(false)
            .build();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(unpaidBill));


        Mono<BigDecimal> result = billService.getInterest(billId, amount, 0);


        StepVerifier.create(result)
            .expectNext(BigDecimal.ZERO)
            .verifyComplete();
    }

    @Test
    void test_getTotalWithInterest_WithOverdueBill_ShouldReturnAmountPlusInterest() {

        String billId = "overdue-bill-id";
        LocalDate dueDate = LocalDate.now().minusMonths(1);
        BigDecimal amount = new BigDecimal("100.00");
        
        Bill overdueBill = Bill.builder()
            .billId(billId)
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(dueDate)
            .interestExempt(false)
            .build();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(overdueBill));


        Mono<BigDecimal> result = billService.getTotalWithInterest(billId, amount, 1);


        StepVerifier.create(result)
            .consumeNextWith(total -> {
                BigDecimal expectedInterest = InterestCalculationUtil.calculateInterest(overdueBill);
                BigDecimal expectedTotal = amount.add(expectedInterest);
                assertEquals(expectedTotal, total);
                assertTrue(total.compareTo(amount) > 0);
            })
            .verifyComplete();
    }

    @Test
    void test_getTotalWithInterest_WithInterestExemptBill_ShouldReturnOriginalAmount() {

        String billId = "exempt-bill-id";
        BigDecimal amount = new BigDecimal("150.00");
        
        Bill exemptBill = Bill.builder()
            .billId(billId)
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .interestExempt(true)
            .build();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(exemptBill));


        Mono<BigDecimal> result = billService.getTotalWithInterest(billId, amount, 2);


        StepVerifier.create(result)
            .expectNext(amount)
            .verifyComplete();
    }

    @Test
    void test_setInterestExempt_SetToTrue_ShouldUpdateBillAndClearInterest() {

        String billId = "test-bill-id";
        BigDecimal originalInterest = new BigDecimal("15.00");
        
        Bill bill = Bill.builder()
            .billId(billId)
            .amount(new BigDecimal("100.00"))
            .interest(originalInterest)
            .interestExempt(false)
            .build();

        Bill savedBill = Bill.builder()
            .billId(billId)
            .amount(new BigDecimal("100.00"))
            .interest(BigDecimal.ZERO)
            .interestExempt(true)
            .build();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(bill));
        when(repo.save(any(Bill.class))).thenReturn(Mono.just(savedBill));


        Mono<Void> result = billService.setInterestExempt(billId, true);


        StepVerifier.create(result)
            .verifyComplete();

        verify(repo, times(1)).save(argThat(savedBillArg -> {
            assertEquals(true, savedBillArg.isInterestExempt());
            assertEquals(BigDecimal.ZERO, savedBillArg.getInterest());
            return true;
        }));
    }

    @Test
    void test_setInterestExempt_SetToFalse_ShouldUpdateBillOnly() {

        String billId = "test-bill-id";
        
        Bill bill = Bill.builder()
            .billId(billId)
            .amount(new BigDecimal("100.00"))
            .interest(BigDecimal.ZERO)
            .interestExempt(true)
            .build();

        Bill savedBill = Bill.builder()
            .billId(billId)
            .amount(new BigDecimal("100.00"))
            .interest(BigDecimal.ZERO)
            .interestExempt(false) 
            .build();

        when(repo.findByBillId(billId)).thenReturn(Mono.just(bill));
        when(repo.save(any(Bill.class))).thenReturn(Mono.just(savedBill));


        Mono<Void> result = billService.setInterestExempt(billId, false);


        StepVerifier.create(result)
            .verifyComplete();


        verify(repo, times(1)).save(argThat(savedBillArg -> {
            assertEquals(false, savedBillArg.isInterestExempt());
            return true;
        }));
    }

    @Test
    void test_setInterestExempt_BillNotFound_ShouldCompleteWithoutError() {

        String billId = "non-existent-bill-id";

        when(repo.findByBillId(billId)).thenReturn(Mono.empty());


        Mono<Void> result = billService.setInterestExempt(billId, true);


        StepVerifier.create(result)
            .verifyComplete();


        verify(repo, never()).save(any(Bill.class));
    }

    @Test
    void test_setInterestExempt_Positive_ShouldClearInterestWhenSetToTrue() {

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
        bill.setBillStatus(BillStatus.UNPAID);

        when(repo.findByBillId(billId)).thenReturn(Mono.just(bill));

        Mono<Void> result = billService.deleteBill(billId);

        StepVerifier.create(result)
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof ResponseStatusException);
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, rse.getStatus());
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
        String currency = "USD";

        Bill bill = buildBill();
        bill.setBillId(billId);
        bill.setCustomerId(repoCustomer);

        when(repo.findByBillId(billId)).thenReturn(Mono.just(bill));

        Mono<byte[]> result = billService.generateBillPdf(requestedCustomer, billId, currency);

        StepVerifier.create(result)
            .expectErrorSatisfies(ex -> {
                assertTrue(ex instanceof RuntimeException);
                assertEquals("Bill not found for given customer", ex.getMessage());
            })
            .verify();

        verify(repo, times(1)).findByBillId(billId);
    }


    @Test
    void test_EntityDtoUtil_ToBillResponseDto_WithOverdueBill_ShouldCalculateFreshInterest() {
        LocalDate dueDate = LocalDate.now().minusMonths(2);
        BigDecimal amount = new BigDecimal("100.00");
        
        Bill overdueBill = Bill.builder()
            .billId("overdue-test-id")
            .customerId("customer-1")
            .ownerFirstName("John")
            .ownerLastName("Doe")
            .visitType("Surgery")
            .vetId("vet-1")
            .vetFirstName("Dr. Jane")
            .vetLastName("Smith")
            .date(LocalDate.now().minusDays(70))
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(dueDate)
            .interestExempt(false)
            .interest(new BigDecimal("5.00"))
            .build();

        BillResponseDTO dto = EntityDtoUtil.toBillResponseDto(overdueBill);
        
        assertEquals(overdueBill.getBillId(), dto.getBillId());
        assertEquals(overdueBill.getCustomerId(), dto.getCustomerId());
        assertEquals(overdueBill.getOwnerFirstName(), dto.getOwnerFirstName());
        assertEquals(overdueBill.getOwnerLastName(), dto.getOwnerLastName());
        assertEquals(overdueBill.getVisitType(), dto.getVisitType());
        assertEquals(overdueBill.getVetId(), dto.getVetId());
        assertEquals(overdueBill.getVetFirstName(), dto.getVetFirstName());
        assertEquals(overdueBill.getVetLastName(), dto.getVetLastName());
        assertEquals(overdueBill.getDate(), dto.getDate());
        assertEquals(overdueBill.getAmount(), dto.getAmount());
        assertEquals(overdueBill.getBillStatus(), dto.getBillStatus());
        assertEquals(overdueBill.getDueDate(), dto.getDueDate());
        assertEquals(overdueBill.isInterestExempt(), dto.isInterestExempt());
        
        BigDecimal expectedInterest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, LocalDate.now());
        assertEquals(expectedInterest, dto.getInterest());
        assertTrue(dto.getInterest().compareTo(BigDecimal.ZERO) > 0);
        
        BigDecimal expectedTaxedAmount = amount.add(expectedInterest).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedTaxedAmount, dto.getTaxedAmount());
        
        assertEquals(0, dto.getTimeRemaining());
    }

    @Test
    void test_EntityDtoUtil_ToBillResponseDto_WithPaidBill_ShouldUseStoredInterest() {
        LocalDate dueDate = LocalDate.now().minusMonths(1);
        BigDecimal amount = new BigDecimal("150.00");
        BigDecimal storedInterest = new BigDecimal("12.50");
        
        Bill paidBill = Bill.builder()
            .billId("paid-test-id")
            .customerId("customer-2")
            .ownerFirstName("Alice")
            .ownerLastName("Johnson")
            .visitType("Checkup")
            .vetId("vet-2")
            .vetFirstName("Dr. Bob")
            .vetLastName("Wilson")
            .date(LocalDate.now().minusDays(40))
            .amount(amount)
            .billStatus(BillStatus.PAID)
            .dueDate(dueDate)
            .interestExempt(false)
            .interest(storedInterest)
            .build();

        BillResponseDTO dto = EntityDtoUtil.toBillResponseDto(paidBill);
        
        assertEquals(paidBill.getBillId(), dto.getBillId());
        assertEquals(paidBill.getCustomerId(), dto.getCustomerId());
        assertEquals(paidBill.getBillStatus(), dto.getBillStatus());
        
        assertEquals(storedInterest, dto.getInterest());
        
        BigDecimal expectedTaxedAmount = amount.add(storedInterest).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedTaxedAmount, dto.getTaxedAmount());
    }

    @Test
    void test_EntityDtoUtil_ToBillResponseDto_WithUnpaidBill_ShouldHaveZeroInterest() {
        LocalDate dueDate = LocalDate.now().plusDays(15);
        BigDecimal amount = new BigDecimal("75.00");
        
        Bill unpaidBill = Bill.builder()
            .billId("unpaid-test-id")
            .customerId("customer-3")
            .amount(amount)
            .billStatus(BillStatus.UNPAID)
            .dueDate(dueDate)
            .interestExempt(false)
            .build();

        BillResponseDTO dto = EntityDtoUtil.toBillResponseDto(unpaidBill);
        
        assertEquals(BigDecimal.ZERO.setScale(2), dto.getInterest().setScale(2));
        
        assertEquals(amount.setScale(2, RoundingMode.HALF_UP), dto.getTaxedAmount());
        
        assertTrue(dto.getTimeRemaining() > 0);
        assertEquals(15, dto.getTimeRemaining());
    }

    @Test
    void test_EntityDtoUtil_ToBillResponseDto_WithInterestExemptBill_ShouldHaveZeroInterest() {
        LocalDate dueDate = LocalDate.now().minusMonths(1);
        BigDecimal amount = new BigDecimal("200.00");
        
        Bill exemptBill = Bill.builder()
            .billId("exempt-test-id")
            .customerId("customer-4")
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(dueDate)
            .interestExempt(true) // Exempt from interest
            .build();

        BillResponseDTO dto = EntityDtoUtil.toBillResponseDto(exemptBill);
        
        assertEquals(BigDecimal.ZERO.setScale(2), dto.getInterest().setScale(2));
        assertTrue(dto.isInterestExempt());
        
        assertEquals(amount.setScale(2, RoundingMode.HALF_UP), dto.getTaxedAmount());
    }

    @Test
    void test_EntityDtoUtil_ToBillResponseDto_WithNullAmount_ShouldHandleGracefully() {
        Bill billWithNullAmount = Bill.builder()
            .billId("null-amount-test")
            .customerId("customer-5")
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.now().plusDays(10))
            .amount(null)
            .interestExempt(false)
            .build();

        BillResponseDTO dto = EntityDtoUtil.toBillResponseDto(billWithNullAmount);
        
        assertNull(dto.getAmount());
        assertEquals(BigDecimal.ZERO.setScale(2), dto.getInterest().setScale(2));
        assertEquals(BigDecimal.ZERO.setScale(2), dto.getTaxedAmount().setScale(2));
    }

    @Test
    void test_InterestCalculationUtil_CalculateCompoundInterest_VariousTimePeriods() {
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate currentDate = LocalDate.now();
        
        LocalDate dueDate1Month = currentDate.minusMonths(1);
        BigDecimal interest1Month = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate1Month, currentDate);
        BigDecimal expected1Month = new BigDecimal("1.50");
        assertEquals(expected1Month, interest1Month);
        
        LocalDate dueDate2Months = currentDate.minusMonths(2);
        BigDecimal interest2Months = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate2Months, currentDate);
        // Expected: 100 * (1.015^2) - 100 = 100 * 1.030225 - 100 = 3.02
        BigDecimal expected2Months = new BigDecimal("3.02");
        assertEquals(expected2Months, interest2Months);
        
        LocalDate dueDate12Months = currentDate.minusMonths(12);
        BigDecimal interest12Months = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate12Months, currentDate);
        // Expected: 100 * (1.015^12) - 100 ≈ 19.56
        assertTrue(interest12Months.compareTo(new BigDecimal("19.00")) > 0);
        assertTrue(interest12Months.compareTo(new BigDecimal("20.00")) < 0);
    }

    @Test
    void test_InterestCalculationUtil_CalculateCompoundInterest_WithNullValues() {
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate date = LocalDate.now();
        
        // Null amount
        assertEquals(BigDecimal.ZERO, InterestCalculationUtil.calculateCompoundInterest(null, date, date));
        
        // Null due date
        assertEquals(BigDecimal.ZERO, InterestCalculationUtil.calculateCompoundInterest(amount, null, date));
        
        assertEquals(BigDecimal.ZERO, InterestCalculationUtil.calculateCompoundInterest(amount, date, null));
    }

    @Test
    void test_InterestCalculationUtil_CalculateCompoundInterest_NonOverdue() {
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate currentDate = LocalDate.now();
        LocalDate futureDueDate = currentDate.plusDays(30);
        
        BigDecimal interest = InterestCalculationUtil.calculateCompoundInterest(amount, futureDueDate, currentDate);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void test_InterestCalculationUtil_CalculateInterest_DifferentBillStatuses() {
        LocalDate overdueDate = LocalDate.now().minusMonths(1);
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal storedInterest = new BigDecimal("15.00");
        
        Bill paidBill = Bill.builder()
            .amount(amount)
            .billStatus(BillStatus.PAID)
            .dueDate(overdueDate)
            .interestExempt(false)
            .interest(storedInterest)
            .build();
        
        BigDecimal paidInterest = InterestCalculationUtil.calculateInterest(paidBill);
        assertEquals(storedInterest, paidInterest);
        
        Bill overdueBill = Bill.builder()
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(overdueDate)
            .interestExempt(false)
            .build();
        
        BigDecimal overdueInterest = InterestCalculationUtil.calculateInterest(overdueBill);
        assertTrue(overdueInterest.compareTo(BigDecimal.ZERO) > 0);
        
        Bill unpaidOverdueBill = Bill.builder()
            .amount(amount)
            .billStatus(BillStatus.UNPAID)
            .dueDate(overdueDate)
            .interestExempt(false)
            .build();
        
        BigDecimal unpaidOverdueInterest = InterestCalculationUtil.calculateInterest(unpaidOverdueBill);
        assertTrue(unpaidOverdueInterest.compareTo(BigDecimal.ZERO) > 0);
        
        Bill unpaidNotDueBill = Bill.builder()
            .amount(amount)
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.now().plusDays(10))
            .interestExempt(false)
            .build();
        
        BigDecimal unpaidNotDueInterest = InterestCalculationUtil.calculateInterest(unpaidNotDueBill);
        assertEquals(BigDecimal.ZERO, unpaidNotDueInterest);
    }

    @Test
    void test_InterestCalculationUtil_CalculateTotalAmountOwed() {
        LocalDate overdueDate = LocalDate.now().minusMonths(1);
        BigDecimal amount = new BigDecimal("100.00");
        
        Bill overdueBill = Bill.builder()
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(overdueDate)
            .interestExempt(false)
            .build();
        
        BigDecimal totalOwed = InterestCalculationUtil.calculateTotalAmountOwed(overdueBill);
        BigDecimal expectedInterest = InterestCalculationUtil.calculateInterest(overdueBill);
        BigDecimal expectedTotal = amount.add(expectedInterest);
        
        assertEquals(expectedTotal, totalOwed);
        assertTrue(totalOwed.compareTo(amount) > 0); // Total should be greater than original amount
        
        Bill nullAmountBill = Bill.builder()
            .amount(null)
            .billStatus(BillStatus.OVERDUE)
            .build();
        
        assertEquals(BigDecimal.ZERO, InterestCalculationUtil.calculateTotalAmountOwed(nullAmountBill));
    }

    @Test
    void test_InterestCalculationUtil_CalculateOverdueMonths() {
        LocalDate currentDate = LocalDate.now();
        
        LocalDate dueDate1Month = currentDate.minusMonths(1);
        assertEquals(1, InterestCalculationUtil.calculateOverdueMonths(dueDate1Month, currentDate));
        
        LocalDate dueDate6Months = currentDate.minusMonths(6);
        assertEquals(6, InterestCalculationUtil.calculateOverdueMonths(dueDate6Months, currentDate));
        
        LocalDate dueDate1Year = currentDate.minusYears(1);
        assertEquals(12, InterestCalculationUtil.calculateOverdueMonths(dueDate1Year, currentDate));
        
        LocalDate futureDueDate = currentDate.plusDays(30);
        assertEquals(0, InterestCalculationUtil.calculateOverdueMonths(futureDueDate, currentDate));
        
        assertEquals(0, InterestCalculationUtil.calculateOverdueMonths(null, currentDate));
        assertEquals(0, InterestCalculationUtil.calculateOverdueMonths(currentDate, null));
    }

    @Test
    void test_InterestCalculationUtil_MonthlyInterestRate_Constant() {
        assertEquals(new BigDecimal("0.015"), InterestCalculationUtil.MONTHLY_INTEREST_RATE);
    }

    @Test
    void test_EntityDtoUtil_ToBillEntity_MapsCorrectly() {
        LocalDate date = LocalDate.now();
        LocalDate dueDate = LocalDate.now().plusDays(30);
        
        BillRequestDTO requestDto = BillRequestDTO.builder()
            .customerId("customer-test")
            .vetId("vet-test")
            .visitType("Surgery")
            .date(date)
            .amount(new BigDecimal("250.00"))
            .billStatus(BillStatus.UNPAID)
            .dueDate(dueDate)
            .build();
        
        Bill bill = EntityDtoUtil.toBillEntity(requestDto);
        
        assertEquals(requestDto.getCustomerId(), bill.getCustomerId());
        assertEquals(requestDto.getVetId(), bill.getVetId());
        assertEquals(requestDto.getVisitType(), bill.getVisitType());
        assertEquals(requestDto.getDate(), bill.getDate());
        assertEquals(requestDto.getAmount(), bill.getAmount());
        assertEquals(requestDto.getBillStatus(), bill.getBillStatus());
        assertEquals(requestDto.getDueDate(), bill.getDueDate());
    }

    @Test
    void test_EntityDtoUtil_GenerateUUIDString_CreatesValidUUID() {
        String uuid1 = EntityDtoUtil.generateUUIDString();
        String uuid2 = EntityDtoUtil.generateUUIDString();
        
        assertTrue(uuid1.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        assertTrue(uuid2.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void test_UtilityIntegration_ServiceMethodsUseUtilsCorrectly() {
        LocalDate dueDate = LocalDate.now().minusMonths(1);
        BigDecimal amount = new BigDecimal("100.00");
        
        Bill overdueBill = Bill.builder()
            .billId("integration-test-id")
            .customerId("customer-integration")
            .amount(amount)
            .billStatus(BillStatus.OVERDUE)
            .dueDate(dueDate)
            .interestExempt(false)
            .build();

        when(repo.findByBillId("integration-test-id")).thenReturn(Mono.just(overdueBill));
        when(repo.findAllBillsByBillStatus(BillStatus.UNPAID)).thenReturn(Flux.empty());

        
        Mono<BillResponseDTO> result = billService.getBillByBillId("integration-test-id");        StepVerifier.create(result)
            .consumeNextWith(dto -> {
                assertEquals(overdueBill.getBillId(), dto.getBillId());
                assertEquals(overdueBill.getCustomerId(), dto.getCustomerId());
                assertEquals(overdueBill.getAmount(), dto.getAmount());
                assertEquals(overdueBill.getBillStatus(), dto.getBillStatus());
                
                BigDecimal expectedInterest = InterestCalculationUtil.calculateInterest(overdueBill);
                assertEquals(expectedInterest, dto.getInterest());
                
                BigDecimal expectedTaxedAmount = amount.add(expectedInterest).setScale(2, RoundingMode.HALF_UP);
                assertEquals(expectedTaxedAmount, dto.getTaxedAmount());
            })
            .verifyComplete();
    }

    @Test
    void generateConfirmationEmail_shouldGenerateCorrectEmail() throws Exception {
        // Arrange
        UserDetails userDetails = new UserDetails();
        userDetails.setEmail("test@example.com");
        userDetails.setUsername("testuser");

        Method method = BillServiceImpl.class.getDeclaredMethod("generateConfirmationEmail", UserDetails.class);
        method.setAccessible(true);

        // Act
        Mail result = (Mail) method.invoke(billService, userDetails);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmailSendTo());
        assertEquals("Pet Clinic - Payment Confirmation", result.getEmailTitle());
        assertEquals("default", result.getTemplateName());
        assertEquals("Pet Clinic confirmation email", result.getHeader());
        assertEquals("testuser", result.getCorrespondantName());
        assertEquals("ChamplainPetClinic@gmail.com", result.getSenderName());
    }

    @Test
    void getBillsByCustomerIdAndAmountRange_shouldReturnBills() {
        String customerId = "cust-1";
        Bill bill = Bill.builder()
                .customerId(customerId)
                .amount(new BigDecimal("100.00"))
                .dueDate(LocalDate.now().plusDays(10)) // <--- add this!
                .build();

        when(repo.findByCustomerIdAndAmountBetween(customerId, new BigDecimal("50"), new BigDecimal("150")))
                .thenReturn(Flux.just(bill));

        StepVerifier.create(billService.getBillsByAmountRange(customerId, new BigDecimal("50"), new BigDecimal("150")))
                .expectNextMatches(dto -> dto.getAmount().equals(new BigDecimal("100.00")))
                .verifyComplete();
    }

    @Test
    void getBillsByCustomerIdAndAmountRange_noBills_shouldReturnEmptyFlux() {
        String customerId = "cust-999";
        BigDecimal minAmount = new BigDecimal("500");
        BigDecimal maxAmount = new BigDecimal("1000");

        when(repo.findByCustomerIdAndAmountBetween(customerId, minAmount, maxAmount))
                .thenReturn(Flux.empty());

        StepVerifier.create(billService.getBillsByAmountRange(customerId, minAmount, maxAmount))
                .expectNextCount(0) // Expect empty
                .verifyComplete();
    }

    @Test
    void getBillsByCustomerIdAndDueDateRange_shouldReturnBills() {
        String customerId = "cust-3";
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now().plusDays(5);

        Bill bill = Bill.builder()
                .billId("bill-456")
                .customerId(customerId)
                .vetId("vet-2")
                .visitType("surgery")
                .date(LocalDate.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(2)) // <-- required for due date test
                .amount(new BigDecimal("200.00"))
                .billStatus(BillStatus.UNPAID)
                .build();

        when(repo.findByCustomerIdAndDueDateBetween(customerId, start, end))
                .thenReturn(Flux.just(bill));

        StepVerifier.create(billService.getBillsByDueDateRange(customerId, start, end))
                .expectNextMatches(dto -> dto.getCustomerId().equals(customerId) && dto.getBillId().equals("bill-456"))
                .verifyComplete();
    }

    @Test
    void getBillsByCustomerIdAndDueDateRange_noBills_shouldReturnNotFound() {
        String customerId = "cust-2";
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        when(repo.findByCustomerIdAndDueDateBetween(customerId, start, end))
                .thenReturn(Flux.empty());

        StepVerifier.create(billService.getBillsByDueDateRange(customerId, start, end))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void getBillsByCustomerIdAndDateRange_shouldReturnBills() {
        String customerId = "cust-3";
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        Bill bill = Bill.builder()
                .billId("bill-123")
                .customerId(customerId)
                .vetId("vet-1")
                .visitType("checkup")
                .date(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(10)) // <-- prevent NPE
                .amount(new BigDecimal("100.00"))
                .billStatus(BillStatus.UNPAID)
                .build();

        when(repo.findByCustomerIdAndDateBetween(customerId, start, end))
                .thenReturn(Flux.just(bill));

        StepVerifier.create(billService.getBillsByCustomerIdAndDateRange(customerId, start, end))
                .expectNextMatches(dto -> dto.getCustomerId().equals(customerId) && dto.getBillId().equals("bill-123"))
                .verifyComplete();
    }

    @Test
    void getBillsByCustomerIdAndDateRange_noBills_shouldEmitNotFound() {
        String customerId = "cust-999";
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        when(repo.findByCustomerIdAndDateBetween(customerId, start, end))
                .thenReturn(Flux.empty());

        StepVerifier.create(billService.getBillsByCustomerIdAndDateRange(customerId, start, end))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatus() == HttpStatus.NOT_FOUND &&
                                ((ResponseStatusException) throwable).getReason().contains("No bills found")
                )
                .verify();
    }

    @Test
    public void testGenerateStaffBillPdf_Success() {
        Bill mockBill = Bill.builder()
                .billId("billId-2")
                .customerId("customerId-2")
                .ownerFirstName("Jane")
                .ownerLastName("Smith")
                .visitType("Surgery")
                .vetId("vetId-2")
                .amount(new BigDecimal(250.0))
                .billStatus(BillStatus.PAID)
                .date(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        String billId = mockBill.getBillId();
        String currency = "USD";

        when(repo.findByBillId(billId)).thenReturn(Mono.just(mockBill));

        Mono<byte[]> pdfBytesMono = billService.generateStaffBillPdf(billId, currency);

        StepVerifier.create(pdfBytesMono)
                .assertNext(pdfBytes -> {
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                })
                .verifyComplete();
    }

    @Test
    public void testGenerateStaffBillPdf_BillNotFound() {
        when(repo.findByBillId(anyString())).thenReturn(Mono.empty());

        String currency = "USD";
        Mono<byte[]> pdfMono = billService.generateStaffBillPdf("nonexistentBillId", currency);

        StepVerifier.create(pdfMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Bill not found for given ID"))
                .verify();
    }

}
