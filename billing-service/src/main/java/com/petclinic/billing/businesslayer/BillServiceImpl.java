package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.*;
//import com.petclinic.billing.domainclientlayer.OwnerClient;
//import com.petclinic.billing.domainclientlayer.VetClient;
import com.petclinic.billing.util.EntityDtoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService{

    private final BillRepository billRepository;
//    private final VetClient vetClient;
//    private final OwnerClient ownerClient;

    @Override
    public Mono<BillResponseDTO> getBillByBillId(String billUUID) {

        return billRepository.findByBillId(billUUID).map(EntityDtoUtil::toBillResponseDto);
    }

    @Override
    public Flux<BillResponseDTO> GetAllBillsByStatus(BillStatus status) {
        return billRepository.findAllBillsByBillStatus(status).map(EntityDtoUtil::toBillResponseDto);
    }


    @Override
    public Flux<BillResponseDTO> GetAllBills() {

        return billRepository.findAll().map(EntityDtoUtil::toBillResponseDto);

    }

    @Override
    public Mono<BillResponseDTO> CreateBill(Mono<BillRequestDTO> billRequestDTO) {

            return billRequestDTO
//                    .map(RequestContextAdd::new)
//                    .flatMap(this::vetRequestResponse)
//                    .flatMap(this::ownerRequestResponse)
//                    .map(EntityDtoUtil::toBillEntityRC)
                    .map(EntityDtoUtil::toBillEntity)
                    .doOnNext(e -> e.setBillId(EntityDtoUtil.generateUUIDString()))
                    .flatMap(billRepository::insert)
                    .map(EntityDtoUtil::toBillResponseDto);
        }


    @Override
    public Mono<BillResponseDTO> updateBill(String billId, Mono<BillRequestDTO> billRequestDTO) {
        return billRequestDTO
                .flatMap(r -> billRepository.findByBillId(billId)
                        .flatMap(existingBill -> {
                            existingBill.setCustomerId(r.getCustomerId());
                            existingBill.setVisitType(r.getVisitType());
                            existingBill.setVetId(r.getVetId());
                            existingBill.setDate(r.getDate());
                            existingBill.setBillStatus(r.getBillStatus());
                            existingBill.setAmount(r.getAmount());
                            existingBill.setDueDate(r.getDueDate());

                            return billRepository.save(existingBill);
                        })
                        .map(EntityDtoUtil::toBillResponseDto)
                );

    }

    @Override
    public Mono<Void> DeleteAllBills() {
        return billRepository.deleteAll();
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
    public Flux<BillResponseDTO> GetBillsByCustomerId(String customerId) {
/**/
        return billRepository.findByCustomerId(customerId).map(EntityDtoUtil::toBillResponseDto);
    }



    @Override
    public Flux<BillResponseDTO> GetBillsByVetId(String vetId) {
        return billRepository.findByVetId(vetId).map(EntityDtoUtil::toBillResponseDto);
    }


    @Override
    public Flux<Void> DeleteBillsByCustomerId(String customerId){
        return billRepository.deleteBillsByCustomerId(customerId);

    }

//    private Mono<RequestContextAdd> vetRequestResponse(RequestContextAdd rc) {
//        return
//                this.vetClient.getVetByVetId(rc.getVetDTO().getVetId())
//                        .doOnNext(rc::setVetDTO)
//                        .thenReturn(rc);
//    }
//    private Mono<RequestContextAdd> ownerRequestResponse(RequestContextAdd rc) {
//        return
//                this.ownerClient.getOwnerByOwnerId(rc.getOwnerResponseDTO().getOwnerId())
//                        .doOnNext(rc::setOwnerResponseDTO)
//                        .thenReturn(rc);
//    }
}
