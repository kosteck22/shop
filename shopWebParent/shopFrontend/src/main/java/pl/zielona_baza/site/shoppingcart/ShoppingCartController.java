package pl.zielona_baza.site.shoppingcart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.zielona_baza.common.entity.Address;
import pl.zielona_baza.common.entity.CartItem;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.ShippingRate;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.address.AddressService;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;
import pl.zielona_baza.site.shipping.ShippingNotFoundException;
import pl.zielona_baza.site.shipping.ShippingRateService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;

@Controller
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ShippingRateService shippingRateService;

    @Autowired private ControllerHelper controllerHelper;

    @GetMapping("/cart")
    public String viewCart(Model model, HttpServletRequest request) {
        Customer authenticatedCustomer = controllerHelper.getAuthenticatedCustomer(request).get();

        List<CartItem> cartItems = shoppingCartService.listCartItems(authenticatedCustomer);

        double estimatedTotal = 0.0F;

        estimatedTotal = cartItems.stream()
                .flatMapToDouble(item -> DoubleStream.of(item.getSubtotal()))
                .sum();

        Address defaultAddress = addressService.getDefaultAddress(authenticatedCustomer);
        Optional<ShippingRate> shippingRate = null;
        boolean usePrimaryAddressAsDefault = false;

        if (defaultAddress != null) {
            shippingRate = shippingRateService.getShippingRateForAddress(defaultAddress);
        } else {
            usePrimaryAddressAsDefault = true;
            shippingRate = shippingRateService.getShippingRateForCustomer(authenticatedCustomer);
        }

        model.addAttribute("shippingSupported", shippingRate.isPresent());
        model.addAttribute("usePrimaryAddressAsDefault", usePrimaryAddressAsDefault);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("estimatedTotal", estimatedTotal);

        return "cart/shopping_cart";
    }
}
