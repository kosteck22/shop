package pl.zielona_baza.admin.brand;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.admin.category.CategoryRepository;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrandRepositoryTest {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @Order(1)
    public void testCreateBrandWithoutCategories() {
        //given
        Brand brand = Brand.builder()
                .name("Asus")
                .logo("asus.png")
                .build();

        //when
        Brand savedBrand = brandRepository.save(brand);

        //then
        assertThat(savedBrand).isNotNull();
        assertThat(savedBrand).isInstanceOf(Brand.class);
        assertThat(savedBrand.getId()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    public void testCreateBrandWithCategoryRelation() {
        //given
        Category category = Category.builder()
                .name("TV")
                .alias("tv")
                .image("tv.png")
                .enabled(true)
                .build();
        Category savedCategory = categoryRepository.save(category);

        Brand brand = Brand.builder()
                .name("Samsung")
                .logo("samsung.png")
                .categories(List.of(category))
                .build();

        //when
        Brand savedBrand = brandRepository.save(brand);

        //then
        assertThat(savedBrand.getCategories()).isNotEmpty();
        assertThat(savedBrand.getCategories().size()).isGreaterThan(0);
        assertThat(savedBrand.getCategories().get(0)).isInstanceOf(Category.class);
    }

    @Test
    @Order(3)
    public void testUpdateBrandName() {
        //given
        Brand brand = brandRepository.findById(1).get();
        String newName = "Apple";
        String newLogo = "apple.png";

        //when
        brand.setName(newName);
        brand.setLogo(newLogo);
        Brand updatedBrand = brandRepository.save(brand);

        //then
        assertThat(updatedBrand.getName()).isEqualTo(newName);
    }

    @Test
    public void test() {
        
    }
}
