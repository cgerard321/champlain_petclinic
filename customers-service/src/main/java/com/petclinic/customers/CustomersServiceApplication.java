package com.petclinic.customers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@SpringBootApplication
public class CustomersServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(CustomersServiceApplication.class, args);

	}
}
