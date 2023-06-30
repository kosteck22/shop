package pl.zielona_baza.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "reviews_votes")
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVote {
    public static final int VOTE_UP_POINT = 1;
    public static final int VOTE_DOWN_POINT = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private int votes;

    public void voteUp() {
        this.votes = VOTE_UP_POINT;
    }

    public void voteDown() {
        this.votes = VOTE_DOWN_POINT;
    }

    @Override
    public String toString() {
        return "ReviewVote{" +
                " review=" + review.getId() +
                ", customer=" + customer.getFullName() +
                ", votes=" + votes +
                '}';
    }

    @Transient
    public boolean isUpVoted() {
        return this.votes == VOTE_UP_POINT;
    }

    @Transient
    public boolean isDownVoted() {
        return this.votes == VOTE_DOWN_POINT;
    }
}
