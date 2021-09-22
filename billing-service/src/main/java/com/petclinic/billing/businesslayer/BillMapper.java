package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BillMapper {

    BillDTO EntityToModel(Bill bill);

    @Mapping(target = "id",ignore = true)
    Bill ModelToEntity(BillDTO billDTO);
}
