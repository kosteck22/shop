package pl.zielona_baza.site.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.Iterator;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderDetail;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;
import pl.zielona_baza.site.review.ReviewService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final ControllerHelper controllerHelper;
    private final ReviewService reviewService;

    public OrderController(OrderService orderService, ControllerHelper controllerHelper, ReviewService reviewService) {
        this.orderService = orderService;
        this.controllerHelper = controllerHelper;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String listFirstPage(Model model, HttpServletRequest request) {
        return listOrdersByPage(model, request, 1, "orderTime", "desc", null);
    }

    @GetMapping("/page/{pageNum}")
    public String listOrdersByPage(
                                   Model model,
                                   HttpServletRequest request,
                                   @PathVariable(name = "pageNum") int pageNum,
                                   @RequestParam(name = "sortField") String sortField,
                                   @RequestParam(name = "sortDir") String sortDir,
                                   @RequestParam(name = "orderKeyword", required = false) String orderKeyword
    ) {
        Customer authenticatedCustomer = controllerHelper.getAuthenticatedCustomer(request).get();

        Page<Order> page = orderService.listForCustomerByPage(authenticatedCustomer, pageNum, sortField, sortDir, orderKeyword);
        List<Order> listOrders = page.getContent();

        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("listOrders", listOrders);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("orderKeyword", orderKeyword);
        model.addAttribute("moduleURL", "/orders");
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        long startCount = (long) (pageNum - 1) * OrderService.ORDERS_PER_PAGE + 1;
        model.addAttribute("startCount", startCount);

        long endCount = startCount + OrderService.ORDERS_PER_PAGE - 1;
        if (endCount > page.getTotalElements()) {
            endCount = page.getTotalElements();
        }

        model.addAttribute("endCount", endCount);

        return "orders/orders_customer";
    }

    @GetMapping("/detail/{id}")
    public String viewOrderDetails(
                                    Model model,
                                    @PathVariable(name = "id") Integer id,
                                    HttpServletRequest request
    ) {
        Customer authenticatedCustomer = controllerHelper.getAuthenticatedCustomer(request).get();

        Order order = orderService.getOrder(id, authenticatedCustomer);

        setProductReviewableStatus(authenticatedCustomer, order);

        model.addAttribute("order", order);

        return "/orders/order_details_modal";
    }

    private void setProductReviewableStatus(Customer authenticatedCustomer, Order order) {
        Iterator<OrderDetail> iterator = order.getOrderDetails().iterator();

        while (iterator.hasNext()) {
            OrderDetail orderDetail = iterator.next();
            Product product = orderDetail.getProduct();
            Integer productId = product.getId();

            boolean didCustomerReviewProduct = reviewService.didCustomerReviewProduct(authenticatedCustomer, productId);
            product.setReviewedByCustomer(didCustomerReviewProduct);

            if (!didCustomerReviewProduct) {
                boolean canCustomerReviewProduct = reviewService.canCustomerReviewProduct(authenticatedCustomer, productId);
                product.setCustomerCanReview(canCustomerReviewProduct);
            }
        }
    }
}
