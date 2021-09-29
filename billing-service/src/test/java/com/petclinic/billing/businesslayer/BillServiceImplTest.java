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

    //Tests for GetBill
    @Test
    public void test_GetBill(){
        //arrange
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        Bill entity = new Bill(billId, date, "Checkup", 50.00);
        when(billRepository.findById(1)).thenReturn(Optional.of(entity));

        //act
        BillDTO returnedBill = billService.GetBill(1);

        //assert
        assertThat(returnedBill.getBillId()).isEqualTo(1);
    }

    @Test
    public void test_GetBill_NotFoundException(){
        //act & assert
        assertThrows(NotFoundException.class, () -> {
            billService.GetBill(1);
        });
    }

    //Tests for CreateBill
    @Test
    public void test_CreateBill(){
        //arrange
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        BillDTO model = new BillDTO(billId, date, "Checkup", 50.00);
        Bill entity = new Bill(billId, date, "Checkup", 50.00);
        when(billRepository.save(any(Bill.class))).thenReturn(entity);

        //act
        BillDTO returnedBill = billService.CreateBill(model);

        //assert
        assertThat(returnedBill.getBillId()).isEqualTo(entity.getBillId());

    }

    @Test
    public void test_CreateBillInvalidInputException(){
        //arrange
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        BillDTO model = new BillDTO(billId, date, "Checkup", 50.00);
        when(billRepository.save(any(Bill.class))).thenThrow(DuplicateKeyException.class);

        //act & assert
        assertThrows(InvalidInputException.class, () -> {
            billService.CreateBill(model);
        });

    }

    //Tests for DeleteBill
    @Test
    public void test_DeleteBill(){
        //arrange
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        Bill entity = new Bill(billId, date, "Checkup", 50.0);
        when(billRepository.findById(1)).thenReturn(Optional.of(entity));

        //act
        billService.DeleteBill(1);

        //assert
        verify(billRepository, times(1)).delete(entity);
    }

    @Test
    public void test_DeleteBill_does_not_exist(){
        //arrange
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.SEPTEMBER, 21);
        Date date = calendar.getTime();
        Bill entity = new Bill(billId, date, "Checkup", 50.0);

        //act
        billService.DeleteBill(1);

        //assert
        verify(billRepository, never()).delete(entity);
    }



}

