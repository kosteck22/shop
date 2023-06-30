package pl.zielona_baza.site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class ControllerHelper {

    @Autowired private CustomerService customerService;

    public Optional<Customer> getAuthenticatedCustomer(HttpServletRequest request) {
        String email = Utility.getEmailOfAuthenticatedCustomer(request);

        return customerService.getCustomerByEmail(email);
    }
}
