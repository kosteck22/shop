package pl.zielona_baza.site.review.vote;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VoteResult {
    private boolean successful;
    private String message;
    private int voteCount;

    public static VoteResult fail(String message) {
        return new VoteResult(false, message, 0);
    }

    public static VoteResult success(String message, int voteCount) {
        return new VoteResult(true, message, voteCount);
    }

    private VoteResult(boolean successful, String message, int voteCount) {
        this.successful = successful;
        this.message = message;
        this.voteCount = voteCount;
    }
}
