package com.petclinic.inventoryservice.utils;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryType;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class InventoryValidator {

    private final InventoryRepository inventoryRepository;
    private final InventoryTypeRepository inventoryTypeRepository;
    private final ProductRepository productRepository;

    public Mono<Inventory> validateInventory(Inventory e) {
      
        if (e.getInventoryName() != null) e.setInventoryName(e.getInventoryName().trim());
        if (e.getInventoryType() != null) e.setInventoryType(e.getInventoryType().trim());
        if (e.getInventoryDescription() != null) e.setInventoryDescription(e.getInventoryDescription().trim());
        if (e.getInventoryImage() != null) e.setInventoryImage(e.getInventoryImage().trim());
        if (e.getInventoryBackupImage() != null) e.setInventoryBackupImage(e.getInventoryBackupImage().trim());

        // 400s
        if (e.getInventoryName() == null || e.getInventoryName().isBlank())
            return Mono.error(new InvalidInputException("Inventory name is required."));
        if (e.getInventoryName().length() < 3)
            return Mono.error(new InvalidInputException("Invalid name must be at least 3 characters long."));
        if (e.getInventoryType() == null || e.getInventoryType().isBlank())
            return Mono.error(new InvalidInputException("Inventory type cannot be blank."));
        if (e.getInventoryDescription() == null || e.getInventoryDescription().isBlank())
            return Mono.error(new InvalidInputException("Inventory description is required."));
        if (hasText(e.getInventoryImage()) && !looksLikeHttpUrl(e.getInventoryImage()))
            return Mono.error(new InvalidInputException("Inventory image must be a valid URL (http/https)."));
        if (hasText(e.getInventoryBackupImage()) && !looksLikeHttpUrl(e.getInventoryBackupImage()))
            return Mono.error(new InvalidInputException("Inventory backup image must be a valid URL (http/https)."));
        if (e.getImageUploaded() != null && e.getImageUploaded().length > 160 * 1024)
            return Mono.error(new InvalidInputException("Uploaded image must be 160KB or smaller."));

        // 422 duplicate name
        return inventoryRepository.existsByInventoryName(e.getInventoryName())
                .flatMap(exists -> exists
                        ? Mono.error(new UnprocessableEntityException("Inventory name already exists."))
                        : Mono.just(e));
    }

    public Mono<InventoryType> validateInventoryType(InventoryType t) {
        if (t.getType() != null) t.setType(t.getType().trim());

        if (t.getType() == null || t.getType().isBlank())
            return Mono.error(new InvalidInputException("Type name is required."));
        if (t.getType().length() < 3)
            return Mono.error(new InvalidInputException("Type name must be at least 3 characters."));

        return inventoryTypeRepository.existsByTypeIgnoreCase(t.getType())
                .flatMap(exists -> exists
                        ? Mono.error(new UnprocessableEntityException("Inventory type already exists."))
                        : Mono.just(t));
    }

    private boolean hasText(String s) { return s != null && !s.trim().isEmpty(); }
    private boolean looksLikeHttpUrl(String url) {
        try {
            var u = java.net.URI.create(url);
            var scheme = u.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && u.getHost() != null;
        } catch (Exception ex) { return false; }
    }
    public Mono<Inventory> validateInventoryForUpdate(Inventory e, String currentId) {


        if (e.getInventoryName() != null) e.setInventoryName(e.getInventoryName().trim());
        if (e.getInventoryType() != null) e.setInventoryType(e.getInventoryType().trim());
        if (e.getInventoryDescription() != null) e.setInventoryDescription(e.getInventoryDescription().trim());
        if (e.getInventoryImage() != null) e.setInventoryImage(e.getInventoryImage().trim());
        if (e.getInventoryBackupImage() != null) e.setInventoryBackupImage(e.getInventoryBackupImage().trim());


        if (e.getInventoryName() == null || e.getInventoryName().isBlank())
            return Mono.error(new InvalidInputException("Inventory name is required."));
        if (e.getInventoryName().length() < 3)
            return Mono.error(new InvalidInputException("Invalid name must be at least 3 characters long."));
        if (e.getInventoryType() == null || e.getInventoryType().isBlank())
            return Mono.error(new InvalidInputException("Inventory type cannot be blank."));
        if (e.getInventoryDescription() == null || e.getInventoryDescription().isBlank())
            return Mono.error(new InvalidInputException("Inventory description is required."));
        if (hasText(e.getInventoryImage()) && !looksLikeHttpUrl(e.getInventoryImage()))
            return Mono.error(new InvalidInputException("Inventory image must be a valid URL (http/https)."));
        if (hasText(e.getInventoryBackupImage()) && !looksLikeHttpUrl(e.getInventoryBackupImage()))
            return Mono.error(new InvalidInputException("Inventory backup image must be a valid URL (http/https)."));
        if (e.getImageUploaded() != null && e.getImageUploaded().length > 160 * 1024)
            return Mono.error(new InvalidInputException("Uploaded image must be 160KB or smaller."));

        return inventoryRepository
                .existsByInventoryNameAndInventoryIdNot(e.getInventoryName(), currentId)
                .flatMap(exists -> exists
                        ? Mono.error(new UnprocessableEntityException("Inventory name already exists."))
                        : Mono.just(e));
    }

    private Mono<Product> validateProductFields(Product p) {
        if (p.getProductName() != null) p.setProductName(p.getProductName().trim());

        if (p.getProductName() == null || p.getProductName().isBlank())
            return Mono.error(new InvalidInputException("Product name is required."));
        if (p.getProductPrice() == null || p.getProductPrice() <= 0)
            return Mono.error(new InvalidInputException("Product price must be greater than 0."));
        if (p.getProductQuantity() == null || p.getProductQuantity() <= 0)
            return Mono.error(new InvalidInputException("Product quantity must be greater than 0."));
        if (p.getProductSalePrice() != null && p.getProductSalePrice() <= 0)
            return Mono.error(new InvalidInputException("Product sale price must be greater than 0."));

        return Mono.just(p);
    }

    public Mono<Product> validateProductForUpdate(Product p, String inventoryId, String productId) {
        return validateProductFields(p)
                .flatMap(valid ->
                        productRepository
                                .existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot(
                                        inventoryId, valid.getProductName(), productId)
                                .flatMap(dup -> dup
                                        ? Mono.error(new UnprocessableEntityException(
                                        "Product name already exists in this inventory."))
                                        : Mono.just(valid)
                                )
                );
    }

}
