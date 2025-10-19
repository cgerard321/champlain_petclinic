package com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions;

import com.itextpdf.text.DocumentException;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FilesServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileRequestDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileResponseDTO;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.PrescriptionPdfGenerator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final VisitRepo visitRepository;
    private final FilesServiceClient filesServiceClient;

    public PrescriptionServiceImpl(VisitRepo visitRepository, FilesServiceClient filesServiceClient) {
        this.visitRepository = visitRepository;
        this.filesServiceClient = filesServiceClient;
    }

    @Override
    public Mono<PrescriptionResponseDTO> createPrescription(String visitId, PrescriptionResponseDTO request) {
        String prescriptionId = request.getPrescriptionId();
        if (prescriptionId == null || prescriptionId.isBlank()) {
            prescriptionId = UUID.randomUUID().toString();
            request.setPrescriptionId(prescriptionId);
        }

        if (request.getDate() == null) {
            request.setDate(LocalDate.now());
        }

        final String finalPrescriptionId = prescriptionId;
        byte[] pdf;

        try {
            pdf = PrescriptionPdfGenerator.generatePrescriptionPdf(request);
        } catch (DocumentException e) {
            return Mono.error(new RuntimeException("Failed to generate prescription PDF", e));
        }

        FileRequestDTO fileRequest = new FileRequestDTO();
        fileRequest.setFileName("prescription-" + finalPrescriptionId + ".pdf");
        fileRequest.setFileType("application/pdf");
        fileRequest.setFileData(pdf);

        // Find the visit → upload file → save fileId → save visit → return prescription info
        return visitRepository.findByVisitId(visitId)
                .switchIfEmpty(Mono.error(new NotFoundException("Visit not found: " + visitId)))
                .flatMap(visit ->
                        filesServiceClient.addFile(fileRequest)
                                .flatMap(fileDetails -> {
                                    visit.setPrescriptionFileId(fileDetails.getFileId());
                                    return visitRepository.save(visit);
                                })
                                .thenReturn(request)
                );
    }


    @Override
    public Mono<byte[]> getPrescriptionPdf(String visitId) {
        return visitRepository.findByVisitId(visitId)
                .switchIfEmpty(Mono.error(new NotFoundException("Visit not found: " + visitId)))
                .flatMap(visit -> {
                    if (visit.getPrescriptionFileId() == null) {
                        return Mono.error(new NotFoundException("No prescription file linked to visit: " + visitId));
                    }

                    return filesServiceClient.getFile(visit.getPrescriptionFileId())
                            .flatMap(fileResponse -> Mono.just(fileResponse.getFileData()));
                });
    }


}
