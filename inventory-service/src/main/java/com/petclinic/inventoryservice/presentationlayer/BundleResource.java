package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.BundleService;
import com.petclinic.inventoryservice.datalayer.BundleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.validation.Valid;

@RestController
@Slf4j
public class BundleResource {
    private final BundleService SERVICE;

    BundleResource(BundleService service){
        this.SERVICE = service;
    }

    @PostMapping("/bundles")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BundleDTO> createBundle(@Valid @RequestBody Mono<BundleDTO> bundleDTO) {
        return SERVICE.CreateBundle(bundleDTO);
    }

    @GetMapping(value = "/bundles/{bundleUUID}")
    public Mono<BundleDTO> findBundle(@PathVariable("bundleUUID") String bundleUUID) {
        return SERVICE.GetBundle(bundleUUID);
    }

    @GetMapping(value = "/bundles")
    public Flux<BundleDTO> findAllBundles() {
        return SERVICE.GetAllBundles();
    }
    @GetMapping(value = "/bundles/item/{item}")
    public Flux<BundleDTO> findBundlesByItem(@PathVariable("item") String item) {
        return SERVICE.GetBundlesByItem(item);
    }
    @DeleteMapping(value = "/bundles/{bundleUUID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBundle(@PathVariable("bundleUUID") String bundleUUID) {
        return SERVICE.DeleteBundle(bundleUUID);
    }
}

