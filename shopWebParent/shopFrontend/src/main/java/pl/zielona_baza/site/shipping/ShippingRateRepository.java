package pl.zielona_baza.site.shipping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.ShippingRate;
import pl.zielona_baza.common.entity.State;

import java.util.Optional;

@Repository
public interface ShippingRateRepository extends JpaRepository<ShippingRate, Integer> {

    Optional<ShippingRate> findByCountryAndState(Country country, String state);
}
