package pl.zielona_baza.admin.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerRestController {
    @Autowired private CustomerService customerService;

    @PostMapping("/customers/check_email")
    public String checkDuplicateEmail(@RequestParam("id") Integer id, @RequestParam("email") String email) {
        if (customerService.isEmailUnique(id, email)) {
            return "OK";
        }

        return "Duplicated";
    }
}
