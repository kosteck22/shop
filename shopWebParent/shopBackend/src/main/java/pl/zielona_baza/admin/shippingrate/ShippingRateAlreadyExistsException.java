package pl.zielona_baza.admin.shippingrate;

public class ShippingRateAlreadyExistsException extends RuntimeException {
    public ShippingRateAlreadyExistsException(String msg) {
        super(msg);
    }
}
