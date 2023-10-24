package com.petclinic.billing.datalayer;


import com.petclinic.billing.businesslayer.BillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;


@Component
public class DataSetupService implements CommandLineRunner {
    private final BillService billService;
    public DataSetupService(BillService billService) {
        this.billService = billService;
    }


    @Override
    public void run(String... args) throws Exception {


        BillRequestDTO b1 = new BillRequestDTO( "1", "general", "1", LocalDate.of(2023,9,19),59.99,BillStatus.PAID, LocalDate.of(2023, 10,3));
        BillRequestDTO b3 = new BillRequestDTO( "3", "operation", "1", LocalDate.of(2023,9,27), 199.99,BillStatus.PAID,LocalDate.of(2023, 10,11));
        BillRequestDTO b4 = new BillRequestDTO( "4", "injury", "1",  LocalDate.of(2023,10,11), 199.99,BillStatus.UNPAID,LocalDate.of(2023, 10,25));
        BillRequestDTO b2 = new BillRequestDTO( "2", "operation", "2", LocalDate.of(2023,10,6), 199.99,BillStatus.OVERDUE,LocalDate.of(2023, 10,20));
        BillRequestDTO b5 = new BillRequestDTO( "5", "chronic", "3",  LocalDate.of(2023,10,13), 199.99,BillStatus.UNPAID,LocalDate.of(2023, 10,27));

        Flux.just(b1,b2,b3,b4,b5)
                .flatMap(b -> billService.CreateBill(Mono.just(b))
                        .log(b.toString()))
                .subscribe();
    }
}
