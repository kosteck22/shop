package pl.zielona_baza.site.shipping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.zielona_baza.common.entity.Address;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.ShippingRate;

import java.util.Optional;

@Service
public class ShippingRateService {

    @Autowired
    private ShippingRateRepository shippingRateRepository;

    public Optional<ShippingRate> getShippingRateForCustomer(Customer customer) {
        String state = customer.getState();
        if (state == null || state.isEmpty()) {
            state = customer.getCity();
        }

        return shippingRateRepository.findByCountryAndState(customer.getCountry(), state);
    }

    public Optional<ShippingRate> getShippingRateForAddress(Address address) {
        String state = address.getState();

        if (state == null || state.isEmpty()) {
            state = address.getCity();
        }

        return shippingRateRepository.findByCountryAndState(address.getCountry(), state);
    }
}
