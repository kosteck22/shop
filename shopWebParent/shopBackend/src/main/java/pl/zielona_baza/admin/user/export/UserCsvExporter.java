package pl.zielona_baza.admin.user.export;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import pl.zielona_baza.admin.AbstractExporter;
import pl.zielona_baza.common.entity.User;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

public class UserCsvExporter extends AbstractExporter {

    public void export(List<User> users, HttpServletResponse response) throws IOException {
        super.setResponseHeader(response, "text/csv", ".csv", "users_");

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        String[] csvHeader = {"User ID", "E-mail", "First Name", "Last Name", "Roles", "Enabled"};
        String[] fieldMapping = {"id", "email", "firstName", "lastName", "roles", "enabled"};
        csvWriter.writeHeader(csvHeader);

        for(User user : users) {
            csvWriter.write(user, fieldMapping);
        }

        csvWriter.close();
    }
}
