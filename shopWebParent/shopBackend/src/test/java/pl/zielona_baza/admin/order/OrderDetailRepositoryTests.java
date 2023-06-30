package pl.zielona_baza.admin.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.order.OrderDetail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class OrderDetailRepositoryTests {

    @Autowired private OrderDetailRepository orderDetailRepository;

    @Test
    public void testFindWithCategoryAndTimeBetween() throws ParseException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startTime = dateFormatter.parse("2021-08-01");
        Date endTime = dateFormatter.parse("2021-08-31");

        List<OrderDetail> listOrderDetails = orderDetailRepository.findWithCategoryAndTimeBetween(startTime, endTime);

        assertThat(listOrderDetails.size()).isGreaterThan(0);


        listOrderDetails.forEach(d -> {
            System.out.printf("%s | %d | %.2f | %.2f | %.2f \n",
                    d.getProduct().getCategory().getName(),
                    d.getQuantity(),
                    d.getProductCost(),
                    d.getShippingCost(),
                    d.getSubtotal());
        });
    }

    @Test
    public void testFindWithProductAndTimeBetween() throws ParseException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startTime = dateFormatter.parse("2021-10-01");
        Date endTime = dateFormatter.parse("2021-10-31");

        List<OrderDetail> listOrderDetails = orderDetailRepository.findWithProductAndTimeBetween(startTime, endTime);

        assertThat(listOrderDetails.size()).isGreaterThan(0);


        listOrderDetails.forEach(d -> {
            System.out.printf("%s | %d | %.2f | %.2f | %.2f \n",
                    d.getProduct().getName(),
                    d.getQuantity(),
                    d.getProductCost(),
                    d.getShippingCost(),
                    d.getSubtotal());
        });
    }
}
