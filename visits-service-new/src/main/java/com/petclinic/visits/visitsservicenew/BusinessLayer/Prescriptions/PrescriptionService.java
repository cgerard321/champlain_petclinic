package com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions;

import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import reactor.core.publisher.Mono;

public interface PrescriptionService {
    PrescriptionResponseDTO createPrescription(String visitId, PrescriptionResponseDTO request);
    Mono<byte[]> getPrescriptionPdf(String visitId, String prescriptionId);
}
