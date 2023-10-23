package com.petclinic.visits.visitsservicenew.Utils;

import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class EntityDtoUtilTest {

    @Autowired
    private EntityDtoUtil entityDtoUtil;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;

    @Test
    public void testGenerateVisitIdString() {
        String visitId = entityDtoUtil.generateVisitIdString();

        // Assert that the generated visitId is not null
        assertNotNull(visitId);

        // Assert that the visitId is in UUID format
        assertTrue(visitId.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"));
    }

    @Test
    public void testToVisitEntity() {
        // Create a sample VisitRequestDTO
        VisitRequestDTO requestDTO = new VisitRequestDTO();
        requestDTO.setVisitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        requestDTO.setDescription("Sample description");
        // Set other properties as needed


        Visit visit = entityDtoUtil.toVisitEntity(requestDTO);

        // Assert that the properties are correctly copied
        assertEquals(requestDTO.getVisitDate(), visit.getVisitDate());
        assertEquals(requestDTO.getDescription(), visit.getDescription());
        // Add more assertions for other properties
    }


/*
    @Test
    public void testToVisitEntity_makeCallToVet



*/

}