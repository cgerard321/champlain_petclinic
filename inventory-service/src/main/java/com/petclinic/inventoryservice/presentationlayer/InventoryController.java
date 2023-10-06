package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.ProductInventoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

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

    @GetMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>>getProductByProductIdInInventory(@PathVariable String inventoryId, @PathVariable String productId){
        return productInventoryService.getProductByProductIdInInventory(inventoryId,productId)
                .map(i -> ResponseEntity.status(HttpStatus.OK).body(i))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


@GetMapping()
public Flux<InventoryResponseDTO> searchInventories(
        @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size,
        @RequestParam(name = "inventoryName", required = false) String inventoryName,
        @RequestParam(name = "inventoryType", required = false) String inventoryType,
        @RequestParam(name = "inventoryDescription", required = false) String inventoryDescription) {

    return productInventoryService.searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)), inventoryName, inventoryType, inventoryDescription);
}


    @GetMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(@PathVariable String inventoryId){
        return productInventoryService.getInventoryById(inventoryId)
                .map(i -> ResponseEntity.status(HttpStatus.OK).body(i))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }





    @PutMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO, @PathVariable String inventoryId) {
        return productInventoryService.updateInventory(inventoryRequestDTO, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    //delete all products and delete all inventory
    @DeleteMapping("/{inventoryId}/products")
    public Mono<ResponseEntity<Void>> deleteProductInventory(@PathVariable String inventoryId) {
        return productInventoryService.deleteAllProductInventory(inventoryId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping()
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return productInventoryService.deleteAllInventory()
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST));
    }





    @PutMapping("/{inventoryId}/products/{productId}")
    public Mono<ResponseEntity<ProductResponseDTO>> updateProductInInventory(@RequestBody Mono<ProductRequestDTO> productRequestDTOMono,
                                                                             @PathVariable String inventoryId,
                                                                             @PathVariable String productId){
        return productInventoryService.updateProductInInventory(productRequestDTOMono, inventoryId, productId)
                .map(productResponseDTO -> ResponseEntity.ok().body(productResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{inventoryId}")
    public Mono<ResponseEntity<Void>> deleteInventoryByInventoryId(@PathVariable String inventoryId){
        return productInventoryService.deleteInventoryByInventoryId(inventoryId)
        .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping("/type")
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> addInventoryType(@RequestBody Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO){
        return productInventoryService.addInventoryType(inventoryTypeRequestDTO)
                .map(inventoryTypeResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryTypeResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}

