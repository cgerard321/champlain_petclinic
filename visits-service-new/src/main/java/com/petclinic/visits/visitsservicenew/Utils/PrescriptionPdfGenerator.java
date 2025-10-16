package com.petclinic.visits.visitsservicenew.Utils;


import com.itextpdf.text.*;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.PrescriptionResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Prescriptions.MedicationDTO;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PrescriptionPdfGenerator {

    public static byte[] generatePrescriptionPdf(PrescriptionResponseDTO prescription) throws DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Header
        Paragraph header = new Paragraph("PetClinic\n123 Main Street\n(514) 555-1234",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK));
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        Paragraph title = new Paragraph("PRESCRIPTION",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(12);
        title.setSpacingAfter(12);
        document.add(title);

        // Metadata table (2 cols)
        PdfPTable meta = new PdfPTable(2);
        meta.setWidthPercentage(100);
        meta.setSpacingAfter(10);

        addMetaCell(meta, "Prescription ID:", Optional.ofNullable(prescription.getPrescriptionId()).orElse("N/A"));
        addMetaCell(meta, "Date:", (prescription.getDate() != null)
                ? prescription.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "N/A");
        addMetaCell(meta, "Prescribed By:", Optional.ofNullable(prescription.getVetFirstName()).orElse("") + " " + Optional.ofNullable(prescription.getVetLastName()).orElse(""));
        String owner = (Optional.ofNullable(prescription.getOwnerFirstName()).orElse("") + " " + Optional.ofNullable(prescription.getOwnerLastName()).orElse("")).trim();
        addMetaCell(meta, "Owner / Patient:", (owner.isEmpty() ? "N/A" : owner) + (prescription.getPetName() != null ? " / " + prescription.getPetName() : ""));
        document.add(meta);

        // Medications table
        PdfPTable medsTable = new PdfPTable(5);
        medsTable.setWidthPercentage(100);
        medsTable.setSpacingAfter(10);
        medsTable.setWidths(new float[]{3, 1.5f, 1.5f, 1.5f, 1});

        addHeaderCell(medsTable, "Medication");
        addHeaderCell(medsTable, "Strength");
        addHeaderCell(medsTable, "Dosage");
        addHeaderCell(medsTable, "Frequency");
        addHeaderCell(medsTable, "Qty");

        List<MedicationDTO> meds = Optional.ofNullable(prescription.getMedications()).orElse(Collections.emptyList());
        if (meds.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No medications", FontFactory.getFont(FontFactory.HELVETICA, 10)));
            empty.setColspan(5);
            empty.setPadding(8);
            medsTable.addCell(empty);
        } else {
            for (MedicationDTO m : meds) {
                medsTable.addCell(new PdfPCell(new Phrase(Optional.ofNullable(m.getName()).orElse(""), FontFactory.getFont(FontFactory.HELVETICA, 10))));
                medsTable.addCell(new PdfPCell(new Phrase(Optional.ofNullable(m.getStrength()).orElse(""), FontFactory.getFont(FontFactory.HELVETICA, 10))));
                medsTable.addCell(new PdfPCell(new Phrase(Optional.ofNullable(m.getDosage()).orElse(""), FontFactory.getFont(FontFactory.HELVETICA, 10))));
                medsTable.addCell(new PdfPCell(new Phrase(Optional.ofNullable(m.getFrequency()).orElse(""), FontFactory.getFont(FontFactory.HELVETICA, 10))));
                medsTable.addCell(rightAligned(Optional.ofNullable(m.getQuantity()).map(Object::toString).orElse("")));
            }
        }

        document.add(medsTable);

        // Directions / Notes block
        Paragraph directions = new Paragraph(
                Optional.ofNullable(prescription.getDirections()).orElse("No additional directions."),
                FontFactory.getFont(FontFactory.HELVETICA, 10));
        directions.setSpacingBefore(6);
        document.add(directions);

        // Signature block
        PdfPTable sign = new PdfPTable(2);
        sign.setWidthPercentage(60);
        sign.setSpacingBefore(30);
        sign.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell prescriberCell = new PdfPCell(new Phrase("Prescriber Signature:\n\n__________________________", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        prescriberCell.setBorder(Rectangle.NO_BORDER);
        sign.addCell(prescriberCell);

        PdfPCell dateCell = new PdfPCell(new Phrase("Date:\n\n" + (prescription.getDate() != null ? prescription.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "____"), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        sign.addCell(dateCell);

        document.add(sign);

        // Footer
        Paragraph footer = new Paragraph("Please follow dosing instructions. Contact PetClinic if you have questions.",
                FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY));
        footer.setSpacingBefore(20);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private static void addMetaCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE)));
        cell.setBackgroundColor(BaseColor.GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private static PdfPCell rightAligned(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
