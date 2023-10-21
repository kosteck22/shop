package pl.zielona_baza.admin.category;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.admin.category.CategoryRepository;
import pl.zielona_baza.admin.customer.CustomerController;
import pl.zielona_baza.admin.customer.CustomerNotFoundException;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.Customer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Rollback(value = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @Order(1)
    public void testCreateRootCategory() {
        //given
        Category rootCategory = Category.builder()
                .name("Electronics")
                .alias("Electronics")
                .enabled(true)
                .image("electronics.png").build();

        //when
        Category savedCategory = categoryRepository.save(rootCategory);

        //then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isGreaterThan(0);
        assertThat(savedCategory).isInstanceOf(Category.class);
    }

    @Test
    @Order(2)
    public void testCreateSubCategory() {
        //given
        Category parentCategory = categoryRepository.findById(1).get();
        Category subCategory = Category.builder()
                .name("CPU Processors Unit")
                .alias("CPU Processors Unit")
                .image("cpu_processors_unit.png")
                .parent(parentCategory)
                .enabled(true).build();

        //when
        Category savedCategory = categoryRepository.save(subCategory);

        //then
        assertThat(savedCategory.getId()).isGreaterThan(0);
        assertThat(savedCategory.getParent()).isInstanceOf(Category.class);
    }

    @Test
    @Order(3)
    public void testGetCategoryWithChildren() {
        //given
        Category category = categoryRepository.findById(1).get();

        //when
        Set<Category> children = category.getChildren();

        //then
        assertThat(children.size()).isGreaterThan(0);
    }

    @Test
    @Order(4)
    public void testListRootCategories() {
        //when
        List<Category> rootCategories = categoryRepository.findRootCategories(Sort.by("name").ascending());
        List<Category> filteredCategories = rootCategories.stream().filter(category -> {
            if(category.getParent() == null) return true;
            return false;
        }).collect(Collectors.toList());

        //then
        assertThat(rootCategories).isNotEmpty();
        assertThat(rootCategories.size()).isEqualTo(filteredCategories.size());
    }

    @Test
    public void testFindByName() {
        //given
        String name = "Electronics";

        //when
        Category category = categoryRepository.findByName(name);

        //then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo(name);
    }

    @Test
    public void testFindByAlias() {
        //given
        String alias = "Electronics";

        //when
        Category category = categoryRepository.findByAlias(alias);

        //then
        assertThat(category).isNotNull();
        assertThat(category.getAlias()).isEqualTo(alias);
    }

    @Test
    public void test() {

    }
}
