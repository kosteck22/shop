package pl.zielona_baza.admin.setting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.zielona_baza.admin.setting.country.CountryRepository;
import pl.zielona_baza.common.entity.Country;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CountryRestControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private CountryRepository countryRepository;

    @Test
    @WithMockUser(username = "kamilostafil@gmail.com", password = "admin123", roles = "ADMIN")
    public void testListCountries() throws Exception {
        String url = "/countries/list";
        MvcResult mvcResult = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        Country[] countries = objectMapper.readValue(jsonResponse, Country[].class);

        assertThat(countries).hasSizeGreaterThan(0);
    }

    @Test
    @WithMockUser(username = "kamilostafil@gmail.com", password = "admin123", roles = "ADMIN")
    public void testCreateCountry() throws Exception {
        Country country = Country.builder()
                .name("Canada")
                .code("CA")
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/countries/save").contentType("application/json")
                        .content(objectMapper.writeValueAsString(country))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        System.out.println("Country ID: " + response);
    }

    @Test
    @WithMockUser(username = "kamilostafil@gmail.com", password = "admin123", roles = "ADMIN")
    public void testUpdateCountry() throws Exception {
        Country country = Country.builder()
                .id(5)
                .name("China")
                .code("CN")
                .build();

        mockMvc.perform(post("/countries/save").contentType("application/json")
                        .content(objectMapper.writeValueAsString(country))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(5)));

        Optional<Country> countryById = countryRepository.findById(5);

        assertThat(countryById.isPresent()).isTrue();
        assertThat(countryById.get().getName()).isEqualTo("China");
    }

    @Test
    @WithMockUser(username = "kamilostafil@gmail.com", password = "admin123", roles = "ADMIN")
    public void testDeleteCountry() throws Exception {
        Optional<Country> countryByIdBefore = countryRepository.findById(5);

        mockMvc.perform(get("/countries/delete/5"))
                .andExpect(status().isOk());

        Optional<Country> countryByIdAfter = countryRepository.findById(5);

        assertThat(countryByIdBefore).isPresent();
        assertThat(countryByIdAfter).isNotPresent();
    }
}
