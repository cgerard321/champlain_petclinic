package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.PetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DataSetupService implements CommandLineRunner {

    @Autowired
    private PetTypeService petTypeService;

    @Override
    public void run(String... args) throws Exception {

        PetType pt1 = new PetType(1, "Cat");
        PetType pt2 = new PetType(2, "Dog");
        PetType pt3 = new PetType(3, "Lizard");
        PetType pt4 = new PetType(4, "Snake");
        PetType pt5 = new PetType(5, "Bird");
        PetType pt6 = new PetType(6, "Hamster");

        Flux.just(pt1, pt2, pt3, pt4, pt5, pt6)
                .flatMap(p -> petTypeService.insertPetType(Mono.just(p))
                        .log(p.toString()))
                .subscribe();

    }
}
