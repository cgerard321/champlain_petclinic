package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.Photo;
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

    @Autowired
    private PhotoService photoService;
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


        VetDTO v1 = new VetDTO("234568", "James", "Carter", "carter.james@email.com",
                "(514)-634-8276 #2384","1","Practicing since 3 years", "Monday, Tuesday, Friday",
                true, set1);

        VetDTO v2 = new VetDTO("327874", "Helen", "Leary", "leary.helen@email.com",
                "(514)-634-8276 #2385","1", "Practicing since 10 years", "Wednesday, Thursday",
                true, set3);

        VetDTO v3 = new VetDTO("238372", "Linda", "Douglas", "douglas.linda@email.com",
                "(514)-634-8276 #2386","1", "Practicing since 5 years", "Monday, Wednesday, Thursday",
                true, set2);

        VetDTO v4 = new VetDTO("823097", "Rafael", "Ortega", "ortega.rafael@email.com",
                "(514)-634-8276 #2387","1", "Practicing since 8 years", "Wednesday, Thursday, Friday",
                false, set2);

        VetDTO v5 = new VetDTO("842370", "Henry", "Stevens", "stevens.henry@email.com",
                "(514)-634-8276 #2389","1", "Practicing since 1 years", "Monday, Tuesday, Wednesday, Thursday",
                false, set1);

        VetDTO v6 = new VetDTO("784233", "Sharon", "Jenkins", "jenkins.sharon@email.com",
                "(514)-634-8276 #2383","1", "Practicing since 6 years", "Monday, Tuesday, Friday",
                false, set1);

        VetDTO v7 = new VetDTO("784233", "John", "Doe", "john.doe@email.com",
                "(514)-634-8276 #2363","1", "Practicing since 9 years", "Monday, Friday",
                true, set1);

        Flux.just(v1, v2, v3, v4, v5, v6, v7)
                .flatMap(p -> vetService.insertVet(Mono.just(p))
                        .log(p.toString()))
                .subscribe();

//        Photo ph1 = new Photo("1", "defaultpic", "jpeg", "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'");
//
//        Flux.just(ph1)
//                .flatMap(p -> photoService.insertPhoto(Mono.just(p))
//                        .log(p.toString()))
//                .subscribe();
    }

}
