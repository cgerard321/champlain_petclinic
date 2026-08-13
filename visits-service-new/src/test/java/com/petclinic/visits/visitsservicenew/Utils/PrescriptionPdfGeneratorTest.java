package com.petclinic.visits.visitsservicenew.Utils;

import com.itextpdf.text.DocumentException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.MedicationDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionPdfGeneratorTest {

    @Test
    void generatePrescriptionPdf_ReturnsNonEmptyPdf_WhenValidPrescription() throws DocumentException {
        // Arrange
        PrescriptionResponseDTO prescription = new PrescriptionResponseDTO();
        prescription.setPrescriptionId("RX-001");
        prescription.setDate(LocalDate.of(2025, 1, 15));
        prescription.setVetFirstName("John");
        prescription.setVetLastName("Doe");
        prescription.setOwnerFirstName("Sarah");
        prescription.setOwnerLastName("Smith");
        prescription.setPetName("Buddy");

        MedicationDTO med1 = new MedicationDTO();
        med1.setName("Amoxicillin");
        med1.setStrength("50mg");
        med1.setDosage("1 tablet");
        med1.setFrequency("Twice a day");
        med1.setQuantity(14);

        prescription.setMedications(List.of(med1));
        prescription.setDirections("Give with food.");

        // Act
        byte[] result = PrescriptionPdfGenerator.generatePrescriptionPdf(prescription);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 200); // PDF must be > 200 bytes
    }

    @Test
    void generatePrescriptionPdf_HandlesEmptyMedicationList() throws DocumentException {
        // Arrange
        PrescriptionResponseDTO prescription = new PrescriptionResponseDTO();
        prescription.setPrescriptionId("RX-002");
        prescription.setDate(LocalDate.now());
        prescription.setMedications(List.of()); // empty list

        // Act
        byte[] result = PrescriptionPdfGenerator.generatePrescriptionPdf(prescription);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 200);
    }

    @Test
    void generatePrescriptionPdf_HandlesNullMedicationList() throws DocumentException {
        // Arrange
        PrescriptionResponseDTO prescription = new PrescriptionResponseDTO();
        prescription.setPrescriptionId("RX-003");
        prescription.setDate(LocalDate.now());
        prescription.setMedications(null); // null list

        // Act
        byte[] result = PrescriptionPdfGenerator.generatePrescriptionPdf(prescription);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 200);
    }

    @Test
    void generatePrescriptionPdf_HandlesMissingOptionalFields() throws DocumentException {
        // Arrange
        PrescriptionResponseDTO prescription = new PrescriptionResponseDTO();
        // All fields null intentionally

        // Act
        byte[] result = PrescriptionPdfGenerator.generatePrescriptionPdf(prescription);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 200);
    }

    @Test
    void generatePrescriptionPdf_ThrowsException_WhenPrescriptionIsNull() {
        // Act / Assert
        assertThrows(NullPointerException.class, () -> {
            PrescriptionPdfGenerator.generatePrescriptionPdf(null);
        });
    }

}