package com.petclinic.billing.util;


import com.petclinic.billing.businesslayer.RequestContextAdd;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillResponseDTO;
import org.springframework.beans.BeanUtils;

import java.util.UUID;


public class EntityDtoUtil {

    public static BillDTO toDto (Bill bill){
        BillDTO dto = new BillDTO();
        BeanUtils.copyProperties(bill, dto);
        return dto;
    }

    public static Bill toEntity (BillDTO dto){
        Bill bill = new Bill();
        BeanUtils.copyProperties(dto, bill);
        return bill;
    }

    public static BillResponseDTO toBillResponseDto(Bill bill){
        BillResponseDTO billResponseDTO =new BillResponseDTO();
        BeanUtils.copyProperties(bill,billResponseDTO);
        return billResponseDTO;
    }

    public static Bill toBillEntity(BillRequestDTO billRequestDTO){
        Bill bill = new Bill();
        BeanUtils.copyProperties(billRequestDTO,bill);
        return bill;
    }

//    public static Bill toBillEntityRC(RequestContextAdd rc){
//        return Bill.builder()
//                .billId(generateUUIDString())
//                .amount(rc.getBillRequestDTO().getAmount())
//                .visitDate(rc.getBillRequestDTO().getDate())
//                .visitType(rc.getBillRequestDTO().getVisitType())
//                .ownerId(rc.getOwnerResponseDTO().getOwnerId())
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
