package pl.zielona_baza.admin.brand;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zielona_baza.common.entity.Brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    private BrandService underTest;

    @BeforeEach
    public void setUp() {
        underTest = new BrandService(brandRepository);
    }

    @Test
    public void testNameValidatorNewBrandUniqueName() {
        //given
        Integer id = null;
        String name = "Acer";
        when(brandRepository.findByName(name)).thenReturn(null);

        //when
        boolean result = underTest.isNameValid(id, name);

        //then
        assertThat(result).isTrue();
    }

    @Test
    public void testNameValidatorNewBrandDuplicateName() {
        //given
        Integer id = null;
        String name = "Acer";
        when(brandRepository.findByName(name)).thenReturn(new Brand(1, "Acer"));

        //when
        boolean result = underTest.isNameValid(id, name);

        //then
        assertThat(result).isFalse();
    }

    @Test
    public void testNameValidatorEditBrandDuplicateName() {
        //given
        Integer id = 1;
        String name = "Acer";
        when(brandRepository.findByName(name)).thenReturn(new Brand(2, "Acer"));

        //when
        boolean result = underTest.isNameValid(id, name);

        //then
        assertThat(result).isFalse();
    }

    @Test
    public void testNameValidatorEditBrandSameName() {
        //given
        Integer id = 1;
        String name = "Acer";
        when(brandRepository.findByName(name)).thenReturn(new Brand(1, "Acer"));

        //when
        boolean result = underTest.isNameValid(id, name);

        //then
        assertThat(result).isTrue();
    }
}
