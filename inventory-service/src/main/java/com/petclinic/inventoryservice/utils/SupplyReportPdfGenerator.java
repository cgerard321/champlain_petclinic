package com.petclinic.inventoryservice.utils;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.petclinic.inventoryservice.datalayer.Product.Product;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;


public final class SupplyReportPdfGenerator {

    // employee-frontend theme colours
    private static final Color NAV_GREEN = new Color(0xC2, 0xDA, 0xAE);   // header.css / footer.css #c2daae
    private static final Color FOREST_700 = new Color(0x24, 0x4B, 0x3F);  // --forest-700
    private static final Color HEADING = new Color(0x1B, 0x38, 0x2B);     // page headings #1b382b
    private static final Color TEXT = new Color(0x26, 0x38, 0x32);        // --text-primary
    private static final Color TEXT_MUTED = new Color(0x66, 0x76, 0x6D);  // --text-secondary
    private static final Color SAGE_50 = new Color(0xF7, 0xFA, 0xF3);     // --sage-50
    private static final Color SAGE_100 = new Color(0xED, 0xF3, 0xE7);    // --sage-100
    private static final Color SAGE_200 = new Color(0xDC, 0xE8, 0xD2);    // --sage-200
    private static final Color BADGE_BG = new Color(0xE5, 0xEF, 0xE4);    // home page badge
    private static final Color BADGE_TEXT = new Color(0x2E, 0x52, 0x3E);
    private static final Color ERROR = new Color(0xB0, 0x00, 0x20);       // portal error red #b00020

