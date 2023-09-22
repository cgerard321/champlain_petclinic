package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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

    @DeleteMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteProductToInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return productInventoryService.deleteProductInInventory(inventoryId, productId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
    @PostMapping()
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO){
        return productInventoryService.addInventory(inventoryRequestDTO)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    @GetMapping("/{inventoryId}/products")
    public Flux<ProductResponseDTO>
    getProductsInInventoryByInventoryIdAndProductField(@PathVariable String inventoryId,
                                                       @RequestParam(required = false) String productName,
                                                       @RequestParam(required = false) Double productPrice,
                                                       @RequestParam(required = false) Integer productQuantity){
        return productInventoryService.getProductsInInventoryByInventoryIdAndProductsField(inventoryId, productName, productPrice, productQuantity);
    }

    @GetMapping()
    public Flux<InventoryResponseDTO> getAllInventory(){
        return productInventoryService.getAllInventory();
    }
    @PutMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO, @PathVariable String inventoryId) {
        return productInventoryService.updateInventory(inventoryRequestDTO, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO, @PathVariable String inventoryId) {
        return productInventoryService.updateInventory(inventoryRequestDTO, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
