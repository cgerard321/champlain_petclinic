package com.petclinic.billing.presentationlayer;

import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.dataaccesslayer.*;
import com.petclinic.billing.domainclientlayer.Auth.AuthServiceClient;
import com.petclinic.billing.domainclientlayer.Auth.Rethrower;
import com.petclinic.billing.exceptionshandling.exceptions.NotFoundException;
import com.petclinic.billing.presentationlayer.models.BillRequestModel;
import com.petclinic.billing.presentationlayer.models.BillResponseModel;
import com.petclinic.billing.util.InterestCalculationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.Month;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

@WebFluxTest(controllers = BillController.class)
class BillControllerUnitTest {

    private BillResponseModel responseDTO = buildBillResponseDTO();
    private BillResponseModel unpaidResponseDTO = buildUnpaidBillResponseDTO();
    private BillResponseModel overdueResponseDTO = buildBillOverdueResponseDTO();
    private final String BILL_ID_OK = responseDTO.getBillId();
    private final String CUSTOMER_ID_OK = responseDTO.getCustomerId();
    private final String VET_ID_OK = responseDTO.getVetId();

    @Autowired
    private WebTestClient client;

    @MockBean
    BillService billService;
    @MockBean
    AuthServiceClient authServiceClient;

    @MockBean
    Rethrower rethrower;

