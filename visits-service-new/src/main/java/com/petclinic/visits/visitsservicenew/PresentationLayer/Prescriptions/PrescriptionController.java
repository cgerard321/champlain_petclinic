package com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions;

import com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions.PrescriptionService;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/visits/{visitId}/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("")
    public Mono<ResponseEntity<PrescriptionResponseDTO>> createPrescription(
            @PathVariable String visitId,
            @RequestBody PrescriptionResponseDTO dto) {
        return prescriptionService.createPrescription(visitId, dto)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{prescriptionId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> downloadPdf(
            @PathVariable String visitId,
            @PathVariable String prescriptionId) {

        return prescriptionService.getPrescriptionPdf(visitId, prescriptionId)
                .map(pdf -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.add(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=prescription-" + prescriptionId + ".pdf");
                    return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
                })
                .onErrorResume(NotFoundException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

}
