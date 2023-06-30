package pl.zielona_baza.admin.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.zielona_baza.common.entity.setting.Setting;
import pl.zielona_baza.common.entity.setting.SettingCategory;

import java.util.ArrayList;
import  java.util.List;

@Service
public class SettingService {

    @Autowired
    private SettingRepository settingRepository;

    public List<Setting> listAllSettings() {
        return settingRepository.findAll();
    }

    public GeneralSettingBag getGeneralSettings() {
        List<Setting> generalSettingsFromRepo = settingRepository.findByCategory(SettingCategory.GENERAL);
        List<Setting> currencySettingsFromRepo = settingRepository.findByCategory(SettingCategory.CURRENCY);
        List<Setting> generalSettings = new ArrayList<>();

        generalSettings.addAll(generalSettingsFromRepo);
        generalSettings.addAll(currencySettingsFromRepo);

        return new GeneralSettingBag(generalSettings);
    }

    public void saveAll(Iterable<Setting> settings) {
        settingRepository.saveAll(settings);
    }

    public List<Setting> getMailServerSettings() {
        return settingRepository.findByCategory(SettingCategory.MAIL_SERVER);
    }

    public List<Setting> getMailTemplateSettings() {
        return settingRepository.findByCategory(SettingCategory.MAIL_TEMPLATES);
    }

    public List<Setting> getCurrencySettings() {
        return settingRepository.findByCategory(SettingCategory.CURRENCY);
    }

    public List<Setting> getPaymentSettings() {
        return settingRepository.findByCategory(SettingCategory.PAYMENT);
    }
}
