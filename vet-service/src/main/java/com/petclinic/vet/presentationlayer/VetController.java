package com.petclinic.vet.presentationlayer;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 202
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.petclinic.vet.servicelayer.VetDTO;
import com.petclinic.vet.servicelayer.VetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("vets")
public class VetController {

    @Autowired
    VetService vetService;

    @GetMapping()
    public Flux<VetDTO> getAllVets() {
        return vetService.getAll();
    }
    @GetMapping("{vetIdString}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetId(@PathVariable String vetIdString) {
        //System.out.println("getVetByVetId, VetController in Vet-Service");
        return vetService.getVetByVetId(vetIdString)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<VetDTO> insertVet(@RequestBody Mono<VetDTO> vetDTOMono) {
        return vetService.insertVet(vetDTOMono);
    }

    @PutMapping("{vetIdString}")
    public Mono<ResponseEntity<VetDTO>> updateVetByVetId(@PathVariable String vetIdString, @RequestBody Mono<VetDTO> vetDTOMono) {
        return vetService.updateVet(vetIdString, vetDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{vetIdString}")
    public Mono<Void> deleteVet(@PathVariable String vetIdString) {
        return vetService.deleteVet(vetIdString);
    }


}
