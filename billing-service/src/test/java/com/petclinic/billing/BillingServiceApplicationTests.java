package com.petclinic.billing;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
class BillingServiceApplicationTests {
	private static final int BILL_ID = 1;

	@Autowired
	private WebTestClient client;

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
	public void getBillByBillId(){
		int expectedLength = 1;
		Date d = new Date();
		Bill entity = new Bill(d, "Daily Checkup", 199.86);
		billRepository.save(entity);

		assertEquals(expectedLength, billRepository.findByBillID(BILL_ID).size());

		// Todo: Complete the Billing Service Application tests //
	}

}
