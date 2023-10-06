package com.petclinic.vet.servicelayer;

import com.petclinic.vet.dataaccesslayer.*;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.ratings.PredefinedDescription;
import com.petclinic.vet.dataaccesslayer.ratings.Rating;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.util.EntityDtoUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Service
public class DataSetupService implements CommandLineRunner {


    private final VetRepository vetRepository;

    private final RatingRepository ratingRepository;

    private final EducationRepository educationRepository;

    public DataSetupService(VetRepository vetRepository, RatingRepository ratingRepository, EducationRepository educationRepository){
        this.vetRepository = vetRepository;
        this.ratingRepository = ratingRepository;
        this.educationRepository = educationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Specialty s1 = new Specialty("100001", "radiology");
        Specialty s2 = new Specialty("100002", "surgery");
        Specialty s3 = new Specialty("100003", "dentistry");
        Specialty s4 = new Specialty("100004", "general");

        Set<Workday> workdays1 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Friday);
        Set<Workday> workdays2 = EnumSet.of(Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays3 = EnumSet.of(Workday.Monday, Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays4 = EnumSet.of(Workday.Wednesday, Workday.Thursday, Workday.Friday);
        Set<Workday> workdays5 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Wednesday, Workday.Thursday);
        Set<Workday> workdays6 = EnumSet.of(Workday.Monday, Workday.Tuesday, Workday.Friday);


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
                .vetId("69f852ca-625b-11ee-8c99-0242ac120002")
                .vetBillId("1")
                .firstName("James")
                .lastName("Carter")
                .email("carterjames@email.com")
                .phoneNumber("(514)-634-8276 #2384")
                .imageId("1")
                .resume("Practicing since 3 years")
                .workday(workdays1)
                .active(true)
                .specialties(set1)
                .build();
        Vet v2 = Vet.builder()
                .vetId("69f85766-625b-11ee-8c99-0242ac120002")
                .vetBillId("2")
                .firstName("Helen")
                .lastName("Leary")
                .email("learyhelen@email.com")
                .phoneNumber("(514)-634-8276 #2385")
                .imageId("1")
                .resume("Practicing since 10 years")
                .workday(workdays2)
                .active(true)
                .specialties(set3)
                .build();
        Vet v3 = Vet.builder()
                .vetId("69f85bda-625b-11ee-8c99-0242ac120002")
                .vetBillId("3")
                .firstName("Linda")
                .lastName("Douglas")
                .email("douglaslinda@email.com")
                .phoneNumber("(514)-634-8276 #2386")
                .imageId("1")
                .resume("Practicing since 5 years")
                .workday(workdays3)
                .active(true)
                .specialties(set2)
                .build();
        Vet v4 = Vet.builder()
                .vetId("69f85d2e-625b-11ee-8c99-0242ac120002")
                .vetBillId("4")
                .firstName("Rafael")
                .lastName("Ortega")
                .email("ortegarafael@email.com")
                .phoneNumber("(514)-634-8276 #2387")
                .imageId("1")
                .resume("Practicing since 8 years")
                .workday(workdays4)
                .active(false)
                .specialties(set2)
                .build();
        Vet v5 = Vet.builder()
                .vetId("ac9adeb8-625b-11ee-8c99-0242ac120002")
                .vetBillId("5")
                .firstName("Henry")
                .lastName("Stevens")
                .email("stevenshenry@email.com")
                .phoneNumber("(514)-634-8276 #2389")
                .imageId("1")
                .resume("Practicing since 1 years")
                .workday(workdays5)
                .active(false)
                .specialties(set1)
                .build();
        Vet v6 = Vet.builder()
                .vetId("b2bc331e-625b-11ee-8c99-0242ac120002")
                .vetBillId("6")
                .firstName("Sharon")
                .lastName("Jenkins")
                .email("jenkinssharon@email.com")
                .phoneNumber("(514)-634-8276 #2383")
                .imageId("1")
                .resume("Practicing since 6 years")
                .workday(workdays5)
                .active(false)
                .specialties(set1)
                .build();
        Vet v7 = Vet.builder()
                .vetId("c02cbf82-625b-11ee-8c99-0242ac120002")
                .vetBillId("7")
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@email.com")
                .phoneNumber("(514)-634-8276 #2363")
                .imageId("1")
                .resume("Practicing since 9 years")
                .workday(workdays6)
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
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .build();
        Rating r2 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v2.getVetId())
                .rateScore(4.0)
                .predefinedDescription(null)
                .rateDescription("Good vet.")
                .build();
        Rating r3 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .rateScore(3.0)
                .rateDescription("The vet is ok.")
                .predefinedDescription(null)
                .build();
        Rating r4 = Rating.builder()
                .ratingId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .rateScore(4.0)
                .rateDescription(null)
                .predefinedDescription(PredefinedDescription.GOOD)
                .build();
        Flux.just(r1, r2, r3, r4)
                .flatMap(ratingRepository::insert)
                .log()
                .subscribe();
        Education e1 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v1.getVetId())
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Montreal")
                .startDate("2010")
                .endDate("2014")
                .build();
        Education e2 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v2.getVetId())
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Veterinary Medicine")
                .schoolName("University of Veterinary Sciences")
                .startDate("2008")
                .endDate("2013")
                .build();
        Education e3 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v3.getVetId())
                .degree("Doctor of Veterinary Medicine")
                .fieldOfStudy("Animal Surgery")
                .schoolName("California Veterinary College")
                .startDate("2009")
                .endDate("2015")
                .build();
        Education e4 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v4.getVetId())
                .degree("Master of Science in Veterinary Pathology")
                .fieldOfStudy("Pathology")
                .schoolName("Texas A&M University")
                .startDate("2016")
                .endDate("2018")
                .build();
        Education e5 = Education.builder()
                .educationId(UUID.randomUUID().toString())
                .vetId(v1.getVetId())
                .degree("Bachelor of Veterinary Science")
                .fieldOfStudy("Veterinary Genetics")
                .schoolName("University of Sydney")
                .startDate("2016")
                .endDate("2018")
                .build();
        Flux.just(e1, e2, e3, e4, e5)
                .flatMap(educationRepository::insert)
                .log()
                .subscribe();
    }

}
