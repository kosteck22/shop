package pl.zielona_baza.site.customer;

import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.common.entity.AuthenticationType;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.site.setting.CountryRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CountryRepository countryRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, CountryRepository countryRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.countryRepository = countryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Country> listAllCountries() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    public boolean isEmailUnique(String email) {
        return customerRepository.findByEmail(email).isEmpty();
    }

    public Customer registerCustomer(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setEnabled(false);
        customer.setCreatedAt(new Date());
        customer.setAuthenticationType(AuthenticationType.DATABASE);

        String randomCode = RandomString.make(64);
        customer.setVerificationCode(randomCode);

        Customer savedCustomer = customerRepository.save(customer);

        return savedCustomer;
    }

    public boolean verify(String verificationCode) {
        Optional<Customer> customer = customerRepository.findByVerificationCode(verificationCode);

        if (customer.isEmpty()) return false;
        if (customer.get().isEnabled()) return false;

        customerRepository.enable(customer.get().getId());
        return true;
    }

    public void updateAuthenticationType(Customer customer, AuthenticationType type) {
        if (!customer.getAuthenticationType().equals(type)) {
            customerRepository.updateAuthenticationType(customer.getId(), type);
        }
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public void addNewCustomerUponOAuthLogin(String name, String email, String countryCode,
                                             AuthenticationType authenticationType) {
        Customer customer = Customer.builder()
                .firstName("")
                .lastName("")
                .email(email)
                .enabled(true)
                .createdAt(new Date())
                .authenticationType(authenticationType)
                .password("")
                .addressLine1("")
                .city("")
                .state("")
                .phoneNumber("")
                .postalCode("")
                .country(countryRepository.findByCode(countryCode))
                .build();

        setName(name, customer);

        customerRepository.save(customer);
    }

    private void setName(String name, Customer customer) {
        String[] nameArray = name.split(" ");
        if (nameArray.length < 2) {
            customer.setFirstName(name);
            customer.setLastName("");
        } else {
            String firstName = nameArray[0];

            customer.setFirstName(firstName);
            customer.setLastName(name.replace(firstName + " ", ""));
        }
    }

    public void update(Customer customerInForm) {
        Customer customerInDB = customerRepository.findById(customerInForm.getId()).get();

        if (customerInDB.getAuthenticationType().equals(AuthenticationType.DATABASE)) {
            if (!customerInForm.getPassword().isEmpty()) {
                customerInForm.setPassword(passwordEncoder.encode(customerInForm.getPassword()));
            } else {
                customerInForm.setPassword(customerInDB.getPassword());
            }
        } else {
            customerInForm.setPassword(customerInDB.getPassword());
        }

        customerInForm.setEnabled(customerInDB.isEnabled());
        customerInForm.setCreatedAt(customerInDB.getCreatedAt());
        customerInForm.setVerificationCode(customerInDB.getVerificationCode());
        customerInForm.setAuthenticationType(customerInDB.getAuthenticationType());

        customerRepository.save(customerInForm);
    }

    public String updateResetPasswordToken(String email) throws CustomerNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Could not find any customer with email: " + email));

        String token = RandomString.make(30);
        customer.setResetPasswordToken(token);
        customerRepository.save(customer);

        return token;
    }

    public Customer getByResetPasswordToken(String token) {
        return customerRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with reset token " + token + " not found."));
    }

    public void updatePassword(String token, String newPassword) {
        Customer customer = customerRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with token " + token));

        String passwordEncoded = passwordEncoder.encode(newPassword);
        customer.setPassword(passwordEncoded);
        customer.setResetPasswordToken(null);
        customerRepository.save(customer);
    }
}
