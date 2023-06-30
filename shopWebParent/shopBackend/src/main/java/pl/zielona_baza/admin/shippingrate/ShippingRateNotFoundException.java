package pl.zielona_baza.admin.shippingrate;

public class ShippingRateNotFoundException extends RuntimeException {
    public ShippingRateNotFoundException(String msg) {
        super(msg);
    }
}
