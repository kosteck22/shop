package pl.zielona_baza.admin.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.admin.paging.SearchRepository;
import pl.zielona_baza.common.entity.Category;
import java.util.List;

@Repository
public interface CategoryRepository extends SearchRepository<Category, Integer> {

    @Query("SELECT c FROM Category c WHERE CONCAT(c.id, ' ', c.name, ' ', c.alias)" +
            " LIKE %?1%")
    Page<Category> findAll(String keyword, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.parent.id is NULL")
    List<Category> findRootCategories(Sort sort);

    Category findByName(String name);

    Category findByAlias(String alias);

    @Query("UPDATE Category c SET c.enabled = ?2 WHERE c.id = ?1")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateEnabledStatus(Integer id, boolean enabled);
}
