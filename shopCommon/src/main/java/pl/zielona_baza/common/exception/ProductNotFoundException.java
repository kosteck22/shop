package pl.zielona_baza.common.exception;

public class ProductNotFoundException extends Exception{
    public ProductNotFoundException(String msg) {
        super(msg);
    }
}
