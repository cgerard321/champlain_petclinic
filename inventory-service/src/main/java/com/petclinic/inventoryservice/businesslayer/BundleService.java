package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.BundleDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BundleService {
    Mono<BundleDTO> GetBundle(@RequestParam(value = "bundleUUID", required = true)String bundleUUID);
    Flux<BundleDTO> GetAllBundles();

    Mono<BundleDTO> CreateBundle(@RequestBody Mono<BundleDTO> model);

    Mono<Void> DeleteBundle(@RequestParam(value = "bundleUUID", required = true) String bundleUUID);

    Flux<BundleDTO> GetBundlesByItem(@RequestParam(value = "item", required = true) String item);
    //Flux<BundleDTO> GetNonEmptyBundles(@RequestParam(value = "quantity", required = true) int quantity);
}
