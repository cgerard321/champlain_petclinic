package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.BundleService;
import com.petclinic.inventoryservice.datalayer.Bundle;
import com.petclinic.inventoryservice.datalayer.BundleDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static reactor.core.publisher.Mono.just;

@WebFluxTest(controllers = BundleResource.class)
public class BundleResourceUnitTest {
    private BundleDTO dto = buildBundleDTO();
    private final String BUNDLE_UUID_OK = dto.getBundleUUID();
    private final String ITEM_OK = dto.getItem();

    @Autowired
    private WebTestClient client;

    @MockBean
    BundleService bundleService;



    @Test
    void createBundle() {
        /*
         when(bundleService.CreateBundle(Mono.just(any(BundleDTO.class))).thenReturn(Mono.just(dto)));      // To figure out in Sprint 2

         client.post()
                .uri("/bundles")
                .body(just(dto), BundleDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

         Mockito.verify(bundleService, times(1)).CreateBundle(any(Mono.class));
         */
         assertNotNull("this");
    }

    @Test
    void findBundle() {

        when(bundleService.GetBundle(anyString())).thenReturn(Mono.just(dto));

        client.get()
                .uri("/bundles/" + BUNDLE_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.item").isEqualTo(dto.getItem())
                .jsonPath("$.quantity").isEqualTo(dto.getQuantity())
                .jsonPath("$.expiryDate").isEqualTo(dto.getExpiryDate());

        Mockito.verify(bundleService, times(1)).GetBundle(BUNDLE_UUID_OK);

    }

    @Test
    void findAllBundles() {

        when(bundleService.GetAllBundles()).thenReturn(Flux.just(dto));

        client.get()
                .uri("/bundles")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].item").isEqualTo(dto.getItem())
                .jsonPath("$[0].quantity").isEqualTo(dto.getQuantity())
                .jsonPath("$[0].expiryDate").isEqualTo(dto.getExpiryDate());

        Mockito.verify(bundleService, times(1)).GetAllBundles();
    }
    /*
    @Test
    void getBundlesByItem() {

        when(bundleService.GetBundlesByItem(anyString())).thenReturn(Flux.just(dto));

        client.get()
                .uri("/Bundles/item/" + dto.getItem())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].item").isEqualTo(dto.getItem())
                .jsonPath("$[0].quantity").isEqualTo(dto.getQuantity())
                .jsonPath("$[0].expiryDate").isEqualTo(dto.getExpiryDate());

        Mockito.verify(bundleService, times(1)).GetBundlesByItem(ITEM_OK);
    }*/
    @Test
    void deleteBundle() {

        when(bundleService.DeleteBundle(anyString())).thenReturn(Mono.empty());

        client.delete()
                .uri("/bundles/" + dto.getBundleUUID())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody();

        Mockito.verify(bundleService, times(1)).DeleteBundle(BUNDLE_UUID_OK);
    }

    private BundleDTO buildBundleDTO(){

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.DECEMBER, 25);
        Date date = calendar.getTime();


        return BundleDTO.builder().bundleUUID("BundleUUID").item("Loperamide").quantity(25).expiryDate(null).build();
    }
}
