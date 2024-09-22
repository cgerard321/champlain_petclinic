package com.petclinic.inventoryservice.datalayer.Supply;

import org.junit.jupiter.api.BeforeEach;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

@DataMongoTest
class SupplyRepositoryTest {
    @Autowired
    SupplyRepository supplyRepository;

    Supply supply1;
    Supply supply2;

    @BeforeEach
    public void setupDB() {
        supply1 = buildSupply("inventoryId_4", "supplyId_1", "Desc", "Sedative Medications", 100.00, 10, 10.00);
        supply2 = buildSupply("inventoryId_4", "supplyId_2", "Desc", "Anxiety Relief Tablets", 150.00, 10, 15.00);

        Publisher<Supply> setup1 = supplyRepository.deleteAll()
                .then(supplyRepository.save(supply1));

        Publisher<Supply> setup2 = supplyRepository.save(supply2);

        StepVerifier
                .create(setup1)
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier
                .create(setup2)
                .expectNextCount(1)
                .verifyComplete();
    }

    private Supply buildSupply(String inventoryId, String supplyId, String supplyDescription, String supplyName, double supplyPrice, int supplyQuantity, double supplySalePrice) {
        return Supply.builder()
                .inventoryId(inventoryId)
                .supplyId(supplyId)
                .supplyDescription(supplyDescription)
                .supplyName(supplyName)
                .supplyPrice(supplyPrice)
                .supplyQuantity(supplyQuantity)
                .supplySalePrice(supplySalePrice)
                .status(Status.AVAILABLE)
                .build();
    }
}
