package pl.zielona_baza.admin.category.export;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import pl.zielona_baza.admin.AbstractExporter;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.User;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class CategoryCsvExport extends AbstractExporter {


    public void export(List<Category> categories, HttpServletResponse response) throws IOException {
        super.setResponseHeader(response, "text/csv", ".csv", "categories_");

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        String[] csvHeader = {"Category ID", "Name"};
        String[] fieldMapping = {"id", "name"};
        csvWriter.writeHeader(csvHeader);

        for(Category category : categories) {
            csvWriter.write(category, fieldMapping);
        }

        csvWriter.close();
    }
}
