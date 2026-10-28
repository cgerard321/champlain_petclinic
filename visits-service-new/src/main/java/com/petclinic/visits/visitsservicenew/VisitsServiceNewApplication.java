package com.petclinic.visits.visitsservicenew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class VisitsServiceNewApplication {


    public static void main(String[] args) {
        SpringApplication.run(VisitsServiceNewApplication.class, args);
    }

}
