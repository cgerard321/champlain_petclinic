package com.petclinic.products.utils;

import com.petclinic.products.datalayer.products.Product;
import com.petclinic.products.datalayer.products.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Service
@Profile("!test")
public class DataLoaderService implements CommandLineRunner {

    @Autowired
    ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        Product product1 = Product.builder()
                .productId("06a7d573-bcab-4db3-956f-773324b92a80")
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
                .productName("Fish Tank Heater")
                .productDescription("Submersible heater for tropical fish tanks")
                .productSalePrice(14.99)
                .requestCount(0)
                .averageRating(0.0)
                .productType("Accessory")
                .productQuantity(6)
                .build();

        Flux.just(product1, product2, product3, product4, product5, product6, product7, product8)
                .flatMap(s -> productRepository.insert(Mono.just(s))
                        .log(s.toString()))
                .subscribe();
    }
}
