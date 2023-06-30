package pl.zielona_baza.admin.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.common.entity.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p WHERE CONCAT(p.name, ' ', p.shortDescription, ' ', p.fullDescription, ' '," +
            " p.brand.name, ' ', p.category.name) LIKE %?1%")
    Page<Product> findAll(String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = ?1 " +
            "OR p.category.allParentIds LIKE %?2%")
    public Page<Product> findAllInCategory(Integer categoryId, String categoryIdMatch, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (p.category.id = ?1 " +
            "OR p.category.allParentIds LIKE %?2%) AND " +
            "(CONCAT(p.name, ' ', p.shortDescription, ' ', p.fullDescription, ' '," +
            " p.brand.name, ' ', p.category.name) LIKE %?3%)")
    public Page<Product> searchInCategory(Integer categoryId, String categoryIdMatch, String keyword, Pageable pageable);

    Product findByName(String name);
    @Query("UPDATE Product p SET p.enabled = ?2 WHERE p.id = ?1")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateEnabledStatus(Integer id, boolean enabled);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1%")
    public Page<Product> searchProductsByName(String keyword, Pageable pageable);

    @Query("UPDATE Product p SET " +
            "p.averageRating = COALESCE((SELECT AVG(r.rating) FROM Review r WHERE r.product.id = ?1), 0), " +
            "p.reviewCount = COALESCE((SELECT COUNT(r.id) FROM Review r WHERE r.id = ?1), 0) " +
            "WHERE p.id = ?1")
    @Modifying
    void updateReviewCountAndAverageRating(Integer productId);
}
