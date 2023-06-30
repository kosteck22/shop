package pl.zielona_baza.admin.setting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.setting.Setting;
import pl.zielona_baza.common.entity.setting.SettingCategory;

import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class SettingRepositoryTest {

    @Autowired private SettingRepository settingRepository;

    @Test
    public void testCreateGeneralSettings() {
        //given
        Setting siteName = Setting.builder()
                .key("SITE_NAME")
                .value("Shopme")
                .category(SettingCategory.GENERAL)
                .build();

        Setting siteLogo = Setting.builder()
                .key("SITE_LOGO")
                .value("Shopme.png")
                .category(SettingCategory.GENERAL)
                .build();

        Setting copyright = Setting.builder()
                .key("COPYRIGHT")
                .value("Copyright (C) 2023 Shopme Ltd.")
                .category(SettingCategory.GENERAL)
                .build();

        //when
        List<Setting> settings = settingRepository.saveAll(List.of(siteName, siteLogo, copyright));

        //then
        assertThat(settings.size()).isGreaterThan(0);
    }

    @Test
    public void testCreateCurrencySettings() {
        //given
        Setting currencyId = Setting.builder()
                .key("CURRENCY_ID")
                .value("1")
                .category(SettingCategory.CURRENCY)
                .build();

        Setting symbol = Setting.builder()
                .key("CURRENCY_SYMBOL")
                .value("$")
                .category(SettingCategory.CURRENCY)
                .build();

        Setting symbolPosition = Setting.builder()
                .key("CURRENCY_SYMBOL_POSITION")
                .value("before")
                .category(SettingCategory.CURRENCY)
                .build();

        Setting decimalPointType = Setting.builder()
                .key("DECIMAL_POINT_TYPE")
                .value("POINT")
                .category(SettingCategory.CURRENCY)
                .build();

        Setting decimalDigits = Setting.builder()
                .key("DECIMAL_DIGITS")
                .value("2")
                .category(SettingCategory.CURRENCY)
                .build();

        Setting thousandsPointType = Setting.builder()
                .key("THOUSANDS_POINT_TYPE")
                .value("COMMA")
                .category(SettingCategory.CURRENCY)
                .build();

        //when
        List<Setting> settings = settingRepository.saveAll(List.of(currencyId, symbol, symbolPosition, decimalPointType,
                decimalDigits, thousandsPointType));

        //then
        assertThat(settings.size()).isGreaterThan(0);
    }

    @Test
    public void testListSettingsByCategory() {
        List<Setting> settings = settingRepository.findByCategory(SettingCategory.GENERAL);

        settings.forEach(System.out::println);
    }
}
