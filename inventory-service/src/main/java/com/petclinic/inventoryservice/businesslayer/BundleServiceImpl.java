package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.BundleDTO;
import com.petclinic.inventoryservice.datalayer.BundleRepository;
import com.petclinic.inventoryservice.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.exceptions.NotFoundException;
import com.petclinic.inventoryservice.utils.EntityDTOUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BundleServiceImpl implements BundleService{
    private final BundleRepository bundleRepository;

    public BundleServiceImpl(BundleRepository bundleRepository) {
        this.bundleRepository = bundleRepository;
    }
    @Override
    public Mono<BundleDTO> GetBundle(String bundleUUID) {
        return bundleRepository.findByBundleUUID(bundleUUID).map(EntityDTOUtil::toDto);
    }
    @Override
    public Flux<BundleDTO> GetAllBundles() {
        return bundleRepository.findAll().map(EntityDTOUtil::toDto);
    }
    @Override
    public Flux<BundleDTO> GetBundlesByItem(String item) {
        return bundleRepository.findBundlesByItem(item).map(EntityDTOUtil::toDto);
    }
    @Override
    public Mono<BundleDTO> CreateBundle(Mono<BundleDTO> model) {
        return model
                .map(EntityDTOUtil::toEntity)
                .doOnNext(e -> e.setBundleUUID(EntityDTOUtil.generateUUID()))
                .flatMap(bundleRepository::insert)
                .map(EntityDTOUtil::toDto);
    }
    @Override
    public Mono<Void> DeleteBundle(String bundleUUID) {
        return bundleRepository.deleteBundleByBundleUUID(bundleUUID);
    }
}
