package com.petclinic.billing;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

// TODO: CONFIGURE WEB TEST CLIENT //

@SpringBootTest
@ExtendWith(SpringExtension.class)
class BillingServiceApplicationTests {
	private static final int BILL_ID = 1;
	private static final int CUSTOMER_ID =1;
	@Autowired
	private BillRepository billRepository;

	@BeforeEach
	public void setupDb(){
		billRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}


	@Test
	public void createBill(){
		int expectedSize = 1;
		Date d = new Date();
		Bill newBill = new Bill(BILL_ID,CUSTOMER_ID, "Daily Checkup", d, 199.86);
		billRepository.save(newBill);

		assertEquals(expectedSize, billRepository.findByBillId(BILL_ID).size());
	}

	@Test
	public void getBillByBillId(){
		int expectedLength = 1;

		Date d = new Date();
		Bill entity = new Bill(BILL_ID,CUSTOMER_ID, "Daily Checkup", d, 199.86);
		billRepository.save(entity);

		assertEquals(expectedLength, billRepository.findByBillId(BILL_ID).size());


	}

	@Test
	public void deleteBill(){
		Date d = new Date();
		Bill bill = new Bill(BILL_ID,CUSTOMER_ID, "Daily Checkup", d, 199.86);
		billRepository.save(bill);

		assertEquals(1, billRepository.findByBillId(BILL_ID).size());

		billRepository.delete(bill);

		assertEquals(0, billRepository.findByBillId(BILL_ID).size());
	}

}
