package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductInventoryService productInventoryService;

    @PostMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<ProductResponseDTO>> addProductToInventory(@RequestBody Mono<ProductRequestDTO> newProduct, @PathVariable String inventoryId){
        return productInventoryService.addProductToInventory(newProduct, inventoryId)
                .map(productResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(@RequestBody Mono<ProductRequestDTO> productRequestDTOMono, @PathVariable String inventoryId, @PathVariable String productId){
        return productInventoryService.updateProductInInventory(productRequestDTOMono, inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok().body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
