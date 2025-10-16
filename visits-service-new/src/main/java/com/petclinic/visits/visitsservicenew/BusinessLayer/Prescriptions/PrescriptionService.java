package com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions;

import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;

public interface PrescriptionService {
    PrescriptionResponseDTO createPrescription(String visitId, PrescriptionResponseDTO request);
    byte[] getPrescriptionPdf(String visitId, String prescriptionId);
}
