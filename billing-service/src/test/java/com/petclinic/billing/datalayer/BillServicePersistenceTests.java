package com.petclinic.billing.datalayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
public class BillServicePersistenceTests {
    @Autowired BillRepository repository;

    private Bill savedBill;



    @BeforeEach
    public void setupDb(){
        repository.deleteAll();
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, 9, 21);
        Date date = calendar.getTime();
        Bill bill = new Bill(1,1, "General", date,199.99);
        savedBill = repository.save(bill);

        assertThat(savedBill, samePropertyValuesAs(bill));

    }

    @Test
    public void create(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, 9, 21);
        Date date = calendar.getTime();
        Bill bill = new Bill(1,1,"General", date,199.99);

        repository.save(bill);
        Bill foundBill = repository.findById(bill.getId()).get();
        assertEquals(2,repository.count());
    }

    @Test
    public void update() {
        savedBill.setVisitType("Operation");
        repository.save(savedBill);

        Bill foundBill = repository.findById(savedBill.getId()).get();
        assertEquals(savedBill.getId(), foundBill.getId());
        assertEquals("Operation",foundBill.getVisitType());
    }

    @Test
    public void delete(){
        repository.delete(savedBill);
        assertFalse(repository.existsById(savedBill.getId()));
    }

    @Test
    public void getByBillId(){
        List<Bill> billList = repository.findByBillId(savedBill.getBillId());
        assertThat(billList,hasSize(1));
        Bill bill = billList.get(0);
        bill.setDate(savedBill.getDate());
        assertThat(billList.get(0),samePropertyValuesAs(savedBill));
    }
}
