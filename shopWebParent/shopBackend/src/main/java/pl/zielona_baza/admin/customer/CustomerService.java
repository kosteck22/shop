package pl.zielona_baza.admin.customer;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import pl.zielona_baza.admin.exception.ValidationException;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.setting.country.CountryRepository;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;
import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.validateSortDir;

@Service
@Transactional
public class CustomerService {
    private static final List<String> SORTABLE_FIELDS_AVAILABLE = new ArrayList<>(
            List.of("id", "email", "firstName", "lastName", "city", "state", "country", "enabled"));
    private static final Integer CUSTOMERS_PER_PAGE = 20;
    private final CustomerDTOMapper customerDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final CountryRepository countryRepository;

    public CustomerService(CustomerDTOMapper customerDTOMapper, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, CountryRepository countryRepository) {
        this.customerDTOMapper = customerDTOMapper;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.countryRepository = countryRepository;
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, CUSTOMERS_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "email");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper( "listCustomers", sortField, sortDir, keyword, limit);

        helper.listEntities(pageNumber, customerRepository, model, customerDTOMapper);
    }

    public CustomerDTO get(Integer id) throws CustomerNotFoundException {
        return customerRepository
                .findById(id)
                .map(customerDTOMapper)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID %d".formatted(id)));
    }

    public void save(CustomerUpdateRequest customerUpdateRequest) throws ValidationException, CustomerNotFoundException {
        //Checking if customer exist in DB
        Customer customerInDB = customerRepository.findById(customerUpdateRequest.getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer you want to edit not found with ID %d".formatted(customerUpdateRequest.getId())));

        //Checking if country exist in DB
        Country country = countryRepository.findById(customerUpdateRequest.getCountry())
                .orElseThrow(() -> new ValidationException("Country not found. Pick correct one"));

        //Validate email
        if (!isEmailUnique(customerUpdateRequest.getId(), customerUpdateRequest.getEmail())) {
            throw new ValidationException("Choose another email. This one is already taken");
        }


        if (!customerUpdateRequest.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(customerUpdateRequest.getPassword());
            customerInDB.setPassword(encodedPassword);
        }

        customerInDB.setEmail(customerUpdateRequest.getEmail());
        customerInDB.setFirstName(customerUpdateRequest.getFirstName());
        customerInDB.setLastName(customerUpdateRequest.getLastName());
        customerInDB.setPhoneNumber(customerUpdateRequest.getPhoneNumber());
        customerInDB.setAddressLine1(customerUpdateRequest.getAddressLine1());

        if (!customerUpdateRequest.getAddressLine2().isEmpty()) {
            customerInDB.setAddressLine2(customerUpdateRequest.getAddressLine2());
        }

        customerInDB.setPostalCode(customerUpdateRequest.getPostalCode());
        customerInDB.setCity(customerUpdateRequest.getCity());
        customerInDB.setState(customerUpdateRequest.getState());
        customerInDB.setCountry(country);

        customerRepository.save(customerInDB);
    }

    public void delete(Integer id) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Could not find customer with ID %d".formatted(id)));

        customerRepository.delete(customer);
    }

    public List<Country> listAllCountries() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    public void updateCustomerEnabledStatus(Integer id, boolean enabled) throws CustomerNotFoundException {
        customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID %d".formatted(id)));
        customerRepository.updateEnabledStatus(id, enabled);
    }

    public boolean isEmailUnique(Integer id, String email) {
        Customer customer = customerRepository.findByEmail(email);

        return customer == null || Objects.equals(customer.getId(), id);
    }
}