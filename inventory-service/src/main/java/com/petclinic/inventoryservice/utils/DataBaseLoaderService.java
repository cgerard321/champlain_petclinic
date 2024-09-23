package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.*;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.datalayer.Supply.Supply;
import com.petclinic.inventoryservice.datalayer.Supply.SupplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class DataBaseLoaderService  implements CommandLineRunner {
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    SupplyRepository supplyRepository;
    @Autowired
    InventoryTypeRepository inventoryTypeRepository;
    @Autowired
    InventoryNameRepository inventoryNameRepository;

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



        InventoryName inventoryName1 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Medical equipment")
                .build();
        InventoryName inventoryName2 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("First-Aid")
                .build();
        InventoryName inventoryName3 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Vaccines")
                .build();
        InventoryName inventoryName4 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Medications")
                .build();
        InventoryName inventoryName5 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Pet Carriers")
                .build();
        InventoryName inventoryName6 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Diagnostic Kits")
                .build();
        InventoryName inventoryName7 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Surgical Instruments")
                .build();
        InventoryName inventoryName8 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Anesthesia Supplies")
                .build();
        InventoryName inventoryName9 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Wound Care Supplies")
                .build();
        InventoryName inventoryName10 = InventoryName.builder()
                .nameId(UUID.randomUUID().toString())
                .name("Infectious Disease Test Kits")
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

        //MEDICATIONS
        Supply Supply1 = Supply.builder()
                .supplyName("Sedative Medications")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(100.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .supplyQuantity(10)
                .supplyDescription("Medications for relaxation and sleep")
                .supplySalePrice(10.00)
                .build();

        Supply Supply2 = Supply.builder()
                .supplyName("Anxiety Relief Tablets")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(150.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .supplyQuantity(10)
                .supplyDescription("Tablets for reducing anxiety and stress")
                .supplySalePrice(10.00)
                .build();

        Supply Supply3 = Supply.builder()
                .supplyName("Pain Relief Medication")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(130.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .supplyQuantity(12)
                .supplyDescription("Non-steroidal pain relief medication")
                .supplySalePrice(140.00)
                .build();

        Supply Supply4 = Supply.builder()
                .supplyName("Adhesive Bandages")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(15.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .supplyQuantity(25)
                .supplyDescription("Adhesive bandages for wound care")
                .supplySalePrice(75.00)
                .build();

        Supply Supply5 = Supply.builder()
                .supplyName("Non-Woven Gauze Pads")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(10.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .supplyQuantity(30)
                .supplyDescription("Absorbent gauze pads for wound care")
                .supplySalePrice(20.00)
                .build();

        Supply Supply6 = Supply.builder()
                .supplyName("Wound Cleaning Solution")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(5.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .supplyQuantity(40)
                .supplyDescription("Antiseptic solution for cleaning wounds")
                .supplySalePrice(10.00)
                .build();

        Supply Supply7 = Supply.builder()
                .supplyName("Diagnostic MRI Machine")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(100000.00)
                .inventoryId(inventory1.getInventoryId()) // Equipment
                .supplyQuantity(1)
                .supplyDescription("Advanced imaging machine for diagnostics")
                .supplySalePrice(110000.00)
                .build();

        Supply Supply8 = Supply.builder()
                .supplyName("Canine Rabies Vaccine")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(50.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .supplyQuantity(20)
                .supplyDescription("Vaccine for rabies prevention in dogs")
                .supplySalePrice(55.00)
                .build();

// Equipment
        Supply Supply9 = Supply.builder()
                .supplyName("Flexible Endoscope")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(20000.00)
                .inventoryId(inventory1.getInventoryId()) // Medical equipment
                .supplyQuantity(1)
                .supplyDescription("Flexible endoscope for internal examinations")
                .supplySalePrice(22000.00)
                .build();

        Supply Supply10 = Supply.builder()
                .supplyName("Portable Ultrasound System")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(30000.00)
                .inventoryId(inventory1.getInventoryId()) // Medical equipment
                .supplyQuantity(1)
                .supplyDescription("Portable ultrasound system for diagnostics")
                .supplySalePrice(32000.00)
                .build();

// Bandages
        Supply Supply11 = Supply.builder()
                .supplyName("Compression Bandage")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(8.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .supplyQuantity(50)
                .supplyDescription("Compression bandage for support")
                .supplySalePrice(15.00)
                .build();

        // Bandages
        Supply Supply12 = Supply.builder()
                .supplyName("Antiseptic Wipes")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(12.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .supplyQuantity(60)
                .supplyDescription("Antiseptic wipes for cleaning wounds")
                .supplySalePrice(25.00)
                .build();

// Injections
        Supply Supply13 = Supply.builder()
                .supplyName("Insulin Syringes")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(50.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .supplyQuantity(100)
                .supplyDescription("Syringes for administering insulin")
                .supplySalePrice(55.00)
                .build();

        Supply Supply14 = Supply.builder()
                .supplyName("IV Catheters")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(30.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .supplyQuantity(50)
                .supplyDescription("Intravenous catheters for fluid administration")
                .supplySalePrice(35.00)
                .build();

// Medications
        Supply Supply15 = Supply.builder()
                .supplyName("Antihistamines")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(80.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .supplyQuantity(20)
                .supplyDescription("Antihistamines for allergic reactions")
                .supplySalePrice(90.00)
                .build();

        Supply Supply16 = Supply.builder()
                .supplyName("Antibiotic Ointment")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(25.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .supplyQuantity(25)
                .supplyDescription("Topical antibiotic ointment for wound care")
                .supplySalePrice(30.00)
                .build();

// Diagnostic Kits
        Supply Supply17 = Supply.builder()
                .supplyName("Blood Glucose Test Kits")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(70.00)
                .inventoryId(inventory6.getInventoryId()) // Diagnostic Kits
                .supplyQuantity(30)
                .supplyDescription("Test kits for measuring blood glucose levels")
                .supplySalePrice(75.00)
                .build();

        Supply Supply18 = Supply.builder()
                .supplyName("Urinalysis Test Kits")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(60.00)
                .inventoryId(inventory6.getInventoryId()) // Diagnostic Kits
                .supplyQuantity(30)
                .supplyDescription("Test kits for analyzing urine samples")
                .supplySalePrice(65.00)
                .build();

// Wound Care Supplies
        Supply Supply19 = Supply.builder()
                .supplyName("Hydrogel Dressing")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(20.00)
                .inventoryId(inventory9.getInventoryId()) // Wound Care Supplies
                .supplyQuantity(40)
                .supplyDescription("Hydrogel dressing for moist wound healing")
                .supplySalePrice(25.00)
                .build();

        Supply Supply20 = Supply.builder()
                .supplyName("Sutures Kit")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(120.00)
                .inventoryId(inventory9.getInventoryId()) // Wound Care Supplies
                .supplyQuantity(15)
                .supplyDescription("Kit containing sutures for wound closure")
                .supplySalePrice(130.00)
                .build();

// Anesthesia Supplies
        Supply Supply21 = Supply.builder()
                .supplyName("Anesthesia Machines")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(5000.00)
                .inventoryId(inventory8.getInventoryId()) // Anesthesia Supplies
                .supplyQuantity(2)
                .supplyDescription("Machines for administering anesthesia")
                .supplySalePrice(5500.00)
                .build();

        Supply Supply22 = Supply.builder()
                .supplyName("Anesthetic Masks")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(200.00)
                .inventoryId(inventory8.getInventoryId()) // Anesthesia Supplies
                .supplyQuantity(20)
                .supplyDescription("Masks for delivering anesthesia")
                .supplySalePrice(220.00)
                .build();

        Supply Supply23 = Supply.builder()
                .supplyName("Surgical Scissors")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(150.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .supplyQuantity(10)
                .supplyDescription("Scissors for cutting tissues during surgery")
                .supplySalePrice(160.00)
                .build();

        Supply Supply24 = Supply.builder()
                .supplyName("Hemostats")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(120.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .supplyQuantity(15)
                .supplyDescription("Clamps for controlling bleeding during surgery")
                .supplySalePrice(130.00)
                .build();

        Supply Supply25 = Supply.builder()
                .supplyName("Needle Holders")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(180.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .supplyQuantity(10)
                .supplyDescription("Hold needles securely during suturing")
                .supplySalePrice(190.00)
                .build();

        Supply Supply26 = Supply.builder()
                .supplyName("Pet Carrier Accessories")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(25.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .supplyQuantity(50)
                .supplyDescription("Accessories like water bowls and food trays for pet carriers")
                .supplySalePrice(30.00)
                .build();

        Supply Supply27 = Supply.builder()
                .supplyName("Carrier Cleaning Supplies")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(10.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .supplyQuantity(60)
                .supplyDescription("Cleaning sprays and wipes for pet carriers")
                .supplySalePrice(15.00)
                .build();

        Supply Supply28 = Supply.builder()
                .supplyName("Comfort Pads")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(20.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .supplyQuantity(40)
                .supplyDescription("Comfortable pads for lining pet carriers")
                .supplySalePrice(25.00)
                .build();

        Supply Supply29 = Supply.builder()
                .supplyName("Parvovirus Test Kits")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(50.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .supplyQuantity(30)
                .supplyDescription("Test kits for detecting canine parvovirus")
                .supplySalePrice(55.00)
                .build();

        Supply Supply30 = Supply.builder()
                .supplyName("Feline Leukemia Test Kits")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(60.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .supplyQuantity(25)
                .supplyDescription("Test kits for detecting feline leukemia virus")
                .supplySalePrice(65.00)
                .build();

        Supply Supply31 = Supply.builder()
                .supplyName("Heartworm Test Kits")
                .supplyId(UUID.randomUUID().toString())
                .supplyPrice(55.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .supplyQuantity(20)
                .supplyDescription("Test kits for detecting heartworm infection")
                .supplySalePrice(60.00)
                .build();


        //Inventory1 ---------------------------------------------------
        inventory1.addSupply(Supply9);
        inventory1.addSupply(Supply10);
        //--------------------------------------------------------------

        //Inventory2 ---------------------------------------------------
        inventory2.addSupply(Supply4);
        inventory2.addSupply(Supply5);
        inventory2.addSupply(Supply6);
        //--------------------------------------------------------------

        //Inventory3 ---------------------------------------------------
        inventory3.addSupply(Supply8);
        //--------------------------------------------------------------

        //Inventory4 ---------------------------------------------------
        inventory4.addSupply(Supply1);
        inventory4.addSupply(Supply2);
        inventory4.addSupply(Supply3);
        //--------------------------------------------------------------

        //Inventory5 ---------------------------------------------------
        inventory5.addSupply(Supply26);
        inventory5.addSupply(Supply27);
        inventory5.addSupply(Supply28);
        //--------------------------------------------------------------

        //Inventory6 ---------------------------------------------------
        inventory6.addSupply(Supply17);
        inventory6.addSupply(Supply18);
        //--------------------------------------------------------------

        //Inventory7 ---------------------------------------------------
        inventory7.addSupply(Supply23);
        inventory7.addSupply(Supply24);
        inventory7.addSupply(Supply25);
        //--------------------------------------------------------------

        //Inventory8 ---------------------------------------------------
        inventory8.addSupply(Supply21);
        inventory8.addSupply(Supply22);
        //--------------------------------------------------------------

        //Inventory9 ---------------------------------------------------
        inventory9.addSupply(Supply19);
        inventory9.addSupply(Supply20);
        //--------------------------------------------------------------

        //Inventory10 ---------------------------------------------------
        inventory10.addSupply(Supply29);
        inventory10.addSupply(Supply30);
        inventory10.addSupply(Supply31);
        //--------------------------------------------------------------



        Flux.just(supply1, supply2, supply3, supply4, supply5, supply6, supply7, supply8, supply9,
                        supply10, supply11, supply12, supply13, supply14, supply15,
                        supply16, supply17, supply18, supply19, supply20, supply21, supply22,
                        supply23, supply24, supply25, supply26, supply27, supply28, supply29, supply30, supply31)
                .flatMap(productRepository::insert)
                .log()
                .subscribe();

        Flux.just(Supply1, Supply2, Supply3, Supply4, Supply5, Supply6, Supply7, Supply8, Supply9,
                        Supply10, Supply11, Supply12, Supply13, Supply14, Supply15,
                        Supply16, Supply17, Supply18, Supply19, Supply20, Supply21, Supply22,
                        Supply23, Supply24, Supply25, Supply26, Supply27, Supply28, Supply29, Supply30, Supply31)
                .flatMap(supplyRepository::insert)
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

        Flux.just(inventoryName1,inventoryName2,inventoryName3,inventoryName4,inventoryName5,
                inventoryName6, inventoryName7, inventoryName8, inventoryName9, inventoryName10)
                .flatMap(inventoryNameRepository::insert)
                .log()
                .subscribe();
    }
}
