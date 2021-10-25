package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.petclinic.vets.datalayer.VetRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 *
 * User: @BunTymofiy
 * Date: 2021-9-27
 * Ticket: feat(vets-cpc-40): modify vet info
 *
 * User: @BunTymofiy
 * Date: 2021-9-28
 * Ticket: feat(VETS-CPC-65): disabled vet list
 *
 * User: @BunTymofiy
 * Date: 2021-10-11
 * Ticket: feat(VETS-CPC-228): add dto and vet mapper
 */

@RequestMapping("/vets")
@RestController
@Timed("petclinic.vets")
class VetResource {

    private final VetService vetService;
    private static final Logger LOG = LoggerFactory.getLogger(VetResource.class);

    VetResource(VetService vetService)
    {
        this.vetService = vetService;
    }

    @GetMapping("/enabled")
    public List<VetDTO> showResourcesVetListEnabled() {
        List<VetDTO> vetList = vetService.getAllEnabledVetDTOs();
        return vetList;
    }

    @GetMapping
    public List<VetDTO> showResourcesVetList() {
        List<VetDTO> vetList = vetService.getAllVetDTOs();
        return vetList;
    }

    @GetMapping("/disabled")
    public List<VetDTO> showResourcesVetDisabledList() {
        List<VetDTO> vetList = vetService.getAllDisabledVetDTOs();
        return vetList;
    }

    @GetMapping("/{vetId}")
    public VetDTO findVet(@PathVariable int vetId)
    {
        LOG.debug("/vet MS return the found product for vetId: " + vetId);
        if(vetId < 1) throw new InvalidInputException("Invalid vetId: " + vetId);
        VetDTO vet = vetService.getVetDTOByVetId(vetId);
        return vet;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Vet addVet(@Valid @RequestBody Vet vet)
    {
        return vetService.createVet(vet);
    }

    @PutMapping( value = "/{vetId}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public VetDTO updateVet(@PathVariable int vetId, @RequestBody VetDTO vetRequest)
    {
        return vetService.updateVetWithDTO(vetId,vetRequest);
    }

    @PutMapping(path = "/{vetId}/disableVet",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VetDTO disableVet(@PathVariable("vetId") int vetId, @RequestBody VetDTO vetRequest) {
        return vetService.disableVetFromDTO(vetId, vetRequest);
    }

    @PutMapping(path = "/{vetId}/enableVet",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VetDTO enableVet(@PathVariable("vetId") int vetId, @RequestBody VetDTO vetRequest) {
        return vetService.enableVetFromDTO(vetId, vetRequest);
    }

    @DeleteMapping(path ="/{vetId}")
    public void deleteByVetId(@PathVariable("vetId") int vetId ){
        vetService.deleteVetByVetIdFromVetDTO(vetId);
    }
}
