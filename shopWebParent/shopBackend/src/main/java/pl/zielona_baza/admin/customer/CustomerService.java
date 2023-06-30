package pl.zielona_baza.admin.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.setting.country.CountryRepository;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.Customer;
import java.util.List;

@Service
@Transactional
public class CustomerService {
    public static final Integer CUSTOMERS_PER_PAGE = 15;

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private CountryRepository countryRepository;

    public void listByPage(Integer pageNum, PagingAndSortingHelper helper) {
       helper.listEntities(pageNum, CUSTOMERS_PER_PAGE, customerRepository);
    }

    public void updateCustomerEnabledStatus(Integer id, boolean enabled) {
        customerRepository.updateEnabledStatus(id, enabled);
    }

    public Customer get(Integer id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID " + id));
    }

    public List<Country> listAllCountries() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    public boolean isEmailUnique(Integer id, String email) {
        Customer customer = customerRepository.findByEmail(email);

        if (customer != null && customer.getId() != id) {
            return false;
        }

        return true;
    }

    public void save(Customer customerInForm) {
        Customer customerInDB = customerRepository.findById(customerInForm.getId()).get();

        if (!customerInForm.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(customerInForm.getPassword());
            customerInForm.setPassword(encodedPassword);
        } else {
            customerInForm.setPassword(customerInDB.getPassword());
        }

        customerInForm.setEnabled(customerInDB.isEnabled());
        customerInForm.setCreatedAt(customerInDB.getCreatedAt());
        customerInForm.setVerificationCode(customerInDB.getVerificationCode());
        customerInForm.setAuthenticationType(customerInDB.getAuthenticationType());

        customerRepository.save(customerInForm);
    }

    public void delete(Integer id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Could not find " +
                "customer with ID " + id));

        customerRepository.delete(customer);
    }
}