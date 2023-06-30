package pl.zielona_baza.admin.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.zielona_baza.common.entity.Category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class CategoryServiceTest {

    @MockBean
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    public void testCheckUniqueInCreateModeReturnDuplicateName() {
        //given
        Integer id = null;
        String name = "Electronics";
        String alias = "abc";
        Category category = Category.builder()
                .id(1)
                .name(name)
                .alias(name)
                .build();

        when(categoryRepository.findByName(name)).thenReturn(category);
        when(categoryRepository.findByAlias(alias)).thenReturn(null);

        //when
        String result = categoryService.checkUnique(id, name, alias);

        //then
        assertThat(result).isEqualTo("DuplicateName");
        verify(categoryRepository, times(1)).findByName(name);
        verify(categoryRepository, times(1)).findByAlias(alias);
    }

    @Test
    public void testCheckUniqueInCreateModeReturnDuplicateAlias() {
        //given
        Integer id = null;
        String name = "Electronics";
        String alias = "Electronics";
        Category category = Category.builder()
                .id(1)
                .name(name)
                .alias(alias)
                .build();

        when(categoryRepository.findByName(name)).thenReturn(null);
        when(categoryRepository.findByAlias(alias)).thenReturn(category);

        //when
        String result = categoryService.checkUnique(id, name, alias);

        //then
        assertThat(result).isEqualTo("DuplicateAlias");
        verify(categoryRepository, times(1)).findByName(name);
        verify(categoryRepository, times(1)).findByAlias(alias);
    }

    @Test
    public void testCheckUniqueInCreateModeReturnOk() {
        //given
        Integer id = null;
        String name = "Electronics";
        String alias = "Electronics";
        Category category = Category.builder()
                .id(1)
                .name(name)
                .alias(alias)
                .build();

        when(categoryRepository.findByName(name)).thenReturn(null);
        when(categoryRepository.findByAlias(alias)).thenReturn(null);

        //when
        String result = categoryService.checkUnique(id, name, alias);

        //then
        assertThat(result).isEqualTo("OK");
        verify(categoryRepository, times(1)).findByName(name);
        verify(categoryRepository, times(1)).findByAlias(alias);
    }

    @Test
    public void testCheckUniqueInEditModeReturnOk() {
        //given
        Integer id = 1;
        String name = "Electronics";
        String alias = "Electronics";
        Category category = Category.builder()
                .id(id)
                .name(name)
                .alias(alias)
                .build();

        when(categoryRepository.findByName(name)).thenReturn(category);
        when(categoryRepository.findByAlias(alias)).thenReturn(category);

        //when
        String result = categoryService.checkUnique(id, name, alias);

        //then
        assertThat(result).isEqualTo("OK");
        verify(categoryRepository, times(1)).findByName(name);
        verify(categoryRepository, times(1)).findByAlias(alias);
    }

    @Test
    public void testCheckUniqueInEditModeReturnDuplicateName() {
        //given
        Integer id = 1;
        String name = "Electronics";
        String alias = "Electronics";
        Category category = Category.builder()
                .id(2)
                .name(name)
                .alias(alias)
                .build();

        when(categoryRepository.findByName(name)).thenReturn(category);
        when(categoryRepository.findByAlias(alias)).thenReturn(null);

        //when
        String result = categoryService.checkUnique(id, name, alias);

        //then
        assertThat(result).isEqualTo("DuplicateName");
        verify(categoryRepository, times(1)).findByName(name);
        verify(categoryRepository, times(1)).findByAlias(alias);
    }

    @Test
    public void testCheckUniqueInEditModeReturnDuplicateAlias() {
        //given
        Integer id = 1;
        String name = "Electronics";
        String alias = "Electronics";
        Category category = Category.builder()
                .id(2)
                .name(name)
                .alias(alias)
                .build();

        when(categoryRepository.findByName(name)).thenReturn(null);
        when(categoryRepository.findByAlias(alias)).thenReturn(category);

        //when
        String result = categoryService.checkUnique(id, name, alias);

        //then
        assertThat(result).isEqualTo("DuplicateAlias");
        verify(categoryRepository, times(1)).findByName(name);
        verify(categoryRepository, times(1)).findByAlias(alias);
    }
}
