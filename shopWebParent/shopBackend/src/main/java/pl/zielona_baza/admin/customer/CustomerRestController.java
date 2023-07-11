package pl.zielona_baza.admin.customer;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerRestController {
    private final CustomerService customerService;

    public CustomerRestController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/customers/check_email")
    public String checkDuplicateEmail(@RequestParam("id") Integer id, @RequestParam("email") String email) {
        if (customerService.isEmailUnique(id, email)) {
            return "OK";
        }

        return "Duplicated";
    }
}
