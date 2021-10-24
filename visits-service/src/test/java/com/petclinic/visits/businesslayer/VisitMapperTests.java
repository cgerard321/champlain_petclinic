package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.petclinic.visits.datalayer.Visit.visit;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class VisitMapperTests {

    @Autowired
    VisitMapper mapper;

    @Test
    public void shouldConvertToModel() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-12");
        Visit entity = visit()
                .visitId(UUID.randomUUID())
                .petId(200)
                .date(date)
                .description("hello")
                .status(true)
                .practitionerId(123456)
                .build();

        VisitDTO model = mapper.entityToModel(entity);

        assertEquals(entity.getPractitionerId(), model.getPractitionerId());
        assertEquals(entity.getDate(), model.getDate());
        assertEquals(entity.getDescription(), model.getDescription());
        assertEquals(entity.isStatus(), model.isStatus());
        assertEquals(entity.getPetId(), model.getPetId());
        assertEquals(entity.getVisitId().toString(), model.getVisitId());
    }

    @Test
    public void shouldConvertVisitDtoToEntity() throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-12");
        VisitDTO model = new VisitDTO(UUID.randomUUID().toString(),date, "hello", 200, 123456, true);

        Visit entity = mapper.VisitDtoToEntity(model);

        assertEquals(model.getPractitionerId(), entity.getPractitionerId());
        assertEquals(model.getDate(), entity.getDate());
        assertEquals(model.getDescription(),entity.getDescription());
        assertEquals(model.isStatus(), entity.isStatus());
        assertEquals(model.getPetId(), entity.getPetId());
        assertEquals(model.getVisitId(), entity.getVisitId().toString());
    }

    @Test
    public void shouldConvertToEntityWithRandomVisitIdDWhenVisitIdLessDTO() throws ParseException {
        VisitIdLessDTO visitIdLessDTO = new VisitIdLessDTO();
        visitIdLessDTO.setDate(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-12"));
        visitIdLessDTO.setDescription("hello");
        visitIdLessDTO.setPetId(200);
        visitIdLessDTO.setPractitionerId(123456);
        visitIdLessDTO.setStatus(true);

        Visit entity = mapper.VisitIdLessDtoToEntity(visitIdLessDTO);

        assertEquals(visitIdLessDTO.getDate(), entity.getDate());
        assertEquals(visitIdLessDTO.getDescription(), entity.getDescription());
        assertEquals(visitIdLessDTO.getPetId(), entity.getPetId());
        assertEquals(visitIdLessDTO.getPractitionerId(), entity.getPractitionerId());
        assertEquals(visitIdLessDTO.isStatus(), entity.isStatus());
        System.out.println(entity.getVisitId());
        assertNotEquals(null, entity.getVisitId());
    }

    @Test
    public void shouldReturnNullWhenGivenNull(){
        assertNull(mapper.entityToModel(null));
        assertNull(mapper.VisitDtoToEntity(null));
        assertNull(mapper.VisitIdLessDtoToEntity(null));
    }
}
