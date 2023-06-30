package pl.zielona_baza.admin.category.export;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import pl.zielona_baza.admin.AbstractExporter;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.User;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CategoryPdfExporter extends AbstractExporter {
    public void export(List<Category> categories, HttpServletResponse response) throws IOException {
        super.setResponseHeader(response, "application/pdf", ".pdf", "categories_");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(Color.BLUE);

        Paragraph paragraph = new Paragraph("List of categories", font);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);

        document.add(paragraph);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10);
        table.setWidths(new float[] {1.2f, 3.5f, 3.0f, 3.0f, 3.0f, 1.7f});

        writeTableHeader(table);
        writeTableData(table, categories);

        document.add(table);

        document.close();
    }

    private void writeTableData(PdfPTable table, List<Category> categories) {
        for (Category category : categories) {
            table.addCell(String.valueOf(category.getId()));
            table.addCell(category.getName());
            table.addCell(category.getAlias());
            table.addCell(String.valueOf(category.isEnabled()));
            table.addCell(category.getImage());

            Category parent = category.getParent();
            Integer parentId = parent == null ? null : parent.getId();
            table.addCell(String.valueOf(parentId));

        }
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.lightGray);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setColor(Color.BLACK);

        cell.setPhrase(new Phrase("Category ID", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Name", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Alias", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Enabled", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Image", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Parent ID", font));
        table.addCell(cell);
    }
}
