package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
import com.petclinic.billing.domainclientlayer.Auth.UserDetails;
import com.petclinic.billing.domainclientlayer.Mailing.Mail;
import com.petclinic.billing.domainclientlayer.OwnerClient;
import com.petclinic.billing.domainclientlayer.VetClient;
import com.petclinic.billing.exceptions.InvalidPaymentException;
import com.petclinic.billing.exceptions.NotFoundException;
import com.petclinic.billing.util.EntityDtoUtil;
import com.petclinic.billing.util.InterestCalculationUtil;
import com.petclinic.billing.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillServiceImpl implements BillService{

    private final BillRepository billRepository;
    private final VetClient vetClient;
    private final OwnerClient ownerClient;


   @Override
    public Mono<BillResponseDTO> getBillByBillId(String billUUID) {
        return billRepository.findByBillId(billUUID)
            .doOnNext(bill -> log.info("Retrieved Bill: {}", bill))
            .map(EntityDtoUtil::toBillResponseDto);
}
    @Override
    public Flux<BillResponseDTO> getAllBillsByStatus(BillStatus status) {
        return billRepository.findAllBillsByBillStatus(status).map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Mono<Bill> CreateBillForDB(Mono<Bill> bill) {
        return bill.flatMap(billRepository::insert);
    }

    @Override
    public Flux<BillResponseDTO> getAllBills() {
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
    public Flux<BillResponseDTO> getAllBillsByOwnerName(String ownerFirstName, String ownerLastName) {
        return billRepository.findAll()
                .filter(bill -> (ownerFirstName == null || bill.getOwnerFirstName().equals(ownerFirstName)) &&
                        (ownerLastName == null || bill.getOwnerLastName().equals(ownerLastName)))
                .switchIfEmpty(Flux.error(new NotFoundException("No bills found for the given owner name")))
                .map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Flux<BillResponseDTO> getAllBillsByVetName(String vetFirstName, String vetLastName) {
        return billRepository.findAll()
                .filter(bill -> (vetFirstName == null || bill.getVetFirstName().equals(vetFirstName)) &&
                        (vetLastName == null || bill.getVetLastName().equals(vetLastName)))
                .switchIfEmpty(Flux.error(new NotFoundException("No bills found for the given vet name")))
                .map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Flux<BillResponseDTO> getAllBillsByVisitType(String visitType) {
        return billRepository.findAll()
                .filter(bill -> visitType == null || bill.getVisitType().equals(visitType))
                .switchIfEmpty(Flux.error(new NotFoundException("No bills found for the given visit type")))
                .map(EntityDtoUtil::toBillResponseDto);
    }


    @Override
    public Mono<BillResponseDTO> createBill(Mono<BillRequestDTO> billRequestDTO) {
        return billRequestDTO
                .flatMap(dto -> {
                    // Validate required fields
                    if (dto.getBillStatus() == null) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Bill status is required"
                        ));
                    }

                    // Validate vetId and customerId
                    if (dto.getVetId() == null || dto.getVetId().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vet ID is required"));
                    }
                    if (dto.getCustomerId() == null || dto.getCustomerId().isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID is required"));
                    }

                    // Fetch Vet and Owner details
                    Mono<VetResponseDTO> vetMono = vetClient.getVetByVetId(dto.getVetId());
                    Mono<OwnerResponseDTO> ownerMono = ownerClient.getOwnerByOwnerId(dto.getCustomerId());

                    return Mono.zip(vetMono, ownerMono, Mono.just(dto));
                })
                .map(tuple -> {
                    VetResponseDTO vet = tuple.getT1();
                    OwnerResponseDTO owner = tuple.getT2();
                    BillRequestDTO dto = tuple.getT3();

                    // Map to Bill entity and populate names
                    Bill bill = EntityDtoUtil.toBillEntity(dto);
                    bill.setBillId(EntityDtoUtil.generateUUIDString());
                    bill.setVetFirstName(vet.getFirstName());
                    bill.setVetLastName(vet.getLastName());
                    bill.setOwnerFirstName(owner.getFirstName());
                    bill.setOwnerLastName(owner.getLastName());

                    return bill;
                })
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
    public Mono<Void> deleteAllBills() {
        return billRepository.deleteAll();
    }


    @Override
    public Mono<Void> deleteBill(String billId) {
        return billRepository.findByBillId(billId)
                .flatMap(bill -> {
                    if (bill.getBillStatus() == BillStatus.UNPAID || bill.getBillStatus() == BillStatus.OVERDUE) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot delete a bill that is unpaid or overdue."));
                    }
                    return billRepository.deleteBillByBillId(billId);
                });
    }

    @Override
    public Flux<Void> deleteBillsByVetId(String vetId) {
        return billRepository.deleteBillsByVetId(vetId);
    }

    @Override
    public Flux<BillResponseDTO> getBillsByCustomerId(String customerId) {
/**/
        return billRepository.findByCustomerId(customerId).map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Flux<BillResponseDTO> getBillsByVetId(String vetId) {
        return billRepository.findByVetId(vetId).map(EntityDtoUtil::toBillResponseDto);
    }


    @Override
    public Flux<Void> deleteBillsByCustomerId(String customerId){
        return billRepository.deleteBillsByCustomerId(customerId);

    }
/*
    private long timeRemaining(BillResponseDTO bill){
        if (bill.getDueDate().isBefore(LocalDate.now())) {
            return 0;
        }

        return Duration.between(LocalDate.now().atStartOfDay(), bill.getDueDate().atStartOfDay()).toDays();
    }

 */

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
    public Mono<BillResponseDTO> getBillByCustomerIdAndBillId(String customerId, String billId) {
        return billRepository.findByBillId(billId)
                .filter(bill -> bill.getCustomerId().equals(customerId))
                .map(EntityDtoUtil::toBillResponseDto);
    }

    // Fetch filtered bills by status for a customer
    @Override
    public Flux<BillResponseDTO> getBillsByCustomerIdAndStatus(String customerId, BillStatus status) {
        return billRepository.findByCustomerIdAndBillStatus(customerId, status)
                .map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Mono<byte[]> generateBillPdf(String customerId, String billId, String currency) {
        return billRepository.findByBillId(billId)
                .filter(bill -> bill.getCustomerId().equals(customerId))
                .switchIfEmpty(Mono.error(new RuntimeException("Bill not found for given customer")))
                .map(EntityDtoUtil::toBillResponseDto)
                .flatMap(bill -> {
                    try {
                        byte[] pdfBytes = PdfGenerator.generateBillPdf(bill, currency);
                        return Mono.just(pdfBytes);
                    } catch (Exception e) {
                        log.error("PDF generation failed for billId: {}, currency: {}. Error: {}", bill.getBillId(), currency, e.getMessage(), e);
                        return Mono.error(new RuntimeException("Error generating PDF", e));
                    }
                });
    }

    @Override
    public Flux<BillResponseDTO> getBillsByMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth().plusDays(1);

        return billRepository.findByDateBetween(start, end)
                .map(EntityDtoUtil::toBillResponseDto)
                .switchIfEmpty(Flux.empty());
    }

    public Mono<BigDecimal> calculateCurrentBalance(String customerId) {
        return billRepository.findByCustomerIdAndBillStatus(customerId, BillStatus.UNPAID)
            .concatWith(billRepository.findByCustomerIdAndBillStatus(customerId, BillStatus.OVERDUE))
            .map(bill -> {
                BigDecimal total = bill.getAmount();
                // Check interest exemption first - if exempt, don't add interest regardless of status
                if (!bill.isInterestExempt()) {
                    BigDecimal interest = InterestCalculationUtil.calculateInterest(bill);
                    total = total.add(interest);
                }
                return total;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));
        }

    @Override
    public Mono<BillResponseDTO> processPayment(String customerId, String billId, PaymentRequestDTO paymentRequestDTO)
    {

        // 1. Validate card details before reactive pipeline.
        //    If card number, CVV, or expiration date lengths are invalid, throw InvalidPaymentException.
        if (paymentRequestDTO.getCardNumber() == null || paymentRequestDTO.getCardNumber().length() != 16 ||
                paymentRequestDTO.getCvv() == null || paymentRequestDTO.getCvv().length() != 3 ||
                paymentRequestDTO.getExpirationDate() == null || paymentRequestDTO.getExpirationDate().length() != 5) {
            return Mono.error(new InvalidPaymentException("Invalid payment details"));
        }

            // 2. Try to find the bill by customerId and billId.
            //    If no bill exists, immediately return a 404 ResponseStatusException.
            return billRepository.findByCustomerIdAndBillId(customerId, billId)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found")))

                    // 3. If the bill exists, calculate and preserve the interest, then set status to PAID.
                    .flatMap(bill -> {
                        // Calculate and preserve the interest before changing status
                        BigDecimal interestAtPayment = InterestCalculationUtil.calculateInterest(bill);
                        bill.setInterest(interestAtPayment);
                        bill.setBillStatus(BillStatus.PAID);

                        // 4. Save the updated bill back into the repository.
                        return billRepository.save(bill);
                    })

                    // 5. Map the updated Bill entity into a BillResponseDTO before returning.
                    .map(EntityDtoUtil::toBillResponseDto);

    }


    public Mono<BigDecimal> getInterest(String billId, BigDecimal amount, int overdueMonths) {
        return billRepository.findByBillId(billId)
            .map(bill -> {
                if (bill.isInterestExempt()) {
                    return BigDecimal.ZERO;
                } else {
                    return InterestCalculationUtil.calculateInterest(bill);
                }
            });
    }

    public Mono<BigDecimal> getTotalWithInterest(String billId, BigDecimal amount, int overdueMonths) {
        return getInterest(billId, amount, overdueMonths)
            .map(interest -> amount.add(interest));
    }

    @Override
    public Mono<Void> setInterestExempt(String billId, boolean exempt) {
        log.info("exempt called");
        return billRepository.findByBillId(billId)
            .flatMap(bill -> {
                bill.setInterestExempt(exempt);
                // If setting exemption to true, also clear any existing interest
                if (exempt) {
                    bill.setInterest(BigDecimal.ZERO);
                }
                return billRepository.save(bill);
            })
            .then();
    }
    @Override
    public Flux<Bill> archiveBill() {
        return billRepository.findAllByArchiveFalse()
                .flatMap(bill -> {
                    if (bill.getBillStatus() == BillStatus.UNPAID || bill.getBillStatus() == BillStatus.OVERDUE) {
                        // No action needed; archive is already false by default.
                    }
                    else if (bill.getDate().isBefore(LocalDate.now().minusYears(1))) {
                        bill.setArchive(true);
                        return billRepository.save(bill);
                    }
                    return Mono.just(bill);
                });
    }

    private Mail generateConfirmationEmail(UserDetails user){
        return new Mail(
                user.getEmail(), "Pet Clinic - Payment Confirmation", "default", "Pet Clinic confirmation email",
                "Dear, " + user.getUsername() + "\n" +
                "Your bill has been succesfully paid",
                "Thank you for choosing Pet Clinic.", user.getUsername(), "ChamplainPetClinic@gmail.com");
    }



}