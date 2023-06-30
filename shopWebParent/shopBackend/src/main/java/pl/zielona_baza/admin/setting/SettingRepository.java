package pl.zielona_baza.admin.setting;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.zielona_baza.common.entity.setting.Setting;
import pl.zielona_baza.common.entity.setting.SettingCategory;

import java.util.List;
public interface SettingRepository extends JpaRepository<Setting, String> {
    public List<Setting> findByCategory(SettingCategory category);
}
