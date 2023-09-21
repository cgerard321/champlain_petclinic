package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
import com.petclinic.billing.exceptions.InvalidInputException;
import com.petclinic.billing.exceptions.NotFoundException;
import com.petclinic.billing.util.EntityDtoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

@Service
public class BillServiceImpl implements BillService{

    private final BillRepository billRepository;

    public BillServiceImpl(BillRepository billRepository) {
        this.billRepository = billRepository;
    }
    @Override
    public Mono<BillResponseDTO> GetBill(String billUUID) {

        return billRepository.findByBillId(billUUID).map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Flux<BillResponseDTO> GetAllBills() {

        return billRepository.findAll().map(EntityDtoUtil::toBillResponseDto);

    }

    @Override
    public Mono<BillResponseDTO> CreateBill(Mono<BillRequestDTO> model) {

            return model
                    .map(EntityDtoUtil::toBillEntity)
                    .doOnNext(e -> e.setBillId(EntityDtoUtil.generateUUIDString()))
                    .flatMap(billRepository::insert)
                    .map(EntityDtoUtil::toBillResponseDto);
        }


    @Override
    public Mono<BillDTO> updateBill(String billId, Mono<BillDTO> billDTOMono) {
        return billRepository.findByBillId(billId)
                .flatMap(p -> billDTOMono
                        .map(EntityDtoUtil::toEntity)
                        .doOnNext(e -> e.setBillId(p.getBillId()))
                        .doOnNext(e -> e.setId(p.getId()))
                )
                .flatMap(billRepository::save)
                .map(EntityDtoUtil::toDto);
    }


    @Override
    public Mono<Void> DeleteBill(String billId) {
        return billRepository.deleteBillByBillId(billId);
    }


    @Override
    public Flux<Void> DeleteBillsByVetId(String vetId) {
        return billRepository.deleteBillsByVetId(vetId);
    }

    @Override
    public Flux<BillResponseDTO> GetBillsByCustomerId(int customerId) {
/**/
        return billRepository.findByCustomerId(customerId).map(EntityDtoUtil::toBillResponseDto);
    }



    @Override
    public Flux<BillResponseDTO> GetBillsByVetId(String vetId) {
        return billRepository.findByVetId(vetId).map(EntityDtoUtil::toBillResponseDto);
    }


    @Override
    public Flux<Void> DeleteBillsByCustomerId(int customerId){
        return billRepository.deleteBillsByCustomerId(customerId);

    }
}
