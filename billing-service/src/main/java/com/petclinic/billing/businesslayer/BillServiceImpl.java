package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.exceptions.InvalidInputException;
import com.petclinic.billing.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class BillServiceImpl implements BillService{
    private static final Logger LOG = LoggerFactory.getLogger(BillServiceImpl.class);
    private final BillRepository billRepository;
    private final BillMapper billMapper;

    public BillServiceImpl(BillRepository billRepository, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }

    @Override
    public BillDTO GetBill(int billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new NotFoundException("No bill found for billId: " + billId));

        BillDTO response = billMapper.EntityToModel(bill);
        LOG.debug("Bill: GetBillByID: found bill ID: {}", billId);
        return response;
    }

    @Override
    public BillDTO CreateBill(BillDTO model) {
        try{
            Bill entity = billMapper.ModelToEntity(model);
            Bill newEntity = billRepository.save(entity);

            LOG.debug("Entity created for bill ID: {}", newEntity.getId());

            return billMapper.EntityToModel(newEntity);
        }
        catch(DuplicateKeyException dKE){
            Bill entity = billMapper.ModelToEntity(model);
            throw new InvalidInputException("Duplicate key, bill ID: " + entity.getId());
        }
    }

    @Override
    public void DeleteBill(int billId) {
        LOG.debug("Delete for bill ID: {}", billId);
        billRepository.findById(billId).ifPresent(entity -> billRepository.delete(entity));
    }
}
