package org.menesty.ikea;

/**
 * Created by Menesty on
 * 8/7/14.
 * 8:12.
 */

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.*;


public class App {
    public static void main(String[] args) throws IOException, COSVisitorException {
        PDDocument document = new PDDocument();//PDDocument.load()
        TableRender tableRender = new TableRender();

        Table table = new Table();

        table.addColumn(new Column("Lp", 30).setAlignment(Column.Alignment.CENTER));
        table.addColumn(new Column("Nazwa", 120));
        table.addColumn(new Column(new String("Ilośd".getBytes("ISO-8859-2")), 40));
        table.addColumn(new Column("Jm", 40));
        table.addColumn(new Column("netto", 160).addColumn(new Column("Cena", 80)).addColumn(new Column("Wartośd", 80)));
        table.addColumn(new Column("Podatek VAT", 100).addColumn(new Column("st. (%)", 50)).addColumn(new Column("kwota", 50)));
        table.addColumn(new Column("Wartośd", 80).addColumn(new Column("brutto", 80)));

        for (int i = 1; i < 10; i++)
            table.addRow(new String[]{"" + i, "Półka ś", "" + i, "207,31 " + i, "207,31 " + i, "23", "47,68", "254,99"});

        tableRender.render(table, document, null);


// Save the results and ensure that the document is properly closed:
        document.save("Hello World.pdf");
        document.close();
    }
}

class Margin {
    private final float top;
    private final float bottom;
    private final float left;
    private final float right;

    public Margin(float margin) {
        this(margin, margin);
    }

    public Margin(float topBottom, float leftRight) {
        this(topBottom, leftRight, topBottom, leftRight);
    }

    public Margin(float top, float right, float bottom, float left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public float getTop() {
        return top;
    }

    public float getBottom() {
        return bottom;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }
}

class Table {
    private Margin margin;
    private List<Column> columns = new ArrayList<Column>();
    private List<String[]> data = new ArrayList<String[]>();

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void setMargin(Margin margin) {
        this.margin = margin;
    }

    public void addRow(String[] row) {
        data.add(row);
    }

    public List<Column> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    public List<String[]> getData() {
        return Collections.unmodifiableList(data);
    }
}

class TableRender {
    private PDRectangle pageSize = PDPage.PAGE_SIZE_A4;
    private PDFont font = PDType1Font.TIMES_ROMAN;
    private float fontSize = 10;
    private float pageHeight;
    private Table table;
    private float tableWidth;

    private Map<Integer, Integer> columnWidth = new HashMap<Integer, Integer>();

    public TableRender() {

    }

    private PDPage createPage() {
        PDPage page = new PDPage(pageSize);

        return page;
    }

    public void render(Table table, PDDocument document, PDPage page) throws IOException {
        //font = PDTrueTypeFont.loadTTF(document,new File("/Users/andrewhome/Downloads/calibri.ttf"));
        //font.setFontEncoding(new PdfDocEncoding());
        if (page == null) {
            page = createPage();
            document.addPage(page);
        }
        this.table = table;
        Margin pageMargin = new Margin(2);
        //calculate column width
        PDRectangle pageSize = page.findMediaBox();
        float pageWidth = pageSize.getWidth();
        pageHeight = pageSize.getHeight();

        String message = "dsafsdfsd";
        float stringWidth = font.getStringWidth(message) * fontSize / 1000f;
        float startX = 100;
        float startY = 100;


        float workWidth = pageWidth - pageMargin.getLeft() - pageMargin.getRight();
        float workHeight = pageHeight - pageMargin.getTop() - pageMargin.getBottom();


        //calculate table weight

        for (Column column : table.getColumns())
            tableWidth += column.getWidth();
        //add 1 px for each side border

        tableWidth += 1f;

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.setFont(font, fontSize);
        renderHeader(contentStream, pageMargin.getLeft(), pageMargin.getTop(), table.getColumns());

        contentStream.close();
    }

    private void renderHeader(PDPageContentStream contentStream, float startPointX, float startPointY, List<Column> columns) throws IOException {
        //draw up line
        contentStream.setLineWidth(0.1f);
        contentStream.drawLine(startPointX, translateY(startPointY), startPointX + tableWidth, translateY(startPointY));


        float fontHeight = fontSize;
        float maxMargin = 0;

        for (Column column : columns) {
            Margin margin = column.getMargin();
            if (margin != null) {
                float currentMargin = margin.getBottom() + margin.getTop();
                if (currentMargin > maxMargin)
                    maxMargin = currentMargin;
            }
        }
        float downY = translateY(startPointY + fontHeight + maxMargin);
        contentStream.drawLine(startPointX, downY, startPointX + tableWidth, downY);
        contentStream.drawLine(startPointX + 0.5f, translateY(startPointY), startPointX + 0.5f, downY);

        float nextX = startPointX + 0.5f;
        for (Column column : columns) {
            Margin columnMargin = column.getMargin() != null ? column.getMargin() : new Margin(0, 2, 2, 1);

            float leftX = nextX + columnMargin.getLeft();
            String message = column.getName();

            if (Column.Alignment.CENTER == column.getAlignment()) {
                float messageWidth = fontSize * font.getStringWidth(message) / 1000;

                if (column.getWidth() > messageWidth)
                    leftX += (column.getWidth() - messageWidth) / 2;
            }

            contentStream.beginText();
            contentStream.moveTextPositionByAmount(leftX, downY + columnMargin.getBottom());
            contentStream.drawString(message);
            contentStream.endText();

            nextX += column.getWidth();
            contentStream.drawLine(nextX, translateY(startPointY), nextX, downY);


        }

        for (String[] row : table.getData()) {
            downY = renderRow(contentStream, startPointX, downY, table.getColumns(), row);
        }
    }


    private float renderRow(PDPageContentStream contentStream, float startPointX, float startPointY, List<Column> columns, String[] data) throws IOException {
        float downY = startPointY - fontSize + 0;
        contentStream.drawLine(startPointX + 0.5f, startPointY, startPointX + 0.5f, downY);
        //draw bottom line
        contentStream.drawLine(startPointX, downY, startPointX + tableWidth, downY);
        float nextX = startPointX + 0.5f;

        for (int index = 0; index < columns.size(); index++) {
            Column column = columns.get(index);
            Margin columnMargin = column.getMargin() != null ? column.getMargin() : new Margin(0, 2, 2, 1);

            contentStream.beginText();

            float leftX = nextX + columnMargin.getLeft();
            String message = data[index];

            if (Column.Alignment.CENTER == column.getAlignment()) {
                float messageWidth = fontSize * font.getStringWidth(message) / 1000;

                if (column.getWidth() > messageWidth)
                    leftX += (column.getWidth() - messageWidth) / 2;
            }

            contentStream.moveTextPositionByAmount(leftX, downY + columnMargin.getBottom());
            contentStream.drawString(message);
            contentStream.endText();

            nextX += column.getWidth();
            contentStream.drawLine(nextX, startPointY, nextX, downY);
        }
        return downY;
    }

    private float translateY(float point) {
        return pageHeight - point;
    }
}
