package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetMapper;
import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import com.petclinic.vets.datalayer.VetRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 *
 * @author Tymofiy Bun
 * @author Christian Chitanu: Added enable and disable tests for jcoco + added create vet test
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "spring.datasource.url=jdbc:h2:mem:vets-db"})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class VetBusinessLayerTest
{

    @Autowired
    private VetRepository vetRepository;


    @Autowired
    private VetService vetService;





    @BeforeEach
    void setup()
    {
        vetRepository.deleteAll();
        VetDTO vet1 = new VetDTO(234568, "James", "Carter", "carter.james@email.com", "#2384", "practicing since 3 years", "Monday, Tuesday, Friday", 1, null);
        vetService.createVet(vet1);
        VetDTO vet2 = new VetDTO(327874, "Helen", "Leary", "leary.helen@email.com", "#2385", "Practicing since 10 years", "Wednesday, Thursday", 1, null);
        vetService.createVet(vet2);
        VetDTO vet3 = new VetDTO(147258, "James2", "Carter2", "carter2.james@email.com", "#2384", "practicing since 32 years", "Monday, Tuesday, Friday", 0, null);
        vetService.createVet(vet3);
        VetDTO vet4 = new VetDTO(369852, "Helen2", "Leary2", "leary2.helen@email.com", "#2385", "Practicing since 103 years", "Wednesday, Thursday", 0, null);
        vetService.createVet(vet4);
    }

    @Test
    public void createNewVetTest()
    {
        VetDTO vet1 = new VetDTO(234568, "James", "Carter", "carter.james@email.com", "#2384", "practicing since 3 years", "Monday, Tuesday, Friday", 1, null);
        vetService.createVet(vet1);
        System.out.println(vetRepository.count());
        VetDTO vet2 = new VetDTO(327874, "Helen", "Leary", "leary.helen@email.com", "#2385", "Practicing since 10 years", "Wednesday, Thursday", 1, null);
        vetService.createVet(vet2);
        assertThat(vetRepository.count()).isGreaterThan(0);
        Vet vet1 = new Vet(5, 234567, "James3", "Carter", "carter.james@email.com", "(514)-634-8276 #2384", "practicing since 3 years", "Monday, Tuesday, Friday", 1, null);
        assertThrows(NoSuchElementException.class,()->{
            vetRepository.findByVetId(234567).get();
        });
        Vet result = vetService.createVet(vet1);
        Vet repo = vetRepository.findByVetId(234567).get();
        assertEquals(repo.getVetId(),result.getVetId());
        assertEquals(repo.getFirstName(),result.getFirstName());
    }
    @Test
    public void getAllVetsTest()
    {
        int expectedNumOfVets = 4;
        assertThat(vetService.getAllVets().size()).isEqualTo(expectedNumOfVets);
    }
    @Test
    public void getByVetIdTest()
    {
        assertEquals(vetService.getVetByVetId(234568).getFirstName(), "James");
        assertEquals(vetService.getVetByVetId(234568).getLastName(), "Carter");
        assertEquals(vetService.getVetByVetId(234568).getEmail(), "carter.james@email.com");
        assertEquals(vetService.getVetByVetId(234568).getPhoneNumber(), "(514)-634-8276 #2384");
        assertEquals(vetService.getVetByVetId(234568).getResume(), "practicing since 3 years");
        assertEquals(vetService.getVetByVetId(234568).getWorkday(), "Monday, Tuesday, Friday");
    }

    @Test
    public void updateVetByVetId()
    {
        VetDTO vet1 = new VetDTO(234568, "JamesUpdate", "CarterUpdate", "carterUpdate.james@email.com", "(514)-634-8276 #2384", "practicing since 3 yearsUpdate", "Monday, Tuesday, Friday", 1, null);


        vetService.updateVet(234568, vet1);

        assertEquals(vetService.getVetByVetId(234568).getFirstName(), "JamesUpdate");
        assertEquals(vetService.getVetByVetId(234568).getLastName(), "CarterUpdate");
        assertEquals(vetService.getVetByVetId(234568).getEmail(), "carterUpdate.james@email.com");
        assertEquals(vetService.getVetByVetId(234568).getResume(), "practicing since 3 yearsUpdate");

        Vet vet2 = new Vet(1, 234568, "", "", "", "", "", "", 1, null);


        vetService.updateVet(vetService.getVetByVetId(234568), vet2);

        assertEquals(vetService.getVetByVetId(234568).getFirstName(), "JamesUpdate");
        assertEquals(vetService.getVetByVetId(234568).getLastName(), "CarterUpdate");
        assertEquals(vetService.getVetByVetId(234568).getEmail(), "carterUpdate.james@email.com");
        assertEquals(vetService.getVetByVetId(234568).getResume(), "practicing since 3 yearsUpdate");
    }

    @Test
    public void getAllDisabledVets()
    {
        assertEquals(vetService.getAllDisabledVets().size(),2);
    }
    @Test
    public void getAllEnabledVets()
    {
        assertEquals(vetService.getAllEnabledVets().size(),2);

    }

    @Test
    @DisplayName("Enable Vet Service Test")
    public void enableVetTest(){
        Vet activeVet = new Vet();
        activeVet.setIsActive(1);
        Vet searchedVet = vetService.getVetByVetId(147258);
        assertEquals(searchedVet.getFirstName(),"James2");
        assertEquals(searchedVet.getIsActive(),0);
        Vet resultVet = vetService.enableVet(searchedVet,activeVet);
        assertEquals(resultVet.getFirstName(),"James2");
        assertEquals(resultVet.getIsActive(),1);
    }
    @Test
    @DisplayName("Disable Vet Service Test")
    public void disableVetTest(){
        Vet activeVet = new Vet();
        activeVet.setIsActive(0);
        Vet searchedVet = vetService.getVetByVetId(234568);
        assertEquals(searchedVet.getFirstName(),"James");
        assertEquals(searchedVet.getIsActive(),1);
        Vet resultVet = vetService.disableVet(searchedVet,activeVet);
        assertEquals(resultVet.getFirstName(),"James");
        assertEquals(resultVet.getIsActive(),0);
    }



}
