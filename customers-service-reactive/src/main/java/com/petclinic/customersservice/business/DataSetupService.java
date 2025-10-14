package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetType;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.text.SimpleDateFormat;
import java.util.List;

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

        // If the db is not empty, then skip the data setup
        try {

            if (Boolean.TRUE.equals(petTypeService.getAllPetTypes().hasElements().block())) return;
            if (Boolean.TRUE.equals(petService.getAllPets().hasElements().block())) return;
            if (Boolean.TRUE.equals(ownerService.getAllOwners().hasElements().block())) return;

        } catch (Exception e) {
            System.out.println("Error checking if data exists: " + e.getMessage());
            return;
        }


        PetType pt1 = new PetType("1", "4283c9b8-4ffd-4866-a5ed-287117c60a40", "Cat", "Mammal");
        PetType pt2 = new PetType("2", "1233c9b8-4ffd-4866-4h36-287117c60a35", "Dog", "Mammal");
        PetType pt3 = new PetType("3", "9783c9b8-4ffd-4866-a5ed-287117c60a10", "Lizard", "Reptile");
        PetType pt4 = new PetType("4", "9133c9b8-4ffd-4866-a5ed-287117c60a19", "Snake", "Reptile");
        PetType pt5 = new PetType("5", "2093c9b8-4ffd-4866-a5ed-287117c60a11", "Bird", "Mammal");
        PetType pt6 = new PetType("6", "1103c9b8-4ffd-4866-a5ed-287117c60a89", "Hamster", "Mammal");
        PetType pt7 = new PetType("7", "9993c9b8-4ffd-4866-a5ed-287117c60a99", "Others", "Various");

        Flux.just(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
                .flatMap(p -> petTypeService.insertPetType(Mono.just(p))
                        .log(p.toString()))
                .subscribe();


        Pet p1 = new Pet("c3eecf3a-d732-46d6-9e51-ab03314f3c4d", "0e4d8481-b611-4e52-baed-af16caa8bf8a", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", "Leo", new SimpleDateFormat("yyyyMMdd").parse("2010-05-20"), "1", "1", "false", "3.7");
        Pet p2 = new Pet("180143e7-547d-46c2-82fd-7c84547e126c", "ecb109cd-57ea-4b85-b51e-99751fd1c349", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "Basil", new SimpleDateFormat("yyyyMMdd").parse("2002-08-06"), "6", "1", "true", "0.20");
        Pet p3 = new Pet("6566cc34-21f7-4f71-9388-c70c95b01636", "53163352-8398-4513-bdff-b7715c056d1d", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "Rosy", new SimpleDateFormat("yyyyMMdd").parse("2001-04-17"), "2", "1", "false", "5.2");
        Pet p4 = new Pet("daa049e0-b6ec-4465-a20e-6bd4be11606e", "7056652d-f2fd-4873-a480-5d2e86bed641", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "Jewel", new SimpleDateFormat("yyyyMMdd").parse("2000-03-07"), "2", "1", "true", "3.8");
        Pet p5 = new Pet("09b1085e-9ddc-468b-9a20-1bd8fe284d2c", "fde4c2a1-b663-45a2-affe-7b3d08cebf75", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "Iggy", new SimpleDateFormat("yyyyMMdd").parse("2000-11-30"), "3", "1", "false", "0.07");
        Pet p6 = new Pet("4713b5c9-0426-4f70-a070-47e97ed25fa6", "f42b727d-c0d2-4b37-b99e-af7c7da556c0", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", "George", new SimpleDateFormat("yyyyMMdd").parse("2000-11-30"), "6", "1", "true", "1.1");
        Pet p7 = new Pet("534a9744-e316-461b-9ada-3552fbeb86b7", "306882c1-2019-43fe-96d3-a05ef7efad25", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", "Samantha", new SimpleDateFormat("yyyyMMdd").parse("1995-09-04"), "1", "1", "true", "5");
        Pet p8 = new Pet("19979a4f-cd0b-4cd3-a593-c94e96172756", "399f2e7a-3c48-486a-956f-044808b0da6b", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", "Max", new SimpleDateFormat("yyyyMMdd").parse("1995-09-04"), "1", "1", "true", "2.7");
        Pet p9 = new Pet("91f56a80-049b-4bd7-8620-3354854b9541", "7493b72f-bbd7-48bb-b535-4165ae8a94f3", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", "Lucky", new SimpleDateFormat("yyyyMMdd").parse("1999-08-06"), "5", "1", "true", "0.64");
        Pet p10 = new Pet("15d16020-3056-4a8f-a754-18fbd19ab31c", "9d1aa0b7-be08-4cab-a1c9-db0d2af82bd7", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "Mulligan", new SimpleDateFormat("yyyyMMdd").parse("1997-02-24"), "2", "1", "true", "26");
        Pet p11 = new Pet("913149c2-a712-4151-8b4c-a4b55b7f8d49", "aca3f26b-a8c6-4ccd-bb24-1fb12ef75142", "9f6accd1-e943-4322-932e-199d93824317", "Freddy", new SimpleDateFormat("yyyyMMdd").parse("2000-03-09"), "5", "1", "true", "0.40");
        Pet p12 = new Pet("f9540265-6ff7-46a6-b3f9-b25a9f150733", "db2685cf-8c34-4930-828e-d07208ab39f4", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", "Ulysses", new SimpleDateFormat("yyyyMMdd").parse("2000-06-24"), "2", "1", "true", "20");
        Pet p13 = new Pet("706a12a4-5e7a-42ea-b818-5add08accece", "907d0744-e2a4-4a34-9706-95aa1bdd9bbe", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", "Sly", new SimpleDateFormat("yyyyMMdd").parse("2002-06-08"), "1", "1", "true", "3.4");


        Flux.just(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)
                .flatMap(p -> petService.insertPet(Mono.just(p))
                        .log(p.toString()))
                .subscribe();


        Flux<PetResponseDTO> petResponseFlux = petService.getPetsByOwnerId("f470653d-05c5-4c45-b7a0-7d70f003d2ac");
        Mono<List<PetResponseDTO>> petResponseListMono = petResponseFlux.collectList();
        List<PetResponseDTO> petResponseList = petResponseListMono.block();

        Flux<PetResponseDTO> petResponseFlux2 = petService.getPetsByOwnerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        Mono<List<PetResponseDTO>> petResponseListMono2 = petResponseFlux2.collectList();
        List<PetResponseDTO> petResponseList2 = petResponseListMono2.block();

        Flux<PetResponseDTO> petResponseFlux3 = petService.getPetsByOwnerId("3f59dca2-903e-495c-90c3-7f4d01f3a2aa");
        Mono<List<PetResponseDTO>> petResponseListMono3 = petResponseFlux3.collectList();
        List<PetResponseDTO> petResponseList3 = petResponseListMono3.block();

        Flux<PetResponseDTO> petResponseFlux4 = petService.getPetsByOwnerId("a6e0e5b0-5f60-45f0-8ac7-becd8b330486");
        Mono<List<PetResponseDTO>> petResponseListMono4 = petResponseFlux4.collectList();
        List<PetResponseDTO> petResponseList4 = petResponseListMono4.block();

        Flux<PetResponseDTO> petResponseFlux5 = petService.getPetsByOwnerId("c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2");
        Mono<List<PetResponseDTO>> petResponseListMono5 = petResponseFlux5.collectList();
        List<PetResponseDTO> petResponseList5 = petResponseListMono5.block();

        Flux<PetResponseDTO> petResponseFlux6 = petService.getPetsByOwnerId("b3d09eab-4085-4b2d-a121-78a0a2f9e501");
        Mono<List<PetResponseDTO>> petResponseListMono6 = petResponseFlux6.collectList();
        List<PetResponseDTO> petResponseList6 = petResponseListMono6.block();

        Flux<PetResponseDTO> petResponseFlux7 = petService.getPetsByOwnerId("5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd");
        Mono<List<PetResponseDTO>> petResponseListMono7 = petResponseFlux7.collectList();
        List<PetResponseDTO> petResponseList7 = petResponseListMono7.block();

        Flux<PetResponseDTO> petResponseFlux8 = petService.getPetsByOwnerId("48f9945a-4ee0-4b0b-9b44-3da829a0f0f7");
        Mono<List<PetResponseDTO>> petResponseListMono8 = petResponseFlux8.collectList();
        List<PetResponseDTO> petResponseList8 = petResponseListMono8.block();

        Flux<PetResponseDTO> petResponseFlux9 = petService.getPetsByOwnerId("9f6accd1-e943-4322-932e-199d93824317");
        Mono<List<PetResponseDTO>> petResponseListMono9 = petResponseFlux9.collectList();
        List<PetResponseDTO> petResponseList9 = petResponseListMono9.block();

        Flux<PetResponseDTO> petResponseFlux10 = petService.getPetsByOwnerId("7c0d42c2-0c2d-41ce-bd9c-6ca67478956f");
        Mono<List<PetResponseDTO>> petResponseListMono10 = petResponseFlux10.collectList();
        List<PetResponseDTO> petResponseList10 = petResponseListMono10.block();


        Owner o1 = new Owner("1", "f470653d-05c5-4c45-b7a0-7d70f003d2ac", "George", "Franklin",
                "110 W. Liberty St.", "Madison", "Ontario", "6085551023", petResponseList, "3e5a214b-009d-4a25-9313-344676e6157d");
        Owner o2 = new Owner("2", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "Betty", "Davis",
                "638 Cardinal Ave.", "Sun Prairie", "Quebec", "6085551749", petResponseList2, null);
        Owner o3 = new Owner("3", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "Eduardo", "Rodriguez",
                "2693 Commerce St.", "McFarland", "Ontario", "6085558763", petResponseList3, null);
        Owner o4 = new Owner("4", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "Harold", "Davis",
                "563 Friendly St.", "Windsor", "Ontario", "6085553198", petResponseList4, null);
        Owner o5 = new Owner("5", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", "Peter", "McTavish",
                "2387 S. Fair Way", "Madison", "Quebec", "6085552765", petResponseList5, null);
        Owner o6 = new Owner("6", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", "Jean", "Coleman",
                "105 N. Lake St.", "Monona", "Quebec", "6085552654", petResponseList6, null);
        Owner o7 = new Owner("7", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", "Jeff", "Black",
                "1450 Oak Blvd.", "Monona", "Quebec", "6085555387", petResponseList7, null);
        Owner o8 = new Owner("8", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "Maria", "Escobito",
                "345 Maple St.", "Madison", "Quebec", "6085557683", petResponseList8, null);
        Owner o9 = new Owner("9", "9f6accd1-e943-4322-932e-199d93824317", "David", "Schroeder",
                "2749 Blackhawk Trail", "Madison", "Quebec", "6085559435", petResponseList9, null);
        Owner o10 = new Owner("10", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", "Carlos", "Esteban",
                "2335 Independence La.", "Waunakee", "Ontario", "6085555487", petResponseList10, null);

        Flux.just(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10)
                .flatMap(p -> ownerService.insertOwner(Mono.just(p))
                        .log(p.toString()))
                .subscribe();


    }
}

