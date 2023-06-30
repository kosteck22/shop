package pl.zielona_baza.admin.category.export;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.*;
import pl.zielona_baza.admin.AbstractExporter;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.User;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class CategoryExcelExporter extends AbstractExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public CategoryExcelExporter() {
        workbook = new XSSFWorkbook();
    }

    public void export(List<Category> categories, HttpServletResponse response) throws IOException {
        super.setResponseHeader(response, "application/octet-stream", ".xlsx", "categories_");

        writeHeaderLine();
        writeDataLines(categories);

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    private void writeDataLines(List<Category> categories) {
        int rowIndex = 1;

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        cellStyle.setFont(font);

        for (Category category : categories) {
            XSSFRow row = sheet.createRow(rowIndex);

            createCell(row, 0, category.getId(), cellStyle);
            createCell(row, 1, category.getName(), cellStyle);
            createCell(row, 2, category.getAlias(), cellStyle);
            createCell(row, 3, category.isEnabled(), cellStyle);
            createCell(row, 4, category.getImage(), cellStyle);

            Category parent = category.getParent();
            Integer parentId = parent == null ? null : parent.getId();
            createCell(row, 5, parentId, cellStyle);

            rowIndex++;
        }
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("Categories");
        XSSFRow row = sheet.createRow(0);

        //configuration font for header row
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        cellStyle.setFont(font);

        //write header cells
        createCell(row, 0, "Category ID", cellStyle);
        createCell(row, 1, "Name", cellStyle);
        createCell(row, 2, "Alias", cellStyle);
        createCell(row, 3, "Enabled", cellStyle);
        createCell(row, 4, "Image Name", cellStyle);
        createCell(row, 5, "Parent ID", cellStyle);
    }

    private void createCell(XSSFRow row, int columnIndex, Object value, CellStyle style) {
        XSSFCell cell = row.createCell(columnIndex);
        sheet.autoSizeColumn(columnIndex);

        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }
}
