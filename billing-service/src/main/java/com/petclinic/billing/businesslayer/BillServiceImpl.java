package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.util.EntityDtoUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                    .doOnNext(event -> {
                        if (event.getBillId() == null || event.getBillId().trim().isEmpty()) {
                            event.setBillId(EntityDtoUtil.generateUUIDString());
                        }
                        System.out.println(event.getOwnerId());
                    })
                    .flatMap(billRepository::insert)
                    .map(EntityDtoUtil::toDto);
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
    public Flux<BillDTO> GetBillsByOwnerId(String ownerId) {
        return billRepository.findByOwnerId(ownerId).map(EntityDtoUtil::toDto);
    }

    @Override
    public Flux<BillDTO> GetBillsByVetId(String vetId) {
        return billRepository.findByVetId(vetId).map(EntityDtoUtil::toDto);
    }

    @Override
    public Flux<Void> DeleteBillsByOwnerId(String ownerId){
        return billRepository.deleteBillsByOwnerId(ownerId);
    }
}
