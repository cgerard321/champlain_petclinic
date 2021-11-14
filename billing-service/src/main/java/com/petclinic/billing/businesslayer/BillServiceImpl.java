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

import java.util.HashMap;
import java.util.List;

@Service
public class BillServiceImpl implements BillService{
    private static final Logger LOG = LoggerFactory.getLogger(BillServiceImpl.class);
    private final BillRepository billRepository;
    private final BillMapper billMapper;

    public BillServiceImpl(BillRepository billRepository, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }

    private HashMap<String, Double> setUpVisitList(){
        HashMap<String, Double> visitTypesPrices = new HashMap<String, Double>();
        visitTypesPrices.put("Examinations", 59.99);
        visitTypesPrices.put("Injury", 229.99);
        visitTypesPrices.put("Medical", 109.99);
        visitTypesPrices.put("Chronic", 89.99);
        visitTypesPrices.put("Consultations", 39.99);
        visitTypesPrices.put("Operations", 399.99);
        return visitTypesPrices;
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
    public List<BillDTO> GetAllBills() {
        List<Bill> bills = billRepository.findAll();
        List<BillDTO> dtos = billMapper.ListEntityToListModel(bills);

        return dtos;
    }

    @Override
    public BillDTO CreateBill(BillDTO model) {
        if(model.getBillId() <= 0) {
            throw new InvalidInputException("That bill id does not exist");
        }

        HashMap<String, Double> list = setUpVisitList();

        if(list.get(model.getVisitType()) == null){
            throw new InvalidInputException("That visit type does not exist");
        }

        try{
            Bill entity = billMapper.ModelToEntity(model);
            entity.setAmount(list.get(entity.getVisitType()));
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

    @Override
    public List<BillDTO> GetBillByCustomerId(int customerId) {

            List<Bill> bills = billRepository.findByCustomerId(customerId);

            List<BillDTO> response = billMapper.EntityListToModelList(bills);

            if(response.isEmpty()){
                throw new NotFoundException("No bill found for customerId: " + customerId);
            }
            return response;

    }
}
