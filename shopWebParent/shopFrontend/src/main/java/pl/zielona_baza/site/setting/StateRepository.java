package pl.zielona_baza.site.setting;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.State;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Integer> {
    public List<State> findByCountryOrderByNameAsc(Country country);
}
