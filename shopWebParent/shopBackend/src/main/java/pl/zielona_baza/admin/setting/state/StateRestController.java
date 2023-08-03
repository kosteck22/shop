package pl.zielona_baza.admin.setting.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.State;
import pl.zielona_baza.common.entity.StateDTO;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/states")
public class StateRestController {
    private final StateRepository stateRepository;

    public StateRestController(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @GetMapping("/list_by_country/{id}")
    public List<StateDTO> listByCountry(@PathVariable("id") Integer countryId) {
        List<State> listStates = stateRepository.findByCountryOrderByNameAsc(
                Country.builder()
                        .id(countryId).build());
        List<StateDTO> result = new ArrayList<>();

        listStates.forEach(state ->
            result.add(StateDTO.builder()
                            .id(state.getId())
                            .name(state.getName()).build()));

        return result;
    }

    @PostMapping("/save")
    public String save(@RequestBody State state) {
        State savedState = stateRepository.save(state);
        return String.valueOf(savedState.getId());
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") Integer id) {
        stateRepository.deleteById(id);
    }
}
