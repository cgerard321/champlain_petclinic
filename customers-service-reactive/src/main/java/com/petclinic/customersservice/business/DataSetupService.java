package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

        Pet p1 = new Pet(1, "Leo", new SimpleDateFormat( "yyyyMMdd" ).parse( "20100520" ), 1, 1, 1);
        Pet p2 = new Pet(2, "Basil", new SimpleDateFormat( "yyyyMMdd" ).parse( "2002-08-06" ), 6, 2, 1);
        Pet p3 = new Pet(3, "Rosy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2001-04-17" ), 2, 3, 1);
        Pet p4 = new Pet(4, "Jewel", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-03-07"), 2, 3, 1);
        Pet p5 = new Pet(5, "Iggy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"), 3, 4, 1);
        Pet p6 = new Pet(6, "George", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"), 4, 5, 1);
        Pet p7 = new Pet(7, "Samantha", new SimpleDateFormat( "yyyyMMdd" ).parse( "1995-09-04"), 1, 6, 1);
        Pet p8 = new Pet(8, "Max", new SimpleDateFormat( "yyyyMMdd" ).parse( "1995-09-04"), 1, 6, 1);
        Pet p9 = new Pet(9, "Lucky", new SimpleDateFormat( "yyyyMMdd" ).parse( "1999-08-06"), 5, 7, 1);
        Pet p10 = new Pet(10, "Mulligan", new SimpleDateFormat( "yyyyMMdd" ).parse( "1997-02-24"), 2, 8, 1);
        Pet p11 = new Pet(11, "Freddy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-03-09"), 5, 9, 1);
        Pet p12 = new Pet(12, "Ulysses", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-06-24"), 2, 10, 1);
        Pet p13 = new Pet(13, "Sly", new SimpleDateFormat( "yyyyMMdd" ).parse( "2002-06-08"), 1, 10, 1);

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
    }
}
