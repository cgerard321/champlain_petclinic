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


        BillRequestDTO b1 = new BillRequestDTO( "1", "general", "1", LocalDate.of(2023,9,19),59.99,BillStatus.PAID, LocalDate.of(2023, 10,3));
        BillRequestDTO b3 = new BillRequestDTO( "3", "operation", "1", LocalDate.of(2023,9,27), 199.99,BillStatus.PAID,LocalDate.of(2023, 10,11));
        BillRequestDTO b4 = new BillRequestDTO( "4", "injury", "1",  LocalDate.of(2023,10,11), 199.99,BillStatus.UNPAID,LocalDate.of(2023, 10,25));
        BillRequestDTO b2 = new BillRequestDTO( "2", "operation", "2", LocalDate.of(2023,10,6), 199.99,BillStatus.OVERDUE,LocalDate.of(2023, 10,20));
        BillRequestDTO b5 = new BillRequestDTO( "5", "chronic", "3",  LocalDate.of(2023,10,13), 199.99,BillStatus.UNPAID,LocalDate.of(2023, 10,27));
        BillRequestDTO b1 = new BillRequestDTO( "1", "general", "1", LocalDate.of(2021,9,19),59.99,BillStatus.PAID);
        BillRequestDTO b3 = new BillRequestDTO( "3", "operation", "1", LocalDate.of(2021,9,21), 199.99,BillStatus.PAID);
        BillRequestDTO b4 = new BillRequestDTO( "4", "injury", "1",  LocalDate.of(2021,9,22), 199.99,BillStatus.UNPAID);
        BillRequestDTO b2 = new BillRequestDTO( "2", "operation", "2", LocalDate.of(2021,9,20), 199.99,BillStatus.OVERDUE);
        BillRequestDTO b5 = new BillRequestDTO( "5", "chronic", "3",  LocalDate.of(2021,9,23), 199.99,BillStatus.UNPAID);
        BillRequestDTO b6 = new BillRequestDTO( "6", "general", "1", LocalDate.of(2021,9,24), 59.99, BillStatus.PAID);
        BillRequestDTO b7 = new BillRequestDTO( "7", "operation", "3", LocalDate.of(2021,9,25), 199.99, BillStatus.PAID);
        BillRequestDTO b8 = new BillRequestDTO( "8", "injury", "1",  LocalDate.of(2021,9,26), 199.99, BillStatus.UNPAID);
        BillRequestDTO b9 = new BillRequestDTO( "9", "operation", "2", LocalDate.of(2021,9,27), 199.99, BillStatus.OVERDUE);
        BillRequestDTO b10 = new BillRequestDTO( "10", "chronic", "3",  LocalDate.of(2021,9,28), 199.99, BillStatus.UNPAID);
        BillRequestDTO b11 = new BillRequestDTO( "11", "general", "1", LocalDate.of(2021,9,29), 59.99, BillStatus.PAID);
        BillRequestDTO b12 = new BillRequestDTO( "12", "operation", "2", LocalDate.of(2021,9,30), 199.99, BillStatus.PAID);
        BillRequestDTO b13 = new BillRequestDTO( "13", "injury", "2",  LocalDate.of(2021,10,1), 199.99, BillStatus.UNPAID);
        BillRequestDTO b14 = new BillRequestDTO( "14", "operation", "3", LocalDate.of(2021,10,2), 199.99, BillStatus.OVERDUE);
        BillRequestDTO b15 = new BillRequestDTO( "15", "chronic", "3",  LocalDate.of(2021,10,3), 199.99, BillStatus.UNPAID);
        BillRequestDTO b16 = new BillRequestDTO( "16", "general", "1", LocalDate.of(2021,10,4), 59.99, BillStatus.PAID);
        BillRequestDTO b17 = new BillRequestDTO( "17", "operation", "1", LocalDate.of(2021,10,5), 199.99, BillStatus.PAID);
        BillRequestDTO b18 = new BillRequestDTO( "18", "injury", "2",  LocalDate.of(2021,10,6), 199.99, BillStatus.UNPAID);
        BillRequestDTO b19 = new BillRequestDTO( "19", "operation", "1", LocalDate.of(2021,10,7), 199.99, BillStatus.OVERDUE);
        BillRequestDTO b20 = new BillRequestDTO( "20", "chronic", "3",  LocalDate.of(2021,10,8), 199.99, BillStatus.UNPAID);
        BillRequestDTO b21 = new BillRequestDTO( "21", "general", "1", LocalDate.of(2021,10,9), 59.99, BillStatus.PAID);
        BillRequestDTO b22 = new BillRequestDTO( "22", "operation", "2", LocalDate.of(2021,10,10), 199.99, BillStatus.PAID);
        BillRequestDTO b23 = new BillRequestDTO( "23", "injury", "3",  LocalDate.of(2021,10,11), 199.99, BillStatus.UNPAID);
        BillRequestDTO b24 = new BillRequestDTO( "24", "operation", "1", LocalDate.of(2021,10,12), 199.99, BillStatus.OVERDUE);
        BillRequestDTO b25 = new BillRequestDTO( "25", "chronic", "2",  LocalDate.of(2021,10,13), 199.99, BillStatus.UNPAID);
        BillRequestDTO b26 = new BillRequestDTO( "26", "general", "2", LocalDate.of(2021,10,14), 59.99, BillStatus.PAID);
        BillRequestDTO b27 = new BillRequestDTO( "27", "operation", "1", LocalDate.of(2021,10,15), 199.99, BillStatus.PAID);
        BillRequestDTO b28 = new BillRequestDTO( "28", "injury", "1",  LocalDate.of(2021,10,16), 199.99, BillStatus.UNPAID);
        BillRequestDTO b29 = new BillRequestDTO( "29", "operation", "3", LocalDate.of(2021,10,17), 199.99, BillStatus.OVERDUE);
        BillRequestDTO b30 = new BillRequestDTO( "30", "chronic", "1",  LocalDate.of(2021,10,18), 199.99, BillStatus.UNPAID);

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
