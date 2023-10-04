package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DataBaseLoaderService  implements CommandLineRunner {
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryTypeRepository inventoryTypeRepository;

    @Override
    public void run(String... args) throws Exception {
        InventoryType inventoryType1 = InventoryType.builder()
                .typeId(UUID.randomUUID().toString())
                .type("Internal")
                .build();
        InventoryType inventoryType2 = InventoryType.builder()
                .typeId(UUID.randomUUID().toString())
                .type("Sales")
                .build();
        Inventory inventory1 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Medications for Dogs")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Medications for canine patients")
                .build();
        Inventory inventory2 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Surgical Instruments")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Instruments for surgical procedures on pets")
                .build();
        Inventory inventory3 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Pet Food")
                .inventoryType(inventoryType2.getType())
                .inventoryDescription("Various types of pet food for sale")
                .build();
        Inventory inventory4 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("X-ray Machine")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("X-ray machine for diagnosing pet conditions")
                .build();
        Inventory inventory5 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Pet Carriers")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Carriers for transporting pets")
                .build();
        Inventory inventory6 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Lab Equipment")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Laboratory equipment for pet diagnostics")
                .build();
        Inventory inventory7 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Vaccines")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Vaccines for preventing pet diseases")
                .build();
        Inventory inventory8 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Pet Grooming Supplies")
                .inventoryType(inventoryType2.getType())
                .inventoryDescription("Supplies for grooming and hygiene of pets")
                .build();
        Inventory inventory9 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Dental Care Products")
                .inventoryType(inventoryType2.getType())
                .inventoryDescription("Products for pet dental care")
                .build();
        Inventory inventory10 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Pet Bedding")
                .inventoryType(inventoryType2.getType())
                .inventoryDescription("Bedding materials for pet comfort")
                .build();
        Inventory inventory11 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Pet Leashes and Collars")
                .inventoryType(inventoryType2.getType())
                .inventoryDescription("Leashes and collars for pet restraint and identification")
                .build();


        Product product1 = Product.builder()
                .productName("Benzodiazepines")
                .productId(UUID.randomUUID().toString())
                .productPrice(100.00)
                .inventoryId("1")
                .productQuantity(10)
                .productDescription("Drugs for sleep")
                .build();
        Product product2 = Product.builder()
                .productName("Trazodone")
                .productId(UUID.randomUUID().toString())
                .productPrice(150.00)
                .inventoryId("1")
                .productQuantity(10)
                .productDescription("Drugs for anxiety/stress")
                .build();
        Flux.just(product1, product2)
                .flatMap(productRepository::insert)
                .log()
                .subscribe();
        Flux.just(inventory1, inventory2, inventory3, inventory4, inventory5, inventory6, inventory7, inventory8, inventory9, inventory10, inventory11)
                .flatMap(inventoryRepository::insert)
                .log()
                .subscribe();
        Mono.just(inventoryType1)
                .flatMap(inventoryTypeRepository::insert)
                .log()
                .subscribe();
        Mono.just(inventoryType2)
                .flatMap(inventoryTypeRepository::insert)
                .log()
                .subscribe();
    }
}
