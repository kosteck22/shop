package pl.zielona_baza.admin.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.admin.paging.SearchRepository;
import pl.zielona_baza.common.entity.Review;

@Repository
public interface ReviewRepository extends SearchRepository<Review, Integer> {
    @Query("SELECT r FROM Review r WHERE CONCAT(r.product.name, ' ', r.customer.firstName, ' ', r.customer.lastName, " +
            "' ', r.headline, ' ', r.rating) LIKE %?1%")
    Page<Review> findAll(String keyword, Pageable pageable);
}
