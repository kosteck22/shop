package pl.zielona_baza.site.shoppingcart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.exception.ProductNotFoundException;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ShoppingCartRestController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private ControllerHelper controllerHelper;

    @PostMapping("/cart/add/{productId}/{quantity}")
    public String addProductToCart(@PathVariable(name = "productId") Integer productId,
                                   @PathVariable(name = "quantity") Integer quantity,
                                   HttpServletRequest request) {
        try {
            Customer customer = controllerHelper.getAuthenticatedCustomer(request)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
            Integer updatedQuantity = shoppingCartService.addProduct(productId, quantity, customer);
        } catch (CustomerNotFoundException ex) {
            return "You must login to add this product to cart.";
        } catch (ProductNotFoundException ex) {
            return "Product with id " + productId + " not found";
        }

        return "";
    }

    @PostMapping("/cart/update/{productId}/{quantity}")
    public String updateQuantity(@PathVariable(name = "productId") Integer productId,
                                 @PathVariable(name = "quantity") Integer quantity,
                                 HttpServletRequest request) {
        try {
            Customer customer = controllerHelper.getAuthenticatedCustomer(request)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
            float subtotal = shoppingCartService.updateQuantity(productId, quantity, customer);

            return String.valueOf(subtotal);
        } catch (CustomerNotFoundException ex) {
            return "You must login to change quantity of products.";
        }
    }

    @DeleteMapping("/cart/remove/{productId}")
    public String removeProduct(@PathVariable(name = "productId") Integer productId,
                                HttpServletRequest request) {
        try {
            Customer customer = controllerHelper.getAuthenticatedCustomer(request)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
            shoppingCartService.removeProduct(productId, customer);

            return "The product has been removed";
        } catch (CustomerNotFoundException ex) {
            return "You must login to remove product";
        }
    }
}
