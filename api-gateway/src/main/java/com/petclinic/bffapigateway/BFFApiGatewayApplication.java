package com.petclinic.bffapigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.petclinic")
public class BFFApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(BFFApiGatewayApplication.class, args);
	}

}
