package pl.zielona_baza.admin.setting.country;

import org.springframework.web.bind.annotation.*;
import pl.zielona_baza.common.entity.Country;
import java.util.List;

@RestController
@RequestMapping("/countries")
public class CountryRestController {
    private final CountryRepository countryRepository;

    public CountryRestController(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @GetMapping("/list")
    public List<Country> listAll() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    @PostMapping("/save")
    public String save(@RequestBody Country country) {
        Country savedCountry = countryRepository.save(country);

        return String.valueOf(savedCountry.getId());
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") Integer id) {
        countryRepository.deleteById(id);
    }
}
