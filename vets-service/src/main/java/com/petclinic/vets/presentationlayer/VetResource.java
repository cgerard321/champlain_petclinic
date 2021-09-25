package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
=======
>>>>>>> main

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

class VetResource {

    private final VetRepository vetRepository;

    VetResource(VetRepository vetRepository){this.vetRepository = vetRepository;}

    @GetMapping
    public List<Vet> showResourcesVetList() {
        return vetRepository.findAll();
    }

    @GetMapping(value = "/{vetId}")
    public Optional<Vet> findVet(@PathVariable("vetId") int vetId) {
<<<<<<< HEAD
        return vetRepository.findById(vetId);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vet addVet(@Valid @RequestBody Vet vet) {
        return vetRepository.save(vet);
    }

    @GetMapping(value = "/{vetId}")
    public Optional<Vet> findVetById(@PathVariable("vetId") int vetId) {
        return vetRepository.findById(vetId);
    }


    @GetMapping(value = "/{vetId}")
    public Optional<Vet> findVet(@PathVariable("vetId") int vetId) {
=======
>>>>>>> main
        return vetRepository.findById(vetId);
    }

}
