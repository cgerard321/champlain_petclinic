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
            backfillSeedTranslations();
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

        SeededProduct seededProduct1 = seedProduct("06a7d573-bcab-4db3-956f-773324b92a80", "Dog Food", "Premium dry food for adult dogs", 45.99, 44, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2002-09-26", "dog_food.png", "08a5af6b-3501-4157-9a99-1aa82387b9e4");
        SeededProduct seededProduct2 = seedProduct("98f7b33a-d62a-420a-a84a-05a27c85fc91", "Cat Litter", "Clumping cat litter with odor control", 12.99, 3, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2020-06-30", "cat_litter.png", "36b06c01-10f3-4645-9c45-900afc5a8b8a");
        SeededProduct seededProduct3 = seedProduct("baee7cd2-b67a-449f-b262-91f45dde8a6d", "Flea Collar", "Flea and tick prevention for small dogs", 9.99, 53, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2019-09-29", "flea_collar.png", "be4e60a4-2369-46e8-abee-20c1a8dce3e5");
        SeededProduct seededProduct4 = seedProduct("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "Bird Cage", "Spacious cage for small birds like parakeets", 29.99, 8, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2023-05-06", "bird_cage.png", "7074e0ef-d041-452f-8a0f-cb9ab20d1fed");
        SeededProduct seededProduct5 = seedProduct("4d508fb7-f1f2-4952-829d-10dd7254cf26", "Aquarium Filter", "Filter system for small to medium-sized aquariums", 19.99, 14, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2025-09-29", "aquarium_filter.png", "392c42d9-9505-4c27-b82e-20351b25d33f");
        SeededProduct seededProduct6 = seedProduct("a6a27433-e7a9-4e78-8ae3-0cb57d756863", "Horse Saddle", "Lightweight saddle for riding horses", 199.99, 58, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "1988-09-29", "horse_saddle.png", "664aa14b-db66-4b25-9d05-f3a9164eb401");
        SeededProduct seededProduct7 = seedProduct("4affcab7-3ab1-4917-a114-2b6301aa5565", "Rabbit Hutch", "Outdoor wooden hutch for rabbits", 79.99, 66, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-02-22", "rabbit_hutch.png", "3377a03f-8105-47d7-8d8a-d89fd170c7e6");
        SeededProduct seededProduct8 = seedProduct("1501f30e-1db1-44b2-a555-bca6f64450e4", "Fish Tank Heater", "Submersible heater for tropical fish tanks", 14.99, 0, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2022-09-19", "fish_tank_heater.png", "c76ed4c1-fc5d-4868-8b39-1bca6b0be368");

        Product product1 = seededProduct1.product();
        Product product2 = seededProduct2.product();
        Product product3 = seededProduct3.product();
        Product product4 = seededProduct4.product();
        Product product5 = seededProduct5.product();
        Product product6 = seededProduct6.product();
        Product product7 = seededProduct7.product();
        Product product8 = seededProduct8.product();

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

        List<SeededProduct> additionalProductSeeds = List.of(
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "Senior Dog Joint Supplement", "Glucosamine chews supporting mobility in older dogs", 24.99, 18, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-11-15", "senior_dog_joint_supplement.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7101"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "Puppy Deworming Tablets", "Broad-spectrum deworming treatment for puppies", 16.49, 2, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.NO_DELIVERY_OPTION, ProductStatus.PRE_ORDER, "2026-02-10", "puppy_deworming_tablets.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7102"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "Ear Cleaning Solution", "Gentle cleanser for routine canine and feline ear care", 13.99, 27, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-08-21", "ear_cleaning_solution.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7103"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "Veterinary Dental Gel", "Enzymatic gel for reducing plaque and tartar", 18.99, 11, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-04-12", "veterinary_dental_gel.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7104"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "Calming Chews", "Herbal calming supplement for stressful visits and travel", 21.99, 1, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2026-01-18", "calming_chews.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7105"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "Cat Hairball Remedy", "Lubricating paste to help prevent hairballs", 9.49, 22, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2023-10-03", "cat_hairball_remedy.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7106"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "Wound Care Spray", "Antiseptic spray for minor cuts and abrasions", 15.99, 0, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.DELIVERY, ProductStatus.OUT_OF_STOCK, "2024-01-09", "wound_care_spray.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7128"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "Flea Treatment Drops", "Monthly topical flea and tick treatment for cats", 29.99, 9, "86627454-970e-41a9-baa6-71ab759bf66c", DeliveryType.NO_DELIVERY_OPTION, ProductStatus.AVAILABLE, "2025-06-27", "flea_treatment_drops.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7129"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "Stainless Steel Food Bowl", "Non-slip bowl suitable for cats and small dogs", 11.99, 31, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-03-14", "stainless_steel_food_bowl.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7107"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "Reflective Dog Leash", "Reflective nylon leash for safer evening walks", 17.99, 14, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-02-26", "reflective_dog_leash.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7108"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "Adjustable Recovery Collar", "Soft adjustable collar for post-treatment recovery", 19.99, 6, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2023-07-19", "adjustable_recovery_collar.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7109"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "Orthopedic Pet Bed", "Supportive washable bed for aging pets", 64.99, 4, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.PRE_ORDER, "2026-03-01", "orthopedic_pet_bed.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7110"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013", "Cat Scratching Post", "Compact sisal scratching post with toy attachment", 34.99, 16, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2024-11-08", "cat_scratching_post.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7111"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "Grooming Brush Set", "Brush and comb set for routine coat maintenance", 22.49, 25, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2025-09-05", "grooming_brush_set.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7112"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "Portable Water Bottle", "Leak-resistant travel bottle for dogs", 14.99, 3, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2026-04-06", "portable_water_bottle.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7113"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "Heated Pet Blanket", "Low-voltage fleece blanket for cold-weather comfort", 49.99, 0, "6a247af0-52d9-4179-a5b4-ad4b92e686b1", DeliveryType.DELIVERY, ProductStatus.OUT_OF_STOCK, "2022-12-11", "heated_pet_blanket.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7130"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017", "Sensitive Stomach Dog Food", "Easily digestible dry food for adult dogs", 52.99, 20, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-12-02", "sensitive_stomach_dog_food.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7114"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "Indoor Cat Formula", "Balanced dry food formulated for indoor cats", 38.99, 13, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2024-05-23", "indoor_cat_formula.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7115"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "Kitten Starter Food", "Nutrient-rich food for growing kittens", 31.99, 7, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2026-02-22", "kitten_starter_food.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7116"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "Small Animal Timothy Hay", "Fresh timothy hay for rabbits and guinea pigs", 18.49, 29, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2023-04-17", "small_animal_timothy_hay.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7117"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021", "Tropical Fish Flakes", "Daily flake food for tropical aquarium fish", 8.99, 2, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2025-07-31", "tropical_fish_flakes.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7118"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "Parakeet Seed Blend", "Mixed seed blend for small companion birds", 12.99, 24, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-09-12", "parakeet_seed_blend.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7119"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "Senior Cat Wet Food Pack", "Moisture-rich meal pack for senior cats", 26.99, 10, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-03-30", "senior_cat_wet_food_pack.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7120"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "Equine Electrolyte Supplement", "Electrolyte powder for active horses", 44.99, 5, "586d0700-57db-4312-b6f1-413b79dd018c", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2022-06-28", "equine_electrolyte_supplement.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7131"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025", "Digital Pet Scale", "Precise scale for monitoring pet weight at home", 39.99, 8, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2024-02-05", "digital_pet_scale.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7121"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "Automatic Feeder", "Programmable feeder for scheduled meals", 79.99, 12, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY, ProductStatus.PRE_ORDER, "2026-05-15", "automatic_feeder.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7122"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "Compact Pet Carrier", "Ventilated carrier for cats and small dogs", 54.99, 15, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2025-10-19", "compact_pet_carrier.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7123"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "Aquarium Water Test Kit", "Multi-parameter test kit for aquarium water quality", 27.99, 19, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2023-11-26", "aquarium_water_test_kit.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7124"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "LED Examination Light", "Rechargeable light for home health checks", 32.99, 3, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2025-08-08", "led_examination_light.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7125"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "Pet Nail Grinder", "Quiet rechargeable grinder for routine nail care", 42.99, 17, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.PICKUP, ProductStatus.AVAILABLE, "2024-06-16", "pet_nail_grinder.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7126"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "Foldable Exercise Pen", "Portable enclosure for puppies and small animals", 89.99, 6, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY_AND_PICKUP, ProductStatus.AVAILABLE, "2026-01-30", "foldable_exercise_pen.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7127"),
                seedProduct("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "Canine First Aid Kit", "Practical first aid kit for common pet emergencies", 36.99, 1, "79c8723a-8df3-495d-8eb0-07d574ff5ae5", DeliveryType.DELIVERY, ProductStatus.AVAILABLE, "2023-02-13", "canine_first_aid_kit.png", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7132")
        );

        List<Product> seededProducts = new ArrayList<>(List.of(
                product1, product2, product3, product4,
                product5, product6, product7, product8));
        seededProducts.addAll(additionalProductSeeds.stream()
                .map(SeededProduct::product)
                .toList());

        List<Image> seededImages = new ArrayList<>(List.of(
                seededProduct1.image(), seededProduct2.image(),
                seededProduct3.image(), seededProduct4.image(),
                seededProduct5.image(), seededProduct6.image(),
                seededProduct7.image(), seededProduct8.image()));
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

        Flux.fromIterable(seededImages)
                .flatMap(s -> imageRepository.save(s)
                        .log(s.toString()))
                .subscribe();

        Flux.just(productType1, productType2, productType3, productType4)
                .flatMap(s -> productTypeRepository.save(s)
                        .log(s.toString()))
                .subscribe();
    }

    // Only missing translations of unchanged seed text are filled on existing databases.
    private void backfillSeedTranslations() {
        Flux.fromIterable(productTranslations())
                .concatMap(seed -> productRepository.backfillFrenchName(seed.productId(), seed.name(), seed.nameFr())
                        .then(productRepository.backfillFrenchDescription(seed.productId(), seed.description(), seed.descriptionFr())))
                .then().block();
    }

    private static List<ProductTranslation> productTranslations() {
        return List.of(
                new ProductTranslation("06a7d573-bcab-4db3-956f-773324b92a80", "Dog Food", "Nourriture pour chiens", "Premium dry food for adult dogs", "Nourriture sèche de qualité supérieure pour chiens adultes"),
                new ProductTranslation("98f7b33a-d62a-420a-a84a-05a27c85fc91", "Cat Litter", "Litière pour chats", "Clumping cat litter with odor control", "Litière agglomérante pour chats avec contrôle des odeurs"),
                new ProductTranslation("baee7cd2-b67a-449f-b262-91f45dde8a6d", "Flea Collar", "Collier antipuces", "Flea and tick prevention for small dogs", "Protection contre les puces et les tiques pour petits chiens"),
                new ProductTranslation("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "Bird Cage", "Cage pour oiseaux", "Spacious cage for small birds like parakeets", "Cage spacieuse pour petits oiseaux comme les perruches"),
                new ProductTranslation("4d508fb7-f1f2-4952-829d-10dd7254cf26", "Aquarium Filter", "Filtre pour aquarium", "Filter system for small to medium-sized aquariums", "Système de filtration pour aquariums de petite et moyenne taille"),
                new ProductTranslation("a6a27433-e7a9-4e78-8ae3-0cb57d756863", "Horse Saddle", "Selle pour cheval", "Lightweight saddle for riding horses", "Selle légère pour monter à cheval"),
                new ProductTranslation("4affcab7-3ab1-4917-a114-2b6301aa5565", "Rabbit Hutch", "Clapier pour lapins", "Outdoor wooden hutch for rabbits", "Clapier extérieur en bois pour lapins"),
                new ProductTranslation("1501f30e-1db1-44b2-a555-bca6f64450e4", "Fish Tank Heater", "Chauffe-eau pour aquarium", "Submersible heater for tropical fish tanks", "Chauffe-eau submersible pour aquariums de poissons tropicaux"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "Senior Dog Joint Supplement", "Supplément articulaire pour chiens âgés", "Glucosamine chews supporting mobility in older dogs", "Bouchées à la glucosamine favorisant la mobilité des chiens âgés"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "Puppy Deworming Tablets", "Comprimés vermifuges pour chiots", "Broad-spectrum deworming treatment for puppies", "Traitement vermifuge à large spectre pour chiots"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "Ear Cleaning Solution", "Solution nettoyante pour les oreilles", "Gentle cleanser for routine canine and feline ear care", "Nettoyant doux pour les soins réguliers des oreilles des chiens et des chats"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "Veterinary Dental Gel", "Gel dentaire vétérinaire", "Enzymatic gel for reducing plaque and tartar", "Gel enzymatique pour réduire la plaque et le tartre"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "Calming Chews", "Bouchées apaisantes", "Herbal calming supplement for stressful visits and travel", "Supplément apaisant à base de plantes pour les visites et les déplacements stressants"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "Cat Hairball Remedy", "Remède contre les boules de poils pour chats", "Lubricating paste to help prevent hairballs", "Pâte lubrifiante pour aider à prévenir les boules de poils"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "Wound Care Spray", "Vaporisateur pour le soin des plaies", "Antiseptic spray for minor cuts and abrasions", "Vaporisateur antiseptique pour les coupures et les éraflures mineures"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "Flea Treatment Drops", "Gouttes antipuces", "Monthly topical flea and tick treatment for cats", "Traitement topique mensuel contre les puces et les tiques pour chats"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "Stainless Steel Food Bowl", "Gamelle en acier inoxydable", "Non-slip bowl suitable for cats and small dogs", "Gamelle antidérapante pour chats et petits chiens"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "Reflective Dog Leash", "Laisse réfléchissante pour chiens", "Reflective nylon leash for safer evening walks", "Laisse en nylon réfléchissant pour des promenades plus sûres en soirée"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "Adjustable Recovery Collar", "Collerette de protection réglable", "Soft adjustable collar for post-treatment recovery", "Collerette souple et réglable pour la convalescence après un traitement"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "Orthopedic Pet Bed", "Lit orthopédique pour animaux", "Supportive washable bed for aging pets", "Lit lavable offrant un bon soutien aux animaux vieillissants"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013", "Cat Scratching Post", "Griffoir pour chats", "Compact sisal scratching post with toy attachment", "Griffoir compact en sisal avec jouet fixé"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "Grooming Brush Set", "Ensemble de brosses de toilettage", "Brush and comb set for routine coat maintenance", "Ensemble de brosses et de peignes pour l’entretien régulier du pelage"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "Portable Water Bottle", "Bouteille d’eau portative", "Leak-resistant travel bottle for dogs", "Bouteille de voyage étanche pour chiens"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "Heated Pet Blanket", "Couverture chauffante pour animaux", "Low-voltage fleece blanket for cold-weather comfort", "Couverture en molleton à basse tension pour le confort par temps froid"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017", "Sensitive Stomach Dog Food", "Nourriture pour chiens à l’estomac sensible", "Easily digestible dry food for adult dogs", "Nourriture sèche facile à digérer pour chiens adultes"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "Indoor Cat Formula", "Nourriture pour chats d’intérieur", "Balanced dry food formulated for indoor cats", "Nourriture sèche équilibrée formulée pour les chats d’intérieur"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "Kitten Starter Food", "Nourriture de départ pour chatons", "Nutrient-rich food for growing kittens", "Nourriture riche en nutriments pour chatons en croissance"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "Small Animal Timothy Hay", "Foin de fléole pour petits animaux", "Fresh timothy hay for rabbits and guinea pigs", "Foin de fléole frais pour lapins et cochons d’Inde"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021", "Tropical Fish Flakes", "Flocons pour poissons tropicaux", "Daily flake food for tropical aquarium fish", "Nourriture quotidienne en flocons pour poissons tropicaux d’aquarium"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "Parakeet Seed Blend", "Mélange de graines pour perruches", "Mixed seed blend for small companion birds", "Mélange de graines pour petits oiseaux de compagnie"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "Senior Cat Wet Food Pack", "Assortiment de nourriture humide pour chats âgés", "Moisture-rich meal pack for senior cats", "Assortiment de repas riches en eau pour chats âgés"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "Equine Electrolyte Supplement", "Supplément d’électrolytes pour chevaux", "Electrolyte powder for active horses", "Poudre d’électrolytes pour chevaux actifs"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025", "Digital Pet Scale", "Balance numérique pour animaux", "Precise scale for monitoring pet weight at home", "Balance précise pour surveiller le poids des animaux à la maison"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "Automatic Feeder", "Distributeur automatique de nourriture", "Programmable feeder for scheduled meals", "Distributeur programmable pour des repas à heures fixes"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "Compact Pet Carrier", "Cage de transport compacte", "Ventilated carrier for cats and small dogs", "Cage de transport ventilée pour chats et petits chiens"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "Aquarium Water Test Kit", "Trousse d’analyse de l’eau d’aquarium", "Multi-parameter test kit for aquarium water quality", "Trousse d’analyse multiparamètre pour la qualité de l’eau d’aquarium"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "LED Examination Light", "Lampe d’examen à DEL", "Rechargeable light for home health checks", "Lampe rechargeable pour les examens de santé à domicile"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "Pet Nail Grinder", "Lime électrique pour griffes", "Quiet rechargeable grinder for routine nail care", "Lime électrique silencieuse et rechargeable pour l’entretien régulier des griffes"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "Foldable Exercise Pen", "Enclos d’exercice pliable", "Portable enclosure for puppies and small animals", "Enclos portatif pour chiots et petits animaux"),
                new ProductTranslation("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "Canine First Aid Kit", "Trousse de premiers soins pour chiens", "Practical first aid kit for common pet emergencies", "Trousse pratique de premiers soins pour les urgences courantes chez les animaux")
        );
    }

    private record ProductTranslation(String productId, String name, String nameFr,
                                      String description, String descriptionFr) { }

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

        productTranslations().stream()
                .filter(seed -> seed.productId().equals(productId))
                .findFirst().ifPresent(seed -> {
                    product.setProductNameFr(seed.nameFr());
                    product.setProductDescriptionFr(seed.descriptionFr());
                });
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
