package com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions;

import com.itextpdf.text.DocumentException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.PrescriptionPdfGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final Map<String, Map<String, PrescriptionResponseDTO>> store = new ConcurrentHashMap<>();
    private final Map<String, Map<String, byte[]>> pdfStore = new ConcurrentHashMap<>();

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
    public byte[] getPrescriptionPdf(String visitId, String prescriptionId) {
        Map<String, byte[]> m = pdfStore.get(visitId);
        if (m == null || !m.containsKey(prescriptionId)) {
            throw new NotFoundException("Prescription PDF not found for id: " + prescriptionId);
        }
        return m.get(prescriptionId);
    }
}
