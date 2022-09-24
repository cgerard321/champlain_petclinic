package com.petclinic.billing.util;


import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import org.springframework.beans.BeanUtils;


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
}
