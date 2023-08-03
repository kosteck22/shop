package pl.zielona_baza.admin.product;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.product.Product;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testCreateProduct() {
        //given
        Brand brand = entityManager.find(Brand.class, 37);
        Category category = entityManager.find(Category.class, 5);

        Product product = Product.builder()
                .name("Acer Aspire Desktop")
                .alias("acer_aspire_desktop")
                .shortDescription("A good desktop from Acer")
                .fullDescription("This is a very good desktop full description")
                .mainImage("main.png")
                .brand(brand)
                .category(category)
                .cost(BigDecimal.valueOf(400))
                .price(BigDecimal.valueOf(456))
                .enabled(true)
                .inStock(true)
                .createdTime(new Date())
                .updatedTime(new Date())
                .build();

        //when
        Product savedProduct = productRepository.save(product);

        //then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isGreaterThan(0);
    }

    @Test
    public void testSaveProductWithImages() {
        //given
        Integer productId = 1;
        Product product = productRepository.findById(productId).get();

        //when
        product.setMainImage("main_image.jpg");
        product.addExtraImage("extra image 1.jpg");
        product.addExtraImage("extra image 2.jpg");
        product.addExtraImage("extra image 3.jpg");
        Product savedProduct = productRepository.save(product);

        //then
        assertThat(savedProduct.getImages().size()).isEqualTo(3);
    }

    @Test
    public void testSaveProductWithProductDetails() {
        //given
        Integer productId = 1;
        Product product = productRepository.findById(productId).get();

        //when
        product.addDetail("Computer Memory Size", "8 GB");
        product.addDetail("CPU Speed", "1.20 GHz");
        product.addDetail("Processor Count", "2");
        product.addDetail("RAM Type", "DDR4 SDRAM");
        Product savedProduct = productRepository.save(product);

        //then
        assertThat(savedProduct.getDetails().size()).isEqualTo(4);
    }
}
