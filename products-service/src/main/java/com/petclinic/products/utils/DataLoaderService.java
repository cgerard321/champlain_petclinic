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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DataLoaderService implements CommandLineRunner {

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

        Rating rating3prod1 = Rating.builder()
                .productId(product1.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 5)
                .review("""
                Finally, a Kibble My Picky Eater Finishes
                =========================================

                Quick Summary
                -------------

                I switched my 6 year old Lab mix to this **premium dry food** about a month ago and the difference is *night and day*.

                ---

                Score Breakdown

                | Category      | Score | Notes                           |
                | :------------ | :---: | ------------------------------: |
                | Taste         | 5/5   | Bowl is empty within minutes    |
                | Digestion     | 5/5   | No more upset stomach           |
                | Coat & Skin   | 4/5   | Shinier after about 3 weeks     |
                | Value         | 4/5   | Pricey, but a bag lasts a month |

                > "He actually waits by his bowl now."
                >
                > > Before this food he would walk away after two bites.

                What I Liked

                - **Small kibble size**: easy to chew for medium dogs.
                - **Real meat first**: the first ingredient is chicken.
                  - No corn or wheat fillers.
                  - No artificial colours.
                - **Resealable bag**: keeps the food fresh.

                How I Switched Foods

                1. Days 1 to 3: mix 25% new food with 75% old food.
                2. Days 4 to 6: go 50/50.
                3. Day 7 onward: ++100% new food++.

                Useful links: [Dog food on Wikipedia](https://en.wikipedia.org/wiki/Dog_food "Dog food"), the [AAFCO pet food guide][aafco], and <https://www.akc.org/expert-advice/nutrition/>.

                [aafco]: https://www.aafco.org/consumers/understanding-pet-food/ "AAFCO"

                ### Final Verdict

                Worth every penny. I would ***definitely*** buy it again.
                """)
                .build();

        Rating rating3prod2 = Rating.builder()
                .productId(product2.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 4)
                .review("""
                Solid Everyday Litter for the Price
                ===================================

                Quick Summary
                -------------

                I picked up the 5 kg bag of Unicorn Litter Sand and have been using it for a couple of weeks now.

                Overall, it's a great value for the price.

                ---

                Rating

                Score: 4/5

                Score Breakdown

                | Category       | Score | Notes                        |
                | :------------- | :---: | ---------------------------: |
                | Clumping       | 5/5   | Firm, easy to scoop          |
                | Odor Control   | 4/5   | Fresh for 4 to 5 days        |
                | Dust Level     | 3/5   | A bit dusty when pouring     |
                | Value          | 5/5   | Great price for 5 kg         |

                > "My cat took to it right away."
                >
                > > Even my picky older cat used it without hesitation.

                What I Liked

                - Clumps well: Forms firm clumps that are easy to scoop.
                - Odor control: Keeps the box fresh for several days.
                  - Works best with a covered litter box.
                  - Still effective after 4 to 5 days.
                - Good value: Only $12.99 for 5 kg.

                What Could Be Better

                - A bit dusty when pouring.
                - Some granules get tracked outside the box.

                How I Use It

                1. Pour about 3 inches into a clean litter box.
                2. Scoop clumps ++once a day++.
                3. Do a full change every 2 weeks.

                My scooping schedule looks like this:

                ```
                Morning: scoop clumps
                Evening: quick check
                Sunday:  top up litter
                ```

                And here's my quick shopping note:

                ```
                Item:     Cat Litter (Unicorn Litter Sand)
                Weight:   5 kg
                Price:    12.99$
                Delivery: Pickup
                ```

                Fresh Out of the Bag

                Right after a full change, the granules are fine and pour evenly.

                Links

                - Product guide: [Cat litter](https://en.wikipedia.org/wiki/Cat_litter "Cat litter on Wikipedia")
                - Litter box tips: [Litter box][litterbox]
                - Vet advice: <https://www.aspca.org/pet-care/cat-care/general-cat-care>
                - More reading: www.humanesociety.org

                [litterbox]: https://en.wikipedia.org/wiki/Litter_box "Litter box on Wikipedia"

                Final Verdict

                A reliable, affordable clumping litter. I would ++definitely++ buy it again.

                \\*This is not italic\\* and prices are in USD &amp; include tax &copy; my honest opinion.
                """)
                .build();

        Rating rating3prod3 = Rating.builder()
                .productId(product3.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 3)
                .review("""
                # Does the Job, But Not for Long

                ## Background

                My beagle **Daisy** spends a lot of time in tall grass, so flea season is a big deal for us. I tried this collar for the whole summer.

                ## The Good

                - Fleas were *gone* within about **48 hours**.
                - Fits small necks well, with plenty of extra length to trim.
                - No smell that I could notice.

                ## The Not So Good

                - Protection faded after about `6 weeks`, not the advertised 8 months.
                - The buckle is a little stiff to open.

                ## Week by Week

                | Week | Fleas Spotted | Notes                     |
                | ---- | :-----------: | ------------------------- |
                | 1    | Many          | Collar put on Monday      |
                | 2    | None          | Huge improvement          |
                | 4    | None          | Still working great       |
                | 6    | A few         | Starting to wear off      |
                | 8    | Many          | Back to square one        |

                ### Tips If You Buy It

                1. Leave room for **two fingers** between the collar and the neck.
                2. Cut off the extra length so your dog can't chew it.
                3. Mark the date on your calendar and ++replace it early++.

                > Talk to your vet before combining this with any other flea treatment.

                More on fleas: [Flea on Wikipedia](https://en.wikipedia.org/wiki/Flea)

                ---

                **Bottom line:** works fast, but plan on buying a few per season.
                """)
                .build();

        Rating rating3prod4 = Rating.builder()
                .productId(product4.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 5)
                .review("""
                # A Happy Home for Two Budgies

                I bought this cage for my two budgerigars, **Sky** and **Lemon**, and they settled in on the very first day.

                ## Dimensions

                | Measurement   | Value      |
                | :------------ | ---------: |
                | Width         | 60 cm      |
                | Depth         | 40 cm      |
                | Height        | 75 cm      |
                | Bar spacing   | 1.2 cm     |

                ## Setup

                1. Unpack all the panels.
                   1. Lay the side panels flat.
                   2. Clip the corners together.
                2. Attach the roof.
                3. Slide in the tray and grate.
                4. Add perches, toys and food cups.

                Took me about *20 minutes* with no tools.

                ## What I Love

                - **Pull out tray**: cleaning takes five minutes.
                - **Two doors**: easy to reach in without birds escaping.
                - **Bar spacing**: safe for small birds like budgies and finches.

                > "They chirp all morning now."
                >
                > > My neighbour said they sound happier than ever.

                ## Sky and Lemon

                Tip: add a natural branch like I did. They ++love++ chewing on it.

                Read more about [budgerigar care](https://en.wikipedia.org/wiki/Budgerigar "Budgerigar on Wikipedia").

                ---

                ***Highly recommended*** for anyone with small birds.
                """)
                .build();

        Rating rating3prod5 = Rating.builder()
                .productId(product5.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 4)
                .review("""
                Crystal Clear Water After One Week
                ==================================

                I run this filter on my **75 litre** community tank with tetras, corydoras and a few shrimp.

                Water Test Results
                ------------------

                | Parameter | Before | After 1 Week |
                | :-------- | :----: | :----------: |
                | Ammonia   | 0.5    | 0            |
                | Nitrite   | 0.25   | 0            |
                | Nitrate   | 40     | 20           |
                | Clarity   | Cloudy | Clear        |

                My weekly maintenance log:

                ```
                Mon: 25% water change
                Wed: rinse sponge in tank water
                Sat: check flow rate
                ```

                Pros

                - Very easy to install, *about 10 minutes*.
                - Adjustable flow, so it's gentle enough for shrimp.
                - The sponge grows lots of good bacteria.

                Cons

                - A slight hum at night.
                - The intake cover pops off if you bump it.

                Maintenance Tips

                1. **Never** rinse the sponge under tap water. The chlorine kills the good bacteria.
                2. Squeeze it out in a bucket of old tank water instead.
                3. Replace the sponge ++only when it falls apart++.

                After one month the sponge is still holding up well.

                > Pro tip: cycle your tank *before* adding fish. See the [nitrogen cycle](https://en.wikipedia.org/wiki/Fishkeeping#Nitrogen_cycle "Nitrogen cycle").

                ---

                **4/5**, would be perfect if it were quieter.
                """)
                .build();

        Rating rating3prod6 = Rating.builder()
                .productId(product6.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 4)
                .review("""
                # Light, Comfortable and Well Made

                ## About Me

                I ride *three to four times a week*, mostly flatwork and small jumps, on my 16 hand gelding **Bruno**.

                ## First Impressions

                The saddle is **noticeably lighter** than my old one, which makes tacking up a lot easier. The leather was a bit stiff at first but softened after a few rides.

                ## Fit and Comfort

                | Feature        | Rating | Comment                         |
                | :------------- | :----: | :------------------------------ |
                | Seat comfort   | 5/5    | Deep seat, very secure          |
                | Weight         | 5/5    | Easy to lift onto a tall horse  |
                | Knee rolls     | 4/5    | Good support over fences        |
                | Leather        | 3/5    | Needed a few rides to break in  |

                ## Break In Routine

                1. Clean the leather with a damp sponge.
                2. Apply a thin layer of conditioner.
                3. Let it sit overnight.
                4. Repeat ++after every ride++ for the first two weeks.

                > "Bruno moved more freely from day one."
                >
                > > My trainer noticed it before I even told her about the new saddle.

                ## Things to Know

                - Always have a professional check the fit. See [saddle fitting][fit].
                - Stirrups and leathers are **not included**.
                - Use a good saddle pad underneath.

                [fit]: https://en.wikipedia.org/wiki/Saddle#Saddle_fitting "Saddle fitting"

                ---

                Great value for a saddle at this price. ***Recommended.***
                """)
                .build();

        Rating rating3prod7 = Rating.builder()
                .productId(product7.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 3)
                .review("""
                # Cozy Hutch, Needs Some Tweaks

                My rabbit **Pepper** has been living in this hutch for two months. It's a nice home, but I had to make a few changes.

                ## Pros and Cons

                | Pros                         | Cons                            |
                | :--------------------------- | :------------------------------ |
                | Solid wood construction      | Not much airflow in summer      |
                | Separate sleeping area       | Latch is a bit flimsy           |
                | Easy to reach inside         | Wood needs weatherproofing      |

                ## What I Changed

                1. Drilled a few extra **ventilation holes** in the back panel.
                2. Replaced the latch with a sturdier one.
                3. Painted the outside with *pet safe* wood stain.
                   - Let it dry for 48 hours before moving Pepper back in.
                   - Two coats worked best.

                ## Daily Routine

                - Fresh hay and water every morning.
                - Spot clean the litter corner.
                - ++Full clean++ every weekend.

                > Rabbits need **lots of exercise** outside the hutch too. Pepper gets at least 3 hours a day in a pen.

                ## Pepper's Home

                More info on rabbit housing: [Domestic rabbit](https://en.wikipedia.org/wiki/Domestic_rabbit "Domestic rabbit on Wikipedia")

                ---

                **3/5**: good starting point, but plan to spend an afternoon improving it.
                """)
                .build();

        Rating rating3prod8 = Rating.builder()
                .productId(product8.getProductId())
                .customerId("9fe8a36a-36f3-4198-a5cc-d96371083493")
                .rating((byte) 2)
                .review("""
                Inconsistent Temperatures
                =========================

                I wanted to love this heater for my **40 litre** betta tank, but it struggles to hold a steady temperature.

                Temperature Log
                ---------------

                Set to **26 °C** the whole time:

                | Time     | Reading | Difference |
                | :------- | :-----: | ---------: |
                | 8:00 AM  | 24.5 °C | -1.5       |
                | 12:00 PM | 26.0 °C | 0          |
                | 6:00 PM  | 27.5 °C | +1.5       |
                | 11:00 PM | 25.0 °C | -1.0       |

                A swing of *three degrees* in one day is too much for tropical fish.

                What I Tried

                1. Moved it closer to the filter outflow for better circulation.
                2. Recalibrated the dial against a separate thermometer.
                3. Fully submerged it, as the manual says.

                None of it made a real difference.

                The Good Parts

                - Compact, it hides behind plants easily.
                - The glass tube feels sturdy.
                - The indicator light is handy.

                > **Warning:** always unplug the heater ++before++ taking it out of the water, or the glass can crack.

                Notes for Other Buyers

                ```
                Tank size:   40 L
                Setting:     26 C
                Daily swing: about 3 C
                ```

                Learn more about keeping tropical tanks stable: [Aquarium heaters](https://en.wikipedia.org/wiki/Aquarium#Heating)

                ---

                **2/5**: fine as a backup, but I wouldn't trust it as my main heater.
                """)
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

        Flux.just(bundle1, bundle2, bundle3)
                .flatMap(s -> productBundleRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.just(product1, product2, product3, product4, product5, product6, product7, product8)
                .flatMap(s -> productRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.just(
                        rating1prod1, rating2prod1, rating3prod1,
                        rating1prod2, rating2prod2, rating3prod2,
                        rating1prod3, rating2prod3, rating3prod3,
                        rating1prod4, rating2prod4, rating3prod4,
                        rating1prod5, rating2prod5, rating3prod5,
                        rating1prod6, rating2prod6, rating3prod6,
                        rating1prod7, rating2prod7, rating3prod7,
                        rating1prod8, rating2prod8, rating3prod8
                )
                .flatMap(s -> ratingRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.just(image1, image2, image3, image4, image5, image6, image7, image8)
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

    private record LegacyImageSeed(
            String imageId,
            String imageName,
            String imageType,
            String resourcePath) {
    }
}
