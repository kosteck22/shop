package pl.zielona_baza.site.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.common.entity.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.enabled=true "
            + "AND (p.category.id = ?1 OR p.category.allParentIds LIKE %?2%) " +
            "ORDER BY p.name ASC")
    Page<Product> listByCategory(Integer categoryId, String categoryIDMatch, Pageable pageable);
    Product findByAlias(String alias);

    @Query(value = "SELECT * FROM products WHERE " +
            "enabled=true AND " +
            "MATCH(name, short_description, full_description) AGAINST (?1)",
            nativeQuery = true)
    Page<Product> search(String keyword, Pageable pageable);

    @Query("UPDATE Product p SET " +
            "p.averageRating = COALESCE((SELECT AVG(r.rating) FROM Review r WHERE r.product.id = ?1), 0), " +
            "p.reviewCount = COALESCE((SELECT COUNT(r.id) FROM Review r WHERE r.id = ?1), 0) " +
            "WHERE p.id = ?1")
    @Modifying
    void updateReviewCountAndAverageRating(Integer productId);
}
