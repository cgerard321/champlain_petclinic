package com.petclinic.bffapigateway.presentationlayer.v1.Bills;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillRequestDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.dtos.Bills.BillStatus;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.presentationlayer.v1.BillControllerV1;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static com.petclinic.bffapigateway.dtos.Bills.BillStatus.PAID;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        BillControllerV1.class,
        BillServiceClient.class
})
@WebFluxTest(controllers = BillControllerV1.class, excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = {BillServiceClient.class}))
public class BillsControllerUnitTest {

    @Autowired
    WebTestClient client;

    @MockBean
    private BillServiceClient billServiceClient;

    @Autowired
    private WebTestClient webTestClient;

    private final String baseBillURL = "/api/gateway/bills";


    private final BillResponseDTO billresponse = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("3")
            .date(LocalDate.now().plusDays(1))
            .amount(new BigDecimal("100.0"))
            .taxedAmount(new BigDecimal("0.0"))
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.now().plusDays(46))
            .build();

    private BillResponseDTO billresponse2 = BillResponseDTO.builder()
            .billId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361b")
            .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
            .visitType("general")
            .vetId("2")
            .date(LocalDate.now().plusDays(1))
            .amount(new BigDecimal("120.0"))
            .taxedAmount(new BigDecimal("10.0"))
            .billStatus(BillStatus.UNPAID)
            .dueDate(LocalDate.now().plusDays(46))
            .build();

    @Test
    void shouldGetAllBills() {
        BillResponseDTO billResponseDTO =  BillResponseDTO.builder()
                .billId("BillUUID")
                .customerId("1")
                .visitType("Test type")
                .vetId("1")
                .date(LocalDate.now().plusDays(1))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.UNPAID)
                .amount(new BigDecimal("25.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(13L)
                .build();


        BillResponseDTO billResponseDTO2 = BillResponseDTO.builder()
                .billId("BillUUID2")
                .customerId("2")
                .visitType("Test type")
                .vetId("2")
                .date(LocalDate.now().plusDays(1))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.UNPAID)
                .amount(new BigDecimal("27.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(13L)
                .build();
        when(billServiceClient.getAllBills()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllBills();
    }

    @Test
    void shouldGetAllPaidBills() {
        BillResponseDTO billResponseDTO = BillResponseDTO.builder()
                .billId("BillUUID")
                .customerId("1")
                .visitType("Test type")
                .vetId("1")
                .date(LocalDate.now().plusDays(1))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(PAID)
                .amount(new BigDecimal("25.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(0L)
                .build();

        BillResponseDTO billResponseDTO2 = BillResponseDTO.builder()
                .billId("BillUUID2")
                .customerId("2")
                .visitType("Test type")
                .vetId("2")
                .date(LocalDate.now().plusDays(1))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(PAID)
                .amount(new BigDecimal("27.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(0L)
                .build();
        when(billServiceClient.getAllPaidBills()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills/paid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllPaidBills();
    }

    @Test
    void shouldGetAllUnpaidBills() {
        BillResponseDTO billResponseDTO = BillResponseDTO.builder()
                .billId("BillUUID")
                .customerId("1")
                .visitType("Test type")
                .vetId("1")
                .date(LocalDate.now().plusDays(1))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.UNPAID)
                .amount(new BigDecimal("25.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(13L)
                .build();

        BillResponseDTO billResponseDTO2 = BillResponseDTO.builder()
                .billId("BillUUID2")
                .customerId("2")
                .visitType("Test type")
                .vetId("2")
                .date(LocalDate.now().plusDays(1))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.UNPAID)
                .amount(new BigDecimal("27.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(13L)
                .build();
        when(billServiceClient.getAllUnpaidBills()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills/unpaid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllUnpaidBills();
    }

    @Test
    void shouldGetAllOverdueBills() {
        BillResponseDTO billResponseDTO = BillResponseDTO.builder()
                .billId("BillUUID")
                .customerId("1")
                .visitType("Test type")
                .vetId("1")
                .date(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(3))
                .billStatus(BillStatus.OVERDUE)
                .amount(new BigDecimal("25.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(0L)
                .build();

        BillResponseDTO billResponseDTO2 = BillResponseDTO.builder()
                .billId("BillUUID2")
                .customerId("2")
                .visitType("Test type")
                .vetId("2")
                .date(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(3))
                .billStatus(BillStatus.OVERDUE)
                .amount(new BigDecimal("27.00"))
                .taxedAmount(BigDecimal.ZERO)
                .timeRemaining(0L)
                .build();
        when(billServiceClient.getAllOverdueBills()).thenReturn(Flux.just(billResponseDTO,billResponseDTO2));

        client.get()
                .uri("/api/gateway/bills/overdue")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .value((list)->assertEquals(list.size(),2));
        Mockito.verify(billServiceClient,times(1)).getAllOverdueBills();
    }

    @Test
    void shouldGetBillById() {
        // Arrange
        String billId = UUID.randomUUID().toString();
        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId(billId);
        bill.setCustomerId("1");
        bill.setAmount(new BigDecimal("499"));
        bill.setVisitType("Test");

        when(billServiceClient.getBillById(billId))
                .thenReturn(Mono.just(bill));

        // Act & Assert
        client.get()
                .uri("/api/gateway/bills/{billId}", billId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.billId").isEqualTo(billId)
                .jsonPath("$.customerId").isEqualTo(bill.getCustomerId())
                .jsonPath("$.visitType").isEqualTo(bill.getVisitType())
                .jsonPath("$.amount").isEqualTo(bill.getAmount());

        Mockito.verify(billServiceClient, times(1)).getBillById(billId);
    }

    @Test
    public void getBillsByVetId(){
        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId(UUID.randomUUID().toString());
        bill.setVetId("1");
        bill.setAmount(new BigDecimal("499"));
        bill.setVisitType("Test");

        when(billServiceClient.getBillsByVetId(bill.getVetId()))
                .thenReturn(Flux.just(bill));

        client.get()
                .uri("/api/gateway/bills/vets/{vetId}", bill.getVetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    List<BillResponseDTO> billResponseDTOS = response.getResponseBody();
                    Assertions.assertNotNull(billResponseDTOS);
                });

    }

    @Test
    void getBillUsingMissingPath(){
        client.get()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void getBillNotFound(){
        client.get()
                .uri("/bills/{billId}", 100)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills/100")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void createBill() {
        // Arrange
        BillResponseDTO billResponseDTO = new BillResponseDTO();
        billResponseDTO.setBillId("9");
        billResponseDTO.setCustomerId("12345");
        billResponseDTO.setVetId("67890");
        billResponseDTO.setVisitType("Adoption");
        billResponseDTO.setDate(LocalDate.now().plusDays(1)); // Updated to a future date
        billResponseDTO.setAmount(new BigDecimal("600"));
        billResponseDTO.setBillStatus(BillStatus.PAID);
        billResponseDTO.setDueDate(LocalDate.now().plusDays(30)); // Ensure dueDate is also valid

        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setCustomerId("12345");
        billRequestDTO.setVetId("67890");
        billRequestDTO.setVisitType("Adoption");
        billRequestDTO.setDate(LocalDate.now().plusDays(1)); // Updated to a future date
        billRequestDTO.setAmount(new BigDecimal("600"));
        billRequestDTO.setBillStatus(BillStatus.PAID);
        billRequestDTO.setDueDate(LocalDate.now().plusDays(30)); // Ensure dueDate is also valid

        when(billServiceClient.createBill(billRequestDTO, false, "CAD", "JWTToken"))
                .thenReturn(Mono.just(billResponseDTO));

        // Act & Assert
        client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/gateway/bills")
                        .queryParam("sendEmail", false)
                        .queryParam("currency", "CAD")
                        .build())
                .cookie("Bearer", "JWTToken") // Ensure the token is valid
                .body(Mono.just(billRequestDTO), BillRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.billId").isEqualTo("9")
                .jsonPath("$.customerId").isEqualTo("12345")
                .jsonPath("$.vetId").isEqualTo("67890")
                .jsonPath("$.visitType").isEqualTo("Adoption")
                .jsonPath("$.date").isEqualTo(LocalDate.now().plusDays(1).toString()) // Match updated date
                .jsonPath("$.amount").isEqualTo(600)
                .jsonPath("$.billStatus").isEqualTo("PAID")
                .jsonPath("$.dueDate").isEqualTo(LocalDate.now().plusDays(30).toString()); // Match updated dueDate

        assertEquals(billResponseDTO.getBillId(), "9");
    }

    @Test
    void putBillRequestNotFound(){
        client.put()
                .uri("/bills/{billId}", 100)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills/100")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void putBillWithMissingPath(){
        client.put()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/bills")
                .jsonPath("$.message").isEqualTo(null);
    }

    @Test
    void shouldDeleteBillById(){
        when(billServiceClient.deleteBill("9"))
                .thenReturn(Mono.empty());
        client.delete()
                .uri("/api/gateway/bills/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(billServiceClient, times(1)).deleteBill("9");
    }

    @Test
    void shouldDeleteBillsByVetId() {
        when(billServiceClient.deleteBillsByVetId("9"))
                .thenReturn(Flux.empty());
        client.delete()
                .uri("/api/gateway/bills/vets/9")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void getAllBillsByVetName(){
        // Arrange
        String vetFirstName = "John";
        String vetLastName = "Doe";

        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId("1");
        bill.setVetFirstName(vetFirstName);
        bill.setVetLastName(vetLastName);

        when(billServiceClient.getBillsByVetName(vetFirstName, vetLastName))
                .thenReturn(Flux.just(bill));

        // Act & Assert
        client.get()
                .uri("/api/gateway/bills/vet/" + vetFirstName + "/" + vetLastName)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    assertEquals(1, response.getResponseBody().size());
                });
    }

    @Test
    public void getBillsByVisitType(){
        String visitType = "Checkup";

        BillResponseDTO bill = new BillResponseDTO();
        bill.setBillId("1");
        bill.setVisitType(visitType);

        when(billServiceClient.getBillsByVisitType(visitType))
                .thenReturn(Flux.just(bill));

        client.get()
                .uri("/api/gateway/bills/visitType/" + visitType)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .consumeWith(response -> {
                    assertEquals(1, response.getResponseBody().size());
                });
    }

    @Test
    void archiveBill_ShouldReturnNoContent_WhenNoBillsArchived() {
        Mockito.when(billServiceClient.archiveBill()).thenReturn(Flux.empty());

        client.patch()
                .uri("/api/gateway/bills/archive")
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(billServiceClient).archiveBill();
    }

    @Test
    public void whenGetAllBills_thenReturnAllBills(){
        when(billServiceClient.getAllBills())
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient
                .get()
                .uri(baseBillURL)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1))
                .getAllBills();
    }

    @Test
    public void whenGetBillById_thenReturnBill(){
        when(billServiceClient.getBillById("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a"))
                .thenReturn(Mono.just(billresponse));

        webTestClient
                .get()
                .uri(baseBillURL + "/{billId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(1)
                .contains(billresponse);

        verify(billServiceClient, times(1))
                .getBillById("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
    }

    @Test
    public void whenGetAllPaidBills_thenReturnAllPaidBills(){
        when(billServiceClient.getAllPaidBills())
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient
                .get()
                .uri(baseBillURL + "/paid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1))
                .getAllPaidBills();
    }

    @Test
    public void whenGetAllUnpaidBills_thenReturnAllUnpaidBills(){
        when(billServiceClient.getAllUnpaidBills())
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient
                .get()
                .uri(baseBillURL + "/unpaid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1))
                .getAllUnpaidBills();
    }

    @Test
    public void whenGetAllOverdueBills_thenReturnAllOverdueBills() {
        when(billServiceClient.getAllOverdueBills())
                .thenReturn(Flux.just(billresponse, billresponse2));

        webTestClient
                .get()
                .uri(baseBillURL + "/overdue")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(billresponse, billresponse2);

        verify(billServiceClient, times(1))
                .getAllOverdueBills();
    }

    @Test
    public void whenDeleteBill_Succeeds_ThenReturnNoContent() {
        String billId = "B-77";

        when(billServiceClient.deleteBill(billId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(baseBillURL + "/{billId}", billId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_NO_CONTENT);

        verify(billServiceClient, times(1)).deleteBill(billId);
    }

    @Test
    public void whenDeleteBill_Unprocessable_ThenReturn422() {
        String billId = "B-88";

        when(billServiceClient.deleteBill(billId))
                .thenReturn(Mono.error(new InvalidInputException("Cannot delete a bill that is unpaid or overdue.")));

        webTestClient.delete()
                .uri(baseBillURL + "/{billId}", billId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        verify(billServiceClient, times(1)).deleteBill(billId);
    }

    @Test
    public void shouldDeleteAllBills(){
        when(billServiceClient.deleteAllBills())
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(baseBillURL)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(billServiceClient, times(1))
                .deleteAllBills();
    }

    @Test
    void shouldUpdateBill() {
        // Arrange
        String billId = "12345";
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAmount(new BigDecimal("500"));
        billRequestDTO.setVisitType("Checkup");

        BillResponseDTO billResponseDTO = new BillResponseDTO();
        billResponseDTO.setBillId(billId);
        billResponseDTO.setAmount(new BigDecimal("500"));
        billResponseDTO.setVisitType("Checkup");

        when(billServiceClient.updateBill(eq(billId), any(Mono.class)))
                .thenReturn(Mono.just(billResponseDTO));

        // Act & Assert
        webTestClient.put()
                .uri(baseBillURL + "/{billId}", billId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(billRequestDTO), BillRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.billId").isEqualTo(billId)
                .jsonPath("$.amount").isEqualTo(billResponseDTO.getAmount())
                .jsonPath("$.visitType").isEqualTo(billResponseDTO.getVisitType());

        verify(billServiceClient, times(1)).updateBill(eq(billId), any(Mono.class));
    }

    @Test
    void shouldGetAllBillsByPage() {
        // Arrange
        BillResponseDTO bill1 = BillResponseDTO.builder()
                .billId("1")
                .customerId("123")
                .visitType("Checkup")
                .vetId("456")
                .build();

        BillResponseDTO bill2 = BillResponseDTO.builder()
                .billId("2")
                .customerId("789")
                .visitType("Surgery")
                .vetId("101")
                .build();

        when(billServiceClient.getAllBillsByPage(Optional.of(0), Optional.of(10), null, null, null, null, null, null, null, null))
                .thenReturn(Flux.just(bill1, bill2));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseBillURL + "/page")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseDTO.class)
                .hasSize(2)
                .contains(bill1, bill2);

        verify(billServiceClient, times(1))
                .getAllBillsByPage(Optional.of(0), Optional.of(10), null, null, null, null, null, null, null, null);
    }

    @Test
    void shouldGetTotalNumberOfBills() {
        // Arrange
        long totalBills = 100L;
        when(billServiceClient.getTotalNumberOfBills()).thenReturn(Mono.just(totalBills));

        // Act & Assert
        webTestClient.get()
                .uri(baseBillURL + "/bills-count")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(totalBills);

        verify(billServiceClient, times(1)).getTotalNumberOfBills();
    }

    @Test
    void shouldGetTotalNumberOfBillsWithFilters() {
        // Arrange
        long filteredBills = 50L;
        when(billServiceClient.getTotalNumberOfBillsWithFilters("1", "123", "John", "Doe", "Checkup", "456", "Jane", "Smith"))
                .thenReturn(Mono.just(filteredBills));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(baseBillURL + "/bills-filtered-count")
                        .queryParam("billId", "1")
                        .queryParam("customerId", "123")
                        .queryParam("ownerFirstName", "John")
                        .queryParam("ownerLastName", "Doe")
                        .queryParam("visitType", "Checkup")
                        .queryParam("vetId", "456")
                        .queryParam("vetFirstName", "Jane")
                        .queryParam("vetLastName", "Smith")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(filteredBills);

        verify(billServiceClient, times(1))
                .getTotalNumberOfBillsWithFilters("1", "123", "John", "Doe", "Checkup", "456", "Jane", "Smith");
    }
}
