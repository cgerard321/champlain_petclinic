package com.petclinic.billing.datalayer;


import com.petclinic.billing.businesslayer.BillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.LocalDate;


@Service
public class DataSetupService implements CommandLineRunner {
    private final BillService billService;
    public DataSetupService(BillService billService) {
        this.billService = billService;
    }


    @Override
    public void run(String... args) throws Exception {
//       BillRequestDTO b1 = new BillRequestDTO( "1", "general", "1", LocalDate.of(2021,9,19),59.99);
        BillRequestDTO b1 = new BillRequestDTO( "f470653d-05c5-4c45-b7a0-7d70f003d2ac", "general", "1", LocalDate.of(2021,9,19),59.99);
//        BillRequestDTO b3 = new BillRequestDTO( "3", "operation", "1", LocalDate.of(2021,9,21), 199.99);
        BillRequestDTO b3 = new BillRequestDTO( "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "operation", "1", LocalDate.of(2021,9,21), 199.99);
//        BillRequestDTO b4 = new BillRequestDTO( "4", "injury", "1",  LocalDate.of(2021,9,22), 199.99);
        BillRequestDTO b4 = new BillRequestDTO( "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "injury", "1",  LocalDate.of(2021,9,22), 199.99);
//        BillRequestDTO b2 = new BillRequestDTO( "2", "operation", "2", LocalDate.of(2021,9,20), 199.99);
        BillRequestDTO b2 = new BillRequestDTO( "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "operation", "2", LocalDate.of(2021,9,20), 199.99);
//        BillRequestDTO b5 = new BillRequestDTO( "5", "chronic", "3",  LocalDate.of(2021,9,23), 199.99);
        BillRequestDTO b5 = new BillRequestDTO( "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", "chronic", "3",  LocalDate.of(2021,9,23), 199.99);

        Flux.just(b1,b2,b3,b4,b5)
                .flatMap(b -> billService.CreateBill(Mono.just(b))
                        .log(b.toString()))
                .subscribe();
    }
}
