package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.datalayer.Bundle;
import com.petclinic.inventoryservice.datalayer.BundleRepository;
import com.petclinic.inventoryservice.http.HttpErrorInfo;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import static reactor.core.publisher.Mono.just;
import java.util.Calendar;
import java.util.Date;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.junit.jupiter.api.Assertions.*;


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BundleResourceIntegrationTest {
    @Autowired
    private WebTestClient client;

    @Autowired
    private BundleRepository repo;


    @Test
    void findAllBundles() {

        Bundle bundleEntity = buildBundle();

        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(bundleEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bundles")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].item").isEqualTo(bundleEntity.getItem())
                .jsonPath("$[0].quantity").isEqualTo(bundleEntity.getQuantity());


    }

    @Test
    void createBundle() {

        Bundle bundleEntity = buildBundle();

        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(bundleEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.post()
                .uri("/bundles")
                .body(just(bundleEntity), Bundle.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        client.get()
                .uri("/bundles/" + bundleEntity.getBundleUUID())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.item").isEqualTo(bundleEntity.getItem())
                .jsonPath("$.quantity").isEqualTo(bundleEntity.getQuantity());
    }

    @Test
    void findBundleByValidBundleUUID() {

        Bundle bundleEntity = buildBundle();

        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(bundleEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bundles/" + bundleEntity.getBundleUUID())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.item").isEqualTo(bundleEntity.getItem())
                .jsonPath("$.quantity").isEqualTo(bundleEntity.getQuantity());

    }


    @Test
    void getBundlesByItem() {

        Bundle bundleEntity = buildBundle();

        Publisher<Bundle> setup = repo.deleteAll().thenMany(repo.save(bundleEntity));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();

        client.get()
                .uri("/bundles/item/" + bundleEntity.getItem())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].item").isEqualTo(bundleEntity.getItem())
                .jsonPath("$[0].quantity").isEqualTo(bundleEntity.getQuantity());
    }
    @Test
    void deleteBundleByBundleUUID() {

        Bundle bundleEntity = buildBundle();

        repo.save(bundleEntity);

        Publisher<Void> setup = repo.deleteBundleByBundleUUID(bundleEntity.getBundleUUID());

        StepVerifier.create(setup)
                .expectNextCount(0)
                .verifyComplete();

        client.delete()
                .uri("/bundles/" + bundleEntity.getBundleUUID())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();


    }
    private Bundle buildBundle(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.SEPTEMBER, 25);
        Date date = calendar.getTime();


        return Bundle.builder().id("Id").bundleUUID("BundleUUID").item("item").quantity(25).expiryDate(date).build();
    }
}
