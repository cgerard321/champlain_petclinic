package com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions;

import com.itextpdf.text.DocumentException;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FilesServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FileRequestDTO;
import com.petclinic.visits.visitsservicenew.Exceptions.InvalidInputException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.Exceptions.UnprocessableEntityException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.PrescriptionPdfGenerator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
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
        if (request == null) {
            return Mono.error(new InvalidInputException("Request body is required"));
        }

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
            return Mono.error(new UnprocessableEntityException("Failed to generate prescription PDF"));
        }

        FileRequestDTO fileRequest = new FileRequestDTO();
        fileRequest.setFileName("prescription-" + finalPrescriptionId + ".pdf");
        fileRequest.setFileType(MediaType.APPLICATION_PDF_VALUE);
        fileRequest.setFileData(pdf);

        return visitRepository.findByVisitId(visitId)
                .switchIfEmpty(Mono.error(new NotFoundException("Visit not found: " + visitId)))
                .flatMap(visit ->
                        filesServiceClient.addFile(fileRequest)
                                .switchIfEmpty(Mono.error(new UnprocessableEntityException("File service returned empty response")))
                                .flatMap(fileDetails -> {
                                    if (fileDetails == null || fileDetails.getFileId() == null) {
                                        return Mono.error(new UnprocessableEntityException("File service did not return a fileId"));
                                    }
                                    visit.setPrescriptionFileId(fileDetails.getFileId());
                                    return visitRepository.save(visit);
                                })
                                .onErrorMap(e -> {
                                    if (e instanceof NotFoundException) return e;
                                    if (e instanceof UnprocessableEntityException) return e;
                                    return new UnprocessableEntityException("Failed to upload prescription file");
                                })
                                .thenReturn(request)
                )
                .onErrorMap(e -> {
                    if (e instanceof NotFoundException) return e;
                    if (e instanceof InvalidInputException) return e;
                    if (e instanceof UnprocessableEntityException) return e;
                    return new UnprocessableEntityException("Failed to create prescription");
                });
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
                            .switchIfEmpty(Mono.error(new NotFoundException("Prescription file not found: " + visit.getPrescriptionFileId())))
                            .flatMap(fileResponse -> {
                                if (fileResponse == null || fileResponse.getFileData() == null) {
                                    return Mono.error(new UnprocessableEntityException("File service returned empty file data"));
                                }
                                return Mono.just(fileResponse.getFileData());
                            })
                            .onErrorMap(e -> {
                                if (e instanceof NotFoundException) return e;
                                if (e instanceof UnprocessableEntityException) return e;
                                return new UnprocessableEntityException("Failed to retrieve prescription file");
                            });
                });
    }


}
