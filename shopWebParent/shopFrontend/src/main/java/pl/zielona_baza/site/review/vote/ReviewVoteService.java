package pl.zielona_baza.site.review.vote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.ReviewVote;
import pl.zielona_baza.site.review.ReviewRepository;

import java.util.Optional;
import java.util.List;

@Service
@Transactional
public class ReviewVoteService {

    @Autowired private ReviewVoteRepository reviewVoteRepository;

    @Autowired private ReviewRepository reviewRepository;

    public VoteResult doVote(Integer reviewId, Customer customer, VoteType voteType) {
        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            return VoteResult.fail("Review with id %d not found".formatted(reviewId));
        }

        Optional<ReviewVote> reviewVoteFromDB = reviewVoteRepository.findByReviewAndCustomer(reviewId, customer.getId());

        ReviewVote reviewVote = null;

        if (reviewVoteFromDB.isPresent()) {
            reviewVote = reviewVoteFromDB.get();

            if ((reviewVote.isUpVoted() && voteType.equals(VoteType.UP)) ||
                (reviewVote.isDownVoted() && voteType.equals(VoteType.DOWN))) {
                return undoVote(reviewVote, reviewId, voteType);
            } else if (reviewVote.isUpVoted() && voteType.equals(VoteType.DOWN)) {
                reviewVote.voteDown();
            } else if (reviewVote.isDownVoted() && voteType.equals(VoteType.UP)) {
                reviewVote.voteUp();
            }
        } else {
            reviewVote = new ReviewVote();
            reviewVote.setReview(review.get());
            reviewVote.setCustomer(customer);

            if (voteType.equals(VoteType.UP)) {
                reviewVote.voteUp();
            } else if (voteType.equals(VoteType.DOWN)) {
                reviewVote.voteDown();
            }
        }

        reviewVoteRepository.save(reviewVote);
        reviewRepository.updateVoteCount(reviewId);
        Integer voteCount = reviewRepository.getVoteCount(reviewId);

        return VoteResult.success("You have successfully voted " + voteType + " that review.", voteCount);
    }

    public VoteResult undoVote(ReviewVote reviewVote, Integer reviewId, VoteType voteType) {
        reviewVoteRepository.delete(reviewVote);
        reviewRepository.updateVoteCount(reviewId);

        Integer voteCount = reviewRepository.getVoteCount(reviewId);

        return VoteResult.success("You have unvoted " + voteType + " that review.", voteCount);
    }

    public void markReviewsVotedForProductByCustomer(List<Review> listReviews, Integer productId, Integer customerId) {
        List<ReviewVote> listVotes = reviewVoteRepository.findByProductAndCustomer(productId, customerId);

        listVotes.forEach(reviewVote -> {
            Review votedReview = reviewVote.getReview();

            if (listReviews.contains(votedReview)) {
                int index = listReviews.indexOf(votedReview);
                Review review = listReviews.get(index);

                if (reviewVote.isUpVoted()) {
                    review.setUpVotedByCurrentCustomer(true);
                } else if (reviewVote.isDownVoted()) {
                    review.setDownVotedByCurrentCustomer(true);
                }
            }
        });
    }
}
