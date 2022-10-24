package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Bundle;
import com.petclinic.inventoryservice.datalayer.BundleDTO;
import com.petclinic.inventoryservice.datalayer.BundleRepository;
import com.petclinic.inventoryservice.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.exceptions.NotFoundException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Date;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class BundleServiceImplTest {
    @MockBean
    BundleRepository repo;


    @Autowired
    BundleService bundleService;

    @Test
    public void test_GetBundle(){
        Bundle bundleEntity = buildBundle();

        String bundleUUID = bundleEntity.getBundleUUID();

        when(repo.findByBundleUUID(anyString())).thenReturn(Mono.just(bundleEntity));

        Mono<BundleDTO> bundleDTOMono = bundleService.GetBundle(bundleUUID);

        StepVerifier.create(bundleDTOMono)
                .consumeNextWith(foundBundle -> {
                    assertEquals(bundleEntity.getBundleUUID(), foundBundle.getBundleUUID());
                    assertEquals(bundleEntity.getQuantity(), foundBundle.getQuantity());
                    assertEquals(bundleEntity.getExpiryDate(), foundBundle.getExpiryDate());
                    assertEquals(bundleEntity.getItem(), foundBundle.getItem());
                })
                .verifyComplete();


    }

    @Test
    public void test_GetAllBundles() {
        Bundle bundleEntity = buildBundle();

        when(repo.findAll()).thenReturn(Flux.just(bundleEntity));

        Flux<BundleDTO> bundleDTOFlux = bundleService.GetAllBundles();

        StepVerifier.create(bundleDTOFlux)
                .consumeNextWith(foundBundle -> {
                    assertNotNull(foundBundle);
                })
                .verifyComplete();
    }


    @Test
    public void test_CreateBundle(){

        Bundle bundleEntity = buildBundle();

        Mono<Bundle> billMono = Mono.just(bundleEntity);
        BundleDTO bundleDTO = buildBundleDTO();

        when(repo.insert(any(Bundle.class))).thenReturn(billMono);

        Mono<BundleDTO> returnedBundle = bundleService.CreateBundle(Mono.just(bundleDTO));

        StepVerifier.create(returnedBundle)
                .consumeNextWith(monoDTO -> {
                    assertEquals(bundleEntity.getBundleUUID(), monoDTO.getBundleUUID());
                    assertEquals(bundleEntity.getItem(), monoDTO.getItem());
                    assertEquals(bundleEntity.getQuantity(), monoDTO.getQuantity());
                    assertEquals(bundleEntity.getExpiryDate(), monoDTO.getExpiryDate());
                })
                .verifyComplete();

    }



    @Test
    public void test_DeleteBundle(){

        Bundle bundleEntity = buildBundle();

        when(repo.deleteBundleByBundleUUID(anyString())).thenReturn(Mono.empty());

        Mono<Void> deletedObj = bundleService.DeleteBundle(bundleEntity.getBundleUUID());

        StepVerifier.create(deletedObj)
                .expectNextCount(0)
                .verifyComplete();
    }


    @Test
    public void test_GetBundlesByItem(){

        Bundle bundleEntity = buildBundle();

        String item = bundleEntity.getItem();

        when(repo.findBundlesByItem(anyString())).thenReturn(Flux.just(bundleEntity));

        Flux<BundleDTO> bundleDTOMono = bundleService.GetBundlesByItem(item);

        StepVerifier.create(bundleDTOMono)
                .consumeNextWith(foundBundle -> {
                    assertEquals(bundleEntity.getBundleUUID(), foundBundle.getBundleUUID());
                    assertEquals(bundleEntity.getQuantity(), foundBundle.getQuantity());
                    assertEquals(bundleEntity.getExpiryDate(), foundBundle.getExpiryDate());
                })
                .verifyComplete();
    }
    private Bundle buildBundle(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.DECEMBER, 25);
        Date date = calendar.getTime();


        return Bundle.builder().id("Id").bundleUUID("bundleUUID").item("item").quantity(25).expiryDate(date).build();
    }

    private BundleDTO buildBundleDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.DECEMBER, 25);
        Date date = calendar.getTime();


        return BundleDTO.builder().bundleUUID("bundleUUID").item("item").quantity(25).expiryDate(date).build();
    }

}
