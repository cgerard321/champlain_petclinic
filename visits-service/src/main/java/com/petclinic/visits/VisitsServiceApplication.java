package com.petclinic.visits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
<<<<<<< HEAD
import org.springframework.stereotype.Component;
=======
>>>>>>> c845a03... Added routings for Add/Delete/Update visit in the API and created Junit tests.

@SpringBootApplication
@ComponentScan("com.petclinic")
public class VisitsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisitsServiceApplication.class, args);
	}

}