package pl.zielona_baza.site.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.zielona_baza.common.entity.order.OrderDetail;
import pl.zielona_baza.common.entity.order.OrderStatus;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("SELECT COUNT(od) FROM OrderDetail od JOIN OrderTrack t ON od.order.id = t.order.id WHERE " +
            "od.product.id = ?1 AND " +
            "od.order.customer.id = ?2 AND " +
            "t.status = ?3")
    Long countByProductAndCustomerAndOrderStatus(Integer productId, Integer CustomerId, OrderStatus status);
}
