package com.petclinic.billing.datalayer;


import com.petclinic.billing.businesslayer.BillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Date;


@Service
public class DataSetupService implements CommandLineRunner {
    private final BillService billService;
    public DataSetupService(BillService billService) {
        this.billService = billService;
    }


    @Override
    public void run(String... args) throws Exception {
       BillDTO b1 = new BillDTO( 1, "general", "234568", Date.valueOf("2021-09-19"),59.99);
       BillDTO b3 = new BillDTO( 3, "operation", "238372", Date.valueOf("2021-09-21"), 199.99);
       BillDTO b4 = new BillDTO( 4, "injury", "823097", Date.valueOf("2021-09-22"), 199.99);
       BillDTO b2 = new BillDTO( 2, "operation", "784233",  Date.valueOf("2021-09-20"), 199.99);
       BillDTO b5 = new BillDTO( 5, "chronic", "823097", Date.valueOf("2021-09-23"), 199.99);
        BillDTO b6 = new BillDTO( 5, "chronic", "238372", Date.valueOf("2021-09-23"), 199.99);
        BillDTO b7 = new BillDTO( 1, "operation", "823097", Date.valueOf("2021-09-23"), 199.99);
        BillDTO b8 = new BillDTO( 2, "chronic", "784233", Date.valueOf("2021-09-23"), 199.99);
        BillDTO b9 = new BillDTO( 2, "injury", "238372", Date.valueOf("2021-09-23"), 199.99);
        BillDTO b10 = new BillDTO( 4, "chronic", "784233", Date.valueOf("2021-09-23"), 199.99);

        Flux.just(b1,b2,b3,b4,b5,b6,b7,b8,b9,b10)
                .flatMap(b -> billService.CreateBill(Mono.just(b))
                        .log(b.toString()))
                .subscribe();

    }
}
