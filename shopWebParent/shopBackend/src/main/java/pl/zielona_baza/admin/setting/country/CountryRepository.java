package pl.zielona_baza.admin.setting.country;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.zielona_baza.common.entity.Country;
import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    List<Country> findAllByOrderByNameAsc();
}
