package com.petclinic.billing.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.petclinic.billing.datalayer.BillResponseDTO;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class PdfGenerator {

    public static byte[] generateBillPdf(BillResponseDTO bill) throws DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);

        document.open();

        // Header
        Paragraph header = new Paragraph("PetClinic\n123 Main Street\n(514) 555-1234",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK));
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        Paragraph title = new Paragraph("VETERINARY RECEIPT",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(15);
        title.setSpacingAfter(15);
        document.add(title);

        // Bill Metadata
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setSpacingAfter(10);

        addMetaCell(metaTable, "Bill ID:", Optional.ofNullable(bill.getBillId()).orElse("N/A"));
        addMetaCell(metaTable, "Date:", (bill.getDate() != null
                ? bill.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "N/A"));
        addMetaCell(metaTable, "Status:", (bill.getBillStatus() != null ? bill.getBillStatus().name() : "N/A"));
        addMetaCell(metaTable, "Visit Type:", Optional.ofNullable(bill.getVisitType()).orElse("N/A"));

        document.add(metaTable);

        // Owner and Vet Info
        PdfPTable partyTable = new PdfPTable(2);
        partyTable.setWidthPercentage(100);
        partyTable.setSpacingAfter(15);

        String ownerName = (Optional.ofNullable(bill.getOwnerFirstName()).orElse("") + " "
                + Optional.ofNullable(bill.getOwnerLastName()).orElse("")).trim();

        addMetaCell(partyTable, "Owner:", (ownerName.isEmpty() ? "N/A" : ownerName));
        addMetaCell(partyTable, "Vet:", Optional.ofNullable(bill.getVetFirstName()).orElse("") + " "
                + Optional.ofNullable(bill.getVetLastName()).orElse(""));

        document.add(partyTable);

        // Charges Table
        PdfPTable charges = new PdfPTable(4);
        charges.setWidthPercentage(100);
        charges.setSpacingAfter(10);
        charges.setWidths(new float[]{3, 1, 1, 1});

        // Header row
        addHeaderCell(charges, "Description");
        addHeaderCell(charges, "Qty");
        addHeaderCell(charges, "Unit Price");
        addHeaderCell(charges, "Subtotal");

        BigDecimal subtotal = bill.getAmount();
        BigDecimal interest = bill.getInterest() != null ? bill.getInterest() : BigDecimal.ZERO;
        BigDecimal totalDue = subtotal.add(interest);

        charges.addCell("Visit – " + Optional.ofNullable(bill.getVisitType()).orElse("N/A"));
        charges.addCell("1");
        charges.addCell(rightAligned(formatCurrency(subtotal)));
        charges.addCell(rightAligned(formatCurrency(subtotal)));

        document.add(charges);

        // Totals
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(40);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setSpacingBefore(10);

        // separator line
        PdfPCell separator = new PdfPCell(new Phrase(""));
        separator.setColspan(2);
        separator.setBorderWidthTop(1f);
        separator.setBorder(Rectangle.TOP);
        separator.setPaddingTop(5);
        totals.addCell(separator);

        totals.addCell("Subtotal");
        totals.addCell(rightAligned(formatCurrency(subtotal)));

        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            totals.addCell("Interest");
            totals.addCell(rightAligned(formatCurrency(interest)));
        }

        PdfPCell labelCell = new PdfPCell(new Phrase("Total Due",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        totals.addCell(labelCell);

        PdfPCell totalCell = new PdfPCell(new Phrase(formatCurrency(totalDue),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        totalCell.setBorder(Rectangle.NO_BORDER);
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.addCell(totalCell);

        document.add(totals);

        // Footer
        Paragraph notes = new Paragraph(
                "Notes: All bills must be paid on time. Late payments may be subject to a " +
                        "1.5% interest charge on the outstanding balance.",
                FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY));
        notes.setSpacingBefore(30);
        document.add(notes);

        Paragraph footer = new Paragraph("Thank you for visiting PetClinic",
                FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
        return byteArrayOutputStream.toByteArray();
    }

    private static String formatCurrency(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(value);
    }

    // Helper: metadata table cells
    private static void addMetaCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    // Helper: header cell for charges table
    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE)));
        cell.setBackgroundColor(BaseColor.GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    // Helper: right-aligned cell
    private static PdfPCell rightAligned(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
