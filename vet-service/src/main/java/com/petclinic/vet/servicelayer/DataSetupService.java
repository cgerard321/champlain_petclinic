package com.petclinic.vet.servicelayer;

//import com.petclinic.vet.dataaccesslayer.Photo;
import com.petclinic.vet.dataaccesslayer.*;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Service
public class DataSetupService implements CommandLineRunner {

    @Autowired
    private VetRepository vetRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Override
    public void run(String... args) throws Exception {
        Specialty s1 = new Specialty("100001", "radiology");
        Specialty s2 = new Specialty("100002", "surgery");
        Specialty s3 = new Specialty("100003", "dentistry");
        Specialty s4 = new Specialty("100004", "general");

        Set<Specialty> set1 = new HashSet<>();
        set1.add(s1);
        set1.add(s3);
        Set<Specialty> set2 = new HashSet<>();
        set2.add(s4);
        set2.add(s2);
        set2.add(s1);
        Set<Specialty> set3 = new HashSet<>();
        set3.add(s1);
        set3.add(s4);
        Vet v1 = Vet.builder()
                .vetId(EntityDtoUtil.generateVetId())
                .vetBillId("1")
                .firstName("James")
                .lastName("Carter")
                .email("carter.james@email.com")
                .phoneNumber("(514)-634-8276 #2384")
                .imageId("1")
                .resume("Practicing since 3 years")
                .workday("Monday, Tuesday, Friday")
                .active(true)
                .specialties(set1)
                .build();
        Vet v2 = Vet.builder()
                .vetId(EntityDtoUtil.generateVetId())
                .vetBillId("2")
                .firstName("Helen")
                .lastName("Leary")
                .email("leary.helen@email.com")
                .phoneNumber("(514)-634-8276 #2385")
                .imageId("1")
                .resume("Practicing since 10 years")
                .workday("Wednesday, Thursday")
                .active(true)
                .specialties(set3)
                .build();
        Vet v3 = Vet.builder()
                .vetId(EntityDtoUtil.generateVetId())
                .vetBillId("3")
                .firstName("Linda")
                .lastName("Douglas")
                .email("douglas.linda@email.com")
                .phoneNumber("(514)-634-8276 #2386")
                .imageId("1")
                .resume("Practicing since 5 years")
                .workday("Monday, Wednesday, Thursday")
                .active(true)
                .specialties(set2)
                .build();
        Vet v4 = Vet.builder()
                .vetId(EntityDtoUtil.generateVetId())
                .vetBillId("4")
                .firstName("Rafael")
                .lastName("Ortega")
                .email("ortega.rafael@email.com")
                .phoneNumber("(514)-634-8276 #2387")
                .imageId("1")
                .resume("Practicing since 8 years")
                .workday("Wednesday, Thursday, Friday")
                .active(false)
                .specialties(set2)
                .build();
        Vet v5 = Vet.builder()
                .vetId(EntityDtoUtil.generateVetId())
                .vetBillId("5")
                .firstName("Henry")
                .lastName("Stevens")
                .email("stevens.henry@email.com")
                .phoneNumber("(514)-634-8276 #2389")
                .imageId("1")
                .resume("Practicing since 1 years")
                .workday("Monday, Tuesday, Wednesday, Thursday")
                .active(false)
                .specialties(set1)
                .build();
        Vet v6 = Vet.builder()
                .vetId(EntityDtoUtil.generateVetId())
                .vetBillId("6")
                .firstName("Sharon")
                .lastName("Jenkins")
                .email("jenkins.sharon@email.com")
                .phoneNumber("(514)-634-8276 #2383")
                .imageId("1")
                .resume("Practicing since 6 years")
                .workday("Monday, Tuesday, Friday")
                .active(false)
                .specialties(set1)
                .build();
        Vet v7 = Vet.builder()
                .vetId(EntityDtoUtil.generateVetId())
                .vetBillId("7")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .phoneNumber("(514)-634-8276 #2363")
                .imageId("1")
                .resume("Practicing since 9 years")
                .workday("Monday, Friday")
                .active(true)
                .specialties(set1)
                .build();
        Flux.just(v1, v2, v3, v4, v5, v6, v7)
                .flatMap(vetRepository::insert)
                .log()
                .subscribe();
        Rating r1 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v1.getVetId())
                .rateScore(5.0)
                .build();
        Rating r2 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v2.getVetId())
                .rateScore(4.0)
                .build();
        Rating r3 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .rateScore(3.0)
                .build();
        Rating r4 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .rateScore(4.0)
                .build();
        Flux.just(r1, r2, r3, r4)
                .flatMap(ratingRepository::insert)
                .log()
                .subscribe();
    }

}
