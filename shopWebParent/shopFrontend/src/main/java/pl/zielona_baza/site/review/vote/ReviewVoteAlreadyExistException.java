package pl.zielona_baza.site.review.vote;

public class ReviewVoteAlreadyExistException extends RuntimeException {
    public ReviewVoteAlreadyExistException(String message) {
        super(message);
    }
}
