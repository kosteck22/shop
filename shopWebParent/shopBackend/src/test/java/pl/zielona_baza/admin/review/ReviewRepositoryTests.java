package pl.zielona_baza.admin.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.product.Product;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class ReviewRepositoryTests {

    @Autowired private ReviewRepository reviewRepository;

    @Test
    public void testCreateReview() {
        //given
        Integer productId = 1;
        Product product = new Product(productId);

        Integer customerId = 5;
        Customer customer = new Customer(customerId);

        Review review = new Review();
            review.setProduct(product);
            review.setCustomer(customer);
            review.setReviewTime(new Date());
            review.setHeadline("Perfect for my needs.");
            review.setComment("Nice to have: wireless remote, IOS app, GPS...");
            review.setRating(5);

        //when
        Review savedReview = reviewRepository.save(review);

        //then
        assertThat(savedReview).isNotNull();
        assertThat(savedReview.getId()).isGreaterThan(0);
    }
}
