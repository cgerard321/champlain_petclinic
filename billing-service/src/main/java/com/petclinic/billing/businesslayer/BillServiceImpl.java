package com.petclinic.billing.businesslayer;

import com.itextpdf.text.DocumentException;
import com.petclinic.billing.datalayer.*;
//import com.petclinic.billing.domainclientlayer.OwnerClient;
//import com.petclinic.billing.domainclientlayer.VetClient;
import com.petclinic.billing.domainclientlayer.OwnerClient;
import com.petclinic.billing.domainclientlayer.VetClient;
import com.petclinic.billing.util.EntityDtoUtil;
import com.petclinic.billing.util.PdfGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.function.Predicate;


@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService{

    private final BillRepository billRepository;
    private final VetClient vetClient;
    private final OwnerClient ownerClient;

    @Override
    public Mono<BillResponseDTO> getBillByBillId(String billUUID) {

        return billRepository.findByBillId(billUUID).map(EntityDtoUtil::toBillResponseDto)
                .doOnNext(t -> t.setTaxedAmount(((t.getAmount() * 15)/100)+ t.getAmount()))
                .doOnNext(t -> t.setTaxedAmount(Math.round(t.getTaxedAmount() * 100.0) / 100.0));
    }

    @Override
    public Flux<BillResponseDTO> GetAllBillsByStatus(BillStatus status) {
        return billRepository.findAllBillsByBillStatus(status).map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Flux<BillResponseDTO> GetAllBills() {
        return billRepository.findAll()
                .map(EntityDtoUtil::toBillResponseDto);
    }

//    @Override
//    public Flux<BillResponseDTO> getAllBillsByPage(Pageable pageable, String billId, String customerId,
//                                                   String ownerFirstName, String ownerLastName, String visitType,
//                                                   String vetId, String vetFirstName, String vetLastName) {
//        Predicate<Bill> filterCriteria = bill ->
//                (billId == null || bill.getBillId().equals(billId)) &&
//                        (customerId == null || bill.getCustomerId().equals(customerId)) &&
//                        (ownerFirstName == null || bill.getOwnerFirstName().equals(ownerFirstName)) &&
//                        (ownerLastName == null || bill.getOwnerLastName().equals(ownerLastName)) &&
//                        (visitType == null || bill.getVisitType().equals(visitType)) &&
//                        (vetId == null || bill.getVetId().equals(vetId)) &&
//                        (vetFirstName == null || bill.getVetFirstName().equals(vetFirstName)) &&
//                        (vetLastName == null || bill.getVetLastName().equals(vetLastName));
//
//
//        if(billId == null && customerId == null && ownerFirstName == null && ownerLastName == null && visitType == null
//                && vetId == null && vetFirstName == null && vetLastName == null){
//            return billRepository.findAll()
//                    .map(EntityDtoUtil::toBillResponseDto)
//                    .skip(pageable.getPageNumber() * pageable.getPageSize())
//                    .take(pageable.getPageSize());
//        } else {
//            return billRepository.findAll()
//                    .filter(filterCriteria)
//                    .map(EntityDtoUtil::toBillResponseDto)
//                    .skip(pageable.getPageNumber() * pageable.getPageSize())
//                    .take(pageable.getPageSize());
//        }
//    }

    @Override
    public Flux<BillResponseDTO> getAllBillsByPage(Pageable pageable, String billId, String customerId,
                                                   String ownerFirstName, String ownerLastName, String visitType,
                                                   String vetId, String vetFirstName, String vetLastName) {

        Predicate<Bill> filterCriteria = bill ->
                (billId == null || bill.getBillId().equals(billId)) &&
                        (customerId == null || bill.getCustomerId().equals(customerId)) &&
                        (ownerFirstName == null || bill.getOwnerFirstName().equals(ownerFirstName)) &&
                        (ownerLastName == null || bill.getOwnerLastName().equals(ownerLastName)) &&
                        (visitType == null || bill.getVisitType().equals(visitType)) &&
                        (vetId == null || bill.getVetId().equals(vetId)) &&
                        (vetFirstName == null || bill.getVetFirstName().equals(vetFirstName)) &&
                        (vetLastName == null || bill.getVetLastName().equals(vetLastName));

        return billRepository.findAll()
                .filter(filterCriteria)
                .skip(pageable.getPageNumber() * pageable.getPageSize())
                .take(pageable.getPageSize())
                .map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Mono<Long> getNumberOfBillsWithFilters(String billId, String customerId, String ownerFirstName, String ownerLastName,
                                                  String visitType, String vetId, String vetFirstName, String vetLastName) {
        Predicate<Bill> filterCriteria = bill ->
                (billId == null || bill.getBillId().equals(billId)) &&
                        (customerId == null || bill.getCustomerId().equals(customerId)) &&
                        (ownerFirstName == null || bill.getOwnerFirstName().equals(ownerFirstName)) &&
                        (ownerLastName == null || bill.getOwnerLastName().equals(ownerLastName)) &&
                        (visitType == null || bill.getVisitType().equals(visitType)) &&
                        (vetId == null || bill.getVetId().equals(vetId)) &&
                        (vetFirstName == null || bill.getVetFirstName().equals(vetFirstName)) &&
                        (vetLastName == null || bill.getVetLastName().equals(vetLastName));

        return billRepository.findAll()
                .filter(filterCriteria)
                .map(EntityDtoUtil::toBillResponseDto)
                .count();
    }


    @Override
    public Mono<BillResponseDTO> CreateBill(Mono<BillRequestDTO> billRequestDTO) {

            return billRequestDTO
//                    .map(RequestContextAdd::new)
//                    .flatMap(this::vetRequestResponse)
//                    .flatMap(this::ownerRequestResponse)
//                    .map(EntityDtoUtil::toBillEntityRC)
                    .map(EntityDtoUtil::toBillEntity)
                    .doOnNext(e -> e.setBillId(EntityDtoUtil.generateUUIDString()))
                    .flatMap(billRepository::insert)
                    .map(EntityDtoUtil::toBillResponseDto);
    }


    @Override
    public Mono<BillResponseDTO> updateBill(String billId, Mono<BillRequestDTO> billRequestDTO) {
        return billRequestDTO
                .flatMap(r -> billRepository.findByBillId(billId)
                        .flatMap(existingBill -> {
                            existingBill.setCustomerId(r.getCustomerId());
                            existingBill.setVisitType(r.getVisitType());
                            existingBill.setVetId(r.getVetId());
                            existingBill.setDate(r.getDate());
                            existingBill.setBillStatus(r.getBillStatus());
                            existingBill.setAmount(r.getAmount());
                            existingBill.setDueDate(r.getDueDate());

                            return billRepository.save(existingBill);
                        })
                        .map(EntityDtoUtil::toBillResponseDto)
                );

    }

    @Override
    public Mono<Void> DeleteAllBills() {
        return billRepository.deleteAll();
    }


    @Override
    public Mono<Void> DeleteBill(String billId) {
        return billRepository.findByBillId(billId)
                .flatMap(bill -> {
                    if (bill.getBillStatus() == BillStatus.UNPAID || bill.getBillStatus() == BillStatus.OVERDUE) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot delete a bill that is unpaid or overdue."));
                    }
                    return billRepository.deleteBillByBillId(billId);
                });
    }


    @Override
    public Flux<Void> DeleteBillsByVetId(String vetId) {
        return billRepository.deleteBillsByVetId(vetId);
    }

    @Override
    public Flux<BillResponseDTO> GetBillsByCustomerId(String customerId) {
/**/
        return billRepository.findByCustomerId(customerId).map(EntityDtoUtil::toBillResponseDto);
    }



    @Override
    public Flux<BillResponseDTO> GetBillsByVetId(String vetId) {
        return billRepository.findByVetId(vetId).map(EntityDtoUtil::toBillResponseDto);
    }


    @Override
    public Flux<Void> DeleteBillsByCustomerId(String customerId){
        return billRepository.deleteBillsByCustomerId(customerId);

    }

//    private Mono<RequestContextAdd> vetRequestResponse(RequestContextAdd rc) {
//        return
//                this.vetClient.getVetByVetId(rc.getBillRequestDTO().getVetId())
//                        .doOnNext(rc::setVetDTO)
//                        .thenReturn(rc);
//    }
//    private Mono<RequestContextAdd> ownerRequestResponse(RequestContextAdd rc) {
//        return
//                this.ownerClient.getOwnerByOwnerId(rc.getBillRequestDTO().getCustomerId())
//                        .doOnNext(rc::setOwnerResponseDTO)
//                        .thenReturn(rc);
//    }


    // Fetch a specific bill for a customer
    @Override
    public Mono<BillResponseDTO> GetBillByCustomerIdAndBillId(String customerId, String billId) {
        return billRepository.findByBillId(billId)
                .filter(bill -> bill.getCustomerId().equals(customerId))
                .map(EntityDtoUtil::toBillResponseDto);
    }

    // Fetch filtered bills by status for a customer
    @Override
    public Flux<BillResponseDTO> GetBillsByCustomerIdAndStatus(String customerId, BillStatus status) {
        return billRepository.findByCustomerIdAndBillStatus(customerId, status)
                .map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Mono<byte[]> generateBillPdf(String customerId, String billId) {
        return billRepository.findByBillId(billId)
                .filter(bill -> bill.getCustomerId().equals(customerId))
                .switchIfEmpty(Mono.error(new RuntimeException("Bill not found for given customer")))
                .map(EntityDtoUtil::toBillResponseDto)
                .flatMap(bill -> {
                    try {
                        byte[] pdfBytes = PdfGenerator.generateBillPdf(bill);
                        return Mono.just(pdfBytes);
                    } catch (DocumentException e) {
                        return Mono.error(new RuntimeException("Error generating PDF", e));
                    }
                });
    }



}
