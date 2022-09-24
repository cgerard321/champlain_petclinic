package com.petclinic.billing;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillRepository;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@ComponentScan("com.petclinic")
public class BillingServiceApplication {


	@Bean()
	ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

		ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		// This will run the schema-mysql.sql file resulting in the creation of the database table and schema
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema-mysql.sql")));
		//this will run the data-mysql.sql file inserting a few data rows in the database
		//initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("data-mysql.sql")));
		return initializer;
	}


/*

	@Bean
	public CommandLineRunner reactiveDatabaseSetup(BillRepository billRepository){
		return args -> {
			List<Bill> bills = Arrays.asList(
				new Bill(1, 1, "general", Date.valueOf("2021-09-19"),59.99),
				new Bill(2, 2, "operation", Date.valueOf("2021-09-20"),199.99),
				new Bill(3, 3, "operation", Date.valueOf("2021-09-21"),199.99),
				new Bill(4, 4, "operation", Date.valueOf("2021-09-22"),199.99),
				new Bill(5, 5, "operation", Date.valueOf("2021-09-23"),199.99),
				new Bill(6, 6, "operation", Date.valueOf("2021-09-23"),199.99)
			);
//			billRepository.saveAll(bills);

		};
	}
*/


	public static void main(String[] args) {
		SpringApplication.run(BillingServiceApplication.class, args);
	}

}
