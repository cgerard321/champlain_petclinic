package com.petclinic.billing.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;

/*
 * This utility class will generate the PDF for the bill
 */
public class PdfGenerator {

    public static byte[] generateBillPdf(BillResponseDTO bill) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);

        document.open();
        // Add bill details
        document.add(new Paragraph("Bill ID: " + Optional.ofNullable(bill.getBillId()).orElse("N/A")));
        document.add(new Paragraph("Owner Name: " +
                Optional.ofNullable(bill.getOwnerFirstName()).orElse("") + " " +
                Optional.ofNullable(bill.getOwnerLastName()).orElse("")));
        document.add(new Paragraph("Visit Type: " + Optional.ofNullable(bill.getVisitType()).orElse("N/A")));
        document.add(new Paragraph("Amount: " + Optional.ofNullable(bill.getAmount()).orElse(0.0)));
        document.add(new Paragraph("Status: " + Optional.ofNullable(bill.getBillStatus()).orElse(BillStatus.UNPAID)));

        document.close();

        return byteArrayOutputStream.toByteArray();
    }

}

