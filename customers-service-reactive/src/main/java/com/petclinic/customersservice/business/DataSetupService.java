package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.data.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class DataSetupService implements CommandLineRunner {

    @Autowired
    private PetTypeService petTypeService;

    @Autowired
    private PetService petService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private PhotoService photoService;

    @Override
    public void run(String... args) throws Exception {

        PetType pt1 = new PetType("1", "Cat");
        PetType pt2 = new PetType("2", "Dog");
        PetType pt3 = new PetType("3", "Lizard");
        PetType pt4 = new PetType("4", "Snake");
        PetType pt5 = new PetType("5", "Bird");
        PetType pt6 = new PetType("6", "Hamster");

        Flux.just(pt1, pt2, pt3, pt4, pt5, pt6)
                .flatMap(p -> petTypeService.insertPetType(Mono.just(p))
                        .log(p.toString()))
                .subscribe();


        Pet p1 = new Pet("1", "1", "Leo", new SimpleDateFormat( "yyyyMMdd" ).parse( "2010-05-20" ), "1", "1");
        Pet p2 = new Pet("2", "2", "Basil", new SimpleDateFormat( "yyyyMMdd" ).parse( "2002-08-06" ), "6",  "1");
        Pet p3 = new Pet("3", "3", "Rosy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2001-04-17" ), "2", "1");
        Pet p4 = new Pet("4", "3", "Jewel", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-03-07"), "2","1");
        Pet p5 = new Pet("5", "4", "Iggy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"),"3",  "1");
        Pet p6 = new Pet("6", "5", "George", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"), "6", "1");
        Pet p7 = new Pet("7", "6", "Samantha", new SimpleDateFormat( "yyyyMMdd" ).parse( "1995-09-04"), "1", "1");
        Pet p8 = new Pet("8", "6", "Max", new SimpleDateFormat( "yyyyMMdd" ).parse( "1995-09-04"), "1", "1");
        Pet p9 = new Pet("9", "7", "Lucky", new SimpleDateFormat( "yyyyMMdd" ).parse( "1999-08-06"), "5", "1");
        Pet p10 = new Pet("10", "8", "Mulligan", new SimpleDateFormat( "yyyyMMdd" ).parse( "1997-02-24"), "2", "1");
        Pet p11 = new Pet("11", "9", "Freddy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-03-09"), "5", "1");
        Pet p12 = new Pet("12", "10", "Ulysses", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-06-24"), "2", "1");
        Pet p13 = new Pet("13", "10", "Sly", new SimpleDateFormat( "yyyyMMdd" ).parse( "2002-06-08"), "1", "1");


        Flux.just(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)
                .flatMap(p -> petService.insertPet(Mono.just(p))
                        .log(p.toString()))
                .subscribe();

        Owner o1 = new Owner(1, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023", 1);
        Owner o2 = new Owner(2, "Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749", 1);
        Owner o3 = new Owner(3, "Eduardo", "Rodriguez", "2693 Commerce St.", "McFarland", "6085558763", 1);
        Owner o4 = new Owner(4, "Harold", "Davis", "563 Friendly St.", "Windsor", "6085553198", 1);
        Owner o5 = new Owner(5, "Peter", "McTavish", "2387 S. Fair Way", "Madison", "6085552765", 1);
        Owner o6 = new Owner(6, "Jean", "Coleman", "105 N. Lake St.", "Monona", "6085552654", 1);
        Owner o7 = new Owner(7, "Jeff", "Black", "1450 Oak Blvd.", "Monona", "6085555387", 1);
        Owner o8 = new Owner(8, "Maria", "Escobito", "345 Maple St.", "Madison", "6085557683", 1);
        Owner o9 = new Owner(9, "David", "Schroeder", "2749 Blackhawk Trail", "Madison", "6085559435", 1);
        Owner o10 = new Owner(10, "Carlos", "Esteban", "2335 Independence La.", "Waunakee", "6085555487", 1);

        Flux.just(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10)
                .flatMap(p -> ownerService.insertOwner(Mono.just(p))
                        .log(p.toString()))
                .subscribe();

        Photo ph1 = new Photo("1", "defaultpic", "jpeg", "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'");

        Flux.just(ph1)
                .flatMap(p -> photoService.insertPhoto(Mono.just(p))
                        .log(p.toString()))
                .subscribe();
    }
}

