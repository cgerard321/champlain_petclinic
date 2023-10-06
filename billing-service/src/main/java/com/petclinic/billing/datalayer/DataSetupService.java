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


        BillRequestDTO b1 = new BillRequestDTO( "1", "general", "1", LocalDate.of(2021,9,19),59.99,BillStatus.PAID);
        BillRequestDTO b3 = new BillRequestDTO( "3", "operation", "1", LocalDate.of(2021,9,21), 199.99,BillStatus.PAID);
        BillRequestDTO b4 = new BillRequestDTO( "4", "injury", "1",  LocalDate.of(2021,9,22), 199.99,BillStatus.UNPAID);
        BillRequestDTO b2 = new BillRequestDTO( "2", "operation", "2", LocalDate.of(2021,9,20), 199.99,BillStatus.OVERDUE);
        BillRequestDTO b5 = new BillRequestDTO( "5", "chronic", "3",  LocalDate.of(2021,9,23), 199.99,BillStatus.UNPAID);

        Flux.just(b1,b2,b3,b4,b5)
                .flatMap(b -> billService.CreateBill(Mono.just(b))
                        .log(b.toString()))
                .subscribe();
    }
}
