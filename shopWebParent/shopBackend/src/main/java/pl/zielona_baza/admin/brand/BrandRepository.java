package pl.zielona_baza.admin.brand;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.admin.paging.SearchRepository;
import pl.zielona_baza.common.entity.Brand;
import java.util.List;
@Repository
public interface BrandRepository extends SearchRepository<Brand, Integer> {

    @Query("SELECT b FROM Brand b WHERE CONCAT(b.id, ' ', b.name)" +
            " LIKE %?1%")
    Page<Brand> findAll(String keyword, Pageable pageable);
    Brand findByName(String name);

    @Query("SELECT NEW Brand(b.id, b.name) FROM Brand b ORDER BY b.name ASC")
    public List<Brand> findAllProjection();

}
