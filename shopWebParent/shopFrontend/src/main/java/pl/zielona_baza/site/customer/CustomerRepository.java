package pl.zielona_baza.site.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.common.entity.AuthenticationType;
import pl.zielona_baza.common.entity.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByVerificationCode(String verificationCode);

    @Query("UPDATE Customer c SET c.enabled=true, c.verificationCode=null WHERE c.id=?1")
    @Modifying
    boolean enable(Integer id);

    @Query("UPDATE Customer c SET c.authenticationType = ?2 WHERE c.id = ?1")
    @Modifying
    void updateAuthenticationType(Integer customerId, AuthenticationType type);

    Optional<Customer> findByResetPasswordToken(String token);
}
