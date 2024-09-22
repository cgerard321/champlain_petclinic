package com.petclinic.inventoryservice.presentationlayer;

import com.petclinic.inventoryservice.businesslayer.SupplyInventoryService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/inventoryV2")
public class InventoryControllerV2 {
    @Autowired
    private SupplyInventoryService supplyInventoryService;

    @PostMapping("/{inventoryId}/supplies")
    public Mono<ResponseEntity<SupplyResponseDTO>> addSupplyToInventory(@RequestBody Mono<SupplyRequestDTO> newSupply, @PathVariable String inventoryId){
        return supplyInventoryService.addSupplyToInventory(newSupply, inventoryId)
                .map(supplyResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(supplyResponseDTO))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{inventoryId}/supplies/{supplyId}")
    public Mono<ResponseEntity<Void>> deleteSupplyToInventory(@PathVariable String inventoryId, @PathVariable String supplyId){
        return supplyInventoryService.deleteSupplyInInventory(inventoryId, supplyId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
    @PostMapping()
    public Mono<ResponseEntity<InventoryResponseDTO>> addInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO){
        return supplyInventoryService.addInventory(inventoryRequestDTO)
                .map(s -> ResponseEntity.status(HttpStatus.CREATED).body(s))
                .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
    }

    @GetMapping("/{inventoryId}/supplies")
    public Flux<SupplyResponseDTO>
    getSuppliesInInventoryByInventoryIdAndSupplyField(@PathVariable String inventoryId,
                                                       @RequestParam(required = false) String supplyName,
                                                       @RequestParam(required = false) Double supplyPrice,
                                                       @RequestParam(required = false) Integer supplyQuantity,
                                                       @RequestParam(required = false) Double supplySalePrice

    ){
        return supplyInventoryService.getSuppliesInInventoryByInventoryIdAndSuppliesField(inventoryId, supplyName, supplyPrice, supplyQuantity, supplySalePrice);
    }

    @GetMapping("/{inventoryId}/supplies-pagination")
    public Flux<SupplyResponseDTO> getSuppliesInInventoryByInventoryIdAndSupplyFieldPagination(@PathVariable String inventoryId,
                                                                                                 @RequestParam(required = false) String supplyName,
                                                                                                 @RequestParam(required = false) Double supplyPrice,
                                                                                                 @RequestParam(required = false) Integer supplyQuantity,
                                                                                                 @RequestParam Optional<Integer> page,
                                                                                                 @RequestParam Optional<Integer> size){
        return supplyInventoryService.getSuppliesInInventoryByInventoryIdAndSuppliesFieldsPagination(inventoryId, supplyName, supplyPrice, supplyQuantity, PageRequest.of(page.orElse(0),size.orElse(5)));
    }

    @GetMapping("/{inventoryId}/supplies-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfSuppliesWithRequestParams(@PathVariable String inventoryId,
                                                                                @RequestParam(required = false) String supplyName,
                                                                                @RequestParam(required = false) Double supplyPrice,
                                                                                @RequestParam(required = false) Integer supplyQuantity,
                                                                                @RequestParam(required = false) Double supplySalePrice){
        return supplyInventoryService.getSuppliesInInventoryByInventoryIdAndSuppliesField(inventoryId, supplyName, supplyPrice, supplyQuantity, supplySalePrice).count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/{inventoryId}/supplies/{supplyId}")
    public Mono<ResponseEntity<SupplyResponseDTO>>getSupplyBySupplyIdInInventory(@PathVariable String inventoryId, @PathVariable String supplyId){
        return supplyInventoryService.getSupplyBySupplyIdInInventory(inventoryId,supplyId)
                .map(i -> ResponseEntity.status(HttpStatus.OK).body(i))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @GetMapping()
    public Flux<InventoryResponseDTO> searchInventories(
            @RequestParam Optional<Integer> page, @RequestParam Optional<Integer> size,
            @RequestParam(name = "inventoryName", required = false) String inventoryName,
            @RequestParam(name = "inventoryType", required = false) String inventoryType,
            @RequestParam(name = "inventoryDescription", required = false) String inventoryDescription) {

        return supplyInventoryService.searchInventories(PageRequest.of(page.orElse(0),size.orElse(10)), inventoryName, inventoryType, inventoryDescription);
    }


    @GetMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> getInventoryById(@PathVariable String inventoryId){
        return supplyInventoryService.getInventoryById(inventoryId)
                .map(i -> ResponseEntity.status(HttpStatus.OK).body(i))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }





    @PutMapping("/{inventoryId}")
    public Mono<ResponseEntity<InventoryResponseDTO>> updateInventory(@RequestBody Mono<InventoryRequestDTO> inventoryRequestDTO, @PathVariable String inventoryId) {
        return supplyInventoryService.updateInventory(inventoryRequestDTO, inventoryId)
                .map(updatedStudent -> ResponseEntity.status(HttpStatus.OK).body(updatedStudent))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    //delete all supplies and delete all inventory
    @DeleteMapping("/{inventoryId}/supplies")
    public Mono<ResponseEntity<Void>> deleteSupplyInventory(@PathVariable String inventoryId) {
        return supplyInventoryService.deleteAllSupplyInventory(inventoryId)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping()
    public Mono<ResponseEntity<Void>> deleteAllInventories() {
        return supplyInventoryService.deleteAllInventory()
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.BAD_REQUEST));
    }





    @PutMapping("/{inventoryId}/supplies/{supplyId}")
    public Mono<ResponseEntity<SupplyResponseDTO>> updateSupplyInInventory(@RequestBody Mono<SupplyRequestDTO> supplyRequestDTOMono,
                                                                             @PathVariable String inventoryId,
                                                                             @PathVariable String supplyId){
        return supplyInventoryService.updateSupplyInInventory(supplyRequestDTOMono, inventoryId, supplyId)
                .map(supplyResponseDTO -> ResponseEntity.ok().body(supplyResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{inventoryId}")
    public Mono<ResponseEntity<Void>> deleteInventoryByInventoryId(@PathVariable String inventoryId){
        return supplyInventoryService.deleteInventoryByInventoryId(inventoryId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @PostMapping("/type")
    public Mono<ResponseEntity<InventoryTypeResponseDTO>> addInventoryType(@RequestBody Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO){
        return supplyInventoryService.addInventoryType(inventoryTypeRequestDTO)
                .map(inventoryTypeResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryTypeResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/type")
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes(){
        return supplyInventoryService.getAllInventoryTypes();
    }

    @GetMapping("/name")
    public Flux<InventoryNameResponseDTO> getAllInventoryNames(){
        return supplyInventoryService.getAllInventoryNames();
    }



    @PostMapping("/{inventoryName}/supplies")
    public Mono<ResponseEntity<InventoryResponseDTO>> addSupplyToInventoryByName(
            @PathVariable String inventoryName,
            @RequestBody Mono<SupplyRequestDTO> supplyRequestDTO) {
        return supplyInventoryService.addSupplyToInventoryByInventoryName(inventoryName, supplyRequestDTO)
                .map(inventoryResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(inventoryResponseDTO))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

}

