package com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions;

import com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions.PrescriptionService;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visits/{visitId}/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PrescriptionResponseDTO> createPrescription(
            @PathVariable String visitId,
            @RequestBody PrescriptionResponseDTO request) {
        PrescriptionResponseDTO created = prescriptionService.createPrescription(visitId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(value = "/{prescriptionId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable String visitId,
            @PathVariable String prescriptionId) {
        try {
            byte[] pdf = prescriptionService.getPrescriptionPdf(visitId, prescriptionId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=prescription-" + prescriptionId + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
