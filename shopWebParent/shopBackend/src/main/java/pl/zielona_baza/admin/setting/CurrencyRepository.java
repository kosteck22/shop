package pl.zielona_baza.admin.setting;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.zielona_baza.common.entity.Currency;
import java.util.List;
public interface CurrencyRepository extends JpaRepository<Currency, Integer> {
    List<Currency> findAllByOrderByNameAsc();
}
