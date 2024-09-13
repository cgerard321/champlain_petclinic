package com.petclinic.billing.datalayer;


import com.petclinic.billing.businesslayer.BillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;


@Service
public class DataSetupService implements CommandLineRunner {
    private final BillService billService;
    public DataSetupService(BillService billService) {
        this.billService = billService;
    }


    @Override
    public void run(String... args) throws Exception {


        BillRequestDTO b1 = new BillRequestDTO( "f470653d-05c5-4c45-b7a0-7d70f003d2ac", "general", "1", LocalDate.of(2023,9,19),59.99,BillStatus.PAID, LocalDate.of(2023, 10,3));
        BillRequestDTO b3 = new BillRequestDTO( "f470653d-05c5-4c45-b7a0-7d70f003d2ac", "operation", "1", LocalDate.of(2023,9,27), 199.99,BillStatus.PAID,LocalDate.of(2023, 10,11));
        BillRequestDTO b4 = new BillRequestDTO( "f470653d-05c5-4c45-b7a0-7d70f003d2ac", "injury", "1",  LocalDate.of(2023,10,11), 199.99,BillStatus.UNPAID,LocalDate.of(2023, 10,25));
        BillRequestDTO b2 = new BillRequestDTO( "f470653d-05c5-4c45-b7a0-7d70f003d2ac", "operation", "2", LocalDate.of(2023,10,6), 199.99,BillStatus.OVERDUE,LocalDate.of(2023, 10,20));
        BillRequestDTO b5 = new BillRequestDTO( "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "chronic", "3",  LocalDate.of(2023,10,13), 199.99,BillStatus.UNPAID,LocalDate.of(2023, 10,27));
        BillRequestDTO b6 = new BillRequestDTO("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "general", "2", LocalDate.of(2023, 10, 5), 59.99, BillStatus.PAID, LocalDate.of(2023, 10, 10));
        BillRequestDTO b7 = new BillRequestDTO("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "operation", "3", LocalDate.of(2023, 10, 8), 199.99, BillStatus.PAID, LocalDate.of(2023, 10, 14));
        BillRequestDTO b8 = new BillRequestDTO("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "injury", "2", LocalDate.of(2023, 10, 15), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 10, 30));
        BillRequestDTO b9 = new BillRequestDTO("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "chronic", "1", LocalDate.of(2023, 10, 18), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 2));
        BillRequestDTO b10 = new BillRequestDTO("3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "general", "3", LocalDate.of(2023, 10, 21), 59.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 6));
        BillRequestDTO b11 = new BillRequestDTO("3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "operation", "1", LocalDate.of(2023, 10, 24), 199.99, BillStatus.PAID, LocalDate.of(2023, 11, 10));
        BillRequestDTO b12 = new BillRequestDTO("a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "injury", "3", LocalDate.of(2023, 10, 27), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 11, 14));
        BillRequestDTO b13 = new BillRequestDTO("a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "chronic", "2", LocalDate.of(2023, 10, 30), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 18));
        BillRequestDTO b14 = new BillRequestDTO("a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "general", "1", LocalDate.of(2023, 11, 2), 59.99, BillStatus.PAID, LocalDate.of(2023, 11, 22));
        BillRequestDTO b15 = new BillRequestDTO("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", "operation", "2", LocalDate.of(2023, 11, 5), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 26));
        BillRequestDTO b16 = new BillRequestDTO("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", "injury", "1", LocalDate.of(2023, 11, 8), 199.99, BillStatus.PAID, LocalDate.of(2023, 12, 2));
        BillRequestDTO b17 = new BillRequestDTO("b3d09eab-4085-4b2d-a121-78a0a2f9e501", "chronic", "3", LocalDate.of(2023, 11, 11), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 12, 6));
        BillRequestDTO b18 = new BillRequestDTO("b3d09eab-4085-4b2d-a121-78a0a2f9e501", "general", "2", LocalDate.of(2023, 11, 14), 59.99, BillStatus.PAID, LocalDate.of(2023, 12, 10));
        BillRequestDTO b19 = new BillRequestDTO("b3d09eab-4085-4b2d-a121-78a0a2f9e501", "operation", "3", LocalDate.of(2023, 11, 17), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 12, 14));
        BillRequestDTO b20 = new BillRequestDTO("b3d09eab-4085-4b2d-a121-78a0a2f9e501", "injury", "1", LocalDate.of(2023, 11, 20), 199.99, BillStatus.PAID, LocalDate.of(2023, 12, 18));
        BillRequestDTO b21 = new BillRequestDTO("b3d09eab-4085-4b2d-a121-78a0a2f9e501", "chronic", "2", LocalDate.of(2023, 11, 23), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 12, 22));
        BillRequestDTO b22 = new BillRequestDTO("5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", "general", "3", LocalDate.of(2023, 11, 26), 59.99, BillStatus.PAID, LocalDate.of(2023, 12, 26));
        BillRequestDTO b23 = new BillRequestDTO("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "operation", "2", LocalDate.of(2023, 11, 29), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 12, 30));
        BillRequestDTO b24 = new BillRequestDTO("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "injury", "1", LocalDate.of(2023, 12, 2), 199.99, BillStatus.PAID, LocalDate.of(2024, 1, 2));
        BillRequestDTO b25 = new BillRequestDTO("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "chronic", "3", LocalDate.of(2023, 12, 5), 199.99, BillStatus.OVERDUE, LocalDate.of(2024, 1, 6));
        BillRequestDTO b26 = new BillRequestDTO("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "general", "2", LocalDate.of(2023, 12, 8), 59.99, BillStatus.UNPAID, LocalDate.of(2024, 1, 10));
        BillRequestDTO b27 = new BillRequestDTO("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "operation", "3", LocalDate.of(2023, 12, 11), 199.99, BillStatus.PAID, LocalDate.of(2024, 1, 14));
        BillRequestDTO b28 = new BillRequestDTO("9f6accd1-e943-4322-932e-199d93824317", "injury", "1", LocalDate.of(2023, 12, 14), 199.99, BillStatus.PAID, LocalDate.of(2024, 1, 18));
        BillRequestDTO b29 = new BillRequestDTO("9f6accd1-e943-4322-932e-199d93824317", "chronic", "2", LocalDate.of(2023, 12, 17), 199.99, BillStatus.OVERDUE, LocalDate.of(2024, 1, 22));
        BillRequestDTO b30 = new BillRequestDTO("7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", "general", "3", LocalDate.of(2023, 12, 20), 59.99, BillStatus.UNPAID, LocalDate.of(2024, 1, 26));

        Flux.just(b6, b7, b8, b9, b10, b11, b12, b13, b14, b15, b16, b17, b18, b19, b20, b21, b22, b23, b24, b25, b26, b27, b28, b29, b30)
                .flatMap(b -> billService.CreateBill(Mono.just(b))
                        .log(b.toString()))
                .subscribe();

        Flux.just(b1,b2,b3,b4,b5)
                .flatMap(b -> billService.CreateBill(Mono.just(b))
                        .log(b.toString()))
                .subscribe();
    }
}
