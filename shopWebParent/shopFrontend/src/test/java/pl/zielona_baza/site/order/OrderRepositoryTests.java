package pl.zielona_baza.site.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderStatus;
import pl.zielona_baza.common.entity.order.OrderTrack;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Rollback(value = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTests {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testUpdateOrderTracks() {
        //given
        Integer orderId = 1;
        Order order = orderRepository.findById(orderId).get();

        OrderTrack newTrack = OrderTrack.builder()
                .order(order)
                .updatedTime(new Date())
                .status(OrderStatus.NEW)
                .notes(OrderStatus.NEW.defaultDescription())
                .build();

        List<OrderTrack> orderTracks = order.getOrderTracks();
        orderTracks.add(newTrack);

        //when
        Order updatedOrder = orderRepository.save(order);

        //then
        assertThat(updatedOrder.getOrderTracks()).hasSizeGreaterThan(1);
    }
}
