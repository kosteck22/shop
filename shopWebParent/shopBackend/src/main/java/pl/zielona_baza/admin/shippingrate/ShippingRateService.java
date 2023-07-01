package pl.zielona_baza.admin.shippingrate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.product.ProductRepository;
import pl.zielona_baza.admin.setting.country.CountryRepository;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.ShippingRate;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ProductNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShippingRateService {
    public static final int RATES_PER_PAGE = 10;
    public static final int DIM_DIVISOR = 139;

    @Autowired
    private ShippingRateRepository shippingRateRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ProductRepository productRepository;

    public void listByPage(int pageNum, PagingAndSortingHelper helper) {
        //helper.listEntities(pageNum, RATES_PER_PAGE, shippingRateRepository);
    }

    public List<Country> listAllCountries() {
        return countryRepository.findAllByOrderByNameAsc();
    }

    public void save(ShippingRate rateInForm) throws ShippingRateAlreadyExistsException {
        Optional<ShippingRate> rateInDB = shippingRateRepository.findByCountryAndState(rateInForm.getCountry().getId(), rateInForm.getState());

        if ((rateInDB.isPresent() && rateInForm.getId() == null) || (rateInDB.isPresent() && rateInForm.getId() != null)) {
            throw new ShippingRateAlreadyExistsException("There's already a rate for the destination " +
                    rateInForm.getCountry().getName() + ", " + rateInForm.getState());
        }
        shippingRateRepository.save(rateInForm);
    }

    public ShippingRate get(Integer id) {
        return shippingRateRepository.findById(id).orElseThrow(() -> new ShippingRateNotFoundException("Shipping rate not found " +
                "with ID " + id));
    }

    public void updateCODSupport(Integer id, boolean codSupported) {
        ShippingRate shippingRate = shippingRateRepository.findById(id)
                .orElseThrow(() -> new ShippingRateNotFoundException("Shipping rate not found with ID " + id));

        shippingRateRepository.updateCODSupport(id, codSupported);
    }

    public void delete(Integer id) {
        ShippingRate shippingRate = shippingRateRepository.findById(id)
                .orElseThrow(() -> new ShippingRateNotFoundException("Shipping rate not found with ID " + id));

        shippingRateRepository.delete(shippingRate);
    }

    public float calculateShippingCost(Integer productId, Integer countryId, String state) throws ProductNotFoundException {
        ShippingRate shippingRate = shippingRateRepository.findByCountryAndState(countryId, state)
                .orElseThrow(() -> new ShippingRateNotFoundException("No shipping rate found for the given destination"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        float dimWeight = (product.getLength() * product.getWidth() * product.getHeight()) / DIM_DIVISOR;
        float finalWeight = Math.max(product.getWeight(), dimWeight);

        return finalWeight * shippingRate.getRate();
    }
}
