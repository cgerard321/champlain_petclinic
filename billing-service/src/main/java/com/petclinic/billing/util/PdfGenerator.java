package com.petclinic.billing.util;

import java.io.ByteArrayOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;

/**
 * This utility class will generate the PDF for the bill
 * 
 * This class takes billing information from a BillResponseDTO 
 * and generates a PDF document containing the bill details. 
 * The content is generated in-memory using a ByteArrayOutputStream 
 * and is returned as a byte array, which could be sent to a client or stored. 
 * The iText library is used to create and manage the PDF content. 
 * Optional values are used to handle possible null values in the bill details, 
 * ensuring the PDF is generated without NullPointerExceptions.
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
