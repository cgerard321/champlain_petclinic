package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.exceptions.InvalidInputException;
import com.petclinic.billing.exceptions.NotFoundException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class BillServiceImplTest {

    @MockBean
    BillRepository billRepository;

    @Autowired
    BillMapper MAPPER;

    @Autowired
    BillService billService;

    private Map<Integer, Bill> db;

    private final int billId = 1;
    private final int customerId = 1;

    @BeforeEach
    void setup() {

        db = new HashMap<>();

        when(billRepository.count())
                .thenAnswer(ignore -> Long.valueOf(db.size()));
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

    @Test
    public void test_GetBill(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);

        Date date = calendar.getTime();
        Bill entity = new Bill(billId,customerId, "Examinations", date, 59.99);
        when(billRepository.findById(1)).thenReturn(Optional.of(entity));

        BillDTO returnedBill = billService.GetBill(1);

        assertThat(returnedBill.getBillId()).isEqualTo(1);
    }

    @Test
    public void test_GetAllBills() {
        when(billRepository.save(any()))
            .thenAnswer(args -> {
                Bill argument = args.getArgument(0, Bill.class);
                db.put(argument.getBillId(), argument);
                return argument;
            });
        List<Bill> bills = new ArrayList<>();
        db.forEach((k, v) -> bills.add(v));

        when(billRepository.findAll())
                .thenReturn(bills);
        assertEquals(billRepository.count(), billService.GetAllBills().size());
    }

    @Test
    public void test_GetBill_NotFoundException(){
        when(billRepository.findById(any()))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> {
            billService.GetBill(1);
        });
    }

    @Test
    public void test_CreateBill(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();

        HashMap<String, Double> list = setUpVisitList();
        BillDTO receivedDTO = new BillDTO(billId,customerId, "Consultations", date, 39.99);
        Bill entity = new Bill(billId,customerId, "Consultations", date, 39.99);
        receivedDTO.setAmount(list.get(receivedDTO.getVisitType()));
        when(billRepository.save(any(Bill.class))).thenReturn(entity);

        BillDTO returnedBill = billService.CreateBill(receivedDTO);

        assertThat(returnedBill.getBillId()).isEqualTo(entity.getBillId());
        assertThat(returnedBill.getAmount()).isEqualTo(entity.getAmount());
        assertThat(returnedBill.getVisitType()).isEqualTo(entity.getVisitType());
        assertThat(returnedBill.getAmount()).isEqualTo(entity.getAmount());

    }


    @Test
    public void test_CreateBillInvalidVisitTypeReceived(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();

        HashMap<String, Double> list = setUpVisitList();
        BillDTO receivedDTO = new BillDTO(billId,customerId, date, "Consultations");
        when(billRepository.save(any(Bill.class))).thenThrow(DuplicateKeyException.class);


        assertThrows(InvalidInputException.class, () -> {
            billService.CreateBill(receivedDTO);
        });

    }

    @Test
    public void test_CreateBillInvalidInputException(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        BillDTO model = new BillDTO(billId,customerId, date, "Consultations");
        when(billRepository.save(any())).thenThrow(DuplicateKeyException.class);


        assertThrows(InvalidInputException.class, () -> {
            billService.CreateBill(model);
        });

    }


    @Test
    public void test_DeleteBill(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        Bill entity = new Bill(billId,customerId, "Consultations", date, 59.99);
        when(billRepository.findById(1)).thenReturn(Optional.of(entity));


        billService.DeleteBill(1);


        verify(billRepository, times(1)).delete(entity);
    }

    @Test
    public void test_DeleteBill_does_not_exist(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        Bill entity = new Bill(billId,customerId, "Consultations", date, 59.99);


        billService.DeleteBill(1);


        verify(billRepository, never()).delete(entity);
    }

    @Test
    public void NullMappingEntityToModelTest() {
        BillDTO emptyDTO = MAPPER.EntityToModel(null);

        assertThat(emptyDTO).isEqualTo(null);
    }

    @Test
    public void NullMappingModelToEntityTest() {
        Bill emptyBill = MAPPER.ModelToEntity(null);

        assertThat(emptyBill).isEqualTo(null);
    }

    @Test
    public void test_GetBillByCustomerId(){

        int expectedSize = 2;

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);

        Date date = calendar.getTime();
        Bill entity1 = new Bill(billId,customerId, "Checkup", date, 50.00);
        Bill entity2 = new Bill(billId,customerId, "Vaccine", date, 100.00);

        List<Bill> bills = new ArrayList<>();
        bills.add(entity1);
        bills.add(entity2);
        when(billRepository.findByCustomerId(customerId)).thenReturn(bills);

        List<BillDTO> returnedBills = billService.GetBillByCustomerId(customerId);


        assertEquals(expectedSize, returnedBills.size());
    }

    @Test
    public void test_GetBillByCustomerId_NotFoundException(){
        assertThrows(NotFoundException.class, () -> {
            billService.GetBillByCustomerId(customerId);
        });
    }

}

