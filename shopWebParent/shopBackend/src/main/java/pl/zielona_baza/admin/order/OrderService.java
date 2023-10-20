package pl.zielona_baza.admin.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import pl.zielona_baza.admin.exception.ValidationException;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.setting.country.CountryRepository;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderDetail;
import pl.zielona_baza.common.entity.order.OrderStatus;
import pl.zielona_baza.common.entity.order.OrderTrack;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.OrderNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;
import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.validateSortDir;

@Service
public class OrderService {
    private static final int ORDERS_PER_PAGE = 10;
    private static final List<String> SORTABLE_FIELDS_AVAILABLE = new ArrayList<>(
            List.of("id", "customer", "total", "orderTime", "destination", "paymentMethod", "status"));
    private final OrderRepository orderRepository;
    private final CountryRepository countryRepository;

    public OrderService(OrderRepository orderRepository, CountryRepository countryRepository) {
        this.orderRepository = orderRepository;
        this.countryRepository = countryRepository;
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, ORDERS_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "orderTime");
        sortDir = validateSortDir(sortDir);

        Sort sort;
        if ("destination".equals(sortField)) {
            sort = Sort.by("country").and(Sort.by("state")).and(Sort.by("city"));
        } else {
            sort = Sort.by(sortField);
        }

        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNumber - 1, limit, sort);

        Page<Order> page = null;

        if (keyword != null) {
            page = orderRepository.findAll(keyword, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        PagingAndSortingHelper helper = new PagingAndSortingHelper( "listOrders", sortField, sortDir, keyword, limit);

        helper.updateModelAttributes(pageNumber, page, model);
    }

    public Order get(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Count not find order with ID %d".formatted(id)));
    }

    public void delete(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id %d".formatted(id)));
        orderRepository.delete(order);
    }

    public List<Country> listAllCountries() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    public void save(Order orderInForm, ProductDetailsParamHelper productDetailsHelper, OrderTrackParamHelper orderTrackParamHelper) {
        updateProductDetails(orderInForm, productDetailsHelper);
        updateOrderTracks(orderInForm, orderTrackParamHelper);

        Order orderInDB = orderRepository.findById(orderInForm.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id " + orderInForm.getId()));


        orderInForm.setOrderTime(orderInDB.getOrderTime());
        orderInForm.setCustomer(orderInDB.getCustomer());

        orderRepository.save(orderInForm);
    }

    public void updateStatus(Integer orderId, String status) {
        Order orderInDB = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id %d".formatted(orderId)));
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

    private void updateOrderTracks(Order order, OrderTrackParamHelper helper) {
        helper.setOrderTracks(order);
    }

    private void updateProductDetails(Order order, ProductDetailsParamHelper helper) {
        helper.setOrderDetails(order);
    }

    public void restoreProductDetailsAndOrderTracks(Order order, ProductDetailsParamHelper productDetailsHelper, OrderTrackParamHelper orderTrackParamHelper) {
        updateProductDetails(order, productDetailsHelper);
        updateOrderTracks(order, orderTrackParamHelper);

    }
}
