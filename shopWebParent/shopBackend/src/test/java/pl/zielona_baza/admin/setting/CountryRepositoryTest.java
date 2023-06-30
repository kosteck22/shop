package pl.zielona_baza.admin.setting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.admin.setting.country.CountryRepository;
import pl.zielona_baza.common.entity.Country;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class CountryRepositoryTest {

    @Autowired private CountryRepository countryRepository;

    @Test
    public void testCreateCountries() {
        Country country1 = Country.builder()
                .name("United States")
                .code("US")
                .build();

        Country country2 = Country.builder()
                .name("United Kingdom")
                .code("UK")
                .build();

        Country country3 = Country.builder()
                .name("Republic of India")
                .code("IN")
                .build();

        Country country4 = Country.builder()
                .name("Poland")
                .code("PL")
                .build();

        List<Country> countries = countryRepository.saveAll(List.of(country1, country2, country3, country4));

        assertThat(countries.size()).isEqualTo(4);
        assertThat(countries.get(0).getId()).isNotNull();
    }
}
