package pl.zielona_baza.admin.customer;

import org.springframework.stereotype.Service;
import pl.zielona_baza.admin.DTOMapper;
import pl.zielona_baza.common.entity.Customer;

@Service
public class CustomerDTOMapper implements DTOMapper<Customer, CustomerDTO> {
    @Override
    public CustomerDTO apply(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .postalCode(customer.getPostalCode())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .createdAt(customer.getCreatedAt())
                .enabled(customer.isEnabled())
                .build();
    }
}
