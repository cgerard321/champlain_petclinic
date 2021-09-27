package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Vet;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
//@RequiredArgsConstructor
class VetResource {

    private final VetService vetService;


    VetResource(VetService vetService)
    {
        this.vetService = vetService;
    }

    @GetMapping
    public List<Vet> showResourcesVetList() {
        return vetService.getAllVets();
    }

    @GetMapping("/{vetId}")
    public Vet findVet(@PathVariable int vetId)
    {
        return vetService.getVetByVetId(vetId);
    }


    @PutMapping( value = "/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Vet updateVet(@PathVariable int vetId, @RequestBody Vet vetRequest)
    {
        return  vetService.updateVet(vetService.getVetByVetId(vetId),vetRequest);
    }

    @PutMapping(path = "/{vetId}/disableVet",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Vet disableVet(@PathVariable("vetId") int vetId, @RequestBody Vet vetRequest) {
        Vet vet = vetService.getVetByVetId(vetId);
        vetService.disableVet(vet,vetRequest);
        return vet;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vet addVet(@Valid @RequestBody Vet vet)
    {
        return vetService.createVet(vet);
    }


}
