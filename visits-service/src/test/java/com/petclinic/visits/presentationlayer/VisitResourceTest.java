package com.petclinic.visits.presentationlayer;

import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static com.petclinic.visits.datalayer.Visit.visit;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
public class VisitResourceTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	VisitsService visitsService;

	@Test
	void whenValidPetIdThenShouldReturnVisitsForPet() throws Exception {

		given(visitsService.getVisitsForPet(1))
				.willReturn(
						asList(
								visit()
										.id(1)
										.petId(1)
										.build(),
								visit()
										.id(2)
										.petId(1)
										.build()
						)
				);

		mvc.perform(get("/visits/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2))
				.andExpect(jsonPath("[0].petId").value(1))
				.andExpect(jsonPath("[1].petId").value(1));
	}


	@Test
	void shouldFetchVisits() throws Exception {
		given(visitsService.getVisitsForPets(asList(111, 222)))
				.willReturn(
						asList(
								visit()
										.id(1)
										.petId(111)
										.build(),
								visit()
										.id(2)
										.petId(222)
										.build(),
								visit()
										.id(3)
										.petId(222)
										.build()
						)
				);

		mvc.perform(get("/pets/visits?petId=111,222"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(1))
				.andExpect(jsonPath("$.items[1].id").value(2))
				.andExpect(jsonPath("$.items[2].id").value(3))
				.andExpect(jsonPath("$.items[0].petId").value(111))
				.andExpect(jsonPath("$.items[1].petId").value(222))
				.andExpect(jsonPath("$.items[2].petId").value(222));
	}

	//Do not uncomment this method
//	@Test
//	public void SaveVisits() {
//	int visitId = 5;
//	Visit visit = new Visit(visitId, new Date("2015/06/17"), "Head accident", 1);
//	visitRepository.save(visit);
//
//	if(visitRepository.findById(visitId).isPresent()){
//		System.out.println("Visits is present");
//	}
//	else{
//		System.out.println("Visits is not present");
//	}
//	visitRepository.findById(visitId).ifPresent(e -> visitRepository.delete(e));
//	}

	@Test
	public void whenValidVisitIdDeleteTheVisit() throws Exception {
		mvc.perform(delete("/visits/1"))
				.andExpect(status().isOk());
	}

	@Test
	public void whenInvalidVisitIdDontDeleteTheVisit() throws Exception {
		mvc.perform(delete("/visits/faso"))
				.andExpect(status().isBadRequest());
	}
}




