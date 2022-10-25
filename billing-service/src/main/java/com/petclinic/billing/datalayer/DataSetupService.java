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
       BillDTO b1 = new BillDTO( "bill1", "1", "general", "1", Date.valueOf("2021-09-19"),59.99);
       BillDTO b2 = new BillDTO( "bill2", "2", "operation", "2", Date.valueOf("2021-09-20"), 199.99);
       BillDTO b3 = new BillDTO( "bill3", "3", "operation", "1", Date.valueOf("2021-09-21"), 199.99);
       BillDTO b4 = new BillDTO( "bill4", "4", "injury", "1", Date.valueOf("2021-09-22"), 199.99);
       BillDTO b5 = new BillDTO( "bill5", "5", "chronic", "3", Date.valueOf("2021-09-23"), 199.99);

        Flux.just(b1,b2,b3,b4,b5)
                .flatMap(b -> billService.CreateBill(Mono.just(b))
                        .log(b.toString()))
                .subscribe();
    }
}
