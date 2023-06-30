package pl.zielona_baza.site.review.vote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.ReviewVote;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class ReviewVoteRepositoryTests {

    @Autowired private ReviewVoteRepository reviewVoteRepository;

    @Test
    public void testCreateReviewVote() {
        Review review = new Review(4);
        Customer customer = new Customer(1);

        ReviewVote reviewVote = new ReviewVote();
        reviewVote.setCustomer(customer);
        reviewVote.setReview(review);
        reviewVote.setVotes(1);

        ReviewVote savedReviewVote = reviewVoteRepository.save(reviewVote);

        assertThat(savedReviewVote).isNotNull();
        assertThat(savedReviewVote.getId()).isGreaterThan(0);
    }
}
