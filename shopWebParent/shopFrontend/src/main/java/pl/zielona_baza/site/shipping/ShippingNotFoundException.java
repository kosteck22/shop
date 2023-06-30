package pl.zielona_baza.site.shipping;

public class ShippingNotFoundException extends RuntimeException {
    public ShippingNotFoundException(String msg) {
        super(msg);
    }
}
