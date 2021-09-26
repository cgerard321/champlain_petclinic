package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.Valid;
import java.util.List;

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
@Timed("petclinic.vets")
@RequiredArgsConstructor
class VetResource {

    private final VetService vetService;


    VetResource(VetRepository vetRepository){this.vetRepository = vetRepository;}

    @GetMapping
    public List<Vet> showResourcesVetList() {
        return vetService.getAllVets();
    }

    @GetMapping(value = "/{vetId}")
    public Optional<Vet> findVet(@PathVariable("vetId") int vetId) {

        return vetRepository.findById(vetId);
    @GetMapping("/{vetId}")
    public Vet findVet(@PathVariable int vetId){return vetService.getVetByVetId(vetId);}


    @PutMapping( value = "/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Vet updateVet(@PathVariable int vetId, @RequestBody Vet vetRequest)
    {
        return  vetService.updateVet(vetService.getVetByVetId(vetId),vetRequest);}
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vet addVet(@Valid @RequestBody Vet vet) {
        return vetRepository.save(vet);
    }


}
