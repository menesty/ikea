package org.menesty.ikea.pdf;

/*
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
*/

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Menesty on
 * 8/28/14.
 * 17:30.
 */
public class PdfTest {
    public static String polish = "Ą Ć Ę Ł Ń Ó Ś Ź Ż ą ć ę ł ń ó ś ź ż";

   /* public static void main(String... arg) throws IOException, DocumentException {
        new PdfTest().createPdf("test.pdf");
    }


    public void createPdf(String filename) throws IOException, DocumentException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        // step 3
        document.open();
        // step 4
        document.add(createFirstTable());
        // step 5

        BaseFont bf = BaseFont.createFont("/Users/andrewhome/development/workspace/ikea/src/test/java/org/menesty/ikea/pdf/arialuni_2.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Paragraph p = new Paragraph(polish, new Font(bf, 22));
        document.add(p);
        document.close();
    }

    *//**
     * Creates our first table
     *
     * @return our first table
     *//*
    public static PdfPTable createFirstTable() throws IOException, DocumentException {
        BaseFont courier = BaseFont.createFont("/Users/andrewhome/development/workspace/ikea/src/test/java/org/menesty/ikea/pdf/Polish-European Courier.TTF", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font font = new Font(courier, 12, Font.NORMAL);
        // a table with three columns
        PdfPTable table = new PdfPTable(3);
        // the cell object
        PdfPCell cell;
        // we add a cell with colspan 3

        cell = new PdfPCell(new Paragraph(polish));
        cell.setColspan(3);
        table.addCell(cell);
        // now we add a cell with rowspan 2
        cell = new PdfPCell(new Phrase("Cell with rowspan", font));
        cell.setRowspan(2);
        table.addCell(cell);
        // we add the four remaining cells with addCell()
        table.addCell("ł row 1; cell 1 ó ą ę ł");
        table.addCell("ł row 1; cell 2 ó ą ę ł");
        table.addCell("ł row 2; cell 1 ó ą ę ł");
        table.addCell("ł row 2; cell 2 ó ą ę ł");
        return table;
    }*/

}
