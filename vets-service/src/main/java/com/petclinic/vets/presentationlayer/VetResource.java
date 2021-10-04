package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.petclinic.vets.datalayer.VetRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
@Timed("petclinic.vets")
//@RequiredArgsConstructor
class VetResource {

    private final VetService vetService;
    private static final Logger LOG = LoggerFactory.getLogger(VetResource.class);


    VetResource(VetService vetService)
    {
        this.vetService = vetService;
    }

    @GetMapping
    public List<Vet> showResourcesVetList() {
        return vetService.getAllEnabledVets();
    }

//    @GetMapping("/enabled")
//    public List<Vet> showResourcesVetEnabledList() {
//        return vetService.getAllEnabledVets();
//    }

    @GetMapping("/disabled")
    public List<Vet> showResourcesVetDisabledList() {
        return vetService.getAllDisabledVets();
    }


    @GetMapping("/{vetId}")
    public Vet findVet(@PathVariable int vetId)
    {
        LOG.debug("/vet MS return the found product for vetId: " + vetId);

        if(vetId < 1) throw new InvalidInputException("Invalid vetId: " + vetId);
        return vetService.getVetByVetId(vetId);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Vet addVet(@Valid @RequestBody Vet vet)
    {
        return vetService.createVet(vet);
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


    @PutMapping(path = "/{vetId}/enableVet",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Vet enableVet(@PathVariable("vetId") int vetId, @RequestBody Vet vetRequest) {
        Vet vet = vetService.getVetByVetId(vetId);
        vetService.enableVet(vet,vetRequest);
        return vet;
    }



}
