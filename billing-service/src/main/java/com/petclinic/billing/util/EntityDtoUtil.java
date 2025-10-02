package com.petclinic.billing.util;


//import com.petclinic.billing.businesslayer.RequestContextAdd;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

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
        billResponseDTO.setOwnerFirstName(bill.getOwnerFirstName());
        billResponseDTO.setOwnerLastName(bill.getOwnerLastName());
        billResponseDTO.setVisitType(bill.getVisitType());
        billResponseDTO.setVetId(bill.getVetId());
        billResponseDTO.setVetFirstName(bill.getVetFirstName());
        billResponseDTO.setVetLastName(bill.getVetLastName());
        billResponseDTO.setDate(bill.getDate());
        billResponseDTO.setAmount(bill.getAmount());
        billResponseDTO.setTaxedAmount(bill.getTaxedAmount());
        billResponseDTO.setBillStatus(bill.getBillStatus());
        billResponseDTO.setDueDate(bill.getDueDate());
        billResponseDTO.setInterestExempt(bill.isInterestExempt());
        
        // Calculate and set interest with exemption check
        if (bill.isInterestExempt()) {
            billResponseDTO.setInterest(java.math.BigDecimal.ZERO);
        } else if (bill.getBillStatus() == com.petclinic.billing.datalayer.BillStatus.OVERDUE && bill.getDueDate() != null) {
            java.time.LocalDate dueDate = bill.getDueDate();
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.Period period = java.time.Period.between(dueDate, now);
            int overdueMonths = period.getYears() * 12 + period.getMonths();
            if (overdueMonths > 0) {
                java.math.BigDecimal monthlyRate = new java.math.BigDecimal("0.015");
                java.math.BigDecimal onePlusRate = java.math.BigDecimal.ONE.add(monthlyRate);
                java.math.BigDecimal compounded = onePlusRate.pow(overdueMonths);
                java.math.BigDecimal finalAmount = bill.getAmount().multiply(compounded).setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal interest = finalAmount.subtract(bill.getAmount()).setScale(2, java.math.RoundingMode.HALF_UP);
                billResponseDTO.setInterest(interest);
            } else {
                billResponseDTO.setInterest(java.math.BigDecimal.ZERO);
            }
        } else {
            billResponseDTO.setInterest(java.math.BigDecimal.ZERO);
        }
        
        billResponseDTO.setTimeRemaining(timeRemaining(bill));

        log.info("Mapped BillResponseDTO: {}", billResponseDTO);


        return billResponseDTO;
    }

    public static Bill toBillEntity(BillRequestDTO billRequestDTO){
        Bill bill = new Bill();
        BeanUtils.copyProperties(billRequestDTO,bill);
        return bill;
    }

    private static long timeRemaining(Bill bill){
        if (bill.getDueDate().isBefore(LocalDate.now())) {
            return 0;
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
