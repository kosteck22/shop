package pl.zielona_baza.site.review.vote;

public class ReviewVoteNotFoundException extends RuntimeException {
    public ReviewVoteNotFoundException(String message) {
        super(message);
    }
}
