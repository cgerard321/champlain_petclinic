package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@RequestMapping("/vets")
@RestController
@RequiredArgsConstructor
class VetResource {


    private final VetService vetService;


    @GetMapping
    public List<Vet> showResourcesVetList() {
        return vetService.getAllVets();
    }

    @GetMapping("/{vetId}")
    public Vet findVet(@PathVariable int vetId){return vetService.getVetByVetId(vetId);}


    @PutMapping( value = "/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Vet updateVet(@PathVariable("vetId") int vetId, @Valid @RequestBody Vet vetRequest) {
        return  vetService.updateVet(vetRequest);}
}
