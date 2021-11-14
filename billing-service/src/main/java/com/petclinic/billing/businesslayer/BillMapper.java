package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillMapper {

    BillDTO EntityToModel(Bill bill);

    List<BillDTO> EntityListToModelList(List<Bill> bills);

    @Mapping(target = "id", ignore = true)
    Bill ModelToEntity(BillDTO billDTO);

    List<BillDTO> ListEntityToListModel(List<Bill> bills);

}