package pl.zielona_baza.site.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.exception.OrderNotFoundException;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;

import javax.servlet.http.HttpServletRequest;

@RestController
public class OrderRestController {
    private final OrderService orderService;
    private final ControllerHelper controllerHelper;

    public OrderRestController(OrderService orderService, ControllerHelper controllerHelper) {
        this.orderService = orderService;
        this.controllerHelper = controllerHelper;
    }

    @PostMapping("/orders/return")
    public ResponseEntity<?> handleOrderReturnRequest(@RequestBody OrderReturnRequest requestDTO, HttpServletRequest request) {
        try {
            Customer authenticatedCustomer = controllerHelper.getAuthenticatedCustomer(request)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
            orderService.setOrderReturnRequested(requestDTO, authenticatedCustomer);
        } catch (CustomerNotFoundException ex) {
            return new ResponseEntity<>("Authentication required", HttpStatus.BAD_REQUEST);
        } catch (OrderNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(new OrderReturnResponse(requestDTO.getOrderId()), HttpStatus.OK);
    }

}
