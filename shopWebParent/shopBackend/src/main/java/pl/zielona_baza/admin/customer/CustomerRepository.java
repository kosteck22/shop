package pl.zielona_baza.admin.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.admin.paging.SearchRepository;
import pl.zielona_baza.common.entity.Customer;

@Repository
public interface CustomerRepository extends SearchRepository<Customer, Integer> {

    Customer findByEmail(String email);

    @Query("SELECT c " +
            "FROM Customer c " +
            "WHERE CONCAT(c.firstName, ' ', c.lastName, ' ', c.email, ' ', c.addressLine1, ' ', c.addressLine2, ' '," +
                         "c.city, ' ', c.postalCode, ' ', c.state, ' ', c.country.name) " +
            "LIKE %?1%")
    Page<Customer>  findAll(String keyword, Pageable pageable);

    @Query("UPDATE Customer c SET c.enabled=?2 WHERE c.id=?1")
    @Modifying
    void updateEnabledStatus(Integer id, boolean enabled);
}
