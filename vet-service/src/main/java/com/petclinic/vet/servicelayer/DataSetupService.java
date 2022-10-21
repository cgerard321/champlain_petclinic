package com.petclinic.vet.servicelayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;


@Service
public class DataSetupService implements CommandLineRunner {

    @Autowired
    private VetService vetService;

    @Override
    public void run(String... args) throws Exception {

    SpecialtyDTO s1 = new SpecialtyDTO("100001", "radiology");
        SpecialtyDTO s2 = new SpecialtyDTO("100002", "surgery");
        SpecialtyDTO s3 = new SpecialtyDTO("100003", "dentistry");
        SpecialtyDTO s4 = new SpecialtyDTO("100004", "general");

        Set<SpecialtyDTO> set1 = new HashSet<>();
        set1.add(s1);
        set1.add(s3);

        Set<SpecialtyDTO> set2 = new HashSet<>();
        set2.add(s4);
        set2.add(s2);
        set2.add(s1);

        Set<SpecialtyDTO> set3 = new HashSet<>();
        set3.add(s1);
        set3.add(s4);


        VetDTO v1 = new VetDTO("234568","1", "James", "Carter", "carter.james@email.com",
                "(514)-634-8276 #2384","".getBytes(),"Practicing since 3 years", "Monday, Tuesday, Friday",
                true, set1);

        VetDTO v2 = new VetDTO("327874","2",  "Helen", "Leary", "leary.helen@email.com",
                "(514)-634-8276 #2385","".getBytes(), "Practicing since 10 years", "Wednesday, Thursday",
                true, set3);

        VetDTO v3 = new VetDTO("238372","3", "Linda", "Douglas", "douglas.linda@email.com",
                "(514)-634-8276 #2386","".getBytes(), "Practicing since 5 years", "Monday, Wednesday, Thursday",
                true, set2);

        VetDTO v4 = new VetDTO("823097","4", "Rafael", "Ortega", "ortega.rafael@email.com",
                "(514)-634-8276 #2387","".getBytes(), "Practicing since 8 years", "Wednesday, Thursday, Friday",
                false, set2);

        VetDTO v5 = new VetDTO("842370","5", "Henry", "Stevens", "stevens.henry@email.com",
                "(514)-634-8276 #2389","".getBytes(), "Practicing since 1 years", "Monday, Tuesday, Wednesday, Thursday",
                false, set1);

        VetDTO v6 = new VetDTO("784233","6", "Sharon", "Jenkins", "jenkins.sharon@email.com",
                "(514)-634-8276 #2383","".getBytes(), "Practicing since 6 years", "Monday, Tuesday, Friday",
                false, set1);

        VetDTO v7 = new VetDTO("784233","7", "John", "Doe", "john.doe@email.com",
                "(514)-634-8276 #2363","".getBytes(), "Practicing since 9 years", "Monday, Friday",
                true, set1);

        Flux.just(v1, v2, v3, v4, v5, v6, v7)
                .flatMap(p -> vetService.insertVet(Mono.just(p))
                        .log(p.toString()))
                .subscribe();
    }

}
