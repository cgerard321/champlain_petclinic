package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.*;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.datalayer.Product.Status;
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
                .type("Equipment")
                .build();
        InventoryType inventoryType2 = InventoryType.builder()
                .typeId(UUID.randomUUID().toString())
                .type("Bandages")
                .build();
        InventoryType inventoryType3 = InventoryType.builder()
                .typeId(UUID.randomUUID().toString())
                .type("Injections")
                .build();
        InventoryType inventoryType4 = InventoryType.builder()
                .typeId(UUID.randomUUID().toString())
                .type("Medications")
                .build();
        InventoryType inventoryType5 = InventoryType.builder()
                .typeId(UUID.randomUUID().toString())
                .type("Diagnostic Kits")
                .build();



        Inventory inventory1 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Medical equipment")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Medical equipment for surgery")
                .build();

        Inventory inventory2 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("First-Aid")
                .inventoryType(inventoryType2.getType())
                .inventoryDescription("First-aid supplies for pet emergencies")
                .build();

        Inventory inventory3 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Vaccines")
                .inventoryType(inventoryType3.getType())
                .inventoryDescription("Supplies for disease prevention")
                .build();

        Inventory inventory4 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Medications")
                .inventoryType(inventoryType4.getType())
                .inventoryDescription("Antibiotics for pet infections")
                .build();

        Inventory inventory5 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Pet Carriers")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Carriers for transporting pets")
                .build();

        Inventory inventory6 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Diagnostic Kits")
                .inventoryType(inventoryType1.getType()) // Assuming medical supplies use the same type
                .inventoryDescription("Kits for diagnosing various pet illnesses and conditions")
                .build();

        Inventory inventory7 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Surgical Instruments")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Tools for performing surgical procedures on pets")
                .build();

        Inventory inventory8 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Anesthesia Supplies")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Supplies for administering anesthesia during surgeries")
                .build();

        Inventory inventory9 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Wound Care Supplies")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Supplies for treating and dressing wounds")
                .build();

        Inventory inventory10 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryName("Infectious Disease Test Kits")
                .inventoryType(inventoryType5.getType())
                .inventoryDescription("Kits for testing infectious diseases in pets")
                .build();






        //MEDICATIONS
        Product supply1 = Product.builder()
                .productName("Benzodiazepines")
                .productId(UUID.randomUUID().toString())
                .productPrice(100.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(10)
                .productDescription("Drugs for sleep")
                .productSalePrice(10.00)
                .build();

        Product supply2 = Product.builder()
                .productName("Trazodone")
                .productId(UUID.randomUUID().toString())
                .productPrice(150.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(10)
                .productDescription("Drugs for anxiety/stress")
                .productSalePrice(10.00)
                .build();

        Product supply3 = Product.builder()
                .productName("Carprofen")
                .productId(UUID.randomUUID().toString())
                .productPrice(130.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(12)
                .productDescription("Non-steroidal anti-inflammatory drug")
                .productSalePrice(140.00)
                .build();

        Product supply4 = Product.builder()
                .productName("Self-adhesive Bandages")
                .productId(UUID.randomUUID().toString())
                .productPrice(15.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(25)
                .productDescription("Bandages for wound care")
                .productSalePrice(75.00)
                .build();

        Product supply5 = Product.builder()
                .productName("Gauze Pads")
                .productId(UUID.randomUUID().toString())
                .productPrice(10.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(30)
                .productDescription("Absorbent pads for wound care")
                .productSalePrice(20.00)
                .build();

        Product supply6 = Product.builder()
                .productName("Hydrogen Peroxide")
                .productId(UUID.randomUUID().toString())
                .productPrice(5.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(40)
                .productDescription("Antiseptic for wound cleaning")
                .productSalePrice(10.00)
                .build();

        Product supply7 = Product.builder()
                .productName("MRI Machine")
                .productId(UUID.randomUUID().toString())
                .productPrice(100000.00)
                .inventoryId(inventory1.getInventoryId()) // Equipment
                .productQuantity(1)
                .productDescription("Imaging machine for diagnostics")
                .productSalePrice(110000.00)
                .build();

        Product supply8 = Product.builder()
                .productName("Rabies Vaccine")
                .productId(UUID.randomUUID().toString())
                .productPrice(50.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .productQuantity(20)
                .productDescription("Core vaccine for rabies prevention")
                .productSalePrice(55.00)
                .build();

        // Equipment
        Product supply9 = Product.builder()
                .productName("Endoscope")
                .productId(UUID.randomUUID().toString())
                .productPrice(20000.00)
                .inventoryId(inventory1.getInventoryId()) // Medical equipment
                .productQuantity(1)
                .productDescription("Endoscope for internal examinations")
                .productSalePrice(22000.00)
                .build();

        Product supply10 = Product.builder()
                .productName("Ultrasound Machine")
                .productId(UUID.randomUUID().toString())
                .productPrice(30000.00)
                .inventoryId(inventory1.getInventoryId()) // Medical equipment
                .productQuantity(1)
                .productDescription("Ultrasound machine for diagnostic imaging")
                .productSalePrice(32000.00)
                .build();

// Bandages
        Product supply11 = Product.builder()
                .productName("Elastic Bandage")
                .productId(UUID.randomUUID().toString())
                .productPrice(8.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(50)
                .productDescription("Elastic bandage for flexible wound support")
                .productSalePrice(15.00)
                .build();

        Product supply12 = Product.builder()
                .productName("Antiseptic Wipes")
                .productId(UUID.randomUUID().toString())
                .productPrice(12.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(60)
                .productDescription("Antiseptic wipes for cleaning wounds")
                .productSalePrice(25.00)
                .build();

// Injections
        Product supply13 = Product.builder()
                .productName("Insulin Syringes")
                .productId(UUID.randomUUID().toString())
                .productPrice(50.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .productQuantity(100)
                .productDescription("Syringes for administering insulin")
                .productSalePrice(55.00)
                .build();

        Product supply14 = Product.builder()
                .productName("IV Catheters")
                .productId(UUID.randomUUID().toString())
                .productPrice(30.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .productQuantity(50)
                .productDescription("Intravenous catheters for fluid administration")
                .productSalePrice(35.00)
                .build();

// Medications
        Product supply15 = Product.builder()
                .productName("Antihistamines")
                .productId(UUID.randomUUID().toString())
                .productPrice(80.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(20)
                .productDescription("Antihistamines for allergic reactions")
                .productSalePrice(90.00)
                .build();

        Product supply16 = Product.builder()
                .productName("Antibiotic Ointment")
                .productId(UUID.randomUUID().toString())
                .productPrice(25.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(25)
                .productDescription("Topical antibiotic ointment for wound care")
                .productSalePrice(30.00)
                .build();

// Diagnostic Kits
        Product supply17 = Product.builder()
                .productName("Blood Glucose Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(70.00)
                .inventoryId(inventory6.getInventoryId()) // Diagnostic Kits
                .productQuantity(30)
                .productDescription("Test kits for measuring blood glucose levels")
                .productSalePrice(75.00)
                .build();

        Product supply18 = Product.builder()
                .productName("Urinalysis Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(60.00)
                .inventoryId(inventory6.getInventoryId()) // Diagnostic Kits
                .productQuantity(30)
                .productDescription("Test kits for analyzing urine samples")
                .productSalePrice(65.00)
                .build();

// Wound Care Supplies
        Product supply19 = Product.builder()
                .productName("Hydrogel Dressing")
                .productId(UUID.randomUUID().toString())
                .productPrice(20.00)
                .inventoryId(inventory9.getInventoryId()) // Wound Care Supplies
                .productQuantity(40)
                .productDescription("Hydrogel dressing for moist wound healing")
                .productSalePrice(25.00)
                .build();

        Product supply20 = Product.builder()
                .productName("Sutures Kit")
                .productId(UUID.randomUUID().toString())
                .productPrice(120.00)
                .inventoryId(inventory9.getInventoryId()) // Wound Care Supplies
                .productQuantity(15)
                .productDescription("Kit containing sutures for wound closure")
                .productSalePrice(130.00)
                .build();

        // Anesthesia Supplies
        Product supply21 = Product.builder()
                .productName("Anesthesia Machines")
                .productId(UUID.randomUUID().toString())
                .productPrice(5000.00)
                .inventoryId(inventory8.getInventoryId()) // Anesthesia Supplies
                .productQuantity(2)
                .productDescription("Machines for administering anesthesia")
                .productSalePrice(5500.00)
                .build();

        Product supply22 = Product.builder()
                .productName("Anesthetic Masks")
                .productId(UUID.randomUUID().toString())
                .productPrice(200.00)
                .inventoryId(inventory8.getInventoryId()) // Anesthesia Supplies
                .productQuantity(20)
                .productDescription("Masks for delivering anesthesia")
                .productSalePrice(220.00)
                .build();


        Product supply23 = Product.builder()
                .productName("Surgical Scissors")
                .productId(UUID.randomUUID().toString())
                .productPrice(150.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .productQuantity(10)
                .productDescription("Scissors for cutting tissues during surgery")
                .productSalePrice(160.00)
                .build();

        Product supply24 = Product.builder()
                .productName("Hemostats")
                .productId(UUID.randomUUID().toString())
                .productPrice(120.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .productQuantity(15)
                .productDescription("Clamps for controlling bleeding during surgery")
                .productSalePrice(130.00)
                .build();

        Product supply25 = Product.builder()
                .productName("Needle Holders")
                .productId(UUID.randomUUID().toString())
                .productPrice(180.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .productQuantity(10)
                .productDescription("Hold needles securely during suturing")
                .productSalePrice(190.00)
                .build();

        Product supply26 = Product.builder()
                .productName("Pet Carrier Accessories")
                .productId(UUID.randomUUID().toString())
                .productPrice(25.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .productQuantity(50)
                .productDescription("Accessories like water bowls and food trays for pet carriers")
                .productSalePrice(30.00)
                .build();

        Product supply27 = Product.builder()
                .productName("Carrier Cleaning Supplies")
                .productId(UUID.randomUUID().toString())
                .productPrice(10.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .productQuantity(60)
                .productDescription("Cleaning sprays and wipes for pet carriers")
                .productSalePrice(15.00)
                .build();

        Product supply28 = Product.builder()
                .productName("Comfort Pads")
                .productId(UUID.randomUUID().toString())
                .productPrice(20.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .productQuantity(40)
                .productDescription("Comfortable pads for lining pet carriers")
                .productSalePrice(25.00)
                .build();

        Product supply29 = Product.builder()
                .productName("Parvovirus Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(50.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .productQuantity(30)
                .productDescription("Test kits for detecting canine parvovirus")
                .productSalePrice(55.00)
                .build();

        Product supply30 = Product.builder()
                .productName("Feline Leukemia Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(60.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .productQuantity(25)
                .productDescription("Test kits for detecting feline leukemia virus")
                .productSalePrice(65.00)
                .build();

        Product supply31 = Product.builder()
                .productName("Heartworm Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(55.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .productQuantity(20)
                .productDescription("Test kits for detecting heartworm infection")
                .productSalePrice(60.00)
                .build();

        Flux.just(supply1, supply2, supply3, supply4, supply5, supply6, supply7, supply8, supply9,
                        supply10, supply11, supply12, supply13, supply14, supply15,
                        supply16, supply17, supply18, supply19, supply20, supply21, supply22,
                        supply23, supply24, supply25, supply26, supply27, supply28, supply29, supply30, supply31)
                .flatMap(productRepository::insert)
                .log()
                .subscribe();

        Flux.just(inventory1, inventory2, inventory3, inventory4, inventory5,
                        inventory6, inventory7, inventory8, inventory9, inventory10)
                .flatMap(inventoryRepository::insert)
                .log()
                .subscribe();

        Flux.just(inventoryType1,inventoryType2,inventoryType3,inventoryType4,inventoryType5)
                .flatMap(inventoryTypeRepository::insert)
                .log()
                .subscribe();
    }
}
