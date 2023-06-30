package pl.zielona_baza.site.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.order.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
class OrderDetailRepositoryTests {

    @Autowired private OrderDetailRepository orderDetailRepository;

    @Test
    public void testCountByProductIdCustomerIdAndOrderStatus() {
        Integer productId = 99;
        Integer customerId = 1;
        OrderStatus orderStatus = OrderStatus.DELIVERED;

        Long countOrderDetails = orderDetailRepository.countByProductAndCustomerAndOrderStatus(productId, customerId, orderStatus);

        System.out.println("Count: " + countOrderDetails);

        assertThat(countOrderDetails).isGreaterThan(0);
    }
}