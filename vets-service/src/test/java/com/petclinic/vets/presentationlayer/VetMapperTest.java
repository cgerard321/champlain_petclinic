package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetMapper;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class VetMapperTest {
    Vet vet1 = new Vet(1, 234568, "JamesUpdate", "CarterUpdate", "carterUpdate.james@email.com", "(514)-634-8276 #2384", null,
            "practicing since 3 yearsUpdate", "Monday, Tuesday, Friday", 1, new HashSet<>());
    Vet vet2 = new Vet(2, 234098, "JamesUpdate", "CarterUpdate", "carterUpdate.james@email.com", "(514)-634-8276 #2384", null,
            "practicing since 3 yearsUpdate", "Monday, Tuesday, Friday", 1, new HashSet<>());
    VetDTO vetDTO = new VetDTO(456791, "JamesFive", "Carter", "carter.james@email.com", "2384", null,
            "practicing since 999 years", "Monday, Tuesday, Friday", 1, new HashSet<>());
    VetDTO vetDTO2 = new VetDTO(754896, "JamesFive", "Carter", "carter.james@email.com", "2384", null,
            "practicing since 999 years", "Monday, Tuesday, Friday", 1, new HashSet<>());
    @Autowired
    private VetMapper vetMapper;

    @Test
    public void map_Vet_to_VetDTO() {
        VetDTO vet = vetMapper.vetToVetDTO(vet1);
        assertEquals(234568, vet.getVetId());
        assertEquals("JamesUpdate", vet.getFirstName());
        assertEquals("CarterUpdate", vet.getLastName());
        assertEquals("carterUpdate.james@email.com", vet.getEmail());
        assertEquals("(514)-634-8276 #2384", vet.getPhoneNumber());
        assertEquals(null, vet.getImage());
        assertEquals("practicing since 3 yearsUpdate", vet.getResume());
        assertEquals("Monday, Tuesday, Friday", vet.getWorkday());
        assertEquals(0, vet.getSpecialties().stream().count());
    }

    @Test
    public void map_VetDTO_to_Vet() {
        Vet vet = vetMapper.vetDTOToVet(vetDTO);
        assertEquals(456791, vet.getVetId());
        assertEquals("JamesFive", vet.getFirstName());
        assertEquals("Carter", vet.getLastName());
        assertEquals("carter.james@email.com", vet.getEmail());
        assertEquals("(514)-634-8276 #2384", vet.getPhoneNumber());
        assertEquals(null, vet.getImage());
        assertEquals("practicing since 999 years", vet.getResume());
        assertEquals("Monday, Tuesday, Friday", vet.getWorkday());
        assertEquals(0, vet.getSpecialties().stream().count());
    }

    @Test
    public void map_VetDTO_to_Null_Vet() {
        Vet vet = vetMapper.vetDTOToVet(null);
        assertNull(vet);
    }

    @Test
    public void map_VetDTO_Null_to_Vet() {
        VetDTO vet = vetMapper.vetToVetDTO(null);
        assertNull(vet);
    }

    @Test
    public void map_ListVet_to_VetDTOList() {
        List<Vet> vetList = new ArrayList();

        vetList.add(vet1);
        vetList.add(vet2);

        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);

        assertEquals(vetDTOList.size(), 2);
    }

    @Test
    public void map_VetDTOList_to_VetList() {
        List<VetDTO> vetDTOList = new ArrayList();

        vetDTOList.add(vetDTO);
        vetDTOList.add(vetDTO2);

        List<Vet> vetList = vetMapper.vetDTOListToVetList(vetDTOList);

        assertEquals(vetList.size(), 2);
    }

    @Test
    public void map_ListVet_to_Null_VetDTOList() {
        List<Vet> vetList = null;
        List<VetDTO> vetDTOList = vetMapper.vetListToVetDTOList(vetList);

        assertNull(vetDTOList);
    }

    @Test
    public void map_VetDTOList_to_Null_VetList() {
        List<VetDTO> vetDTOList = null;
        List<Vet> vetList = vetMapper.vetDTOListToVetList(vetDTOList);

        assertNull(vetList);
    }
}
