package pl.zielona_baza.admin.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.admin.paging.SearchRepository;
import pl.zielona_baza.common.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends SearchRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Long countById(Integer id);

    @Query("SELECT u FROM User u WHERE CONCAT(u.id, ' ', u.email, ' ', u.firstName, ' ', u.lastName)" +
            " LIKE %?1%")
    Page<User> findAll(String keyword, Pageable pageable);

    @Query("UPDATE User u SET u.enabled = ?2 WHERE u.id = ?1")
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateEnabledStatus(Integer id, boolean enabled);
}
