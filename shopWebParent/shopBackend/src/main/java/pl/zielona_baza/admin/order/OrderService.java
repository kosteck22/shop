package pl.zielona_baza.admin.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.setting.country.CountryRepository;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderStatus;
import pl.zielona_baza.common.entity.order.OrderTrack;
import pl.zielona_baza.common.exception.OrderNotFoundException;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {
    public static final int ORDERS_PER_PAGE = 10;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CountryRepository countryRepository;

    public void listByPage(Integer pageNum, PagingAndSortingHelper helper) {
        String sortField = helper.getSortField();
        String sortDir = helper.getSortDir();
        String keyword = helper.getKeyword();

        Sort sort = null;

        if ("destination".equals(sortField)) {
            sort = Sort.by("country").and(Sort.by("state")).and(Sort.by("city"));
        } else {
            sort = Sort.by(sortField);
        }

        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNum - 1, ORDERS_PER_PAGE, sort);

        Page<Order> page = null;

        if (keyword != null) {
            page = orderRepository.findAll(keyword, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }

        helper.updateModelAttributes(pageNum, page);
    }

    public Order get(Integer id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Count not find order with ID " + id));
    }

    public void delete(Integer id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found with id " + id));
        orderRepository.delete(order);
    }

    public List<Country> listAllCountries() {

        return countryRepository.findAllByOrderByNameAsc();
    }

    public void save(Order orderInForm) {
        Order orderInDB = orderRepository.findById(orderInForm.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + orderInForm.getId()));

        orderInForm.setOrderTime(orderInDB.getOrderTime());
        orderInForm.setCustomer(orderInDB.getCustomer());

        orderRepository.save(orderInForm);
    }

    public void updateStatus(Integer orderId, String status) {
        Order orderInDB = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + orderId));
        OrderStatus statusToUpdate = OrderStatus.valueOf(status);

        if(!orderInDB.hasStatus(statusToUpdate)) {
            List<OrderTrack> orderTracks = orderInDB.getOrderTracks();

            OrderTrack track = new OrderTrack();
                track.setOrder(orderInDB);
                track.setStatus(statusToUpdate);
                track.setUpdatedTime(new Date());
                track.setNotes(statusToUpdate.defaultDescription());

            orderTracks.add(track);

            orderInDB.setStatus(statusToUpdate);

            orderRepository.save(orderInDB);
        }

    }
}
