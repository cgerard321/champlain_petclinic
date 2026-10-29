package com.petclinic.billing.util;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
public class EntityDtoUtil {

    public static BillResponseDTO toBillResponseDto(Bill bill){
        BillResponseDTO billResponseDTO =new BillResponseDTO();
        //BeanUtils.copyProperties(bill,billResponseDTO);
        billResponseDTO.setBillId(bill.getBillId());
        billResponseDTO.setCustomerId(bill.getCustomerId());
        billResponseDTO.setCustomerFirstName(bill.getCustomerFirstName());
        billResponseDTO.setCustomerLastName(bill.getCustomerLastName());
        billResponseDTO.setVisitType(bill.getVisitType());
        billResponseDTO.setVetId(bill.getVetId());
        billResponseDTO.setVetFirstName(bill.getVetFirstName());
        billResponseDTO.setVetLastName(bill.getVetLastName());
        billResponseDTO.setDate(bill.getDate());
        billResponseDTO.setAmount(bill.getAmount());

        billResponseDTO.setBillStatus(bill.getBillStatus());
        billResponseDTO.setDueDate(bill.getDueDate());
        billResponseDTO.setInterestExempt(bill.isInterestExempt());



        // Use stored interest value if available, otherwise calculate
        BigDecimal interest;
        // For PAID bills, always use stored interest to preserve the amount that was actually paid
        // For OVERDUE/UNPAID bills, always calculate fresh interest to show current amount
        if (bill.getBillStatus() == BillStatus.PAID && bill.getInterest() != null) {
            interest = bill.getInterest();
        } else {
            interest = InterestCalculationUtil.calculateInterest(bill);
        }
        billResponseDTO.setInterest(interest);

        // Calculate final amount, including taxes and interest
        BigDecimal amount = bill.getAmount() != null
                ? bill.getAmount()
                : BigDecimal.ZERO;

        BigDecimal gst = bill.getGstAmount() != null
                ? bill.getGstAmount()
                : BigDecimal.ZERO;

        BigDecimal qst = bill.getQstAmount() != null
                ? bill.getQstAmount()
                : BigDecimal.ZERO;

        // Use stored total taxes when available.
        // Otherwise, calculate the total from the available tax amounts.
        BigDecimal totalTaxes = bill.getTaxedAmount() != null
                ? bill.getTaxedAmount()
                : gst.add(qst);

        billResponseDTO.setAmount(amount);
        billResponseDTO.setGstAmount(gst);
        billResponseDTO.setQstAmount(qst);
        billResponseDTO.setTaxedAmount(totalTaxes);

        // Interest is already calculated above and respects exemption.
        BigDecimal totalWithInterest = amount
                .add(totalTaxes)
                .add(interest)
                .setScale(2, java.math.RoundingMode.HALF_UP);

        billResponseDTO.setTotalAmount(totalWithInterest);

        billResponseDTO.setTimeRemaining(timeRemaining(bill));
        billResponseDTO.setArchive(bill.getArchive());

        log.info("Mapped BillResponseDTO: {}", billResponseDTO);

        return billResponseDTO;
    }

    public static Bill toBillEntity(BillRequestDTO billRequestDTO){
        Bill bill = new Bill();
        BeanUtils.copyProperties(billRequestDTO,bill);
        if (bill.getArchive() == null) {
            bill.setArchive(false);
        }
        return bill;
    }

    private static long timeRemaining(Bill bill){
        if (bill.getDueDate() == null || bill.getDueDate().isBefore(LocalDate.now())) {
            return 0L;
        }

        return Duration.between(LocalDate.now().atStartOfDay(), bill.getDueDate().atStartOfDay()).toDays();
    }

//    public static Bill toBillEntityRC(RequestContextAdd rc){
//        return Bill.builder()
//                .billId(generateUUIDString())
//                .amount(rc.getBillRequestDTO().getAmount())
//                .date(rc.getBillRequestDTO().getDate())
//                .visitType(rc.getBillRequestDTO().getVisitType())
//                .customerId(rc.getOwnerResponseDTO().getOwnerId())
//                .ownerFirstName(rc.getOwnerResponseDTO().getFirstName())
//                .ownerLastName(rc.getOwnerResponseDTO().getLastName())
//                .vetId(rc.getVetDTO().getVetId())
//                .vetFirstName(rc.getVetDTO().getFirstName())
//                .vetLastName(rc.getVetDTO().getLastName())
//                .build();
//    }

    public static String generateUUIDString(){
        return UUID.randomUUID().toString();
    }
}
