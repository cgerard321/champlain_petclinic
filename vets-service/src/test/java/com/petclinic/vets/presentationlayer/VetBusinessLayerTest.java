package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetMapper;
import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Specialty;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 *
 * @author Tymofiy Bun
 * @author Christian Chitanu: Added enable and disable tests for jcoco + added create vet test
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("h2")
public class VetBusinessLayerTest
{


    @Autowired
    VetService vetService;


    @Autowired
    VetRepository vetRepository;

    @AfterEach
    void teardown()
    {
        vetRepository.deleteAll();
    }

    @Test
    public void createNewVetTest()
    {
        Vet vet1 = new Vet(5, 234567, "James3", "Carter",
                "carter.james@email.com", "(514)-634-8276 #2384",null,
                "practicing since 3 years", "Monday, Tuesday, Friday", 1, null);
        assertThrows(NoSuchElementException.class,()->{
            vetRepository.findByVetId(234567).get();
        });
        Vet result = vetService.createVet(vet1);
        Vet repo = vetRepository.findByVetId(234567).get();
        assertEquals(repo.getVetId(),result.getVetId());
        assertEquals(repo.getFirstName(),result.getFirstName());
    }
    @Test
    public void createNewVetFromDTOWithoutSpecialtiesTest()
    {
        VetDTO vetDTO = new VetDTO(456791, "JamesFive", "Carter",
                "carter.james@email.com", "2384",null,
                "practicing since 999 years", "Monday, Tuesday, Friday", 1, null);
        assertThrows(NoSuchElementException.class,()->{
            vetRepository.findByVetId(456791).get();
        });
        VetDTO result = vetService.createVetFromDTO(vetDTO);
        VetDTO repo = vetService.getVetDTOByVetId(456791);
        assertEquals(repo.getVetId(),result.getVetId());
        assertEquals(repo.getFirstName(),result.getFirstName());
    }
    @Test
    public void createNewVetFromDTOWithSpecialtiesTest()
    {
//        Specialty specialty = new Specialty();
//        specialty.setName("tester");
//        Set<Specialty> specialties= new HashSet<>();
//        specialties.add(specialty);
        VetDTO vetDTO = new VetDTO(456791, "JamesFive", "Carter",
                "carter.james@email.com", "1234",null,
                "practicing since 999 years", "Monday, Tuesday, Friday", 1, new HashSet<Specialty>(){{
            add(new Specialty(1, 0, "Test"));
        }});

        assertThrows(NoSuchElementException.class,()->{
            vetRepository.findByVetId(456791).get();
        });

        VetDTO result = vetService.createVetFromDTO(vetDTO);

        VetDTO repo = vetService.getVetDTOByVetId(456791);
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
    public void getAllVetsDTOTest()
    {
        int expectedNumOfVets = 4;
        assertThat(vetService.getAllVetDTOs().size()).isEqualTo(expectedNumOfVets);
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
        Specialty specialty = new Specialty(1,123456,"tester");
        Set<Specialty> specialties= new HashSet<>();
        specialties.add(specialty);
        Vet vet1 = new Vet(1, 784567, "JamesUpdate", "CarterUpdate", "carterUpdate.james@email.com", "(514)-634-8276 #2384",null,
                "practicing since 3 yearsUpdate", "Monday, Tuesday, Friday", 1, specialties);


        vetService.updateVet(vetService.getVetByVetId(234568), vet1);

        assertEquals(vetService.getVetByVetId(234568).getFirstName(), "JamesUpdate");
        assertEquals(vetService.getVetByVetId(234568).getLastName(), "CarterUpdate");
        assertEquals(vetService.getVetByVetId(234568).getEmail(), "carterUpdate.james@email.com");
        assertEquals(vetService.getVetByVetId(234568).getResume(), "practicing since 3 yearsUpdate");
        assertEquals(vetService.getVetByVetId(234568).getSpecialties().get(0).getName(), "tester");


        Vet vet2 = new Vet(1, 456123, "", "", "", "",null, "", "", 1, null);


        vetService.updateVet(vetService.getVetByVetId(234568), vet2);

        assertEquals(vetService.getVetByVetId(234568).getFirstName(), "JamesUpdate");
        assertEquals(vetService.getVetByVetId(234568).getLastName(), "CarterUpdate");
        assertEquals(vetService.getVetByVetId(234568).getEmail(), "carterUpdate.james@email.com");


        Vet vet3 = new Vet(1, 456123, null, null, null, null,null, null, null, 1, null);


        vetService.updateVet(vetService.getVetByVetId(234568), vet3);

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

    @Test
    @DisplayName("Delete Vet Service Test Valid Id")
    public void deleteVetByVetId(){
        assertEquals(vetService.getAllVets().size(),4);
        vetService.deleteVetByVetId(234568);
        assertEquals(vetService.getAllVets().size(),3);
    }
    @Test
    @DisplayName("Delete Vet Service Test Invalid VetId")
    public void deleteVetByVetIdInvlaidId(){
        assertEquals(vetService.getVetByVetId(234568).getFirstName(), "James");
        assertEquals(vetService.getAllVets().size(),4);
        assertThrows(NotFoundException.class, () -> vetService.deleteVetByVetId(1));
    }
}
