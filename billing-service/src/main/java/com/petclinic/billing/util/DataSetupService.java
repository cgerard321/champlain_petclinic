package com.petclinic.billing.util;


import com.petclinic.billing.businesslayer.BillService;
import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
import com.petclinic.billing.datalayer.BillRequestDTO;
import com.petclinic.billing.datalayer.BillStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;


@Service
public class DataSetupService implements CommandLineRunner {
    @Autowired
    BillService billService;

    @Autowired
    BillRepository billRepository;

    public DataSetupService(BillService billService) {
        this.billService = billService;
    }



    @Override
    public void run(String... args) throws Exception {

        try {

        // If the db is not empty, then return
        if (Boolean.TRUE.equals(billRepository.findAll().hasElements().block())) {
            return;
        }
        } catch (NullPointerException e) {
            System.out.println("Database not connected, skipping data setup.");
        }

        Bill b1 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .ownerFirstName("George")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("1")
                .vetFirstName("Jane")
                .vetLastName("Doe")
                .date(LocalDate.of(2024, 3, 1))
                .amount(300.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.OVERDUE)
                .dueDate(LocalDate.of(2024, 3, 31))
                .build();

        Bill b2 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .ownerFirstName("George")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("1")
                .vetFirstName("Jane")
                .vetLastName("Doe")
                .date(LocalDate.of(2024, 4, 1))
                .amount(167.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 4, 30))
                .build();

        Bill b3 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .ownerFirstName("George")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("1")
                .vetFirstName("Jane")
                .vetLastName("Doe")
                .date(LocalDate.of(2024, 5, 1))
                .amount(150.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 5, 31))
                .build();

        Bill b4 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac")
                .ownerFirstName("George")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("2")
                .vetFirstName("James")
                .vetLastName("Patterson")
                .date(LocalDate.of(2024, 6, 1))
                .amount(200.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 6, 30))
                .build();

        Bill b5 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .ownerFirstName("Betty")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("2")
                .vetFirstName("James")
                .vetLastName("Patterson")
                .date(LocalDate.of(2024, 10, 1))
                .amount(130.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.UNPAID)
                .dueDate(LocalDate.of(2024, 11, 30))
                .build();

        Bill b6 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("3f59dca2-903e-495c-90c3-7f4d01f3a2aa")
                .ownerFirstName("Jane")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("3")
                .vetFirstName("John")
                .vetLastName("Doe")
                .date(LocalDate.of(2024, 8, 1))
                .amount(100.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.UNPAID)
                .dueDate(LocalDate.of(2024, 8, 30))
                .build();

        Bill b7 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("3f59dca2-903e-495c-90c3-7f4d01f3a2aa")
                .ownerFirstName("Edurado")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("4")
                .vetFirstName("Max")
                .vetLastName("Lincoln")
                .date(LocalDate.of(2024, 9, 1))
                .amount(200.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 9, 30))
                .build();

        Bill b8 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("a6e0e5b0-5f60-45f0-8ac7-becd8b330486")
                .ownerFirstName("Harold")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("5")
                .vetFirstName("Kim")
                .vetLastName("Zimmerman")
                .date(LocalDate.of(2024, 3, 1))
                .amount(150.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 4, 30))
                .build();

        Bill b9 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("a6e0e5b0-5f60-45f0-8ac7-becd8b330486")
                .ownerFirstName("Harold")
                .ownerLastName("Doe")
                .visitType("Emergency")
                .vetId("5")
                .vetFirstName("Kim")
                .vetLastName("Zimmerman")
                .date(LocalDate.of(2024, 5, 1))
                .amount(400.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 5, 31))
                .build();

        Bill b10 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2")
                .ownerFirstName("Peter")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("5")
                .vetFirstName("Kim")
                .vetLastName("Zimmerman")
                .date(LocalDate.of(2024, 6, 1))
                .amount(150.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 6, 30))
                .build();

        Bill b11 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("b3d09eab-4085-4b2d-a121-78a0a2f9e501")
                .ownerFirstName("Jean")
                .ownerLastName("LeBlanc")
                .visitType("Emergency")
                .vetId("5")
                .vetFirstName("Kim")
                .vetLastName("Zimmerman")
                .date(LocalDate.of(2024, 7, 1))
                .amount(500.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 8, 30))
                .build();

        Bill b12 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd")
                .ownerFirstName("Jeff")
                .ownerLastName("Patterson")
                .visitType("Regular")
                .vetId("2")
                .vetFirstName("James")
                .vetLastName("Patterson")
                .date(LocalDate.of(2024, 8, 1))
                .amount(150.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 8, 30))
                .build();

        Bill b13 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7")
                .ownerFirstName("Maria")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("2")
                .vetFirstName("James")
                .vetLastName("Patterson")
                .date(LocalDate.of(2024, 10, 1))
                .amount(200.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.UNPAID)
                .dueDate(LocalDate.of(2024, 11, 30))
                .build();

        Bill b14 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("9f6accd1-e943-4322-932e-199d93824317")
                .ownerFirstName("David")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("2")
                .vetFirstName("James")
                .vetLastName("Patterson")
                .date(LocalDate.of(2024, 4, 1))
                .amount(150.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.PAID)
                .dueDate(LocalDate.of(2024, 4, 30))
                .build();

        Bill b15 = Bill.builder()
                .billId(UUID.randomUUID().toString())
                .customerId("7c0d42c2-0c2d-41ce-bd9c-6ca67478956f")
                .ownerFirstName("Carlos")
                .ownerLastName("Doe")
                .visitType("Regular")
                .vetId("2")
                .vetFirstName("James")
                .vetLastName("Patterson")
                .date(LocalDate.of(2024, 10, 1))
                .amount(150.0)
                .taxedAmount(0.0)
                .billStatus(BillStatus.UNPAID)
                .dueDate(LocalDate.of(2024, 11, 30))
                .build();

        Flux.just(b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12, b13, b14, b15)
                .flatMap(b -> billService.CreateBillForDB(Mono.just(b))
                        .log(b.toString()))
                .subscribe();
    }
}
