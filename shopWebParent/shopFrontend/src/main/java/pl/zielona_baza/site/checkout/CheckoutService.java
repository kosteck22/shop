package pl.zielona_baza.site.checkout;

import org.apache.commons.math3.util.ArithmeticUtils;
import org.springframework.stereotype.Service;
import pl.zielona_baza.common.entity.CartItem;
import pl.zielona_baza.common.entity.ShippingRate;
import pl.zielona_baza.common.entity.product.Product;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutService {
    private static final int DIM_DIVISOR = 139;

    public CheckoutInfo prepareCheckout(List<CartItem> cartItems, ShippingRate shippingRate) {
        CheckoutInfo checkoutInfo = new CheckoutInfo();

        float productCost = calculateProductCost(cartItems);
        float productTotal = calculateProductTotal(cartItems);
        float shippingCostTotal = calculateShippingCost(cartItems, shippingRate);
        float paymentTotal = productTotal + shippingCostTotal;

        checkoutInfo.setProductCost(productCost);
        checkoutInfo.setProductTotal(productTotal);
        checkoutInfo.setPaymentTotal(paymentTotal);
        checkoutInfo.setShippingCostTotal(shippingCostTotal);
        checkoutInfo.setDeliverDays(shippingRate.getDays());
        checkoutInfo.setCodSupported(shippingRate.isCodSupported());

        return checkoutInfo;
    }

    private float calculateShippingCost(List<CartItem> cartItems, ShippingRate shippingRate) {
        return cartItems.stream().map(c -> {
                    Product product = c.getProduct();
                    float dimWeight = (product.getLength() * product.getWidth() * product.getHeight()) / DIM_DIVISOR;
                    float finalWeight = Math.max(product.getWeight(), dimWeight);
                    float shippingCost = finalWeight * c.getQuantity() * shippingRate.getRate();
                    c.setShippingCost(shippingCost);

                    return shippingCost;
        })
                .reduce(0.0F, Float::sum);
    }

    private float calculateProductTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(0.0F, Float::sum);
    }

    private float calculateProductCost(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(c -> c.getQuantity() * c.getProduct().getCost())
                .reduce(0.0F, Float::sum);
    }
}
