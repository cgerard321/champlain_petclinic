package com.petclinic.billing.mapper;

import com.petclinic.billing.dataaccesslayer.Bill;
import com.petclinic.billing.presentationlayer.models.BillRequestModel;
import com.petclinic.billing.presentationlayer.models.BillResponseModel;
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

    public static BillResponseModel toBillResponseModel(Bill bill){
        BillResponseModel billResponseModel =new BillResponseModel();
        //BeanUtils.copyProperties(bill,billResponseDTO);
        billResponseModel.setBillId(bill.getBillId());
        billResponseModel.setCustomerId(bill.getCustomerId());
        billResponseModel.setOwnerFirstName(bill.getCustomerFirstName());
        billResponseModel.setOwnerLastName(bill.getCustomerLastName());
        billResponseModel.setVisitType(bill.getVisitType());
        billResponseModel.setVetId(bill.getVetId());
        billResponseModel.setVetFirstName(bill.getVetFirstName());
        billResponseModel.setVetLastName(bill.getVetLastName());
        billResponseModel.setDate(bill.getDate());
        billResponseModel.setAmount(bill.getAmount());
        billResponseModel.setTaxedAmount(bill.getTaxedAmount());
        billResponseModel.setBillStatus(bill.getBillStatus());
        billResponseModel.setDueDate(bill.getDueDate());
        billResponseModel.setInterestExempt(bill.isInterestExempt());
        
        // Use stored interest value if available, otherwise calculate
        BigDecimal interest;
        // For PAID bills, always use stored interest to preserve the amount that was actually paid
        // For OVERDUE/UNPAID bills, always calculate fresh interest to show current amount
        if (bill.getBillStatus() == BillStatus.PAID && bill.getInterest() != null) {
            interest = bill.getInterest();
        } else {
            interest = InterestCalculationUtil.calculateInterest(bill);
        }
        billResponseModel.setInterest(interest);
        
        // Calculate final amount
        if (bill.getAmount() != null) {
            BigDecimal totalWithInterest = bill.getAmount().add(interest);
            billResponseModel.setTaxedAmount(totalWithInterest.setScale(2, java.math.RoundingMode.HALF_UP));
        } else {
            // If amount is null, set taxedAmount to just the interest (or zero if no interest)
            billResponseModel.setTaxedAmount(interest.setScale(2, java.math.RoundingMode.HALF_UP));
        }
        
        billResponseModel.setTimeRemaining(timeRemaining(bill));
        billResponseModel.setArchive(bill.getIsArchived());

        log.info("Mapped BillResponseDTO: {}", billResponseModel);

        return billResponseModel;
    }

    public static Bill toBillEntity(BillRequestModel billRequestModel){
        Bill bill = new Bill();
        BeanUtils.copyProperties(billRequestModel,bill);
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
