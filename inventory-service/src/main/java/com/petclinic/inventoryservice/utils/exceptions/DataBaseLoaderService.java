package com.petclinic.inventoryservice.utils.exceptions;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class DataBaseLoaderService  implements CommandLineRunner {
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        Inventory inventory1 = Inventory.builder()
                .inventoryId("1")
                .inventoryType("Medication")
                .inventoryDescription("Medication for procedures")
                .build();
        Product product1 = Product.builder()
                .productName("Benzodiazepines")
                .sku(123456789)
                .productPrice(100.00)
                .inventoryId("1")
                .productQuantity(10)
                .build();
        Product product2 = Product.builder()
                .productName("Trazodone")
                .sku(987654321)
                .productPrice(150.00)
                .inventoryId("1")
                .productQuantity(10)
                .build();
        Flux.just(product1, product2)
                .flatMap(productRepository::insert)
                .log()
                .subscribe();
        Flux.just(inventory1)
                .flatMap(inventoryRepository::insert)
                .log()
                .subscribe();
    }
}
