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
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(10)
                .productDescription("Drugs for sleep")
                .productSalePrice(10.00)
                .build();


        Product product2 = Product.builder()
                .productName("Trazodone")
                .productId(UUID.randomUUID().toString())
                .productPrice(150.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(10)
                .productDescription("Drugs for anxiety/stress")
                .productSalePrice(10.00)
                .build();
        // Product 4
        Product product4 = Product.builder()
                .productName("Carprofen")
                .productId(UUID.randomUUID().toString())
                .productPrice(130.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(12)
                .productDescription("Non-steroidal anti-inflammatory drug")
                .productSalePrice(140.00)
                .build();

// Product 5
        Product product5 = Product.builder()
                .productName("Doxycycline")
                .productId(UUID.randomUUID().toString())
                .productPrice(70.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(25)
                .productDescription("Antibiotic for various infections")
                .productSalePrice(75.00)
                .build();

// Product 6
        Product product6 = Product.builder()
                .productName("Fluoxetine")
                .productId(UUID.randomUUID().toString())
                .productPrice(110.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(18)
                .productDescription("Medication for behavioral issues")
                .productSalePrice(120.00)
                .build();

// Product 7
        Product product7 = Product.builder()
                .productName("Deracoxib")
                .productId(UUID.randomUUID().toString())
                .productPrice(140.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(14)
                .productDescription("Pain and inflammation relief")
                .productSalePrice(150.00)
                .build();

// Product 8
        Product product8 = Product.builder()
                .productName("Cephalexin")
                .productId(UUID.randomUUID().toString())
                .productPrice(90.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(22)
                .productDescription("Broad-spectrum antibiotic")
                .productSalePrice(95.00)
                .build();

// Product 9
        Product product9 = Product.builder()
                .productName("Prednisone")
                .productId(UUID.randomUUID().toString())
                .productPrice(60.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(30)
                .productDescription("Corticosteroid for inflammation")
                .productSalePrice(65.00)
                .build();

// Product 10
        Product product10 = Product.builder()
                .productName("Gabapentin")
                .productId(UUID.randomUUID().toString())
                .productPrice(100.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(16)
                .productDescription("Medication for pain management")
                .productSalePrice(110.00)
                .build();

// Product 11
        Product product11 = Product.builder()
                .productName("Cerenia")
                .productId(UUID.randomUUID().toString())
                .productPrice(75.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(27)
                .productDescription("Anti-nausea medication")
                .productSalePrice(80.00)
                .build();

// Product 12
        Product product12 = Product.builder()
                .productName("Simparica")
                .productId(UUID.randomUUID().toString())
                .productPrice(45.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(40)
                .productDescription("Oral flea and tick prevention")
                .productSalePrice(50.00)
                .build();

// Product 13
        Product product13 = Product.builder()
                .productName("Heartgard Plus")
                .productId(UUID.randomUUID().toString())
                .productPrice(60.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(35)
                .productDescription("Heartworm prevention")
                .productSalePrice(65.00)
                .build();

// Product 14
        Product product14 = Product.builder()
                .productName("Apomorphine")
                .productId(UUID.randomUUID().toString())
                .productPrice(40.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(50)
                .productDescription("Induces vomiting in emergencies")
                .productSalePrice(45.00)
                .build();

// Product 15
        Product product15 = Product.builder()
                .productName("Diazepam")
                .productId(UUID.randomUUID().toString())
                .productPrice(55.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(32)
                .productDescription("Anxiety and muscle relaxant")
                .productSalePrice(60.00)
                .build();

// Product 16
        Product product16 = Product.builder()
                .productName("Trifexis")
                .productId(UUID.randomUUID().toString())
                .productPrice(70.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(28)
                .productDescription("Flea, heartworm, and parasite prevention")
                .productSalePrice(75.00)
                .build();

// Product 17
        Product product17 = Product.builder()
                .productName("Ketoconazole")
                .productId(UUID.randomUUID().toString())
                .productPrice(85.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(24)
                .productDescription("Antifungal medication")
                .productSalePrice(90.00)
                .build();

// Product 18
        Product product18 = Product.builder()
                .productName("Apoquel")
                .productId(UUID.randomUUID().toString())
                .productPrice(95.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(21)
                .productDescription("Treatment for allergies and itching")
                .productSalePrice(100.00)
                .build();

// Product 19
        Product product19 = Product.builder()
                .productName("Enalapril")
                .productId(UUID.randomUUID().toString())
                .productPrice(65.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(33)
                .productDescription("Medication for heart conditions")
                .productSalePrice(70.00)
                .build();

// Product 20
        Product product20 = Product.builder()
                .productName("Loperamide")
                .productId(UUID.randomUUID().toString())
                .productPrice(50.00)
                .inventoryId(inventory1.getInventoryId())
                .productQuantity(45)
                .productDescription("Anti-diarrheal medication")
                .productSalePrice(55.00)
                .build();


        Flux.just(product1, product2, product4, product5, product6, product7, product8, product9, product10, product11,
                        product12, product13, product14, product15, product16, product17, product18, product19,
                        product20)
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
