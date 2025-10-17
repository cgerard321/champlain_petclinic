package com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions;

import com.itextpdf.text.DocumentException;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FilesServiceClient;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.PrescriptionPdfGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final Map<String, Map<String, PrescriptionResponseDTO>> store = new ConcurrentHashMap<>();
    private final Map<String, Map<String, byte[]>> pdfStore = new ConcurrentHashMap<>();

    private final VisitRepo visitRepository;
    private final FilesServiceClient filesClient;

    public PrescriptionServiceImpl(VisitRepo visitRepository, FilesServiceClient filesClient) {
        this.visitRepository = visitRepository;
        this.filesClient = filesClient;
    }

    @Override
    public PrescriptionResponseDTO createPrescription(String visitId, PrescriptionResponseDTO request) {
        String prescriptionId = request.getPrescriptionId();
        if (prescriptionId == null || prescriptionId.isBlank()) {
            prescriptionId = UUID.randomUUID().toString();
            request.setPrescriptionId(prescriptionId);
        }

        if (request.getDate() == null) {
            request.setDate(LocalDate.now());
        }

        store.computeIfAbsent(visitId, k -> new ConcurrentHashMap<>())
                .put(prescriptionId, request);

        try {
            byte[] pdf = PrescriptionPdfGenerator.generatePrescriptionPdf(request);
            pdfStore.computeIfAbsent(visitId, k -> new ConcurrentHashMap<>())
                    .put(prescriptionId, pdf);
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate prescription PDF", e);
        }

        return request;
    }


    @Override
    public Mono<byte[]> getPrescriptionPdf(String visitId, String prescriptionId) {
        // Step 1: try in-memory cache
        Map<String, byte[]> visitPdfs = pdfStore.get(visitId);
        if (visitPdfs != null && visitPdfs.containsKey(prescriptionId)) {
            return Mono.just(visitPdfs.get(prescriptionId));
        }

        // Step 2: fallback — verify visit and fetch from Files Service
        return visitRepository.findByVisitId(visitId)
                .switchIfEmpty(Mono.error(new NotFoundException("Visit not found: " + visitId)))
                .flatMap(visit -> {
                    if (visit.getPrescriptionId() == null || !visit.getPrescriptionId().equals(prescriptionId)) {
                        return Mono.error(new NotFoundException(
                                "Prescription " + prescriptionId + " not found for visit " + visitId));
                    }

                    // Fetch file metadata + bytes, then return the bytes only
                    return filesClient.getFile(prescriptionId)
                            .map(file -> {
                                byte[] data = file.getFileData();
                                if (data == null || data.length == 0) {
                                    throw new NotFoundException("Prescription PDF not found for " + prescriptionId);
                                }
                                return data;
                            })
                            .switchIfEmpty(Mono.error(new NotFoundException(
                                    "Prescription PDF not found for " + prescriptionId)));
                });
    }



}
