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
        BillRequestDTO b6 = new BillRequestDTO("6", "general", "2", LocalDate.of(2023, 10, 5), 59.99, BillStatus.PAID, LocalDate.of(2023, 10, 10));
        BillRequestDTO b7 = new BillRequestDTO("7", "operation", "3", LocalDate.of(2023, 10, 8), 199.99, BillStatus.PAID, LocalDate.of(2023, 10, 14));
        BillRequestDTO b8 = new BillRequestDTO("8", "injury", "2", LocalDate.of(2023, 10, 15), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 10, 30));
        BillRequestDTO b9 = new BillRequestDTO("9", "chronic", "1", LocalDate.of(2023, 10, 18), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 2));
        BillRequestDTO b10 = new BillRequestDTO("10", "general", "3", LocalDate.of(2023, 10, 21), 59.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 6));
        BillRequestDTO b11 = new BillRequestDTO("11", "operation", "1", LocalDate.of(2023, 10, 24), 199.99, BillStatus.PAID, LocalDate.of(2023, 11, 10));
        BillRequestDTO b12 = new BillRequestDTO("12", "injury", "3", LocalDate.of(2023, 10, 27), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 11, 14));
        BillRequestDTO b13 = new BillRequestDTO("13", "chronic", "2", LocalDate.of(2023, 10, 30), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 18));
        BillRequestDTO b14 = new BillRequestDTO("14", "general", "1", LocalDate.of(2023, 11, 2), 59.99, BillStatus.PAID, LocalDate.of(2023, 11, 22));
        BillRequestDTO b15 = new BillRequestDTO("15", "operation", "2", LocalDate.of(2023, 11, 5), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 11, 26));
        BillRequestDTO b16 = new BillRequestDTO("16", "injury", "1", LocalDate.of(2023, 11, 8), 199.99, BillStatus.PAID, LocalDate.of(2023, 12, 2));
        BillRequestDTO b17 = new BillRequestDTO("17", "chronic", "3", LocalDate.of(2023, 11, 11), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 12, 6));
        BillRequestDTO b18 = new BillRequestDTO("18", "general", "2", LocalDate.of(2023, 11, 14), 59.99, BillStatus.PAID, LocalDate.of(2023, 12, 10));
        BillRequestDTO b19 = new BillRequestDTO("19", "operation", "3", LocalDate.of(2023, 11, 17), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 12, 14));
        BillRequestDTO b20 = new BillRequestDTO("20", "injury", "1", LocalDate.of(2023, 11, 20), 199.99, BillStatus.PAID, LocalDate.of(2023, 12, 18));
        BillRequestDTO b21 = new BillRequestDTO("21", "chronic", "2", LocalDate.of(2023, 11, 23), 199.99, BillStatus.OVERDUE, LocalDate.of(2023, 12, 22));
        BillRequestDTO b22 = new BillRequestDTO("22", "general", "3", LocalDate.of(2023, 11, 26), 59.99, BillStatus.PAID, LocalDate.of(2023, 12, 26));
        BillRequestDTO b23 = new BillRequestDTO("23", "operation", "2", LocalDate.of(2023, 11, 29), 199.99, BillStatus.UNPAID, LocalDate.of(2023, 12, 30));
        BillRequestDTO b24 = new BillRequestDTO("24", "injury", "1", LocalDate.of(2023, 12, 2), 199.99, BillStatus.PAID, LocalDate.of(2024, 1, 2));
        BillRequestDTO b25 = new BillRequestDTO("25", "chronic", "3", LocalDate.of(2023, 12, 5), 199.99, BillStatus.OVERDUE, LocalDate.of(2024, 1, 6));
        BillRequestDTO b26 = new BillRequestDTO("26", "general", "2", LocalDate.of(2023, 12, 8), 59.99, BillStatus.UNPAID, LocalDate.of(2024, 1, 10));
        BillRequestDTO b27 = new BillRequestDTO("27", "operation", "3", LocalDate.of(2023, 12, 11), 199.99, BillStatus.PAID, LocalDate.of(2024, 1, 14));
        BillRequestDTO b28 = new BillRequestDTO("28", "injury", "1", LocalDate.of(2023, 12, 14), 199.99, BillStatus.PAID, LocalDate.of(2024, 1, 18));
        BillRequestDTO b29 = new BillRequestDTO("29", "chronic", "2", LocalDate.of(2023, 12, 17), 199.99, BillStatus.OVERDUE, LocalDate.of(2024, 1, 22));
        BillRequestDTO b30 = new BillRequestDTO("30", "general", "3", LocalDate.of(2023, 12, 20), 59.99, BillStatus.UNPAID, LocalDate.of(2024, 1, 26));

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
