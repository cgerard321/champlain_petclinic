package com.petclinic.vet.exceptions.presentationlayer;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.petclinic.vet.servicelayer.VetDTO;
import com.petclinic.vet.servicelayer.VetService;
import com.petclinic.vet.util.EntityDtoUtil;
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

    @GetMapping("{vetId}")
    public Mono<ResponseEntity<VetDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetService.getVetByVetId(EntityDtoUtil.verifyId(vetId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public Flux<VetDTO> getActiveVets() {
        return vetService.getVetByIsActive(true);
    }

    @GetMapping("/inactive")
    public Flux<VetDTO> getInactiveVets() {
        return vetService.getVetByIsActive(false);
    }

    @PostMapping
    public Mono<VetDTO> insertVet(@RequestBody Mono<VetDTO> vetDTOMono) {
        return vetService.insertVet(vetDTOMono);
    }

    @PutMapping("{vetId}")
    public Mono<ResponseEntity<VetDTO>> updateVetByVetId(@PathVariable String vetId, @RequestBody Mono<VetDTO> vetDTOMono) {
        return vetService.updateVet(EntityDtoUtil.verifyId(vetId), vetDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{vetId}")
    public Mono<Void> deleteVet(@PathVariable String vetId) {
        return vetService.deleteVetByVetId(EntityDtoUtil.verifyId(vetId));
    }


}
