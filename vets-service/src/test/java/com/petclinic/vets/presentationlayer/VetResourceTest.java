package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.presentationlayer.VetResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.swing.text.html.parser.Entity;
import java.util.Optional;
import static  org.hamcrest.MatcherAssert.assertThat;
import static  org.hamcrest.Matcher.*;
import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maciej Szarlinski
 */

@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	VetRepository vetRepository;

	@Test
	void shouldGetAListOfVets() throws Exception {

		Vet vet = new Vet();
		vet.setId(1);

		given(vetRepository.findAll()).willReturn(asList(vet));

		mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));
	}

	@Test
	void addANewVet() throws Exception {
		//arrange
		Vet vet = new Vet();
		vet.setId(1);
		Vet vetSaved = vetRepository.save(vet);
		//act //assert
		when(vetRepository.findById(vet.getId())).thenReturn(Optional.of(vet));

		assertEquals(vetSaved.getId(), vet.getId());
	}

	@Test
	void disableAVet() throws Exception {
		//arrange
		Vet vet = new Vet();
		vet.setEnable(false);
		//act
		given(vetRepository.findAll()).willReturn(asList(vet));
		//assert
		mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].enable").value(false));
	}
}
