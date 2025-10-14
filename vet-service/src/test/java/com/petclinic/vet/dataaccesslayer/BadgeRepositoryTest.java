//Spring Boot version incompatibility issue with postgresql r2dbc
//need to be fixed in a later sprint

//package com.petclinic.vet.dataaccesslayer;

//import com.petclinic.vet.dataaccesslayer.badges.Badge;
//import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
//import com.petclinic.vet.dataaccesslayer.badges.BadgeTitle;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.reactivestreams.Publisher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
//import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
//import org.springframework.util.StreamUtils;
//import reactor.core.publisher.Flux;
//import reactor.test.StepVerifier;

//import java.io.IOException;

/*@DataR2dbcTest
class BadgeRepositoryTest {
    @Autowired
    private BadgeRepository badgeRepository;
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;
    Badge badge1;

    @BeforeEach
    public void setupDB() throws IOException {
        badge1=buildBadge();

        Publisher<Badge> setup=badgeRepository.deleteAll()
                .thenMany(Flux.concat(
                        badgeRepository.save(badge1)
                ));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldSaveSingleBadge() throws IOException {
        //arrange
        Badge newBadge=buildBadge();
        Publisher<Badge> setup=badgeRepository.save(newBadge);

        //act and assert
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findBadgeByValidVetId_shouldSucceed(){
        StepVerifier
                .create(badgeRepository.findByVetId("1"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findBadgeByInvalidVetId_shouldNotSucceed(){
        StepVerifier
                .create(badgeRepository.findByVetId("123"))
                .expectNextCount(0)
                .verifyComplete();
    }

    ClassPathResource cpr=new ClassPathResource("images/half-full_food_bowl.png");

    private Badge buildBadge() throws IOException {
        return Badge.builder()
                .vetId("1")
                .badgeDate("2017")
                .badgeTitle(BadgeTitle.MUCH_APPRECIATED)
                .data(StreamUtils.copyToByteArray(cpr.getInputStream()))
                .build();
    }
}*/
