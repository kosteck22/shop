package pl.zielona_baza.admin.shippingrate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zielona_baza.common.exception.ProductNotFoundException;

@RestController
public class ShippingRateRestController {

    @Autowired private ShippingRateService shippingRateService;

    @PostMapping("/get_shipping_cost")
    public String getShippingCost(Integer productId, Integer countryId, String state) throws ProductNotFoundException {
        float shippingCost = shippingRateService.calculateShippingCost(productId, countryId, state);
        return String.valueOf(shippingCost);
    }
}
