package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.*;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DataBaseLoaderService implements CommandLineRunner {
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    InventoryTypeRepository inventoryTypeRepository;
    @Autowired
    InventoryNameRepository inventoryNameRepository;

    @Override
    public void run(String... args) throws Exception {

        // If the db is not empty, then return
        try {
            if (Boolean.TRUE.equals(inventoryRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(productRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(inventoryTypeRepository.findAll().hasElements().block())) {
                return;
            }

            if (Boolean.TRUE.equals(inventoryNameRepository.findAll().hasElements().block())) {
                return;
            }
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
            return;
        }


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


        InputStream inputStream = getClass().getResourceAsStream("/images/Medical-EquipmentImage.jpg");
        byte[] medicalEquipmentImage = ImageUtil.readImage(inputStream);

        Inventory inventory1 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0001")
                .inventoryName("Medical equipment")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Medical equipment for surgery")
                .inventoryImage("https://alliedusa.net/wp-content/uploads/2022/06/Tips-for-Choosing-Medical-Equipment-For-Your-Practice.jpg")
                .inventoryBackupImage("https://northsidemedicalsupply.com/wp-content/uploads/2022/12/Medical-Supply-or-Equipment.jpg")
                .imageUploaded(medicalEquipmentImage)
                .build();

        InputStream inputStream2 = getClass().getResourceAsStream("/images/FirstAidImage.jpg");
        byte[] firstAidImage = ImageUtil.readImage(inputStream2);

        Inventory inventory2 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0002")
                .inventoryName("First-Aid")
                .inventoryType(inventoryType2.getType())
                .inventoryDescription("First-aid supplies for pet emergencies")
                .inventoryImage("https://www.lakesidemedical.ca/app/uploads/featuredimage-The-Importance-of-Having-a-First-Aid-Kit-in-Your-Home-or-Place-of-Business.jpg")
                .inventoryBackupImage("https://insights.ibx.com/wp-content/uploads/2019/06/first-aid-kit-screenshot.png")
                .imageUploaded(firstAidImage)
                .build();

        InputStream inputStream3 = getClass().getResourceAsStream("/images/VaccinesImage.jpg");
        byte[] vaccinesImage = ImageUtil.readImage(inputStream3);

        Inventory inventory3 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0003")
                .inventoryName("Vaccines")
                .inventoryType(inventoryType3.getType())
                .inventoryDescription("Supplies for disease prevention")
                .inventoryImage("https://www.fda.gov/files/iStock-157317886.jpg")
                .inventoryBackupImage("https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14")
                .imageUploaded(vaccinesImage)
                .build();

        InputStream inputStream4 = getClass().getResourceAsStream("/images/MedicationsImage.jpg");
        byte[] medicationsImage = ImageUtil.readImage(inputStream4);

        Inventory inventory4 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0004")
                .inventoryName("Medications")
                .inventoryType(inventoryType4.getType())
                .inventoryDescription("Antibiotics for pet infections")
                .inventoryImage("https://logrx.com/wp-content/uploads/2024/01/colorful-pills-tablets-background.jpg")
                .inventoryBackupImage("https://firstaidforlife.org.uk/wp-content/uploads/2018/03/poisoning-pill-bottle.jpg")
                .imageUploaded(medicationsImage)
                .build();

        InputStream inputStream5 = getClass().getResourceAsStream("/images/PetCarriersImage.jpg");
        byte[] petCarriersImage = ImageUtil.readImage(inputStream5);

        Inventory inventory5 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0005")
                .inventoryName("Pet Carriers")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Carriers for transporting pets")
                .inventoryImage("https://i5.walmartimages.ca/images/Enlarge/034/128/6000203034128.jpg?odnHeight=2000&odnWidth=2000&odnBg=FFFFFF")
                .inventoryBackupImage("https://assets.wfcdn.com/im/60682166/compr-r85/1267/126767614/Gainey+Large+Pet+Carrier.jpg")
                .imageUploaded(petCarriersImage)
                .build();

        InputStream inputStream6 = getClass().getResourceAsStream("/images/DiagnosticKitImage.jpg");
        byte[] diagnosticKitImage = ImageUtil.readImage(inputStream6);

        Inventory inventory6 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0006")
                .inventoryName("Diagnostic Kits")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Kits for diagnosing various pet illnesses and conditions")
                .inventoryImage("https://cdn.labmanager.com/assets/articleNo/22063/aImg/40819/rt-pcr-covid-19-diagnostic-kit-concept-l.jpg")
                .inventoryBackupImage("https://image.made-in-china.com/226f3j00UnelmKsMKwuy/Vet-Animal-Pet-Veterinary-Antigen-Rapid-Test-Kit-for-Dog-Cat.webp")
                .imageUploaded(diagnosticKitImage)
                .build();

        InputStream inputStream7 = getClass().getResourceAsStream("/images/SurgicalInstrumentsImage.jpg");
        byte[] surgicalInstrumentsImage = ImageUtil.readImage(inputStream7);

        Inventory inventory7 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0007")
                .inventoryName("Surgical Instruments")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Tools for performing surgical procedures on pets")
                .inventoryImage("https://www.amerisurgicalinstruments.com/cdn/shop/articles/3_63803250-d4a1-4a10-8c23-39c34b9fd8cf.jpg?v=1680040619")
                .inventoryBackupImage("https://censis.com/hubfs/Imported_Blog_Media/Surgical-Instruments-with-Marks-1.jpg")
                .imageUploaded(surgicalInstrumentsImage)
                .build();

        InputStream inputStream8 = getClass().getResourceAsStream("/images/AnesthesiaSuppliesImage.jpg");
        byte[] anesthesiaSuppliesImage = ImageUtil.readImage(inputStream8);

        Inventory inventory8 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0008")
                .inventoryName("Anesthesia Supplies")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Supplies for administering anesthesia during surgeries")
                .inventoryImage("https://s3.amazonaws.com/cdn-origin-etr.akc.org/wp-content/uploads/2017/03/22110255/Labrador-Retriever-laying-on-an-operating-table.jpg")
                .inventoryBackupImage("https://www.cardinalhealth.com/content/dam/corp/products/professional-products/category-grid-asset/category-featured/CategoryFeatured---AC604P.jpg")
                .imageUploaded(anesthesiaSuppliesImage)
                .build();

        InputStream inputStream9 = getClass().getResourceAsStream("/images/WoundCareSuppliesImage.jpg");
        byte[] woundCareSuppliesImage = ImageUtil.readImage(inputStream9);

        Inventory inventory9 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0009")
                .inventoryName("Wound Care Supplies")
                .inventoryType(inventoryType1.getType())
                .inventoryDescription("Supplies for treating and dressing wounds")
                .inventoryImage("https://silverliningshc.ca/wp-content/uploads/2022/09/WoundCare.jpg")
                .inventoryBackupImage("https://www.allegromedical.com/media/wysiwyg/ALG_Wound-Care-Supplies.jpg")
                .imageUploaded(woundCareSuppliesImage)
                .build();

        InputStream inputStream10 = getClass().getResourceAsStream("/images/InfectiousDiseaseImage.jpg");
        byte[] infectiousDiseaseImage = ImageUtil.readImage(inputStream10);

        Inventory inventory10 = Inventory.builder()
                .inventoryId(UUID.randomUUID().toString())
                .inventoryCode("INV-0010")
                .inventoryName("Infectious Disease Test Kits")
                .inventoryType(inventoryType5.getType())
                .inventoryDescription("Kits for testing infectious diseases in pets")
                .inventoryImage("https://img.medicalexpo.com/images_me/photo-mg/306028-17870793.webp")
                .inventoryBackupImage("https://image.made-in-china.com/202f0j00eqLkbtRgJfcC/Ich-AG-The-Infectious-Canine-Hepatitis-Antigen-Rapid-Test-Kit.jpg")
                .imageUploaded(infectiousDiseaseImage)
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply2 = Product.builder()
                .productName("Trazodone")
                .productId(UUID.randomUUID().toString())
                .productPrice(150.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(10)
                .productDescription("Drugs for anxiety/stress")
                .productSalePrice(10.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply3 = Product.builder()
                .productName("Carprofen")
                .productId(UUID.randomUUID().toString())
                .productPrice(130.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(12)
                .productDescription("Non-steroidal anti-inflammatory drug")
                .productSalePrice(140.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply4 = Product.builder()
                .productName("Self-adhesive Bandages")
                .productId(UUID.randomUUID().toString())
                .productPrice(15.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(25)
                .productDescription("Bandages for wound care")
                .productSalePrice(75.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply5 = Product.builder()
                .productName("Gauze Pads")
                .productId(UUID.randomUUID().toString())
                .productPrice(10.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(30)
                .productDescription("Absorbent pads for wound care")
                .productSalePrice(20.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply6 = Product.builder()
                .productName("Hydrogen Peroxide")
                .productId(UUID.randomUUID().toString())
                .productPrice(5.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(40)
                .productDescription("Antiseptic for wound cleaning")
                .productSalePrice(10.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply7 = Product.builder()
                .productName("MRI Machine")
                .productId(UUID.randomUUID().toString())
                .productPrice(100000.00)
                .inventoryId(inventory1.getInventoryId()) // Equipment
                .productQuantity(1)
                .productDescription("Imaging machine for diagnostics")
                .productSalePrice(110000.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply8 = Product.builder()
                .productName("Rabies Vaccine")
                .productId(UUID.randomUUID().toString())
                .productPrice(50.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .productQuantity(20)
                .productDescription("Core vaccine for rabies prevention")
                .productSalePrice(55.00)
                .lastUpdatedAt(LocalDateTime.now())
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply10 = Product.builder()
                .productName("Ultrasound Machine")
                .productId(UUID.randomUUID().toString())
                .productPrice(30000.00)
                .inventoryId(inventory1.getInventoryId()) // Medical equipment
                .productQuantity(1)
                .productDescription("Ultrasound machine for diagnostic imaging")
                .productSalePrice(32000.00)
                .lastUpdatedAt(LocalDateTime.now())
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply12 = Product.builder()
                .productName("Antiseptic Wipes")
                .productId(UUID.randomUUID().toString())
                .productPrice(12.00)
                .inventoryId(inventory2.getInventoryId()) // Bandages
                .productQuantity(60)
                .productDescription("Antiseptic wipes for cleaning wounds")
                .productSalePrice(25.00)
                .lastUpdatedAt(LocalDateTime.now())
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply14 = Product.builder()
                .productName("IV Catheters")
                .productId(UUID.randomUUID().toString())
                .productPrice(30.00)
                .inventoryId(inventory3.getInventoryId()) // Injections
                .productQuantity(50)
                .productDescription("Intravenous catheters for fluid administration")
                .productSalePrice(35.00)
                .lastUpdatedAt(LocalDateTime.now())
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply16 = Product.builder()
                .productName("Antibiotic Ointment")
                .productId(UUID.randomUUID().toString())
                .productPrice(25.00)
                .inventoryId(inventory4.getInventoryId()) // Medications
                .productQuantity(25)
                .productDescription("Topical antibiotic ointment for wound care")
                .productSalePrice(30.00)
                .lastUpdatedAt(LocalDateTime.now())
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply18 = Product.builder()
                .productName("Urinalysis Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(60.00)
                .inventoryId(inventory6.getInventoryId()) // Diagnostic Kits
                .productQuantity(30)
                .productDescription("Test kits for analyzing urine samples")
                .productSalePrice(65.00)
                .lastUpdatedAt(LocalDateTime.now())
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply20 = Product.builder()
                .productName("Sutures Kit")
                .productId(UUID.randomUUID().toString())
                .productPrice(120.00)
                .inventoryId(inventory9.getInventoryId()) // Wound Care Supplies
                .productQuantity(15)
                .productDescription("Kit containing sutures for wound closure")
                .productSalePrice(130.00)
                .lastUpdatedAt(LocalDateTime.now())
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
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply22 = Product.builder()
                .productName("Anesthetic Masks")
                .productId(UUID.randomUUID().toString())
                .productPrice(200.00)
                .inventoryId(inventory8.getInventoryId()) // Anesthesia Supplies
                .productQuantity(20)
                .productDescription("Masks for delivering anesthesia")
                .productSalePrice(220.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();


        Product supply23 = Product.builder()
                .productName("Surgical Scissors")
                .productId(UUID.randomUUID().toString())
                .productPrice(150.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .productQuantity(10)
                .productDescription("Scissors for cutting tissues during surgery")
                .productSalePrice(160.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply24 = Product.builder()
                .productName("Hemostats")
                .productId(UUID.randomUUID().toString())
                .productPrice(120.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .productQuantity(15)
                .productDescription("Clamps for controlling bleeding during surgery")
                .productSalePrice(130.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply25 = Product.builder()
                .productName("Needle Holders")
                .productId(UUID.randomUUID().toString())
                .productPrice(180.00)
                .inventoryId(inventory7.getInventoryId()) // Surgical Instruments
                .productQuantity(10)
                .productDescription("Hold needles securely during suturing")
                .productSalePrice(190.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply26 = Product.builder()
                .productName("Pet Carrier Accessories")
                .productId(UUID.randomUUID().toString())
                .productPrice(25.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .productQuantity(50)
                .productDescription("Accessories like water bowls and food trays for pet carriers")
                .productSalePrice(30.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply27 = Product.builder()
                .productName("Carrier Cleaning Supplies")
                .productId(UUID.randomUUID().toString())
                .productPrice(10.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .productQuantity(60)
                .productDescription("Cleaning sprays and wipes for pet carriers")
                .productSalePrice(15.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply28 = Product.builder()
                .productName("Comfort Pads")
                .productId(UUID.randomUUID().toString())
                .productPrice(20.00)
                .inventoryId(inventory5.getInventoryId()) // Pet Carriers
                .productQuantity(40)
                .productDescription("Comfortable pads for lining pet carriers")
                .productSalePrice(25.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply29 = Product.builder()
                .productName("Parvovirus Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(50.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .productQuantity(30)
                .productDescription("Test kits for detecting canine parvovirus")
                .productSalePrice(55.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply30 = Product.builder()
                .productName("Feline Leukemia Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(60.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .productQuantity(25)
                .productDescription("Test kits for detecting feline leukemia virus")
                .productSalePrice(65.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();

        Product supply31 = Product.builder()
                .productName("Heartworm Test Kits")
                .productId(UUID.randomUUID().toString())
                .productPrice(55.00)
                .inventoryId(inventory10.getInventoryId()) // Infectious Disease Test Kits
                .productQuantity(20)
                .productDescription("Test kits for detecting heartworm infection")
                .productSalePrice(60.00)
                .lastUpdatedAt(LocalDateTime.now())
                .build();


        //Inventory1 ---------------------------------------------------
        inventory1.addProduct(supply7);
        inventory1.addProduct(supply9);
        inventory1.addProduct(supply10);
        //--------------------------------------------------------------

        //Inventory2 ---------------------------------------------------
        inventory2.addProduct(supply4);
        inventory2.addProduct(supply5);
        inventory2.addProduct(supply6);
        inventory2.addProduct(supply11);
        inventory2.addProduct(supply12);
        //--------------------------------------------------------------

        //Inventory3 ---------------------------------------------------
        inventory3.addProduct(supply8);
        inventory3.addProduct(supply13);
        inventory3.addProduct(supply14);
        //--------------------------------------------------------------

        //Inventory4 ---------------------------------------------------
        inventory4.addProduct(supply1);
        inventory4.addProduct(supply2);
        inventory4.addProduct(supply3);
        inventory4.addProduct(supply15);
        inventory4.addProduct(supply16);
        //--------------------------------------------------------------

        //Inventory5 ---------------------------------------------------
        inventory5.addProduct(supply26);
        inventory5.addProduct(supply27);
        inventory5.addProduct(supply28);
        //--------------------------------------------------------------

        //Inventory6 ---------------------------------------------------
        inventory6.addProduct(supply17);
        inventory6.addProduct(supply18);
        //--------------------------------------------------------------

        //Inventory7 ---------------------------------------------------
        inventory7.addProduct(supply23);
        inventory7.addProduct(supply24);
        inventory7.addProduct(supply25);
        //--------------------------------------------------------------

        //Inventory8 ---------------------------------------------------
        inventory8.addProduct(supply21);
        inventory8.addProduct(supply22);
        //--------------------------------------------------------------

        //Inventory9 ---------------------------------------------------
        inventory9.addProduct(supply19);
        inventory9.addProduct(supply20);
        //--------------------------------------------------------------

        //Inventory10 ---------------------------------------------------
        inventory10.addProduct(supply29);
        inventory10.addProduct(supply30);
        inventory10.addProduct(supply31);
        //--------------------------------------------------------------


        Flux.concat(
                        Flux.just(inventoryType1, inventoryType2, inventoryType3, inventoryType4, inventoryType5)
                                .flatMap(inventoryTypeRepository::insert),
                        Flux.just(inventoryName1, inventoryName2, inventoryName3, inventoryName4, inventoryName5,
                                        inventoryName6, inventoryName7, inventoryName8, inventoryName9, inventoryName10)
                                .flatMap(inventoryNameRepository::insert),
                        Flux.just(inventory1, inventory2, inventory3, inventory4, inventory5,
                                        inventory6, inventory7, inventory8, inventory9, inventory10)
                                .flatMap(inventoryRepository::insert),
                        Flux.just(supply1, supply2, supply3, supply4, supply5, supply6, supply7, supply8, supply9,
                                        supply10, supply11, supply12, supply13, supply14, supply15,
                                        supply16, supply17, supply18, supply19, supply20, supply21, supply22,
                                        supply23, supply24, supply25, supply26, supply27, supply28, supply29, supply30, supply31)
                                .flatMap(productRepository::insert)
                )
                .then()
                .block();

    }
}