    @Test
    void getBillByBillId() {

        when(billService.getBillByBillId(anyString())).thenReturn(Mono.just(responseDTO));

        client.get()
                .uri("/bills/" + BILL_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitType").isEqualTo(responseDTO.getVisitType())
                .jsonPath("$.customerId").isEqualTo(responseDTO.getCustomerId())
                .jsonPath("$.amount").isEqualTo(responseDTO.getAmount());

        Mockito.verify(billService, times(1)).getBillByBillId(BILL_ID_OK);
    }

    @Test
    void getAllBills() {

        when(billService.getAllBills()).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });
        Mockito.verify(billService, times(1)).getAllBills();
    }

    @Test
    void getAllPaidBills() {
        when(billService.getAllBillsByStatus(BillStatus.PAID)).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/paid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });

        Mockito.verify(billService, times(1)).getAllBillsByStatus(BillStatus.PAID);
    }

    @Test
    void getAllUnpaidBills() {
        when(billService.getAllBillsByStatus(BillStatus.UNPAID)).thenReturn(Flux.just(unpaidResponseDTO));

        client.get()
                .uri("/bills/unpaid")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });

        Mockito.verify(billService, times(1)).getAllBillsByStatus(BillStatus.UNPAID);
    }

    @Test
    void getAllOverdueBills() {
        when(billService.getAllBillsByStatus(BillStatus.OVERDUE)).thenReturn(Flux.just(overdueResponseDTO));

        client.get()
                .uri("/bills/overdue")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });

        Mockito.verify(billService, times(1)).getAllBillsByStatus(BillStatus.OVERDUE);
    }

    @Test
    void getBillByCustomerId() {

        when(billService.getBillsByCustomerId(anyString())).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/customer/" + responseDTO.getCustomerId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });
        Mockito.verify(billService, times(1)).getBillsByCustomerId(CUSTOMER_ID_OK);
    }

    @Test
    void getBillByVetId() {

        when(billService.getBillsByVetId(anyString())).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/vet/" + responseDTO.getVetId())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });

        Mockito.verify(billService, times(1)).getBillsByVetId(VET_ID_OK);
    }

    @Test
    void getAllBillsByOwnerName() {
        when(billService.getAllBillsByCustomerName(anyString(), anyString())).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/owner/" + responseDTO.getOwnerFirstName() + "/" + responseDTO.getOwnerLastName())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });

        Mockito.verify(billService, times(1)).getAllBillsByCustomerName(responseDTO.getOwnerFirstName(), responseDTO.getOwnerLastName());
    }

    @Test
    void getBillsByVetName() {
        when(billService.getAllBillsByVetName(anyString(), anyString())).thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/vet/" + responseDTO.getVetFirstName() + "/" + responseDTO.getVetLastName())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                });

        Mockito.verify(billService, times(1)).getAllBillsByVetName(responseDTO.getVetFirstName(), responseDTO.getVetLastName());
    }

    @Test
    void getBillsByVisitType() {
        String visitType = "Regular";

        when(billService.getAllBillsByVisitType(eq(visitType)))
                .thenReturn(Flux.just(responseDTO));

        client.get()
                .uri("/bills/visitType/" + visitType)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(BillResponseModel.class)
                .consumeWith(response -> {
                    List<BillResponseModel> billResponseModels = response.getResponseBody();
                    Assertions.assertNotNull(billResponseModels);
                    Assertions.assertFalse(billResponseModels.isEmpty());
                    Assertions.assertEquals(visitType, billResponseModels.get(0).getVisitType());
                });

        Mockito.verify(billService, times(1)).getAllBillsByVisitType(visitType);
        Mockito.verifyNoMoreInteractions(billService);
    }

    @Test
    void deleteAllBills() {
        when(billService.deleteAllBills()).thenReturn(Mono.empty());

        client.delete()
                .uri("/bills")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).deleteAllBills();
    }

    @Test
    void deleteBill() {

        when(billService.deleteBill(anyString())).thenReturn(Mono.empty());

        client.delete()
                .uri("/bills/" + responseDTO.getBillId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).deleteBill(BILL_ID_OK);
    }

    @Test
    void deleteBillByVetId() {

        when(billService.deleteBillsByVetId(anyString())).thenReturn(Flux.empty());

        client.delete()
                .uri("/bills/vet/" + responseDTO.getVetId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(billService, times(1)).deleteBillsByVetId(VET_ID_OK);
    }

    @Test
    void deleteBillsByCustomerId() {

        when(billService.deleteBillsByCustomerId(anyString())).thenReturn(Flux.empty());

        client.delete()
                .uri("/bills/customer/" + responseDTO.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()//.isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
                .expectBody();

        Mockito.verify(billService, times(1)).deleteBillsByCustomerId(CUSTOMER_ID_OK);
    }

    @Test
    void testArchiveBill() {
        when(billService.archiveBill()).thenReturn(Flux.empty());

        client.patch()
                .uri("/bills/archive")
                .exchange()
                .expectStatus().isNoContent();

        verify(billService, times(1)).archiveBill();
    }


    private BillResponseModel buildBillResponseDTO() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 15);

        return BillResponseModel.builder()
                .billId("BillUUID")
                .customerId("1")
                .vetId("1")
                .visitType("Regular")
                .date(date)
                .amount(new BigDecimal(13.37))
                .taxedAmount(new BigDecimal(15.10))
                .interest(new BigDecimal(0.00))
                .billStatus(BillStatus.PAID)
                .dueDate(dueDate)
                .ownerFirstName("John")
                .ownerLastName("Doe")
                .vetFirstName("Jane") // Set valid vetFirstName
                .vetLastName("Smith") // Set valid vetLastName
                .build();
    }

    private BillResponseModel buildUnpaidBillResponseDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.OCTOBER, 5);

        return BillResponseModel.builder().billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.UNPAID).dueDate(dueDate).build();
    }

    private BillResponseModel buildBillOverdueResponseDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        LocalDate date = calendar.getTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);

        return BillResponseModel.builder().billId("BillUUID").customerId("1").vetId("1").visitType("Test Type").date(date).amount(new BigDecimal(13.37)).billStatus(BillStatus.OVERDUE).dueDate(dueDate).build();
    }

    @Test
    void whenValidParametersForPaginationProvided_thenShouldCallServiceWithCorrectParams() {
        when(billService.getAllBillsByPage(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(responseDTO));

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills")
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseModel.class)
                .hasSize(1);  // Checking that the response body has exactly 1 element

        Mockito.verify(billService, times(1))
                .getAllBillsByPage(PageRequest.of(1, 10), null, null, null,
                        null, null, null, null, null);
    }

    @Test
    void whenGetBillsByMonthCalled_thenShouldCallServiceWithCorrectParams() {
        // Mocking the service layer response
        when(billService.getBillsByMonth(anyInt(), anyInt()))
                .thenReturn(Flux.just(responseDTO));

        // Triggering the controller endpoint
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills/month")
                        .queryParam("month", 1)
                        .queryParam("year", 2022)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BillResponseModel.class)
                .hasSize(1);  // Checking that the response body has exactly 1 element

        // Verifying the correct method calls with correct argument order
        Mockito.verify(billService, times(1))
                .getBillsByMonth(2022, 1);  // year first, then month
    }

    @Test
    void whenGetBillsByMonthCalledWithInvalidParams_thenShouldBadRequest() {
        // Triggering the controller endpoint with invalid parameters
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/bills/month")
                        .queryParam("month", 13)
                        .queryParam("year", -1)
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void whenPostingBillWithNoBillStatus_thenReturnsCreated() {
        // Arrange
        BillRequestModel validBill = BillRequestModel.builder()
                .customerId("C001")
                .visitType("Checkup")
                .vetId("V100")
                .date(LocalDate.now())
                .amount(new BigDecimal(100.0))
                .billStatus(null)
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        BillResponseModel mockResponse = BillResponseModel.builder()
                .billId("mock-bill-id")
                .customerId("C001")
                .visitType("Checkup")
                .vetId("V100")
                .date(LocalDate.now())
                .amount(new BigDecimal(100.0))
                .billStatus(BillStatus.UNPAID)
                .dueDate(LocalDate.now().plusDays(10))
                .build();

        when(billService.createBill(any(Mono.class), eq(false), eq("CAD"), eq("jwtToken")))
                .thenReturn(Mono.just(mockResponse));

        when(billService.getAllBills()).thenReturn(Flux.empty());

        client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/bills")
                        .queryParam("sendEmail", false)
                        .queryParam("currency", "CAD")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .cookie("Bearer", "jwtToken")
                .bodyValue(validBill)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BillResponseModel.class)
                .value(response -> {
                    assertThat(response.getBillStatus()).isEqualTo(BillStatus.UNPAID);
                    assertThat(response.getCustomerId()).isEqualTo("C001");
                    assertThat(response.getVetId()).isEqualTo("V100");
                });

        verify(billService, times(1))
                .createBill(any(Mono.class), eq(false), eq("CAD"), eq("jwtToken"));
    }

    @Test
    void whenDeletingNonExistentBill_thenReturnNotFound() {
        String invalidBillId = "NON_EXISTENT_ID";

        Mockito.when(billService.deleteBill(invalidBillId))
                .thenReturn(Mono.error(new NotFoundException("Bill not found")));

        client.delete()
                .uri("/bills/{billId}", invalidBillId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bill not found");
    }

    @Test
    void getBillByBillId_ShouldReturnInterest() {
        // Calculate expected compound interest using centralized utility
        LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);
        LocalDate currentDate = LocalDate.now();
                
        BigDecimal expectedInterest = InterestCalculationUtil.calculateCompoundInterest(
                overdueResponseDTO.getAmount(), dueDate, currentDate);
                overdueResponseDTO.setInterest(expectedInterest);

                when(billService.getBillByBillId(anyString())).thenReturn(Mono.just(overdueResponseDTO));

                client.get()
                        .uri("/bills/" + overdueResponseDTO.getBillId())
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody()
                        .jsonPath("$.interest").isEqualTo(expectedInterest);

                Mockito.verify(billService, times(1)).getBillByBillId(overdueResponseDTO.getBillId());
        }

        @Test
        void getInterest_WithValidBillId_ShouldReturnInterestAmount() {
                BigDecimal expectedInterest = new BigDecimal("5.50");
                BillResponseModel billWithInterest = buildBillResponseDTO();
                billWithInterest.setInterest(expectedInterest);

                when(billService.getBillByBillId(BILL_ID_OK)).thenReturn(Mono.just(billWithInterest));

                client.get()
                        .uri("/bills/" + BILL_ID_OK + "/interest")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody(BigDecimal.class)
                        .isEqualTo(expectedInterest);

                Mockito.verify(billService, times(1)).getBillByBillId(BILL_ID_OK);
        }

        @Test
        void getInterest_WithZeroInterest_ShouldReturnZero() {
                BillResponseModel billWithZeroInterest = buildBillResponseDTO();
                billWithZeroInterest.setInterest(BigDecimal.ZERO);

                when(billService.getBillByBillId(BILL_ID_OK)).thenReturn(Mono.just(billWithZeroInterest));

                client.get()
                        .uri("/bills/" + BILL_ID_OK + "/interest")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody(BigDecimal.class)
                        .isEqualTo(BigDecimal.ZERO);

                Mockito.verify(billService, times(1)).getBillByBillId(BILL_ID_OK);
        }

        @Test
        void getInterest_WithNonExistentBillId_ShouldReturnError() {
                String nonExistentBillId = "NON_EXISTENT_ID";

                when(billService.getBillByBillId(nonExistentBillId))
                        .thenReturn(Mono.error(new NotFoundException("Bill not found")));

                client.get()
                        .uri("/bills/" + nonExistentBillId + "/interest")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isNotFound();

                Mockito.verify(billService, times(1)).getBillByBillId(nonExistentBillId);
        }

        @Test
        void getTotal_WithValidBillId_ShouldReturnAmountPlusInterest() {
                BigDecimal amount = new BigDecimal("100.00");
                BigDecimal interest = new BigDecimal("5.50");
                BigDecimal expectedTotal = amount.add(interest); // 105.50

                BillResponseModel billWithInterest = buildBillResponseDTO();
                billWithInterest.setAmount(amount);
                billWithInterest.setInterest(interest);

                when(billService.getBillByBillId(BILL_ID_OK)).thenReturn(Mono.just(billWithInterest));

                client.get()
                        .uri("/bills/" + BILL_ID_OK + "/total")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody(BigDecimal.class)
                        .isEqualTo(expectedTotal);

                Mockito.verify(billService, times(1)).getBillByBillId(BILL_ID_OK);
        }

        @Test
        void getTotal_WithZeroInterest_ShouldReturnOnlyAmount() {
                BigDecimal amount = new BigDecimal("100.00");
                BigDecimal interest = BigDecimal.ZERO;
                BigDecimal expectedTotal = amount; // 100.00

                BillResponseModel billWithZeroInterest = buildBillResponseDTO();
                billWithZeroInterest.setAmount(amount);
                billWithZeroInterest.setInterest(interest);

                when(billService.getBillByBillId(BILL_ID_OK)).thenReturn(Mono.just(billWithZeroInterest));

                client.get()
                        .uri("/bills/" + BILL_ID_OK + "/total")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody(BigDecimal.class)
                        .isEqualTo(expectedTotal);

                Mockito.verify(billService, times(1)).getBillByBillId(BILL_ID_OK);
        }

        @Test
        void getTotal_WithNonExistentBillId_ShouldReturnError() {
                String nonExistentBillId = "NON_EXISTENT_ID";

                when(billService.getBillByBillId(nonExistentBillId))
                        .thenReturn(Mono.error(new NotFoundException("Bill not found")));

                client.get()
                        .uri("/bills/" + nonExistentBillId + "/total")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isNotFound();

                Mockito.verify(billService, times(1)).getBillByBillId(nonExistentBillId);
        }

        @Test
        void getTotal_WithOverdueBill_ShouldReturnAmountPlusCompoundInterest() {
                // Create an overdue bill with compound interest
                LocalDate dueDate = LocalDate.of(2022, Month.AUGUST, 15);
                BigDecimal amount = new BigDecimal("100.00");
                BigDecimal expectedInterest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, LocalDate.now());
                BigDecimal expectedTotal = amount.add(expectedInterest);

                BillResponseModel overdueBill = buildBillOverdueResponseDTO();
                overdueBill.setAmount(amount);
                overdueBill.setInterest(expectedInterest);

                when(billService.getBillByBillId(BILL_ID_OK)).thenReturn(Mono.just(overdueBill));

                client.get()
                        .uri("/bills/" + BILL_ID_OK + "/total")
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_JSON)
                        .expectBody(BigDecimal.class)
                        .isEqualTo(expectedTotal);

                Mockito.verify(billService, times(1)).getBillByBillId(BILL_ID_OK);
        }
}
