package pl.zielona_baza.site.review.vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.ReviewVote;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Integer> {

    @Query("SELECT rv FROM ReviewVote rv WHERE rv.review.id = ?1 AND rv.customer.id = ?2")
    Optional<ReviewVote> findByReviewAndCustomer(Integer reviewId, Integer customerId);

    @Query("SELECT rv FROM ReviewVote rv WHERE rv.customer.id = ?2 AND rv.review.product.id = ?1")
    List<ReviewVote> findByProductAndCustomer(Integer productId, Integer customerId);
}
