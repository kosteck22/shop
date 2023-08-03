package pl.zielona_baza.site.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pl.zielona_baza.common.entity.Address;
import pl.zielona_baza.common.entity.CartItem;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.order.*;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.OrderNotFoundException;
import pl.zielona_baza.site.checkout.CheckoutInfo;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {
    public static final int ORDERS_PER_PAGE = 5;
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Customer customer, Address address, List<CartItem> cartItems, PaymentMethod paymentMethod,
                             CheckoutInfo checkoutInfo) {
        Order order = new Order();
                order.setOrderTime(new Date());

                if (paymentMethod.equals(PaymentMethod.PAYPAL)) {
                    order.setStatus(OrderStatus.PAID);
                } else {
                    order.setStatus(OrderStatus.NEW);
                }
                order.setCustomer(customer);
                order.setProductCost(checkoutInfo.getProductCost());
                order.setSubtotal(checkoutInfo.getProductTotal());
                order.setShippingCost(checkoutInfo.getShippingCostTotal());
                order.setTax(0.0F);
                order.setTotal(checkoutInfo.getPaymentTotal());
                order.setDeliverDays(checkoutInfo.getDeliverDays());
                order.setDeliverDate(checkoutInfo.getDeliverDate());

        if (address == null) {
            order.copyAddressFromCustomer();
        } else {
            order.copyShippingAddress(address);
        }

        Set<OrderDetail> orderDetails = order.getOrderDetails();

        cartItems.forEach(i -> {
            Product product = i.getProduct();

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(i.getQuantity())
                    .unitPrice(product.getDiscountPrice().floatValue())
                    .productCost(product.getCost().floatValue() * i.getQuantity())
                    .subtotal(i.getSubtotal())
                    .shippingCost(i.getShippingCost())
                    .build();

            orderDetails.add(orderDetail);
        });

        OrderTrack track = new OrderTrack();
            track.setOrder(order);
            track.setStatus(OrderStatus.NEW);
            track.setNotes(OrderStatus.NEW.defaultDescription());
            track.setUpdatedTime(new Date());

        order.getOrderTracks().add(track);

        return orderRepository.save(order);
    }

    public Page<Order> listForCustomerByPage(Customer customer, int pageNum, String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNum - 1, ORDERS_PER_PAGE, sort);

        if (keyword != null) {
            return orderRepository.findAll(keyword, customer.getId(), pageable);
        }

        return orderRepository.findAll(customer.getId(), pageable);
    }

    public Order getOrder(Integer id, Customer customer) {
        return orderRepository.findByIdAndCustomer(id, customer);
    }

    public void setOrderReturnRequested(OrderReturnRequest request, Customer customer) {
        Order order = orderRepository.findByIdAndCustomer(request.getOrderId(), customer);

        if (order == null) {
            throw new OrderNotFoundException("Order with id [%s] not found".formatted(request.getOrderId()));
        }

        if (order.isReturnRequested()) return;

        OrderTrack track = new OrderTrack();
            track.setOrder(order);
            track.setUpdatedTime(new Date());
            track.setStatus(OrderStatus.RETURN_REQUESTED);

            String notes = "Reason: " + request.getReason();
            if (!"".equals(request.getNote())) {
                notes += ". " + request.getNote();
            }
            track.setNotes(notes);

            order.getOrderTracks().add(track);
            order.setStatus(OrderStatus.RETURN_REQUESTED);

            orderRepository.save(order);
    }
}
