package pl.zielona_baza.admin.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pl.zielona_baza.admin.setting.SettingService;
import pl.zielona_baza.common.entity.setting.Setting;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ReportController {
    private final SettingService settingService;

    public ReportController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/reports")
    public String viewSalesReportHome(HttpServletRequest request) {
        loadCurrencySetting(request);

        return "reports/reports";
    }

    private void loadCurrencySetting(HttpServletRequest request) {
        List<Setting> currencySettings = settingService.getCurrencySettings();

        currencySettings.forEach(s -> request.setAttribute(s.getKey(), s.getValue()));
    }
}
