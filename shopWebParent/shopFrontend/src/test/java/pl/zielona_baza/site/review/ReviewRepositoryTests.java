package pl.zielona_baza.site.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.product.Product;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class ReviewRepositoryTests {
    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    public void testFindReviewsByProduct() {
        //given
        Integer productId = 1;
        Product product = new Product(productId);
        Pageable pageable = PageRequest.of(0, 3);

        //when
        Page<Review> page = reviewRepository.findByProduct(product, pageable);

        //then
        assertThat(page.getTotalElements()).isGreaterThan(0);
    }

    @Test
    public void testCountByCustomerAndProduct() {
        Integer customerId = 5;
        Integer productId = 1;

        Long count = reviewRepository.countByCustomerAndProduct(customerId, productId);

        assertThat(count).isEqualTo(1);
    }
}