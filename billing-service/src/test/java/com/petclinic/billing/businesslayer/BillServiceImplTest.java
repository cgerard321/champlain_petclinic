package com.petclinic.billing.businesslayer;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillDTO;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.exceptions.InvalidInputException;
import com.petclinic.billing.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Calendar;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Date;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class BillServiceImplTest {

    @MockBean
    BillRepository billRepository;

    @Autowired
    BillService billService;

    private final int billId = 1;
    private final int customerId = 1;

    @Test
    public void test_GetBill(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);

        Date date = calendar.getTime();
        Bill entity = new Bill(billId,customerId, date, "Checkup", 50.00);
        when(billRepository.findById(1)).thenReturn(Optional.of(entity));

        BillDTO returnedBill = billService.GetBill(1);

        assertThat(returnedBill.getBillId()).isEqualTo(1);
    }

    @Test
    public void test_GetBill_NotFoundException(){
        assertThrows(NotFoundException.class, () -> {
            billService.GetBill(1);
        });
    }

    @Test
    public void test_CreateBill(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        BillDTO model = new BillDTO(billId,customerId, date, "Checkup", 50.00);
        Bill entity = new Bill(billId,customerId, date, "Checkup", 50.00);
        when(billRepository.save(any(Bill.class))).thenReturn(entity);

        BillDTO returnedBill = billService.CreateBill(model);

        assertThat(returnedBill.getBillId()).isEqualTo(entity.getBillId());

    }

    @Test
    public void test_CreateBillInvalidInputException(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        BillDTO model = new BillDTO(billId,customerId, date, "Checkup", 50.00);
        when(billRepository.save(any(Bill.class))).thenThrow(DuplicateKeyException.class);


        assertThrows(InvalidInputException.class, () -> {
            billService.CreateBill(model);
        });

    }


    @Test
    public void test_DeleteBill(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        Bill entity = new Bill(billId,customerId, date, "Checkup", 50.0);
        when(billRepository.findById(1)).thenReturn(Optional.of(entity));


        billService.DeleteBill(1);


        verify(billRepository, times(1)).delete(entity);
    }

    @Test
    public void test_DeleteBill_does_not_exist(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        Bill entity = new Bill(billId,customerId, date, "Checkup", 50.0);


        billService.DeleteBill(1);


        verify(billRepository, never()).delete(entity);
    }



}

