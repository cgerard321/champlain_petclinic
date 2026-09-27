package com.petclinic.billing.mapper;

import com.petclinic.billing.dataaccesslayer.Bill;
import com.petclinic.billing.presentationlayer.dtos.BillRequestDTO;
import com.petclinic.billing.presentationlayer.dtos.BillResponseDTO;
import com.petclinic.billing.dataaccesslayer.BillStatus;

import com.petclinic.billing.util.InterestCalculationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
public class BillMapper {

    public static BillResponseDTO toBillResponseModel(Bill bill){
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
        billResponseDTO.setTaxedAmount(bill.getTaxedAmount());
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
        
        // Calculate final amount
        if (bill.getAmount() != null) {
            BigDecimal totalWithInterest = bill.getAmount().add(interest);
            billResponseDTO.setTaxedAmount(totalWithInterest.setScale(2, java.math.RoundingMode.HALF_UP));
        } else {
            // If amount is null, set taxedAmount to just the interest (or zero if no interest)
            billResponseDTO.setTaxedAmount(interest.setScale(2, java.math.RoundingMode.HALF_UP));
        }
        
        billResponseDTO.setTimeRemaining(timeRemaining(bill));
        billResponseDTO.setArchive(bill.getIsArchived());

        log.info("Mapped BillResponseDTO: {}", billResponseDTO);

        return billResponseDTO;
    }

    public static Bill toBillEntity(BillRequestDTO billRequestDTO){
        Bill bill = new Bill();
        BeanUtils.copyProperties(billRequestDTO,bill);
        if (bill.getIsArchived() == null) {
            bill.setIsArchived(false);
        }
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
