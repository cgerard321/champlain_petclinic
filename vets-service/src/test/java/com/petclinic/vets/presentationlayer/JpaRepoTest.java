package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest

public class JpaRepoTest
{

    @Autowired
    private VetRepository vetRepository;

    @BeforeEach
    public void setUpDB()
    {
        Vet vet1 = new Vet(1, 234568, "James", "Carter", "carter.james@email.com", "(514)-634-8276 #2384",null, "practicing since 3 years", "Monday, Tuesday, Friday", 1, null);
        vetRepository.save(vet1);
        Vet vet2 = new Vet(2, 327874, "Helen", "Leary", "leary.helen@email.com", "(514)-634-8276 #2385",null, "Practicing since 10 years", "Wednesday, Thursday", 1, null);
        vetRepository.save(vet2);
        Vet vet3 = new Vet(3, 147258, "James2", "Carter2", "carter2.james@email.com", "(514)-634-8276 #2384",null, "practicing since 32 years", "Monday, Tuesday, Friday", 0, null);
        vetRepository.save(vet3);
        Vet vet4 = new Vet(4, 369852, "Helen2", "Leary2", "leary2.helen@email.com", "(514)-634-8276 #2385",null, "Practicing since 103 years", "Wednesday, Thursday", 0, null);
        vetRepository.save(vet4);
    }

    @Test
    public void saveVetTest()
    {
        Vet vet1 = new Vet(1, 234568, "James", "Carter", "carter.james@email.com", "(514)-634-8276 #2384",null, "practicing since 3 years", "Monday, Tuesday, Friday", 1, null);

        vetRepository.save(vet1);
        assertThat(vetRepository.count()).isGreaterThan(0);
    }

    @Test
    public void getVetByVetIdTest()
    {
        Vet vet = vetRepository.findByVetId(234568).get();
        assertEquals(vet.getVetId(), 234568);
    }

    @Test
    public void getDisabledVetList()
    {
        assertThat(vetRepository.findAllDisabledVets().size()).isEqualTo(2);
    }

    @Test
    public void getEnabledVetList()
    {
        assertThat(vetRepository.findAllEnabledVets().size()).isEqualTo(2);
    }

    @Test
    public void deleteVetByVetId()
    {
        assertThat(vetRepository.findAll().size()).isEqualTo(4);
        vetRepository.deleteByVetId(234568);
        assertThat(vetRepository.findAll().size()).isEqualTo(3);
    }
}
