package pl.zielona_baza.site.checkout.paypal;

public class PayPalApiException extends Exception {
    public PayPalApiException(String msg) {
        super(msg);
    }
}
