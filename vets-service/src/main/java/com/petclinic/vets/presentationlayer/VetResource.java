package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Vet;
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
 */

@RequestMapping("/vets")
@RestController
@Timed("petclinic.vets")
//@RequiredArgsConstructor
class VetResource {

    private final VetService vetService;
    private static final Logger LOG = LoggerFactory.getLogger(VetResource.class);


    VetResource(VetService vetService)
    {
        this.vetService = vetService;
    }

    @GetMapping
    public List<Vet> showResourcesVetList() {
        List<Vet> vetList = vetService.getAllEnabledVets();
        for(Vet vet: vetList){                              //decompress all images returned in order for them
            vet.setImage(decompressBytes(vet.getImage()));  //to be in right format for front-end
        }
        return vetList;
    }

//    @GetMapping("/enabled")
//    public List<Vet> showResourcesVetEnabledList() {
//        return vetService.getAllEnabledVets();
//    }

    @GetMapping("/disabled")
    public List<Vet> showResourcesVetDisabledList() {
        List<Vet> vetList = vetService.getAllDisabledVets();
        for(Vet vet: vetList){                              //decompress all images returned in order for them
            vet.setImage(decompressBytes(vet.getImage()));  //to be in right format for front-end
        }
        return vetList;
    }


    @GetMapping("/{vetId}")
    public Vet findVet(@PathVariable int vetId)
    {
        LOG.debug("/vet MS return the found product for vetId: " + vetId);

        if(vetId < 1) throw new InvalidInputException("Invalid vetId: " + vetId);

        Vet vet = vetService.getVetByVetId(vetId);
        vet.setImage(decompressBytes(vet.getImage()));
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

    @PutMapping( value = "/{vetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Vet updateVet(@PathVariable int vetId, @RequestBody Vet vetRequest)
    {
        return  vetService.updateVet(vetService.getVetByVetId(vetId),vetRequest);
    }

    @PutMapping(path = "/{vetId}/disableVet",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Vet disableVet(@PathVariable("vetId") int vetId, @RequestBody Vet vetRequest) {
        Vet vet = vetService.getVetByVetId(vetId);
        vetService.disableVet(vet,vetRequest);
        return vet;
    }


    @PutMapping(path = "/{vetId}/enableVet",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Vet enableVet(@PathVariable("vetId") int vetId, @RequestBody Vet vetRequest) {
        Vet vet = vetService.getVetByVetId(vetId);
        vetService.enableVet(vet,vetRequest);
        return vet;
    }

        //This method is used to compress the vet Image before storing it in the database
        public static byte[] compressBytes(byte[] data){
                Deflater deflater = new Deflater();
                deflater.setInput(data);
                deflater.finish();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
                byte[] buffer = new byte[1024];
                while(!deflater.finished()) {
                    int count = deflater.deflate(buffer);
                    outputStream.write(buffer, 0, count);
                }
                try {
                    outputStream.close();
                }catch (IOException e) {
                }
                return outputStream.toByteArray();
            }

        public static byte[] decompressBytes(byte[] data) {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            try {
                while (!inflater.finished()) {
                    int count = inflater.inflate(buffer);
                    outputStream.write(buffer, 0, count);
                }
                outputStream.close();
            } catch (IOException ioe) {
            } catch (DataFormatException e) {
            }
            return outputStream.toByteArray();
        }
}




