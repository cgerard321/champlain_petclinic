package com.petclinic.inventoryservice.datalayer;

import com.petclinic.inventoryservice.businesslayer.BundleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.sql.Date;

@Service
public class DataSetupService implements CommandLineRunner {
    private final BundleService bundleService;
    public DataSetupService(BundleService bundleService) {
        this.bundleService = bundleService;
    }


    @Override
    public void run(String... args) throws Exception {
        BundleDTO b1 = new BundleDTO("Penicillin", 25, Date.valueOf("2022-11-19"));
        BundleDTO b3 = new BundleDTO("Hydrogen Peroxide", 50, Date.valueOf("2022-10-31"));
        BundleDTO b4 = new BundleDTO("Cephalexin", 12, Date.valueOf("2022-12-25"));
        BundleDTO b2 = new BundleDTO("Enrofloxacin", 34,  Date.valueOf("2023-2-28"));
        BundleDTO b5 = new BundleDTO("Loperamide", 7, Date.valueOf("2022-09-09"));

        Flux.just(b1,b2,b3,b4,b5)
                .flatMap(b -> bundleService.CreateBundle(Mono.just(b))
                        .log(b.toString()))
                .subscribe();

    }
}
