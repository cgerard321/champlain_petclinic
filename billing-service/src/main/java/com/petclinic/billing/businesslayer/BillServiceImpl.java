package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRepository;
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
    public Mono<BillDTO> GetBill(String billUUID) {

        return billRepository.findByBillId(billUUID).map(EntityDtoUtil::toDto);
    }

    @Override
    public Flux<BillDTO> GetAllBills() {

        return billRepository.findAll().map(EntityDtoUtil::toDto);

    }

    @Override
    public Mono<BillDTO> CreateBill(Mono<BillDTO> model) {

            return model
                    .map(EntityDtoUtil::toEntity)
                    .doOnNext(e -> e.setBillId(EntityDtoUtil.generateUUIDString()))
                    .flatMap(billRepository::insert)
                    .map(EntityDtoUtil::toDto);
        }


    @Override
    public Mono<Void> DeleteBill(String billId) {
        return billRepository.deleteBillByBillId(billId);
    }

    @Override
    public Flux<Void> DeleteBillsByCustomerId(int customerId) {
        return billRepository.deleteBillsByCustomerId(customerId);
    }


    @Override
    public Flux<BillDTO> GetBillsByCustomerId(int customerId) {
/**/
        return billRepository.findByCustomerId(customerId).map(EntityDtoUtil::toDto);
    }

    @Override
    public Flux<BillDTO> GetBillsByVetId(String vetId) {
        return billRepository.findByVetId(vetId).map(EntityDtoUtil::toDto);
    }
}
