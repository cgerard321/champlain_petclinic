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

        PetType pt1 = new PetType("4a7fd25e-426f-450d-847f-10d18202769d", "Cat");
        PetType pt2 = new PetType("155591e4-8b69-4f43-9c78-5ba9211c85fb", "Dog");
        PetType pt3 = new PetType("ec640eda-14c6-4273-b2a0-5bd057bd6608", "Lizard");
        PetType pt4 = new PetType("11b6947b-610e-4e92-b79e-7395abfc9251", "Snake");
        PetType pt5 = new PetType("44c1e8e1-c3a5-4bb2-8ca0-b9353102d09e", "Bird");
        PetType pt6 = new PetType("e965b83f-dcf4-4067-8467-7efa0c794c1b", "Hamster");

        Flux.just(pt1, pt2, pt3, pt4, pt5, pt6)
                .flatMap(p -> petTypeService.insertPetType(Mono.just(p))
                        .log(p.toString()))
                .subscribe();


        Pet p1 = new Pet("c3eecf3a-d732-46d6-9e51-ab03314f3c4d", "1","f470653d-05c5-4c45-b7a0-7d70f003d2ac", "Leo", new SimpleDateFormat( "yyyyMMdd" ).parse( "2010-05-20" ), "1", "1");
        Pet p2 = new Pet("180143e7-547d-46c2-82fd-7c84547e126c", "2", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "Basil", new SimpleDateFormat( "yyyyMMdd" ).parse( "2002-08-06" ), "6",  "1");
        Pet p3 = new Pet("6566cc34-21f7-4f71-9388-c70c95b01636", "3", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "Rosy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2001-04-17" ), "2", "1");
        Pet p4 = new Pet("daa049e0-b6ec-4465-a20e-6bd4be11606e", "4", "3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "Jewel", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-03-07"), "2","1");
        Pet p5 = new Pet("09b1085e-9ddc-468b-9a20-1bd8fe284d2c", "5", "a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "Iggy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"),"3",  "1");
        Pet p6 = new Pet("4713b5c9-0426-4f70-a070-47e97ed25fa6", "6", "c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", "George", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-11-30"), "6", "1");
        Pet p7 = new Pet("534a9744-e316-461b-9ada-3552fbeb86b7", "7", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", "Samantha", new SimpleDateFormat( "yyyyMMdd" ).parse( "1995-09-04"), "1", "1");
        Pet p8 = new Pet("19979a4f-cd0b-4cd3-a593-c94e96172756", "8", "b3d09eab-4085-4b2d-a121-78a0a2f9e501", "Max", new SimpleDateFormat( "yyyyMMdd" ).parse( "1995-09-04"), "1", "1");
        Pet p9 = new Pet("91f56a80-049b-4bd7-8620-3354854b9541", "9", "5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", "Lucky", new SimpleDateFormat( "yyyyMMdd" ).parse( "1999-08-06"), "5", "1");
        Pet p10 = new Pet("15d16020-3056-4a8f-a754-18fbd19ab31c", "10", "48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "Mulligan", new SimpleDateFormat( "yyyyMMdd" ).parse( "1997-02-24"), "2", "1");
        Pet p11 = new Pet("913149c2-a712-4151-8b4c-a4b55b7f8d49", "11", "9f6accd1-e943-4322-932e-199d93824317", "Freddy", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-03-09"), "5", "1");
        Pet p12 = new Pet("f9540265-6ff7-46a6-b3f9-b25a9f150733", "12", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", "Ulysses", new SimpleDateFormat( "yyyyMMdd" ).parse( "2000-06-24"), "2", "1");
        Pet p13 = new Pet("706a12a4-5e7a-42ea-b818-5add08accece", "13", "7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", "Sly", new SimpleDateFormat( "yyyyMMdd" ).parse( "2002-06-08"), "1", "1");


        Flux.just(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)
                .flatMap(p -> petService.insertPet(Mono.just(p))
                        .log(p.toString()))
                .subscribe();

        Owner o1 = new Owner("1","f470653d-05c5-4c45-b7a0-7d70f003d2ac", "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
        Owner o2 = new Owner("2","e6c7398e-8ac4-4e10-9ee0-03ef33f0361a", "Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749");
        Owner o3 = new Owner("3","3f59dca2-903e-495c-90c3-7f4d01f3a2aa", "Eduardo", "Rodriguez", "2693 Commerce St.", "McFarland", "6085558763");
        Owner o4 = new Owner("4","a6e0e5b0-5f60-45f0-8ac7-becd8b330486", "Harold", "Davis", "563 Friendly St.", "Windsor", "6085553198");
        Owner o5 = new Owner("5","c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2", "Peter", "McTavish", "2387 S. Fair Way", "Madison", "6085552765");
        Owner o6 = new Owner("6","b3d09eab-4085-4b2d-a121-78a0a2f9e501", "Jean", "Coleman", "105 N. Lake St.", "Monona", "6085552654");
        Owner o7 = new Owner("7","5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd", "Jeff", "Black", "1450 Oak Blvd.", "Monona", "6085555387");
        Owner o8 = new Owner("8","48f9945a-4ee0-4b0b-9b44-3da829a0f0f7", "Maria", "Escobito", "345 Maple St.", "Madison", "6085557683");
        Owner o9 = new Owner("9","9f6accd1-e943-4322-932e-199d93824317", "David", "Schroeder", "2749 Blackhawk Trail", "Madison", "6085559435");
        Owner o10 = new Owner("10","7c0d42c2-0c2d-41ce-bd9c-6ca67478956f", "Carlos", "Esteban", "2335 Independence La.", "Waunakee", "6085555487");

        Flux.just(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10)
                .flatMap(p -> ownerService.insertOwner(Mono.just(p))
                        .log(p.toString()))
                .subscribe();

        Photo ph1 = new Photo("1", "defaultpic", "jpeg", "/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAYGBgYGBgYHBwYJCgkKCQ0MCwsMDRQODw4PDhQfExYTExYTHxshGxkbIRsxJiIiJjE4Ly0vOEQ9PURWUVZwcJYBBgYGBgYGBgcHBgkKCQoJDQwLCwwNFA4PDg8OFB8TFhMTFhMfGyEbGRshGzEmIiImMTgvLS84RD09RFZRVnBwlv/CABEIAPoA+gMBIgACEQEDEQH/xAAcAAEAAwEBAQEBAAAAAAAAAAAAAQIGBQQDBwj/2gAIAQEAAAAA/ogAAAACYgAAAAFogAAAAFogAAAAFogAAAAFogAAAAFogAHg5Pnv0evYAFogAfPKcoPvrOiAFogAri/CBOy6IAtEAGczwB6N1YAtEAI/P6ADVdoAtEAOdjAA6+tALRADi5UAPdtgC0QA4uVAD37UAtEAPBigA7OrALRABgvgANf1QC0QAcPLgHt20gFogAZHkgfXaesAWiABGd4FR0NX6QAtEAA+PH8f26nQAAtEAHL5Xg8lZPR7un1vqAWiAV4XB+AAW7Gj+4FogPFkvKAAX03bBaIHNyFAAAd/ShaIPJiaAAANR3BaIMXzwAAC269BaIeHEgAABoNIWiGezgAAAe3blohmOEAAAH135aIZnggAAB9d+WiGZ4IAAAfXflhmOAAAAH2/QB//xAAWAQEBAQAAAAAAAAAAAAAAAAAAAQL/2gAIAQIQAAAAoAAAAAAAANEgAaozABsJkAugYAGwmQBqjMAGgyALaRIBqgJkNUAZhdAAwXQAMF0ADD//xAAWAQEBAQAAAAAAAAAAAAAAAAAAAgH/2gAIAQMQAAAAwAAAAAAAAEm6AE4NoAQG0AIDaAEBtACcFaAGYoAMzBtAIANoJwAVpkgAsyQAWZIALf/EADkQAAIBAgIHBQYFAwUAAAAAAAECAwQRAAUhMDFAQVFhEiJxgbETIDIzcpEQQnOhwRRj0SRScJLh/9oACAEBAAE/AP8Aky18VGZ0dMSDJ2nH5U0nz4Ylz5zoigUDmxv6YOd1v9oD6cJntUD3442HQEHEGeUzm0qtGefxLhHSVQ8bhlOwg3G8yypBG0kjBVG0nFbms9SSkZMcXIHvHxP8Y8vdp6melftwuVPEcD4jFBmUdaOyVCSjat9vUbu7pGjO7AKBdieAxX1z1spNyIxoRf5PXUKzIwZWIYG4IOkYy6uWtis1hKg7wHEc92zur0rSIeTSfwNVTVD0k8cy7R8Q5rxGEdJEWRDdWAIPQ7o7qiM7HQqknyxLI00skjHS7EnV5JP26ZoSbmNrj6TumayGOglttay/c6zJZOxW9knQ6EHy07pnpIpYhwMo9DrMtP8Ar6Xq9vuDumerekjPKUemsywXr6a3BifsDumZxe1oZwBcqO0PI6zI4y1W8hGhEP3OjdCAQQQCCLEdMVMDU08sJ/K1geY4HVbMZNTmGjDkWMpv5cN1zqjMkYqY1JZBZwOK8/LVUFI1ZULHbuDS55L/AO4AAAAFgBYDkN10EWsORvjM8uamcyxLeEn/AKHkemohgkqJVjiQlj+3XwxR0kdHCI00k6Wbmd3IDAggEEWIOw4rclYEyUguOMZOkeGHRkYqylWG0EWPuaLXJxSZbU1RBCFUO12FvsOOKSkgo07MS6T8THad524np4JxaaNCOZ2jzxLlWWkkrVezP1qw/fByql4ZpFbqBiPKqIkdrMVbopUepxT5dQQkGNFdhxY9s4N+R3clUUlmCgbSTYYnziiiJCsZDyUaPucS57O2iKJEHM944kzCtl+Kpe3IHs+mCzMe85bxJOLAbAPwsOWB0/bRhKqpiI7FRILdScRZzWJYMySDqLH7jEOewNomjaM8x3hiKeCdbwyqw6HZ5bkzqilnYKo2kmwGKrO0W6Uqdo/72+HyHHE9TPUm80rMeR2Dy1iMyEMjFSNIINjimzueMgTr7VeY0NinqYKpO1C4PMbCPEa+sroaJLue05F1QbT/AIGKusnq2vI2gHuoPhGNuvjkeJ1kjcqw2EHFBm6TFYqmyvsDflP+DrcxzBKJOytmlYd0cAOZw8jyu0jsWcm5J47nlmZlCsFS912LITs6Hpq6yrSjgaUgE7FHM4kkeV3kkYlmNyTum3RjKK8ygU0rXdR3CeIHDxGpHLGZ1f8AV1LWPcTur/J892R3jdJENmUgqeRxSzrVQRzD8w0jkeI1GZTmnopXBsx7q+Jxs3fIpyGmpydBHbXxG3UZ9IbU0Q2XLH0/C3XFuuLdcW64t1xbri3XFuuLdcW64t1xbri3XFuuLdcW64t1xbri3XFuuLdcW64y6T2VbTm+gvY+B0ajPvnwH+2R++8QfPg/UX1wdvv5782n+g+u8U/z4f1F9dRnvzaf6D67xT/Ph/UX11Gf/Np/oPrvFP8APh/UX193/8QAGxEAAgMBAQEAAAAAAAAAAAAAAREAIDBAMVD/2gAIAQIBAT8A+soooRwngOQ9seA8B0Bjj0UVFFFgsVYZmg0NBwDf/8QAGhEAAgMBAQAAAAAAAAAAAAAAAREAMEAgUP/aAAgBAwEBPwD1nHHHYTyMAwDAKzyLFFFY4+XifRrHBwHAb//Z'");

        Flux.just(ph1)
                .flatMap(p -> photoService.insertPhoto(Mono.just(p))
                        .log(p.toString()))
                .subscribe();
    }
}

