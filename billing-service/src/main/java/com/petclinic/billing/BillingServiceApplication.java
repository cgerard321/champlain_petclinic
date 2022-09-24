package com.petclinic.billing;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
//import io.r2dbc.spi.ConnectionFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
//import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;

//import java.sql.Date;
//import java.util.Arrays;
//import java.util.List;

@SpringBootApplication
@ComponentScan("com.petclinic")
public class BillingServiceApplication {

/*
	@Bean()
	ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		// This will run the schema-mysql.sql file resulting in the creation of the database table and schema
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema-mysql.sql")));
		//this should run the data-mysql.sql file inserting a few data rows in the database but it was not working properly
		//initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("data-mysql.sql")));
		return initializer;
	}
*/



	public static void main(String[] args) {
		SpringApplication.run(BillingServiceApplication.class, args);
	}

}
