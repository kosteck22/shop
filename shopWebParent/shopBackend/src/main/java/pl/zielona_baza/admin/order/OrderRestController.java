package pl.zielona_baza.admin.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRestController {
    private final OrderService orderService;

    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders_shipper/update/{id}/{status}")
    public OrderRestResponse updateOrderStatus(@PathVariable(name = "id") Integer orderId,
                                  @PathVariable(name = "status") String status) {
        orderService.updateStatus(orderId, status);

        return new OrderRestResponse(orderId, status);
    }
}
