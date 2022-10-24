package com.petclinic.inventoryservice.datalayer;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Calendar;
import java.util.Date;

@DataMongoTest
public class BundleServicePersistenceTests {
    @Autowired
    BundleRepository repo;



    @Test
    void shouldSaveOneBundle(){

        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(buildBundle()));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldFindBundleByBundleUUID(){

        Bundle bundle = buildBundle();

        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(buildBundle()));
        Publisher<Bundle> find = repo.findByBundleUUID(bundle.getBundleUUID());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();

    }


    @Test
    void shouldFindBundlesByItem(){

        Bundle bundle = buildBundle();

        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(buildBundle()));
        Publisher<Bundle> find = repo.findBundlesByItem(bundle.getItem());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();

    }
    @Test
    void shouldDeleteBundleByBundleUUID(){

        Bundle bundle = buildBundle();
        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(bundle));

        Publisher<Void> delete = repo.deleteBundleByBundleUUID(bundle.getBundleUUID());

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier
                .create(delete)
                .expectNextCount(0)
                .verifyComplete();
    }
    private Bundle buildBundle(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.DECEMBER, 25);
        Date date = calendar.getTime();


        return Bundle.builder().id("Id").bundleUUID("BundleUUID").item("item").quantity(25).expiryDate(date).build();
    }
}
