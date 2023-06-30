package pl.zielona_baza.admin.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.FileUploadUtil;
import pl.zielona_baza.common.entity.Currency;
import pl.zielona_baza.common.entity.setting.Setting;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class SettingController {
    
    @Autowired
    private SettingService settingService;
    
    @Autowired
    private CurrencyRepository currencyRepository;
    
    @GetMapping("/settings")
    public String listAll(Model model) {
        List<Setting> listSettings = settingService.listAllSettings();
        List<Currency> listCurrencies = currencyRepository.findAllByOrderByNameAsc();

        model.addAttribute("listCurrencies", listCurrencies);

        listSettings.forEach(setting -> {
            model.addAttribute(setting.getKey(), setting.getValue());
        });
        
        return "settings/settings";
    }

    @PostMapping("/settings/save_general")
    public String saveGeneralSettings(
            @RequestParam("fileImage")MultipartFile fileImage,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) throws IOException {

        GeneralSettingBag settingBag = settingService.getGeneralSettings();
        saveSiteLogo(fileImage, settingBag);
        saveCurrencySymbol(request, settingBag);
        updateSettingValuesFromForm(request, settingBag.list());

        redirectAttributes
                .addFlashAttribute("message", "General settings have beed saved.");

        return  "redirect:/settings";
    }

    private static void saveSiteLogo(MultipartFile fileImage, GeneralSettingBag settingBag) throws IOException {
        if (!fileImage.isEmpty()) {
            String fileName = StringUtils.cleanPath(fileImage.getOriginalFilename());
            String value = "/site-logo/" + fileName;
            settingBag.updateSiteLogo(value);

            //Upload to the file system
            //String uploadDir = "../site-logo/";
            //FileUploadUtil.cleanDir(uploadDir);
            //FileUploadUtil.saveFile(uploadDir, fileName, fileImage);

            //Upload to Amazon S3
            String uploadDir = "site-logo";
            AmazonS3Util.removeFolder(uploadDir);
            AmazonS3Util.uploadFile(uploadDir, fileName, fileImage.getInputStream());
        }
    }

    private void saveCurrencySymbol(HttpServletRequest request, GeneralSettingBag generalSettingBag) {
        Integer currencyId = Integer.parseInt(request.getParameter("CURRENCY_ID"));
        Optional<Currency> findByIdResult = currencyRepository.findById(currencyId);

        if (findByIdResult.isPresent()) {
            Currency currency = findByIdResult.get();
            generalSettingBag.updateCurrencySymbol(currency.getSymbol());
        }
    }

    private void updateSettingValuesFromForm(HttpServletRequest request, List<Setting> listSettings) {
        listSettings.forEach(setting -> {
            String value = request.getParameter(setting.getKey());
            if (value != null) {
                setting.setValue(value);
            }
        });

        settingService.saveAll(listSettings);
    }

    @PostMapping("/settings/save_mail_server")
    public String saveMailServerSettings(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<Setting> mailServerSettings = settingService.getMailServerSettings();

        updateSettingValuesFromForm(request, mailServerSettings);
        redirectAttributes.addFlashAttribute("message", "Mail server settings have been saved");

        return "redirect:/settings";
    }

    @PostMapping("/settings/save_mail_templates")
    public String saveMailTemplateSettings(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<Setting> mailTemplateSettings = settingService.getMailTemplateSettings();

        updateSettingValuesFromForm(request, mailTemplateSettings);
        redirectAttributes.addFlashAttribute("message", "Mail template settings have been saved");

        return "redirect:/settings";
    }

    @PostMapping("/settings/save_payment")
    public String savePaymentSettings(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        List<Setting> paymentSettings = settingService.getPaymentSettings();
        updateSettingValuesFromForm(request, paymentSettings);

        redirectAttributes.addFlashAttribute("message", "Payment settings have beed saved");

        return "redirect:/settings#payment";
    }
}
