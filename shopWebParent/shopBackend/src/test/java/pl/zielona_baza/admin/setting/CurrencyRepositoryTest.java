package pl.zielona_baza.admin.setting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.Currency;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class CurrencyRepositoryTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Test
    public void testCreateCurrencies() {
        //given
        List<Currency> listCurrencies = Arrays.asList(
                Currency.builder().name("United States Dollar").symbol("$").code("USD").build(),
                Currency.builder().name("British Pound").symbol("£").code("GPB").build(),
                Currency.builder().name("Euro").symbol("€").code("EUR").build(),
                Currency.builder().name("Polski Złoty").symbol("zł").code("PLN").build()
        );

        //when
        List<Currency> currencies = currencyRepository.saveAll(listCurrencies);

        //then
        assertThat(currencies.size()).isEqualTo(4);
    }
}
