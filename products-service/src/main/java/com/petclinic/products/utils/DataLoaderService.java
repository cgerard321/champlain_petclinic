package com.petclinic.products.utils;

import com.petclinic.products.datalayer.images.Image;
import com.petclinic.products.migration.LegacyImageMigration;
import com.petclinic.products.datalayer.products.*;
import com.petclinic.products.datalayer.ratings.Rating;
import com.petclinic.products.datalayer.ratings.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Order(0)
public class DataLoaderService implements CommandLineRunner {

    private final boolean seedDataEnabled;

    public DataLoaderService(@Value("${app.seed-data.enabled:true}") boolean seedDataEnabled) {
        this.seedDataEnabled = seedDataEnabled;
    }

    private static final Logger log = LoggerFactory.getLogger(DataLoaderService.class);

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductBundleRepository productBundleRepository;

    @Autowired
    LegacyImageMigration imagePublisher;

    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    ProductTypeRepository productTypeRepository;

    @Override
    public void run(String... args) throws Exception {
        if (!seedDataEnabled) return;
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
                .review("**My dog loves this food!**")
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
                .review("**Great litter, clumps well**")
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
                .review("**Works well, but doesn't last long**")
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
                .review("**Great cage, easy to clean**")
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
                .review("**Works well, but a bit noisy**")
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
                .review("**Great saddle, very comfortable**")
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
                .review("**Spacious hutch, easy to clean**")
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
                .review("**Works well, but hard to adjust**")
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

        List<Rating> additionalRatingSeeds = new ArrayList<>(List.of(
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "**Pros:** easy to give and helpful on daily walks.\n\n- Simple routine\n- Gentle for our older dog", "2026-01-08T09:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 4, "The chews are easy to give and have helped during our daily walks.", "2026-02-14T14:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 4, "**The tablets were simple to administer and gentle on my puppy.**", "2026-01-19T11:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 5, "The clear instructions made the treatment easy to follow.", "2026-03-02T16:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "9fe8a36a-36f3-4198-a5cc-d96371083493", 5, "**This solution makes routine ear cleaning quick and gentle.**", "2026-01-27T10:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 4, "The bottle is easy to use and does not leave a strong smell.", "2026-04-11T13:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 4, "**The gel is easy to apply and our dog tolerates it well.**", "2026-02-06T08:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", 3, "The applicator works well, although the tube is small.", "2026-05-09T17:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", 5, "**Calming chews** were *helpful before a short car trip*.", "2026-02-22T12:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", 4, "They help make stressful visits feel more manageable.", "2026-06-03T15:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", 4, "**This paste has helped reduce hairballs for our indoor cat.**", "2026-01-15T09:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "9f6accd1-e943-4322-932e-199d93824317", 3, "The paste is effective but takes some patience to serve.", "2026-03-18T18:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 5, "### Monthly Routine\n\nThe drops were easy to apply, and our cat stayed comfortable.", "2026-02-28T11:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "810440e8-cae0-48f7-aa4f-830239b82b78", 4, "**The monthly treatment is convenient and easy to remember.**", "2026-07-07T16:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 5, "**The bowl is sturdy, easy to clean, and stays in place.**", "2026-01-31T10:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "9fe8a36a-36f3-4198-a5cc-d96371083493", 4, "The non-slip base works well on our kitchen floor.", "2026-04-23T13:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "**The reflective strip makes evening walks feel much safer.**", "2026-02-12T19:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 4, "The leash is comfortable to hold and feels durable.", "2026-05-17T08:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 4, "**The soft collar helped prevent scratching during recovery.**", "2026-03-06T14:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 3, "The adjustment is useful, though the fit took some trial and error.", "2026-06-21T12:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", 5, "**The scratching post is compact and my cat uses it every day.**", "2026-01-11T17:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", 4, "The toy attachment keeps our cat interested for longer.", "2026-04-02T09:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", 4, "**The brush removes loose fur without pulling.**", "2026-02-03T11:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", 5, "The set has everything needed for our weekly grooming routine.", "2026-05-28T15:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "9f6accd1-e943-4322-932e-199d93824317", 5, "**The bottle does not leak in the car or on walks.**", "2026-03-13T08:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 4, "It is light enough to carry and easy for our dog to drink from.", "2026-07-19T10:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "**This food is gentle on my dog's stomach and easy to digest.**", "2026-01-23T13:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 4, "Our dog enjoys the food and has had fewer stomach upsets.", "2026-04-16T16:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "9fe8a36a-36f3-4198-a5cc-d96371083493", 4, "**The formula works well for our indoor cat's routine.**", "2026-02-09T09:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 3, "The kibble size is good, but my cat needed time to switch foods.", "2026-06-12T14:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 5, "**The kittens took to this food right away.**", "2026-01-06T10:55:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 4, "It provides a convenient starter diet for growing kittens.", "2026-05-04T12:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 5, "**The hay is fresh and our rabbits enjoy it every day.**", "2026-02-18T08:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", 4, "The bag stays fresh and has very little dust.", "2026-07-02T17:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", 5, "| Test | Result |\n| --- | --- |\n| Portion | Small |\n| Acceptance | Quick |\n\nThe fish finished the flakes happily.", "2026-03-22T11:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", 4, "The flakes are a good size for our community tank.", "2026-08-08T09:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", 4, "**The seed blend gives our parakeet a nice variety.**", "2026-01-29T15:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "9f6accd1-e943-4322-932e-199d93824317", 5, "Our bird picks out the favorite seeds first every morning.", "2026-04-28T07:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 5, "**The wet food has a texture our senior cat accepts easily.**", "2026-02-25T18:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "810440e8-cae0-48f7-aa4f-830239b82b78", 4, "The small packs keep each meal fresh and convenient.", "2026-06-30T12:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 4, "The powder mixes easily with water.\n\n~~Hard to measure~~ **Update:** the scoop is easy to use.", "2026-03-09T10:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "The packaging is convenient for keeping portions dry.", "2026-09-01T14:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "**The scale gives consistent readings and is easy to use.**", "2026-01-17T09:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 4, "The display is clear enough to read while holding our cat.", "2026-05-22T16:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 5, "1. Open the carrier.\n2. Add a soft blanket.\n3. Secure the latch.\n\nIt worked well for a short trip.", "2026-02-16T11:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 4, "It is light enough to carry without feeling flimsy.", "2026-07-14T13:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", 4, "**The test strips are clearly marked and easy to compare.**", "2026-03-28T08:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", 5, "This kit makes weekly water checks much less intimidating.", "2026-08-19T15:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", 4, "**The light is bright enough for careful home examinations.**", "2026-04-07T10:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", 5, "The rechargeable battery lasts through several checks.", "2026-09-12T17:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "9f6accd1-e943-4322-932e-199d93824317", 5, "The quiet motor helped our dog stay relaxed.\n\n`Speed 1` was best for the first trim.\n\n---\n\n**Would use again.**", "2026-02-01T14:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 4, "The adjustable speed makes trimming small nails easier.", "2026-06-08T09:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "**The exercise pen folds away easily and gives our puppy room to play.**", "2026-03-15T12:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "9fe8a36a-36f3-4198-a5cc-d96371083493", 4, "The panels are simple to assemble and feel sturdy.", "2026-10-02T16:25:00")
        ));

        additionalRatingSeeds.addAll(List.of(
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 2, "The chews helped a little, but we needed a few weeks to notice a difference.", "2026-08-10T10:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", 1, "My puppy did not enjoy taking these tablets.", "2026-07-25T13:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", 2, "The cap is convenient, but the solution did not suit our routine.", "2026-09-05T09:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", 5, "Our vet recommended this gel and it is easy to use each night.", "2026-08-22T18:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 3, "The chews help somewhat, though the herbal scent is noticeable.", "2026-09-18T11:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "810440e8-cae0-48f7-aa4f-830239b82b78", 2, "It helped occasionally, but our cat still had some hairballs.", "2026-08-04T15:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "810440e8-cae0-48f7-aa4f-830239b82b78", 2, "**The spray is easy to apply, but the nozzle can clog.**", "2026-02-05T10:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 3, "It is useful to keep nearby for small scratches and scrapes.", "2026-04-19T14:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "The spray did not work well for our pet's minor irritation.", "2026-07-11T08:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "The applicator was difficult to use on our cat.", "2026-09-26T16:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 2, "The bowl is sturdy, but the base is larger than expected.", "2026-08-16T12:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "The leash is good for evening walks, although the clasp is stiff.", "2026-09-09T18:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", 2, "The collar is comfortable, but it loosened during active play.", "2026-08-28T11:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "810440e8-cae0-48f7-aa4f-830239b82b78", 4, "> Our dog finally settles comfortably in the new bed.\n\nThe washable cover is a big help.", "2026-03-12T09:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 3, "The cover washes well, but the bed is quite firm.", "2026-06-26T15:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "The shape did not fit comfortably in our pet's usual sleeping area.", "2026-09-30T13:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 1, "The post was too short for our larger cat.", "2026-08-06T17:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "810440e8-cae0-48f7-aa4f-830239b82b78", 2, "The brush works, but the handle is less comfortable than expected.", "2026-09-14T10:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 3, "The bottle is useful, though the cap takes two hands to open.", "2026-08-12T14:55:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "810440e8-cae0-48f7-aa4f-830239b82b78", 1, "**The blanket warmed slowly and did not cover our whole pet bed.**", "2026-01-28T19:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 2, "The fleece is soft, but the heat setting was difficult to adjust.", "2026-05-15T12:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "It adds some warmth on cool evenings when placed under a blanket.", "2026-08-31T16:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 2, "Our dog ate it reluctantly and needed time to adjust.", "2026-09-03T09:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 1, "Our cat would not eat this formula after the food transition.", "2026-08-20T18:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", 3, "The food is convenient, but the pieces are a little large for young kittens.", "2026-09-21T13:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", 2, "Our rabbits ate it, but the bag contained more dust than expected.", "2026-08-02T11:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", 1, "The flakes clouded our tank when too much was added.", "2026-09-17T15:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 3, "The blend is fine, but our bird leaves some of the larger seeds.", "2026-08-24T10:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 2, "The texture was not a good match for our picky senior cat.", "2026-09-07T17:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 1, "Our horse did not seem to benefit from this supplement.", "2026-08-14T08:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 2, "The scale is accurate, but the display is hard to see in low light.", "2026-09-24T12:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "810440e8-cae0-48f7-aa4f-830239b82b78", 4, "**The feeder schedule is easy to set and keeps meals consistent.**", "2026-05-07T09:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 2, "The hopper is useful, but the portion size needed adjustment.", "2026-07-29T16:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "The timer works well after the initial setup.", "2026-09-29T11:50:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", 3, "The carrier is comfortable, but the door latch feels tight.", "2026-08-09T14:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", 2, "The instructions are helpful, but the color chart is small.", "2026-09-19T10:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", 1, "The light stopped holding a charge after only a few uses.", "2026-08-27T18:35:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "810440e8-cae0-48f7-aa4f-830239b82b78", 2, "The grinder is quiet, but it takes a long time on thicker nails.", "2026-09-11T13:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 3, "The pen is useful indoors, but the panels can shift on smooth floors.", "2026-08-18T15:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "810440e8-cae0-48f7-aa4f-830239b82b78", 4, "**The kit has useful basics for handling small household accidents.**", "2026-04-14T09:55:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 2, "The case is compact, but a few items are difficult to remove.", "2026-07-23T12:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "The kit did not include everything we expected for larger emergencies.", "2026-09-27T16:05:00")
        ));

        additionalRatingSeeds.addAll(List.of(
                seedRating("06a7d573-bcab-4db3-956f-773324b92a80", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Dog Food has been useful for our pet's daily routine.", "2026-01-01T09:00:00"),
                seedRating("98f7b33a-d62a-420a-a84a-05a27c85fc91", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Cat Litter has been useful for our pet's daily routine.", "2026-04-10T12:21:00"),
                seedRating("98f7b33a-d62a-420a-a84a-05a27c85fc91", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Cat Litter works as expected, although there is room for improvement.", "2026-05-13T13:28:00"),
                seedRating("baee7cd2-b67a-449f-b262-91f45dde8a6d", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Flea Collar has been useful for our pet's daily routine.", "2026-07-19T15:42:00"),
                seedRating("baee7cd2-b67a-449f-b262-91f45dde8a6d", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Flea Collar works as expected, although there is room for improvement.", "2026-08-22T16:49:00"),
                seedRating("baee7cd2-b67a-449f-b262-91f45dde8a6d", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Flea Collar was not the right fit for our pet, but the product is easy to use.", "2026-09-25T17:56:00"),
                seedRating("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Bird Cage has been useful for our pet's daily routine.", "2026-01-02T09:03:00"),
                seedRating("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Bird Cage works as expected, although there is room for improvement.", "2026-02-05T10:10:00"),
                seedRating("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Bird Cage was not the right fit for our pet, but the product is easy to use.", "2026-03-08T11:17:00"),
                seedRating("ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 2, "The product is useful, though we noticed a few small limitations.", "2026-04-07T13:30:00"),
                seedRating("4d508fb7-f1f2-4952-829d-10dd7254cf26", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Aquarium Filter has been useful for our pet's daily routine.", "2026-04-11T12:24:00"),
                seedRating("a6a27433-e7a9-4e78-8ae3-0cb57d756863", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Horse Saddle has been useful for our pet's daily routine.", "2026-07-20T15:45:00"),
                seedRating("a6a27433-e7a9-4e78-8ae3-0cb57d756863", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Horse Saddle works as expected, although there is room for improvement.", "2026-08-23T16:52:00"),
                seedRating("4affcab7-3ab1-4917-a114-2b6301aa5565", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Rabbit Hutch has been useful for our pet's daily routine.", "2026-01-03T09:06:00"),
                seedRating("4affcab7-3ab1-4917-a114-2b6301aa5565", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Rabbit Hutch works as expected, although there is room for improvement.", "2026-02-06T10:13:00"),
                seedRating("4affcab7-3ab1-4917-a114-2b6301aa5565", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Rabbit Hutch was not the right fit for our pet, but the product is easy to use.", "2026-03-09T11:20:00"),
                seedRating("1501f30e-1db1-44b2-a555-bca6f64450e4", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Fish Tank Heater has been useful for our pet's daily routine.", "2026-04-12T12:27:00"),
                seedRating("1501f30e-1db1-44b2-a555-bca6f64450e4", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Fish Tank Heater works as expected, although there is room for improvement.", "2026-05-15T13:34:00"),
                seedRating("1501f30e-1db1-44b2-a555-bca6f64450e4", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Fish Tank Heater was not the right fit for our pet, but the product is easy to use.", "2026-06-18T14:41:00"),
                seedRating("1501f30e-1db1-44b2-a555-bca6f64450e4", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 4, "The product is useful, though we noticed a few small limitations.", "2026-08-15T17:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 5, "**Pros:** easy to give and helpful on daily walks.", "2026-07-21T15:48:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Puppy Deworming Tablets has been useful for our pet's daily routine.", "2026-01-04T09:09:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "Puppy Deworming Tablets works as expected, although there is room for improvement.", "2026-02-07T10:16:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Ear Cleaning Solution has been useful for our pet's daily routine.", "2026-04-13T12:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Ear Cleaning Solution works as expected, although there is room for improvement.", "2026-05-16T13:37:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 3, "Ear Cleaning Solution was not the right fit for our pet, but the product is easy to use.", "2026-06-19T14:44:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Veterinary Dental Gel has been useful for our pet's daily routine.", "2026-07-22T15:51:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Veterinary Dental Gel works as expected, although there is room for improvement.", "2026-08-25T16:58:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "Veterinary Dental Gel was not the right fit for our pet, but the product is easy to use.", "2026-09-02T17:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "**What I liked:** simple nightly application and a clean tube.", "2026-03-23T13:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Calming Chews has been useful for our pet's daily routine.", "2026-01-05T09:12:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 5, "Cat Hairball Remedy has been useful for our pet's daily routine.", "2026-04-14T12:33:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "Cat Hairball Remedy works as expected, although there is room for improvement.", "2026-05-17T13:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Wound Care Spray has been useful for our pet's daily routine.", "2026-07-23T15:54:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Wound Care Spray works as expected, although there is room for improvement.", "2026-08-26T16:01:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Wound Care Spray was not the right fit for our pet, but the product is easy to use.", "2026-09-03T17:08:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "9fe8a36a-36f3-4198-a5cc-d96371083493", 5, "Flea Treatment Drops has been useful for our pet's daily routine.", "2026-01-06T09:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 1, "Flea Treatment Drops works as expected, although there is room for improvement.", "2026-02-09T10:22:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 3, "Flea Treatment Drops was not the right fit for our pet, but the product is easy to use.", "2026-03-12T11:29:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 2, "**Pros:** easy to apply.\n\n- Compact bottle\n- Simple monthly routine", "2026-07-05T17:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Stainless Steel Food Bowl has been useful for our pet's daily routine.", "2026-04-15T12:36:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Reflective Dog Leash has been useful for our pet's daily routine.", "2026-07-24T15:57:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Reflective Dog Leash works as expected, although there is room for improvement.", "2026-08-01T16:04:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Adjustable Recovery Collar has been useful for our pet's daily routine.", "2026-01-07T09:18:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Adjustable Recovery Collar works as expected, although there is room for improvement.", "2026-02-10T10:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "Adjustable Recovery Collar was not the right fit for our pet, but the product is easy to use.", "2026-03-13T11:32:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Orthopedic Pet Bed has been useful for our pet's daily routine.", "2026-04-16T12:39:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Orthopedic Pet Bed works as expected, although there is room for improvement.", "2026-05-19T13:46:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Orthopedic Pet Bed was not the right fit for our pet, but the product is easy to use.", "2026-06-22T14:53:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 4, "> Our dog finally settles comfortably in the new bed.\n\nThe washable cover is a big help.", "2026-02-13T13:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Cat Scratching Post has been useful for our pet's daily routine.", "2026-07-25T15:00:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 5, "Grooming Brush Set has been useful for our pet's daily routine.", "2026-01-08T09:21:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "Grooming Brush Set works as expected, although there is room for improvement.", "2026-02-11T10:28:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Portable Water Bottle has been useful for our pet's daily routine.", "2026-04-17T12:42:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "Portable Water Bottle works as expected, although there is room for improvement.", "2026-05-20T13:49:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 3, "Portable Water Bottle was not the right fit for our pet, but the product is easy to use.", "2026-06-23T14:56:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Heated Pet Blanket has been useful for our pet's daily routine.", "2026-07-26T15:03:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Heated Pet Blanket works as expected, although there is room for improvement.", "2026-08-03T16:10:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Heated Pet Blanket was not the right fit for our pet, but the product is easy to use.", "2026-09-06T17:17:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 5, "The product is useful, though we noticed a few small limitations.", "2026-06-21T17:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017", "9fe8a36a-36f3-4198-a5cc-d96371083493", 5, "## Food transition\n\nThe kibble was easy to introduce over one week.", "2026-01-09T09:24:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Indoor Cat Formula has been useful for our pet's daily routine.", "2026-04-18T12:45:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Indoor Cat Formula works as expected, although there is room for improvement.", "2026-05-21T13:52:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Kitten Starter Food has been useful for our pet's daily routine.", "2026-07-01T15:06:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Kitten Starter Food works as expected, although there is room for improvement.", "2026-08-04T16:13:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "Kitten Starter Food was not the right fit for our pet, but the product is easy to use.", "2026-09-07T17:20:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Small Animal Timothy Hay has been useful for our pet's daily routine.", "2026-01-10T09:27:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Small Animal Timothy Hay works as expected, although there is room for improvement.", "2026-02-13T10:34:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "Small Animal Timothy Hay was not the right fit for our pet, but the product is easy to use.", "2026-03-16T11:41:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 2, "The product is useful, though we noticed a few small limitations.", "2026-01-03T13:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "**Feeding note:** start with a small portion.\n\nThe fish accepted it quickly.", "2026-04-19T12:48:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Parakeet Seed Blend has been useful for our pet's daily routine.", "2026-07-02T15:09:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Parakeet Seed Blend works as expected, although there is room for improvement.", "2026-08-05T16:16:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "9fe8a36a-36f3-4198-a5cc-d96371083493", 5, "Senior Cat Wet Food Pack has been useful for our pet's daily routine.", "2026-01-11T09:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 1, "Senior Cat Wet Food Pack works as expected, although there is room for improvement.", "2026-02-14T10:37:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 3, "Senior Cat Wet Food Pack was not the right fit for our pet, but the product is easy to use.", "2026-03-17T11:44:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Equine Electrolyte Supplement has been useful for our pet's daily routine.", "2026-04-20T12:51:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 1, "Equine Electrolyte Supplement works as expected, although there is room for improvement.", "2026-05-23T13:58:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 3, "Equine Electrolyte Supplement was not the right fit for our pet, but the product is easy to use.", "2026-06-26T14:05:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 4, "**Good for:** active horses.\n\nThe powder mixes easily with water.", "2026-05-11T17:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Digital Pet Scale has been useful for our pet's daily routine.", "2026-07-03T15:12:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Automatic Feeder has been useful for our pet's daily routine.", "2026-01-12T09:33:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Automatic Feeder works as expected, although there is room for improvement.", "2026-02-15T10:40:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Compact Pet Carrier has been useful for our pet's daily routine.", "2026-04-21T12:54:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Compact Pet Carrier works as expected, although there is room for improvement.", "2026-05-24T13:01:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "- Comfortable carrier\n- Secure latch\n\n**Good for short trips.**", "2026-06-01T14:08:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "Aquarium Water Test Kit has been useful for our pet's daily routine.", "2026-07-04T15:15:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 1, "Aquarium Water Test Kit works as expected, although there is room for improvement.", "2026-08-07T16:22:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "9fe8a36a-36f3-4198-a5cc-d96371083493", 3, "Aquarium Water Test Kit was not the right fit for our pet, but the product is easy to use.", "2026-09-10T17:29:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "The product is useful, though we noticed a few small limitations.", "2026-09-19T13:30:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "810440e8-cae0-48f7-aa4f-830239b82b78", 5, "LED Examination Light has been useful for our pet's daily routine.", "2026-01-13T09:36:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "584c5329-2b9a-43d4-ad2b-debaa92d6c02", 5, "**Quiet motor** and a useful speed setting for careful trimming.", "2026-04-22T12:57:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030", "9fe8a36a-36f3-4198-a5cc-d96371083493", 1, "Pet Nail Grinder works as expected, although there is room for improvement.", "2026-05-25T13:04:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Foldable Exercise Pen has been useful for our pet's daily routine.", "2026-07-05T15:18:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Foldable Exercise Pen works as expected, although there is room for improvement.", "2026-08-08T16:25:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Foldable Exercise Pen was not the right fit for our pet, but the product is easy to use.", "2026-09-11T17:32:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", 5, "Canine First Aid Kit has been useful for our pet's daily routine.", "2026-01-14T09:39:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", 1, "Canine First Aid Kit works as expected, although there is room for improvement.", "2026-02-17T10:46:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", 3, "Canine First Aid Kit was not the right fit for our pet, but the product is easy to use.", "2026-03-20T11:53:00"),
                seedRating("b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", 2, "The product is useful, though we noticed a few small limitations.", "2026-04-01T17:30:00")
        ));

        List<Rating> seededRatings = new ArrayList<>(List.of(
                        rating1prod1, rating2prod1, rating3prod1,
                        rating1prod2, rating2prod2, rating3prod2,
                        rating1prod3, rating2prod3, rating3prod3,
                        rating1prod4, rating2prod4, rating3prod4,
                        rating1prod5, rating2prod5, rating3prod5,
                        rating1prod6, rating2prod6, rating3prod6,
                        rating1prod7, rating2prod7, rating3prod7,
                        rating1prod8, rating2prod8, rating3prod8
                ));
        seededRatings.addAll(additionalRatingSeeds);
        // Demo fixtures only: vary category scores without changing overall ratings.
        // saveSeededRatings skips existing rows, including customer-edited reviews.
        int[][] categoryProfiles = {
                {5, 4, 5, 4}, {4, 3, 4, 5}, {3, 4, 2, 3}, {2, 3, 4, 2}
        };
        for (int index = 0; index < seededRatings.size(); index++) {
            Rating seed = seededRatings.get(index);
            int[] scores = categoryProfiles[index % categoryProfiles.length];
            seed.setQualityRating((byte) scores[0]);
            seed.setValueRating((byte) scores[1]);
            seed.setPetSatisfactionRating((byte) scores[2]);
            seed.setIngredientsRating((byte) scores[3]);
        }

        List<ProductTypeDb> seededProductTypes = List.of(
                productType1, productType2, productType3, productType4);
        Map<String, Product> productsById = seededProducts.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        List<ProductBundle> seededBundles = List.of(
                bundle1, bundle2, bundle3,
                seedBundle(productsById, "Saddle Special", "Horse Saddle on sale", 179.99,
                        "a6a27433-e7a9-4e78-8ae3-0cb57d756863"),
                seedBundle(productsById, "Aquarium Starter Kit", "Filter, heater & fish flakes for a new tank", 37.99,
                        "4d508fb7-f1f2-4952-829d-10dd7254cf26", "1501f30e-1db1-44b2-a555-bca6f64450e4",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7021"),
                seedBundle(productsById, "New Kitten Bundle", "Everything a new kitten needs at home", 89.99,
                        "98f7b33a-d62a-420a-a84a-05a27c85fc91", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7019",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006"),
                seedBundle(productsById, "Dog Walk & Care Bundle", "Food, walking gear and grooming for dogs", 124.99,
                        "06a7d573-bcab-4db3-956f-773324b92a80", "baee7cd2-b67a-449f-b262-91f45dde8a6d",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7010", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014"),
                seedBundle(productsById, "Senior Pet Comfort Bundle", "Comfort, health and monitoring for aging pets", 279.99,
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7001", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7023",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030"),
                seedBundle(productsById, "Small Animal & Bird Bundle", "Housing, food and accessories for rabbits and birds", 379.99,
                        "ae2d3af7-f2a2-407f-ad31-ca7d8220cb7a", "4affcab7-3ab1-4917-a114-2b6301aa5565",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7020", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7022",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7009",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7015", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7014",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030"),
                seedBundle(productsById, "Home Vet Care Bundle", "Treatments and tools for at-home pet health care", 239.99,
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7002", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7003",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7004", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7005",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7006", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7007",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7008", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7011",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7029", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032",
                        "baee7cd2-b67a-449f-b262-91f45dde8a6d", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7025",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7030"),
                seedBundle(productsById, "Ultimate Pet Owner Bundle", "Our biggest bundle: food, gear and equipment for every pet", 599.99,
                        "06a7d573-bcab-4db3-956f-773324b92a80", "98f7b33a-d62a-420a-a84a-05a27c85fc91",
                        "4d508fb7-f1f2-4952-829d-10dd7254cf26", "4affcab7-3ab1-4917-a114-2b6301aa5565",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7012", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7013",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7016", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7017",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7018", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7024",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7026", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7027",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7028", "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7031",
                        "b2d7a3c1-4e5f-4a61-8b72-9c3d1e5f7032"));

        seedData(seededProductTypes, seededImages, seededProducts, seededRatings, seededBundles)
                .then(backfillSeedTranslations())
                .block(java.time.Duration.ofMinutes(10));
    }

    // Only missing translations of unchanged seed text are filled on existing databases.
    private Mono<Void> backfillSeedTranslations() {
        return Flux.fromIterable(productTranslations())
                .concatMap(seed -> productRepository.backfillFrenchName(seed.productId(), seed.name(), seed.nameFr())
                        .then(productRepository.backfillFrenchDescription(seed.productId(), seed.description(), seed.descriptionFr())))
                .then();
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

    private Flux<Void> seedData(
            List<ProductTypeDb> productTypes,
            List<Image> images,
            List<Product> products,
            List<Rating> ratings,
            List<ProductBundle> bundles) {
        return saveSeededProductTypes(productTypes)
                .then(saveSeededProducts(products, images))
                .then(saveSeededRatings(ratings))
                .thenMany(saveSeededBundles(bundles));
    }

    private Mono<Void> saveSeededProductTypes(List<ProductTypeDb> productTypes) {
        return Flux.fromIterable(productTypes)
                .concatMap(productType -> safeMono(productTypeRepository
                        .findByProductTypeId(productType.getProductTypeId()))
                        .hasElement()
                        .flatMap(exists -> exists
                                ? Mono.empty()
                                : safeMono(productTypeRepository.save(productType)).then()))
                .then();
    }

    private Mono<Void> saveSeededProducts(List<Product> products, List<Image> images) {
        Map<String, Image> seedImages = images.stream().filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(Image::getImageId, image -> image));
        return Flux.fromIterable(products)
                .concatMap(product -> productExists(product).flatMap(exists -> {
                    if (exists) return Mono.empty();
                    Image seed = seedImages.get(product.getImageId());
                    Mono<Product> prepared = seed == null ? Mono.just(product)
                            : imagePublisher.publishSeed(seed).map(fileId -> {
                                product.setImageId(fileId);
                                return product;
                            });
                    return prepared.flatMap(productRepository::save).then();
                }))
                .then();
    }

    private Mono<Boolean> productExists(Product product) {
        Mono<Boolean> byId = product.getProductId() == null
                ? Mono.just(false)
                : safeMono(productRepository.findProductByProductId(product.getProductId()))
                        .hasElement();

        return byId.flatMap(exists -> exists
                ? Mono.just(true)
                : safeFlux(productRepository
                        .findByProductNameContainingIgnoreCase(product.getProductName()))
                        .filter(existing -> product.getProductName().equals(existing.getProductName()))
                        .hasElements());
    }

    private Mono<Void> saveSeededRatings(List<Rating> ratings) {
        return Flux.fromIterable(ratings)
                .concatMap(rating -> safeMono(productRepository
                        .findProductByProductId(rating.getProductId()))
                        .hasElement()
                        .flatMap(productExists -> productExists
                                ? safeMono(ratingRepository.findRatingByCustomerIdAndProductId(
                                        rating.getCustomerId(), rating.getProductId()))
                                        .hasElement()
                                        .flatMap(ratingExists -> ratingExists
                                                ? Mono.empty()
                                                : safeMono(ratingRepository.save(rating)).then())
                                : Mono.empty()))
                .then();
    }

    private Mono<Void> saveSeededBundles(List<ProductBundle> bundles) {
        return Flux.fromIterable(bundles)
                .concatMap(bundle -> bundleProductsExist(bundle)
                        .flatMap(productsExist -> productsExist
                                ? bundleExists(bundle)
                                        .flatMap(bundleExists -> bundleExists
                                                ? Mono.empty()
                                                : safeMono(productBundleRepository.save(bundle)).then())
                                : Mono.empty()))
                .then();
    }

    private Mono<Boolean> bundleProductsExist(ProductBundle bundle) {
        return safeFlux(productRepository.findAllByProductIdIn(bundle.getProductIds()))
                .map(Product::getProductId)
                .collectList()
                .map(existingIds -> Set.copyOf(existingIds)
                        .containsAll(bundle.getProductIds()));
    }

    private Mono<Boolean> bundleExists(ProductBundle bundle) {
        return safeMono(productBundleRepository.findByBundleId(bundle.getBundleId()))
                .hasElement()
                .flatMap(existsById -> existsById
                        ? Mono.just(true)
                        : bundle.getProductIds().isEmpty()
                                ? Mono.just(false)
                                : safeFlux(productBundleRepository.findAllByProductIdsContaining(
                                                bundle.getProductIds().get(0)))
                                        .filter(existing -> bundle.getBundleName().equals(existing.getBundleName())
                                                && bundle.getProductIds().equals(existing.getProductIds()))
                                        .hasElements());
    }

    private <T> Mono<T> safeMono(Mono<T> publisher) {
        return publisher == null ? Mono.empty() : publisher;
    }

    private <T> Flux<T> safeFlux(Flux<T> publisher) {
        return publisher == null ? Flux.empty() : publisher;
    }

    private Rating seedRating(
            String productId,
            String customerId,
            int stars,
            String review,
            String createdAt) {
        return Rating.builder()
                .productId(productId)
                .customerId(customerId)
                .rating((byte) stars)
                .review(review)
                .createdAt(LocalDateTime.parse(createdAt))
                .build();
    }

    private ProductBundle seedBundle(
            Map<String, Product> productsById,
            String bundleName,
            String bundleDescription,
            double bundlePrice,
            String... productIds) {
        double originalTotalPrice = Arrays.stream(productIds)
                .mapToDouble(productId -> productsById.get(productId).getProductSalePrice())
                .sum();

        return ProductBundle.builder()
                .bundleId(UUID.randomUUID().toString())
                .bundleName(bundleName)
                .bundleDescription(bundleDescription)
                .productIds(List.of(productIds))
                .originalTotalPrice(Math.round(originalTotalPrice * 100) / 100.0)
                .bundlePrice(bundlePrice)
                .build();
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


}
