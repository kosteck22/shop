package pl.zielona_baza.common.entity.setting;

import java.util.List;

public class SettingBag {
    private final List<Setting> listSettings;

    public SettingBag(List<Setting> listSettings) {
        this.listSettings = listSettings;
    }

    public Setting get(String key) {
        return listSettings.stream()
                .filter(set -> set.getKey().equals(key))
                .findFirst().orElse(null);
    }

    public String getValue(String key) {
        Setting setting = get(key);
        if (setting == null) {
            return null;
        }
        return setting.getValue();
    }

    public void update(String key, String value) {
        Setting setting = get(key);
        if (setting != null && value != null) {
            setting.setValue(value);
        }
    }

    public List<Setting> list() {
        return listSettings;
    }
}
