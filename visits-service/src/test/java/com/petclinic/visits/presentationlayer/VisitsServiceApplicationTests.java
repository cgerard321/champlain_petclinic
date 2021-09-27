package com.petclinic.visits.presentationlayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.datasource.url=jdbc:h2:mem:visits-db"})
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class VisitsServiceApplicationTests {
/*
    private static final int VISIT_CREATED = 1;
    private static final int VISIT_DELETED = 3;
    private static final int VISIT_UPDATED = 2;

    @Autowired
    private WebTestClient client;


    @Autowired
    private VisitRepository repository;

    @BeforeEach
    public void setupDb(){ //added persistence
        repository.deleteAll();
    }

    @Test
    public void createVisit() throws ParseException {
        int expectedSize = 1;
        //create the recommendation

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date visitDate = sdf.parse("2023-12-26");

        Visit visit = new Visit(VISIT_CREATED, visitDate, "Badly hurt", VISIT_CREATED);


        //send the POST request
        client.post()
                .uri("/pets/visits")
                .body(just(visit), Visit.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertEquals(expectedSize, repository.findByPetId(VISIT_CREATED).size());

    }
    @Test
    public void updateVisit() throws ParseException {
        //create the recommendation

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date visitDate = sdf.parse("2003-04-16");
        java.util.Date visitDate2 = sdf.parse("2013-14-26");

        Visit visit = new Visit(VISIT_UPDATED, visitDate, "Badly hurt", VISIT_UPDATED);
        repository.save(visit);

        Visit visit2 = new Visit(VISIT_UPDATED, visitDate2, "Perfectly Fine", VISIT_UPDATED);


        //send the POST request
        client.put()
                .uri("/pets/visits/" + VISIT_UPDATED)
                .body(just(visit2), Visit.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        assertThat(visit2.getId(), is(VISIT_UPDATED));
        assertThat(visit2.getPetId(), is(VISIT_UPDATED));
        assertThat(visit2.getDate(), is(visitDate2));
        assertThat(visit2.getDescription(), is("Perfectly Fine"));

    }

    @Test
    void deleteVisit() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date visitDate = sdf.parse("2022-04-12");
        //create a review entity
        Visit v = new Visit(VISIT_DELETED, visitDate, "Lightly hurt", VISIT_DELETED);
        //save it
        repository.save(v);
        //verify there are exactly 1  entity for product id 1
        assertEquals(1, repository.findByPetId(VISIT_DELETED).size());
        //send the DELETE request
        client.delete()
                .uri("/pets/visits/" + VISIT_DELETED)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();
        //verify there are no entities for product id 1
        assertEquals(0, repository.findByPetId(VISIT_DELETED).size());
    }
*/
}