package pl.zielona_baza.admin.setting.country;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.zielona_baza.common.entity.Country;
import java.util.List;

@RestController
public class CountryRestController {

    @Autowired private CountryRepository countryRepository;

    @GetMapping("/countries/list")
    public List<Country> listAll() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    @PostMapping("/countries/save")
    public String save(@RequestBody Country country) {
        Country savedCountry = countryRepository.save(country);

        return String.valueOf(savedCountry.getId());
    }

    @DeleteMapping("/countries/delete/{id}")
    public void delete(@PathVariable("id") Integer id) {
        countryRepository.deleteById(id);
    }
}
