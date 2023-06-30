package pl.zielona_baza.common.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String s) {
        super(s);
    }
}
