package com.petclinic.visits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan("com.petclinic")
public class VisitsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisitsServiceApplication.class, args);
	}

}
