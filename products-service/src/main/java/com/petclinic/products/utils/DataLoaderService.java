package com.petclinic.products.utils;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.images.ImageRepository;
import com.petclinic.products.datalayer.products.*;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class DataLoaderService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoaderService.class);

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductBundleRepository productBundleRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    ProductTypeRepository productTypeRepository;

    @Override
    public void run(String... args) throws Exception {
        // If the database is not empty, do not load data
        try {
            restoreMissingReferencedLegacyImages();
            if (
                    Boolean.TRUE.equals(productRepository.findAll().hasElements().block()) ||
                            Boolean.TRUE.equals(productBundleRepository.findAll().hasElements().block()) ||
                            Boolean.TRUE.equals(imageRepository.findAll().hasElements().block()) ||
                            Boolean.TRUE.equals(ratingRepository.findAll().hasElements().block()) ||
                            Boolean.TRUE.equals(productTypeRepository.findAll().hasElements().block())) {
                System.out.println("Database not empty, skipping data loading");
                return;
            }
        } catch (Exception e) {
            System.out.println("Error checking if products exist: " + e.getMessage());
            return;
        }

        Product product1 = Product.builder()
                .productId("06a7d573-bcab-4db3-956f-773324b92a80")
                .imageId("08a5af6b-3501-4157-9a99-1aa82387b9e4")
                .productName("Dog Food")
                .productDescription("Premium dry food for adult dogs")
                .productSalePrice(45.99)
                .requestCount(0)
                //.productType(ProductType.FOOD)
                .productTypeId("586d0700-57db-4312-b6f1-413b79dd018c")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(44)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("2002-09-26"))
                .deliveryType(DeliveryType.DELIVERY)
                .build();

        Product product2 = Product.builder()
                .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .imageId("36b06c01-10f3-4645-9c45-900afc5a8b8a")
                .productName("Cat Litter")
                .productDescription("Clumping cat litter with odor control")
                .productSalePrice(12.99)
                .requestCount(0)
                //.productType(ProductType.ACCESSORY)
                .productTypeId("6a247af0-52d9-4179-a5b4-ad4b92e686b1")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(3)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("2020-06-30"))
                .deliveryType(DeliveryType.PICKUP)
                .build();

        Product product3 = Product.builder()
                .productId("baee7cd2-b67a-449f-b262-91f45dde8a6d")
                .imageId("be4e60a4-2369-46e8-abee-20c1a8dce3e5")
                .productName("Flea Collar")
                .productDescription("Flea and tick prevention for small dogs")
                .productSalePrice(9.99)
                .requestCount(0)
                //.productType(ProductType.MEDICATION)
                .productTypeId("86627454-970e-41a9-baa6-71ab759bf66c")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(53)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("2019-09-29"))
                .deliveryType(DeliveryType.DELIVERY)
                .build();

        Product product4 = Product.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .imageId("7074e0ef-d041-452f-8a0f-cb9ab20d1fed")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .requestCount(0)
                //.productType(ProductType.ACCESSORY)
                .productTypeId("6a247af0-52d9-4179-a5b4-ad4b92e686b1")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(8)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("2023-05-06"))
                .deliveryType(DeliveryType.PICKUP)
                .build();

        Product product5 = Product.builder()
                .productId("4d508fb7-f1f2-4952-829d-10dd7254cf26")
                .imageId("392c42d9-9505-4c27-b82e-20351b25d33f")
                .productName("Aquarium Filter")
                .productDescription("Filter system for small to medium-sized aquariums")
                .productSalePrice(19.99)
                .requestCount(0)
                //.productType(ProductType.ACCESSORY)
                .productTypeId("6a247af0-52d9-4179-a5b4-ad4b92e686b1")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(14)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("2025-09-29"))
                .deliveryType(DeliveryType.DELIVERY_AND_PICKUP)
                .build();

        Product product6 = Product.builder()
                .productId("a6a27433-e7a9-4e78-8ae3-0cb57d756863")
                .imageId("664aa14b-db66-4b25-9d05-f3a9164eb401")
                .productName("Horse Saddle")
                .productDescription("Lightweight saddle for riding horses")
                .productSalePrice(199.99)
                .requestCount(0)
                //.productType(ProductType.EQUIPMENT)
                .productTypeId("79c8723a-8df3-495d-8eb0-07d574ff5ae5")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(58)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("1988-09-29"))
                .deliveryType(DeliveryType.DELIVERY)
                .build();

        Product product7 = Product.builder()
                .productId("4affcab7-3ab1-4917-a114-2b6301aa5565")
                .imageId("3377a03f-8105-47d7-8d8a-d89fd170c7e6")
                .productName("Rabbit Hutch")
                .productDescription("Outdoor wooden hutch for rabbits")
                .productSalePrice(79.99)
                .requestCount(0)
                //.productType(ProductType.ACCESSORY)
                .productTypeId("6a247af0-52d9-4179-a5b4-ad4b92e686b1")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(66)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("2024-02-22"))
                .deliveryType(DeliveryType.DELIVERY_AND_PICKUP)
                .build();

        Product product8 = Product.builder()
                .productId("1501f30e-1db1-44b2-a555-bca6f64450e4")
                .imageId("c76ed4c1-fc5d-4868-8b39-1bca6b0be368")
                .productName("Fish Tank Heater")
                .productDescription("Submersible heater for tropical fish tanks")
                .productSalePrice(14.99)
                .requestCount(0)
                //.productType(ProductType.ACCESSORY)
                .productTypeId("6a247af0-52d9-4179-a5b4-ad4b92e686b1")
                .productStatus(ProductStatus.AVAILABLE)
                .productQuantity(0)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse("2022-09-19"))
                .deliveryType(DeliveryType.PICKUP)
                .build();

        ProductTypeDb productType1 = ProductTypeDb.builder()
                .productTypeId("86627454-970e-41a9-baa6-71ab759bf66c")
                .typeName("MEDICATION")
                .build();

        ProductTypeDb productType2 = ProductTypeDb.builder()
                .productTypeId("6a247af0-52d9-4179-a5b4-ad4b92e686b1")
                .typeName("ACCESSORY")
                .build();

        ProductTypeDb productType3 = ProductTypeDb.builder()
                .productTypeId("586d0700-57db-4312-b6f1-413b79dd018c")
                .typeName("FOOD")
                .build();

        ProductTypeDb productType4 = ProductTypeDb.builder()
                .productTypeId("79c8723a-8df3-495d-8eb0-07d574ff5ae5")
                .typeName("EQUIPMENT")
                .build();

        ProductBundle bundle1 = ProductBundle.builder()
                .bundleId(UUID.randomUUID().toString())
                .bundleName("Dog Bundle")
                .bundleDescription("Dog Food & Flea Collar")
                .productIds(List.of("06a7d573-bcab-4db3-956f-773324b92a80", "baee7cd2-b67a-449f-b262-91f45dde8a6d"))
                .originalTotalPrice(product1.getProductSalePrice() + product3.getProductSalePrice())
                .bundlePrice(49.99)
                .build();

        ProductBundle bundle2 = ProductBundle.builder()
                .bundleId(UUID.randomUUID().toString())
                .bundleName("Fish Bundle")
                .bundleDescription("Cat Litter & Fish Tank Heater")
                .productIds(List.of("4d508fb7-f1f2-4952-829d-10dd7254cf26", "1501f30e-1db1-44b2-a555-bca6f64450e4"))
                .originalTotalPrice(product5.getProductSalePrice() + product8.getProductSalePrice())
                .bundlePrice(24.99)
                .build();

        ProductBundle bundle3 = ProductBundle.builder()
                .bundleId(UUID.randomUUID().toString())
                .bundleName("Accessory Bundle")
                .bundleDescription("All Accessories")
                .productIds(List.of("1501f30e-1db1-44b2-a555-bca6f64450e4", "4affcab7-3ab1-4917-a114-2b6301aa5565", "4d508fb7-f1f2-4952-829d-10dd7254cf26", "ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "98f7b33a-d62a-420a-a84a-05a27c85fc91"))
                .originalTotalPrice(product8.getProductSalePrice() + product7.getProductSalePrice() + product5.getProductSalePrice() + product4.getProductSalePrice() + product2.getProductSalePrice())
                .bundlePrice(129.99)
                .build();


        Rating rating1prod1 = Rating.builder()
                .productId(product1.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 5)
                .review("My dog loves this food!")
                .build();

        Rating rating2prod1 = Rating.builder()
                .productId(product1.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 1)
                .review("My dog died because of it, horrible!")
                .build();

        Rating rating1prod2 = Rating.builder()
                .productId(product2.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 4)
                .review("Great litter, clumps well")
                .build();

        Rating rating2prod2 = Rating.builder()
                .productId(product2.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 2)
                .review("Doesn't control odor well")
                .build();

        Rating rating1prod3 = Rating.builder()
                .productId(product3.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 3)
                .review("Works well, but doesn't last long")
                .build();

        Rating rating2prod3 = Rating.builder()
                .productId(product3.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 5)
                .review("Keeps fleas away for months!")
                .build();

        Rating rating1prod4 = Rating.builder()
                .productId(product4.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 5)
                .review("Great cage, easy to clean")
                .build();

        Rating rating2prod4 = Rating.builder()
                .productId(product4.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 4)
                .review("Good for small birds")
                .build();

        Rating rating1prod5 = Rating.builder()
                .productId(product5.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 4)
                .review("Works well, but a bit noisy")
                .build();

        Rating rating2prod5 = Rating.builder()
                .productId(product5.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 3)
                .review("Not enough power for my tank")
                .build();

        Rating rating1prod6 = Rating.builder()
                .productId(product6.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 5)
                .review("Great saddle, very comfortable")
                .build();

        Rating rating2prod6 = Rating.builder()
                .productId(product6.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 4)
                .review("Good for beginners")
                .build();

        Rating rating1prod7 = Rating.builder()
                .productId(product7.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 4)
                .review("Spacious hutch, easy to clean")
                .build();

        Rating rating2prod7 = Rating.builder()
                .productId(product7.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 3)
                .review("Not enough ventilation")
                .build();

        Rating rating1prod8 = Rating.builder()
                .productId(product8.getProductId())
                .customerId("810440e8-cae0-48f7-aa4f-830239b82b78")
                .rating((byte) 3)
                .review("Works well, but hard to adjust")
                .build();

        Rating rating2prod8 = Rating.builder()
                .productId(product8.getProductId())
                .customerId("584c5329-2b9a-43d4-ad2b-debaa92d6c02")
                .rating((byte) 2)
                .review("Doesn't heat evenly")
                .build();

        Resource resource1 = new ClassPathResource("images/dog_food.png");
        Resource resource2 = new ClassPathResource("images/cat_litter.png");
        Resource resource3 = new ClassPathResource("images/flea_collar.png");
        Resource resource4 = new ClassPathResource("images/bird_cage.png");
        Resource resource5 = new ClassPathResource("images/aquarium_filter.png");
        Resource resource6 = new ClassPathResource("images/horse_saddle.png");
        Resource resource7 = new ClassPathResource("images/rabbit_hutch.png");
        Resource resource8 = new ClassPathResource("images/fish_tank_heater.png");

        InputStream inputStream1 = resource1.getInputStream();
        InputStream inputStream2 = resource2.getInputStream();
        InputStream inputStream3 = resource3.getInputStream();
        InputStream inputStream4 = resource4.getInputStream();
        InputStream inputStream5 = resource5.getInputStream();
        InputStream inputStream6 = resource6.getInputStream();
        InputStream inputStream7 = resource7.getInputStream();
        InputStream inputStream8 = resource8.getInputStream();

        byte[] imageBytes1 = inputStream1.readAllBytes();
        byte[] imageBytes2 = inputStream2.readAllBytes();
        byte[] imageBytes3 = inputStream3.readAllBytes();
        byte[] imageBytes4 = inputStream4.readAllBytes();
        byte[] imageBytes5 = inputStream5.readAllBytes();
        byte[] imageBytes6 = inputStream6.readAllBytes();
        byte[] imageBytes7 = inputStream7.readAllBytes();
        byte[] imageBytes8 = inputStream8.readAllBytes();
        inputStream1.close();

        Image image1 = Image.builder()
                .imageId("08a5af6b-3501-4157-9a99-1aa82387b9e4")
                .imageName("dog_food.jpg")
                .imageType("image/jpeg")
                .imageData(imageBytes1)
                .build();

        Image image2 = Image.builder()
                .imageId("36b06c01-10f3-4645-9c45-900afc5a8b8a")
                .imageName("cat_litter.png")
                .imageType("image/png")
                .imageData(imageBytes2)
                .build();

        Image image3 = Image.builder()
                .imageId("be4e60a4-2369-46e8-abee-20c1a8dce3e5")
                .imageName("flea_collar.jpg")
                .imageType("image/jpeg")
                .imageData(imageBytes3)
                .build();

        Image image4 = Image.builder()
                .imageId("7074e0ef-d041-452f-8a0f-cb9ab20d1fed")
                .imageName("bird_cage.jpg")
                .imageType("image/jpeg")
                .imageData(imageBytes4)
                .build();

        Image image5 = Image.builder()
                .imageId("392c42d9-9505-4c27-b82e-20351b25d33f")
                .imageName("aquarium_filter.png")
                .imageType("image/png")
                .imageData(imageBytes5)
                .build();

        Image image6 = Image.builder()
                .imageId("664aa14b-db66-4b25-9d05-f3a9164eb401")
                .imageName("horse_saddle.jpg")
                .imageType("image/jpeg")
                .imageData(imageBytes6)
                .build();

        Image image7 = Image.builder()
                .imageId("3377a03f-8105-47d7-8d8a-d89fd170c7e6")
                .imageName("rabbit_hutch.jpg")
                .imageType("image/jpeg")
                .imageData(imageBytes7)
                .build();

        Image image8 = Image.builder()
                .imageId("c76ed4c1-fc5d-4868-8b39-1bca6b0be368")
                .imageName("fish_tank_heater.jpg")
                .imageType("image/jpeg")
                .imageData(imageBytes8)
                .build();

        List<SeededProduct> additionalProductSeeds = List.of(
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "Senior Dog Joint Supplement", "Glucosamine chews supporting mobility in older dogs", 24.99, 18, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-11-15", "senior_dog_joint_supplement.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7101"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "Puppy Deworming Tablets", "Broad-spectrum deworming treatment for puppies", 16.49, 2, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.NO_DELIVERY_OPTION, ProductStatus.PRE_ORDER, "2026-02-10", "puppy_deworming_tablets.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7102"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "Ear Cleaning Solution", "Gentle cleanser for routine canine and feline ear care", 13.99, 27, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-08-21", "ear_cleaning_solution.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7103"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "Veterinary Dental Gel", "Enzymatic gel for reducing plaque and tartar", 18.99, 11, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-04-12", "veterinary_dental_gel.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7104"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "Calming Chews", "Herbal calming supplement for stressful visits and travel", 21.99, 1, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2026-01-18", "calming_chews.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7105"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "Cat Hairball Remedy", "Lubricating paste to help prevent hairballs", 9.49, 22, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2023-10-03", "cat_hairball_remedy.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7106"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "Wound Care Spray", "Antiseptic spray for minor cuts and abrasions", 15.99, 0, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY, ProductStatus.OUT_OF_STOCK, "2024-01-09", null, null),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "Flea Treatment Drops", "Monthly topical flea and tick treatment for cats", 29.99, 9, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.NO_DELIVERY_OPTION, ProductStatus.AVAILABLE, "2025-06-27", null, null),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "Stainless Steel Food Bowl", "Non-slip bowl suitable for cats and small dogs", 11.99, 31, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-03-14", "stainless_steel_food_bowl.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7107"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "Reflective Dog Leash", "Reflective nylon leash for safer evening walks", 17.99, 14, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-02-26", "reflective_dog_leash.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7108"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "Adjustable Recovery Collar", "Soft adjustable collar for post-treatment recovery", 19.99, 6, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2023-07-19", "adjustable_recovery_collar.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7109"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "Orthopedic Pet Bed", "Supportive washable bed for aging pets", 64.99, 4, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.PRE_ORDER, "2026-03-01", "orthopedic_pet_bed.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7110"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013", "Cat Scratching Post", "Compact sisal scratching post with toy attachment", 34.99, 16, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2024-11-08", "cat_scratching_post.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7111"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "Grooming Brush Set", "Brush and comb set for routine coat maintenance", 22.49, 25, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2025-09-05", "grooming_brush_set.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7112"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "Portable Water Bottle", "Leak-resistant travel bottle for dogs", 14.99, 3, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2026-04-06", "portable_water_bottle.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7113"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "Heated Pet Blanket", "Low-voltage fleece blanket for cold-weather comfort", 49.99, 0, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY, ProductStatus.OUT_OF_STOCK, "2022-12-11", null, null),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017", "Sensitive Stomach Dog Food", "Easily digestible dry food for adult dogs", 52.99, 20, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-12-02", "sensitive_stomach_dog_food.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7114"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "Indoor Cat Formula", "Balanced dry food formulated for indoor cats", 38.99, 13, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2024-05-23", "indoor_cat_formula.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7115"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "Kitten Starter Food", "Nutrient-rich food for growing kittens", 31.99, 7, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2026-02-22", "kitten_starter_food.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7116"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "Small Animal Timothy Hay", "Fresh timothy hay for rabbits and guinea pigs", 18.49, 29, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2023-04-17", "small_animal_timothy_hay.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7117"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021", "Tropical Fish Flakes", "Daily flake food for tropical aquarium fish", 8.99, 2, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2025-07-31", "tropical_fish_flakes.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7118"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "Parakeet Seed Blend", "Mixed seed blend for small companion birds", 12.99, 24, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-09-12", "parakeet_seed_blend.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7119"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "Senior Cat Wet Food Pack", "Moisture-rich meal pack for senior cats", 26.99, 10, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-03-30", "senior_cat_wet_food_pack.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7120"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "Equine Electrolyte Supplement", "Electrolyte powder for active horses", 44.99, 5, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2022-06-28", null, null),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025", "Digital Pet Scale", "Precise scale for monitoring pet weight at home", 39.99, 8, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-02-05", "digital_pet_scale.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7121"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "Automatic Feeder", "Programmable feeder for scheduled meals", 79.99, 12, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY, ProductStatus.PRE_ORDER, "2026-05-15", "automatic_feeder.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7122"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "Compact Pet Carrier", "Ventilated carrier for cats and small dogs", 54.99, 15, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2025-10-19", "compact_pet_carrier.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7123"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "Aquarium Water Test Kit", "Multi-parameter test kit for aquarium water quality", 27.99, 19, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2023-11-26", "aquarium_water_test_kit.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7124"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "LED Examination Light", "Rechargeable light for home health checks", 32.99, 3, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-08-08", "led_examination_light.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7125"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "Pet Nail Grinder", "Quiet rechargeable grinder for routine nail care", 42.99, 17, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2024-06-16", "pet_nail_grinder.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7126"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "Foldable Exercise Pen", "Portable enclosure for puppies and small animals", 89.99, 6, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2026-01-30", "foldable_exercise_pen.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7127"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "Canine First Aid Kit", "Practical first aid kit for common pet emergencies", 36.99, 1, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2023-02-13", null, null)
        );

        List<Product> seededProducts = new ArrayList<>(List.of(
                product1, product2, product3, product4,
                product5, product6, product7, product8));
        seededProducts.addAll(additionalProductSeeds.stream()
                .map(SeededProduct::product)
                .toList());

        List<Image> seededImages = new ArrayList<>(List.of(
                image1, image2, image3, image4,
                image5, image6, image7, image8));
        seededImages.addAll(additionalProductSeeds.stream()
                .map(SeededProduct::image)
                .filter(Objects::nonNull)
                .toList());

        Flux.just(bundle1, bundle2, bundle3)
                .flatMap(s -> productBundleRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.fromIterable(seededProducts)
                .flatMap(s -> productRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.just(
                        rating1prod1, rating2prod1,
                        rating1prod2, rating2prod2,
                        rating1prod3, rating2prod3,
                        rating1prod4, rating2prod4,
                        rating1prod5, rating2prod5,
                        rating1prod6, rating2prod6,
                        rating1prod7, rating2prod7,
                        rating1prod8, rating2prod8
                )
                .flatMap(s -> ratingRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.fromIterable(seededImages)
                .flatMap(s -> imageRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.just(productType1, productType2, productType3, productType4)
                .flatMap(s -> productTypeRepository.save(s)
                        .log(s.toString()))
                .subscribe();
    }

    private void restoreMissingReferencedLegacyImages() {
        Set<String> referencedImageIds = productRepository.findAll()
                .map(Product::getImageId)
                .filter(imageId -> imageId != null && !imageId.isBlank())
                .collectList()
                .map(Set::copyOf)
                .block();

        if (referencedImageIds == null || referencedImageIds.isEmpty()) {
            return;
        }

        Flux.fromIterable(legacyImageSeeds())
                .filter(seed -> referencedImageIds.contains(seed.imageId()))
                .concatMap(seed -> imageRepository.findImageByImageId(seed.imageId())
                        .switchIfEmpty(Mono.defer(() -> saveLegacyImage(seed))))
                .then()
                .block();
    }

    private Mono<Image> saveLegacyImage(LegacyImageSeed seed) {
        try {
            byte[] imageData = new ClassPathResource(seed.resourcePath())
                    .getContentAsByteArray();
            Image image = Image.builder()
                    .imageId(seed.imageId())
                    .imageName(seed.imageName())
                    .imageType(seed.imageType())
                    .imageData(imageData)
                    .build();
            return imageRepository.save(image);
        } catch (Exception error) {
            return Mono.error(error);
        }
    }

    private List<LegacyImageSeed> legacyImageSeeds() {
        return List.of(
                new LegacyImageSeed(
                        "08a5af6b-3501-4157-9a99-1aa82387b9e4",
                        "dog_food.jpg", "image/jpeg", "images/dog_food.png"),
                new LegacyImageSeed(
                        "36b06c01-10f3-4645-9c45-900afc5a8b8a",
                        "cat_litter.png", "image/png", "images/cat_litter.png"),
                new LegacyImageSeed(
                        "be4e60a4-2369-46e8-abee-20c1a8dce3e5",
                        "flea_collar.jpg", "image/jpeg", "images/flea_collar.png"),
                new LegacyImageSeed(
                        "7074e0ef-d041-452f-8a0f-cb9ab20d1fed",
                        "bird_cage.jpg", "image/jpeg", "images/bird_cage.png"),
                new LegacyImageSeed(
                        "392c42d9-9505-4c27-b82e-20351b25d33f",
                        "aquarium_filter.png", "image/png",
                        "images/aquarium_filter.png"),
                new LegacyImageSeed(
                        "664aa14b-db66-4b25-9d05-f3a9164eb401",
                        "horse_saddle.jpg", "image/jpeg", "images/horse_saddle.png"),
                new LegacyImageSeed(
                        "3377a03f-8105-47d7-8d8a-d89fd170c7e6",
                        "rabbit_hutch.jpg", "image/jpeg", "images/rabbit_hutch.png"),
                new LegacyImageSeed(
                        "c76ed4c1-fc5d-4868-8b39-1bca6b0be368",
                        "fish_tank_heater.jpg", "image/jpeg",
                        "images/fish_tank_heater.png"));
    }

    private SeededProduct seedProduct(
            String productId,
            String productName,
            String productDescription,
            double productSalePrice,
            int productQuantity,
            String productTypeId,
            DeliveryType deliveryType,
            ProductStatus productStatus,
            String releaseDate,
            String imageFile,
            String imageId) {
        Image image = null;
        String effectiveImageId = null;

        if (imageFile != null && imageId != null) {
            Resource resource = new ClassPathResource("images/" + imageFile);
            if (!resource.exists()) {
                log.warn("Seed image not found: {}; continuing without an image for product {}", imageFile, productId);
            } else {
                try {
                    image = Image.builder()
                            .imageId(imageId)
                            .imageName(imageFile)
                            .imageType(imageFile.toLowerCase().endsWith(".jpg")
                                    || imageFile.toLowerCase().endsWith(".jpeg")
                                    ? "image/jpeg" : "image/png")
                            .imageData(resource.getContentAsByteArray())
                            .build();
                    effectiveImageId = imageId;
                } catch (Exception error) {
                    log.warn("Unable to load seed image {}; continuing without an image for product {}", imageFile, productId, error);
                }
            }
        }

        Product product = Product.builder()
                .productId(productId)
                .imageId(effectiveImageId)
                .productName(productName)
                .productDescription(productDescription)
                .productSalePrice(productSalePrice)
                .requestCount(0)
                .productTypeId(productTypeId)
                .productStatus(productStatus)
                .productQuantity(productQuantity)
                .isUnlisted(false)
                .releaseDate(LocalDate.parse(releaseDate))
                .deliveryType(deliveryType)
                .build();

        return new SeededProduct(product, image);
    }

    private record SeededProduct(Product product, Image image) {
    }

    private record LegacyImageSeed(
            String imageId,
            String imageName,
            String imageType,
            String resourcePath) {
    }
}
