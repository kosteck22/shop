package pl.zielona_baza.site.setting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.common.entity.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Integer> {
}