    private static final Font BRAND = FontFactory.getFont(FontFactory.HELVETICA, 16, HEADING);
    private static final Font BADGE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BADGE_TEXT);
    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, HEADING);
    private static final Font META = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MUTED);
    private static final Font FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 8, HEADING);
    private static final Font FOOTER_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, HEADING);
    private static final Font HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CELL = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT);
    private static final Font CELL_LOSS = FontFactory.getFont(FontFactory.HELVETICA, 9, ERROR);
    private static final Font CELL_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, HEADING);

    private static final String[] COLUMNS = {"Product", "Qty", "Description", "Regular", "Sale", "Profit"};
    private static final float[] COLUMN_WIDTHS = {3.2f, 1f, 3.6f, 1.4f, 1.4f, 1.4f};

    private static final float BAR_HEIGHT = 40;

    private SupplyReportPdfGenerator() {
    }

    public static byte[] generate(String inventoryId, List<Product> supplies) throws DocumentException {
        return generate(inventoryId, supplies, "en");
    }

    public static byte[] generate(String inventoryId, List<Product> supplies, String language) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, BAR_HEIGHT + 28, BAR_HEIGHT + 24);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        writer.setPageEvent(new PortalHeaderAndFooter());

        document.open();
        document.add(badge("INVENTORY REPORT"));
        Paragraph title = new Paragraph("Supplies Report", TITLE);
        title.setSpacingBefore(8);
        document.add(title);
        Paragraph meta = new Paragraph("Inventory ID: " + inventoryId + "    Generated " + LocalDate.now(), META);
        meta.setSpacingBefore(2);
        document.add(meta);
        document.add(buildTable(supplies, language));
        document.close();

        return out.toByteArray();
    }

    private static PdfPTable badge(String text) {
        PdfPTable badge = new PdfPTable(1);
        badge.setTotalWidth(BADGE.getBaseFont().getWidthPoint(text, BADGE.getSize()) + 20);
        badge.setLockedWidth(true);
        badge.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell cell = new PdfPCell(new Phrase(text, BADGE));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(4);
        cell.setPaddingBottom(6);
        cell.setPaddingLeft(10);
        cell.setCellEvent(new RoundedBackground(BADGE_BG, 9));
        badge.addCell(cell);
        return badge;
    }

    private static PdfPTable buildTable(List<Product> supplies, String language) {
        PdfPTable table = new PdfPTable(COLUMN_WIDTHS);
        table.setWidthPercentage(100);
        table.setSpacingBefore(16);
        table.setHeaderRows(1);

        for (String column : COLUMNS) {
            PdfPCell cell = new PdfPCell(new Phrase(column, HEADER));
            cell.setBackgroundColor(FOREST_700);
            cell.setBorderColor(FOREST_700);
            cell.setPadding(7);
            boolean isText = column.equals("Product") || column.equals("Description");
            cell.setHorizontalAlignment(isText ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(cell);
        }

        int totalQuantity = 0;
        for (int row = 0; row < supplies.size(); row++) {
            Product supply = supplies.get(row);
            Color background = row % 2 == 1 ? SAGE_50 : Color.WHITE;
            int quantity = supply.getProductQuantity() == null ? 0 : supply.getProductQuantity();
            totalQuantity += quantity;
            Double profit = profit(supply);

            table.addCell(cell(LocalizedContent.get(supply.getName(), language, supply.getProductName()), CELL, background, Element.ALIGN_LEFT));
            table.addCell(cell(String.valueOf(quantity), CELL, background, Element.ALIGN_RIGHT));
            table.addCell(cell(LocalizedContent.get(supply.getDescription(), language, supply.getProductDescription()), CELL, background, Element.ALIGN_LEFT));
            table.addCell(cell(money(supply.getProductPrice()), CELL, background, Element.ALIGN_RIGHT));
            table.addCell(cell(money(supply.getProductSalePrice()), CELL, background, Element.ALIGN_RIGHT));
            Font profitFont = profit != null && profit < 0 ? CELL_LOSS : CELL;
            table.addCell(cell(money(profit), profitFont, background, Element.ALIGN_RIGHT));
        }

        table.addCell(cell("Total (" + supplies.size() + " products)", CELL_BOLD, SAGE_100, Element.ALIGN_LEFT));
        table.addCell(cell(String.valueOf(totalQuantity), CELL_BOLD, SAGE_100, Element.ALIGN_RIGHT));
        PdfPCell filler = cell("", CELL, SAGE_100, Element.ALIGN_LEFT);
        filler.setColspan(4);
        table.addCell(filler);

        return table;
    }

    private static PdfPCell cell(String text, Font font, Color background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setBackgroundColor(background);
        cell.setBorderColor(SAGE_200);
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private static Double profit(Product supply) {
        if (supply.getProductPrice() == null || supply.getProductSalePrice() == null) {
            return null;
        }
        return supply.getProductSalePrice() - supply.getProductPrice();
    }

    private static String money(Double value) {
        if (value == null) {
            return "-";
        }
        String amount = String.format(Locale.US, "$%,.2f", Math.abs(value));
        return value < 0 ? "-" + amount : amount;
    }

    private static class RoundedBackground implements PdfPCellEvent {
        private final Color color;
        private final float radius;

        RoundedBackground(Color color, float radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.BACKGROUNDCANVAS];
            canvas.saveState();
            canvas.setColorFill(color);
            canvas.roundRectangle(position.getLeft(), position.getBottom(),
                    position.getWidth(), position.getHeight(), radius);
            canvas.fill();
            canvas.restoreState();
        }
    }


    private static class PortalHeaderAndFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle page = document.getPageSize();
            PdfContentByte background = writer.getDirectContentUnder();
            background.saveState();
            background.setColorFill(NAV_GREEN);
            background.rectangle(0, page.getHeight() - BAR_HEIGHT, page.getWidth(), BAR_HEIGHT);
            background.rectangle(0, 0, page.getWidth(), BAR_HEIGHT);
            background.fill();
            background.restoreState();

            PdfContentByte text = writer.getDirectContent();
            float left = document.left();
            float right = document.right();
            ColumnText.showTextAligned(text, Element.ALIGN_LEFT,
                    new Phrase("Petclinic", BRAND), left, page.getHeight() - BAR_HEIGHT / 2 - 5, 0);
            ColumnText.showTextAligned(text, Element.ALIGN_RIGHT,
                    new Phrase("Employee Portal", META), right, page.getHeight() - BAR_HEIGHT / 2 - 3, 0);

            Phrase contact = new Phrase();
            contact.add(new Chunk("Champlain PetClinic", FOOTER_BOLD));
            contact.add(new Chunk("    900 Rue Riverside  |  +1 (450) 672-7360  |  ChamplainPetClinic@gmail.com", FOOTER));
            ColumnText.showTextAligned(text, Element.ALIGN_LEFT, contact, left, BAR_HEIGHT / 2 - 3, 0);
            ColumnText.showTextAligned(text, Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), FOOTER_BOLD), right, BAR_HEIGHT / 2 - 3, 0);
        }
    }
}
