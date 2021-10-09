package com.petclinic.customers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@SpringBootApplication
@ComponentScan("com.petclinic")
public class CustomersServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(CustomersServiceApplication.class, args);

	}
}
