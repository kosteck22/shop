package pl.zielona_baza.site.setting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.zielona_baza.common.entity.Country;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    public List<Country> findAllByOrderByNameAsc();

    @Query("SELECT c FROM Country c WHERE c.code = ?1")
    public Country findByCode(String code);
}
