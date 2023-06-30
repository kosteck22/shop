package pl.zielona_baza.admin.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.admin.paging.SearchRepository;
import pl.zielona_baza.common.entity.order.Order;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository  extends SearchRepository<Order, Integer> {

    @Query("SELECT o FROM Order o WHERE CONCAT(" +
            "'#', o.id, ' ', " +
            "o.firstName, ' ', " +
            "o.lastName, ' ', " +
            "o.phoneNumber, ' ', " +
            "o.addressLine1, ' ', " +
            "o.addressLine2, ' ', " +
            "o.postalCode, ' ', " +
            "o.city, ' ', " +
            "o.state, ' ', " +
            "o.country, ' ', " +
            "o.paymentMethod, ' ', " +
            "o.customer.firstName, ' ', " +
            "o.customer.lastName) " +
            "LIKE %?1%")
    Page<Order> findAll(String keyword, Pageable pageable);

    @Query("SELECT NEW pl.zielona_baza.common.entity.order.Order(o.id, o.orderTime, o.productCost, o.subtotal, o.total) " +
            "FROM Order o WHERE o.orderTime BETWEEN ?1 AND ?2 ORDER BY o.orderTime ASC")
    List<Order> findByOrderTimeBetween(Date startTime, Date endTime);
}
