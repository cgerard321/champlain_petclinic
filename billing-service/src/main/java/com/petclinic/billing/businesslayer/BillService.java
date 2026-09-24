package com.petclinic.billing.businesslayer;

import java.math.BigDecimal;
import com.petclinic.billing.dataaccesslayer.*;
import com.petclinic.billing.presentationlayer.models.BillRequestModel;
import com.petclinic.billing.presentationlayer.models.BillResponseModel;
import com.petclinic.billing.presentationlayer.models.PaymentRequestModel;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
public interface BillService {
    Mono<BillResponseModel> getBillByBillId(String billId);

    Flux<BillResponseModel> getAllBillsByStatus(BillStatus status);

    Mono<Bill>CreateBillForDB(Mono<Bill> bill);

    Flux<BillResponseModel> getAllBills();

    Flux<BillResponseModel> getAllBillsByPage(Pageable pageable,
                                              String billId,
                                              String customerId,
                                              String customerFirstName,
                                              String customerLastName,
                                              String visitType,
                                              String vetId,
                                              String vetFirstName,
                                              String vetLastName);

    Mono<Long> getNumberOfBillsWithFilters(String billId,
                                           String customerId,
                                           String customerFirstName,
                                           String customerLastName,
                                           String visitType,
                                           String vetId,
                                           String vetFirstName,
                                           String vetLastName);

    Flux<BillResponseModel> getAllBillsByCustomerName(String customerFirstName, String customerLastName);

    Flux<BillResponseModel> getAllBillsByVetName(String vetFirstName, String vetLastName);

    Flux<BillResponseModel> getAllBillsByVisitType(String visitType);

    Mono<BillResponseModel> createBill(@RequestBody Mono<BillRequestModel> model, boolean sendEmail, String currency, String jwtToken);

    Mono<Void> deleteBill(@RequestParam(value = "billId", required = true) String billId);

    Flux<BillResponseModel> getBillsByVetId(@RequestParam(value = "vetId", required = true) String vetId);

    Flux<Void> deleteBillsByVetId(@RequestParam(value="vetId", required = true) String vetId);
    Flux<Void> deleteBillsByCustomerId(@RequestParam(value="customerId", required = true)String customerId);

    Mono<BillResponseModel> updateBill(String billId, Mono<BillRequestModel> billRequestDTO);

    Mono<Void> deleteAllBills();

    Mono<Void> setInterestExempt(String billId, boolean isExempt);

    Mono<BigDecimal> getInterest(String billId, BigDecimal amount, int overdueMonths);

    Mono<BigDecimal> getTotalWithInterest(String billId, BigDecimal amount, int overdueMonths);

    Flux<Bill> archiveBill();

    // Method to fetch bills by month
    Flux<BillResponseModel> getBillsByMonth(int year, int month);

    // Method to check and update bills that are past due date from UNPAID to OVERDUE
    Mono<Void> updateOverdueBills();


///////////////// Used by both BillController and CustomerBillsController /////////////////////

    Flux<BillResponseModel> getBillsByCustomerId(@RequestParam(value = "customerId", required = true) String customerId);


//////////////// Used by CustomerBillsController only ///////////////////////////////////////////

    // Fetch a specific bill for a customer
    Mono<BillResponseModel> getBillByCustomerIdAndBillId(String customerId, String billId);

    // Fetch filtered bills by status
    Flux<BillResponseModel> getBillsByCustomerIdAndStatus(String customerId, BillStatus status);

    // Method to generate the bill PDF
    Mono<byte[]> generateBillPdf(String customerId, String billId, String currency);

    Mono<BigDecimal> calculateCurrentBalance(String customerId);

    Mono<BillResponseModel> processPayment(String customerId, String billId, PaymentRequestModel paymentRequestModel, String jwtToken);

    Flux<BillResponseModel> getBillsByAmountRange(String customerId, BigDecimal minAmount, BigDecimal maxAmount);

    Flux<BillResponseModel> getBillsByDueDateRange(String customerId, LocalDate startDate, LocalDate endDate);

    Flux<BillResponseModel> getBillsByCustomerIdAndDateRange(String customerId, LocalDate startDate, LocalDate endDate);

     Mono<byte[]> generateStaffBillPdf(String billId, String currency);

}