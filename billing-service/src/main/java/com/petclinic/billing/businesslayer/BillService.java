package com.petclinic.billing.businesslayer;


import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BillService {
    Mono<BillResponseDTO> getBillByBillId(String billId);

    Flux<BillResponseDTO> getAllBillsByStatus(BillStatus status);

    Mono<Bill>CreateBillForDB(Mono<Bill> bill);

    Flux<BillResponseDTO> getAllBills();

    //to be changed
    Flux<BillResponseDTO> getAllBillsByPage(Pageable pageable,
                                            String billId,
                                            String customerId,
                                            String ownerFirstName,
                                            String ownerLastName,
                                            String visitType,
                                            String vetId,
                                            String vetFirstName,
                                            String vetLastName);

    //to be changed
    Mono<Long> getNumberOfBillsWithFilters(String billId,
                                           String customerId,
                                           String ownerFirstName,
                                           String ownerLastName,
                                           String visitType,
                                           String vetId,
                                           String vetFirstName,
                                           String vetLastName);


    Mono<BillResponseDTO> createBill(@RequestBody Mono<BillRequestDTO> model);

    Mono<Void> deleteBill(@RequestParam(value = "billId", required = true) String billId);

    Flux<BillResponseDTO> getBillsByCustomerId(@RequestParam(value = "customerId", required = true) String customerId);
    Flux<BillResponseDTO> getBillsByVetId(@RequestParam(value = "vetId", required = true) String vetId);

    Flux<Void> deleteBillsByVetId(@RequestParam(value="vetId", required = true) String vetId);
    Flux<Void> deleteBillsByCustomerId(@RequestParam(value="customerId", required = true)String customerId);

    Mono<BillResponseDTO> updateBill(String billId, Mono<BillRequestDTO> billRequestDTO);

    Mono<Void> deleteAllBills();

    // Fetch a specific bill for a customer
    Mono<BillResponseDTO> getBillByCustomerIdAndBillId(String customerId, String billId);

    // Fetch filtered bills by status
    Flux<BillResponseDTO> getBillsByCustomerIdAndStatus(String customerId, BillStatus status);

    // Method to generate the bill PDF
    Mono<byte[]> generateBillPdf(String customerId, String billId);

    // Method to fetch bills by month
    Flux<BillResponseDTO> getBillsByMonth(int year, int month);



}