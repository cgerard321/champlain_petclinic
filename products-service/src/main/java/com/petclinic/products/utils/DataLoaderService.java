package com.petclinic.products.utils;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.datalayer.images.ImageRepository;
import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Profile("!test")
public class DataLoaderService implements CommandLineRunner {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ImageRepository imageRepository;

    @Override
    public void run(String... args) throws Exception {
        Product product1 = Product.builder()
                .productId("06a7d573-bcab-4db3-956f-773324b92a80")
                .imageId("08a5af6b-3501-4157-9a99-1aa82387b9e4")
                .productName("Dog Food")
                .productDescription("Premium dry food for adult dogs")
                .productSalePrice(45.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Food")
                .productQuantity(44)
                .build();

        Product product2 = Product.builder()
                .productId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .imageId("36b06c01-10f3-4645-9c45-900afc5a8b8a")
                .productName("Cat Litter")
                .productDescription("Clumping cat litter with odor control")
                .productSalePrice(12.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Accessory")
                .productQuantity(3)
                .build();

        Product product3 = Product.builder()
                .productId("baee7cd2-b67a-449f-b262-91f45dde8a6d")
                .imageId("be4e60a4-2369-46e8-abee-20c1a8dce3e5")
                .productName("Flea Collar")
                .productDescription("Flea and tick prevention for small dogs")
                .productSalePrice(9.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Medication")
                .productQuantity(53)
                .build();

        Product product4 = Product.builder()
                .productId("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a")
                .imageId("7074e0ef-d041-452f-8a0f-cb9ab20d1fed")
                .productName("Bird Cage")
                .productDescription("Spacious cage for small birds like parakeets")
                .productSalePrice(29.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Accessory")
                .productQuantity(8)
                .build();

        Product product5 = Product.builder()
                .productId("4d508fb7-f1f2-4952-829d-10dd7254cf26")
                .imageId("392c42d9-9505-4c27-b82e-20351b25d33f")
                .productName("Aquarium Filter")
                .productDescription("Filter system for small to medium-sized aquariums")
                .productSalePrice(19.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Accessory")
                .productQuantity(14)
                .build();

        Product product6 = Product.builder()
                .productId("a6a27433-e7a9-4e78-8ae3-0cb57d756863")
                .imageId("664aa14b-db66-4b25-9d05-f3a9164eb401")
                .productName("Horse Saddle")
                .productDescription("Lightweight saddle for riding horses")
                .productSalePrice(199.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Equipment")
                .productQuantity(58)
                .build();

        Product product7 = Product.builder()
                .productId("4affcab7-3ab1-4917-a114-2b6301aa5565")
                .imageId("3377a03f-8105-47d7-8d8a-d89fd170c7e6")
                .productName("Rabbit Hutch")
                .productDescription("Outdoor wooden hutch for rabbits")
                .productSalePrice(79.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Accessory")
                .productQuantity(66)
                .build();

        Product product8 = Product.builder()
                .productId("1501f30e-1db1-44b2-a555-bca6f64450e4")
                .imageId("c76ed4c1-fc5d-4868-8b39-1bca6b0be368")
                .productName("Fish Tank Heater")
                .productDescription("Submersible heater for tropical fish tanks")
                .productSalePrice(14.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Accessory")
                .productQuantity(6)
                .build();

        Resource resource1 = new ClassPathResource("images/dog_food.jpg");
        Resource resource2 = new ClassPathResource("images/cat_litter.png");
        Resource resource3 = new ClassPathResource("images/flea_collar.jpg");
        Resource resource4 = new ClassPathResource("images/bird_cage.jpg");
        Resource resource5 = new ClassPathResource("images/aquarium_filter.png");
        Resource resource6 = new ClassPathResource("images/horse_saddle.jpg");
        Resource resource7 = new ClassPathResource("images/rabbit_hutch.jpg");
        Resource resource8 = new ClassPathResource("images/fish_tank_heater.jpg");

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

        Flux.just(product1, product2, product3, product4, product5, product6, product7, product8)
                .flatMap(s -> productRepository.insert(Mono.just(s))
                        .log(s.toString()))
                .subscribe();

        Flux.just(image1, image2, image3, image4, image5, image6, image7, image8)
                .flatMap(s -> imageRepository.insert(Mono.just(s))
                        .log(s.toString()))
                .subscribe();
    }
}
