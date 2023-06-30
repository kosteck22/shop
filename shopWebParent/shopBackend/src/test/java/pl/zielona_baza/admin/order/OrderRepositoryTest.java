package pl.zielona_baza.admin.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;
import pl.zielona_baza.common.entity.*;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderDetail;
import pl.zielona_baza.common.entity.order.OrderStatus;
import pl.zielona_baza.common.entity.order.PaymentMethod;
import pl.zielona_baza.common.entity.product.Product;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private TestEntityManager testEntityManager;

    @Test
    public void testAddNewOrder() {
        //given
        Customer customer = testEntityManager.find(Customer.class, 1);
        Product product = testEntityManager.find(Product.class, 1);

        Order mainOrder = new Order();
        mainOrder.setCustomer(customer);
        mainOrder.setFirstName(customer.getFirstName());
        mainOrder.setLastName(customer.getLastName());
        mainOrder.setPhoneNumber(customer.getPhoneNumber());
        mainOrder.setAddressLine1(customer.getAddressLine1());
        mainOrder.setAddressLine2(customer.getAddressLine2());
        mainOrder.setCity(customer.getCity());
        mainOrder.setState(customer.getState());
        mainOrder.setCountry(customer.getCountry().getName());
        mainOrder.setPostalCode(customer.getPostalCode());
        mainOrder.setShippingCost(10);
        mainOrder.setProductCost(product.getCost());
        mainOrder.setTax(0);
        mainOrder.setSubtotal(product.getPrice());
        mainOrder.setTotal(product.getPrice() + 10);
        mainOrder.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        mainOrder.setStatus(OrderStatus.NEW);
        mainOrder.setDeliverDate(new Date());
        mainOrder.setDeliverDays(2);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProduct(product);
        orderDetail.setOrder(mainOrder);
        orderDetail.setProductCost(product.getCost());
        orderDetail.setShippingCost(10);
        orderDetail.setQuantity(1);
        orderDetail.setSubtotal(product.getPrice());
        orderDetail.setUnitPrice(product.getPrice());

        mainOrder.getOrderDetails().add(orderDetail);

        //when
        Order savedOrder = orderRepository.save(mainOrder);
        //then
        assertThat(savedOrder.getId()).isGreaterThan(0);
    }

    @Test
    public void testFindByOrderTimeBetween() throws ParseException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date startTime = dateFormatter.parse("2021-08-01");
        Date endTime = dateFormatter.parse("2021-08-31");

        List<Order> listOrders = orderRepository.findByOrderTimeBetween(startTime, endTime);

        assertThat(listOrders.size()).isGreaterThan(0);

        listOrders.forEach(o -> {
            System.out.printf("%s | %s | %.2f | %.2f | %.2f \n",
                    o.getId(),
                    o.getOrderTime(),
                    o.getProductCost(),
                    o.getSubtotal(),
                    o.getTotal());
        });

    }
}