package com.petclinic.inventoryservice.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class SupplyReportPdfGeneratorTest {

    private static Product supply(String name, int quantity, String description, double price, double salePrice) {
        Product product = new Product();
        product.setProductName(name);
        product.setProductQuantity(quantity);
        product.setProductDescription(description);
        product.setProductPrice(price);
        product.setProductSalePrice(salePrice);
        return product;
    }

    private static String textOfPage(byte[] pdf, int page) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            return new PdfTextExtractor(reader).getTextFromPage(page);
        } finally {
            reader.close();
        }
    }

    @Test
    void generate_ShouldProduceAPdf() {
        byte[] pdf = SupplyReportPdfGenerator.generate("inv-1", List.of(supply("Gauze", 40, "Box of 100", 12.5, 18.99)));

        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void generate_ShouldContainTitleHeaderAndEverySupply() throws IOException {
        byte[] pdf = SupplyReportPdfGenerator.generate("inv-1", List.of(
                supply("Sterile Gauze Pads", 40, "Box of 100", 12.50, 18.99),
                supply("Nitrile Gloves", 12, "Powder-free", 9.75, 14.50)));

        String text = textOfPage(pdf, 1);

        assertThat(text).contains("Supplies Report", "Inventory ID: inv-1");
        assertThat(text).contains("Product", "Qty", "Description", "Regular", "Sale", "Profit");
        assertThat(text).contains("Sterile Gauze Pads", "Box of 100", "$12.50", "$18.99", "$6.49");
        assertThat(text).contains("Nitrile Gloves", "Powder-free", "$9.75", "$14.50", "$4.75");
    }

    @Test
    void generate_UsesRequestedLanguageForSupplyContent() throws IOException {
        Product product = supply("Bandage", 1, "Wound dressing", 2, 3);
        product.setName(Map.of("en", "Bandage", "fr", "Pansement"));
        product.setDescription(Map.of("en", "Wound dressing", "fr", "Soin des plaies"));

        String frenchPdf = textOfPage(SupplyReportPdfGenerator.generate("inv-1", List.of(product), "fr"), 1);
        assertThat(frenchPdf).contains("Pansement", "Soin des plaies");
        assertThat(frenchPdf).doesNotContain("Bandage", "Wound dressing");
    }

    @Test
    void generate_ShouldShowTotalsRow() throws IOException {
        byte[] pdf = SupplyReportPdfGenerator.generate("inv-1", List.of(
                supply("A", 40, "a", 1, 2),
                supply("B", 12, "b", 1, 2)));

        assertThat(textOfPage(pdf, 1)).contains("Total (2 products)", "52");
    }

    @Test
    void generate_ShouldShowALossWithTheMinusSignFirst() throws IOException {
        byte[] pdf = SupplyReportPdfGenerator.generate("inv-1", List.of(supply("Sold at a loss", 1, "x", 100.00, 15.99)));

        assertThat(textOfPage(pdf, 1)).contains("-$84.01");
    }

    @Test
    void generate_ShouldNotFailOnMissingValues() throws IOException {
        Product incomplete = new Product();
        incomplete.setProductName("No price yet");

        byte[] pdf = SupplyReportPdfGenerator.generate("inv-1", List.of(incomplete));

        assertThat(textOfPage(pdf, 1)).contains("No price yet");
    }

    @Test
    void generate_ShouldRepeatTheHeaderOnEveryPage() throws IOException {
        List<Product> supplies = new ArrayList<>();
        for (int i = 1; i <= 80; i++) {
            supplies.add(supply("Supply " + i, i, "Item " + i, 1, 2));
        }

        byte[] pdf = SupplyReportPdfGenerator.generate("inv-1", supplies);
        PdfReader reader = new PdfReader(pdf);
        int pages = reader.getNumberOfPages();
        reader.close();

        assertThat(pages).isGreaterThan(1);
        assertThat(textOfPage(pdf, pages)).contains("Profit", "Page " + pages);
    }
}
