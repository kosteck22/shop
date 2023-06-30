package pl.zielona_baza.site.review.vote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.customer.CustomerNotFoundException;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ReviewVoteRestController {

    @Autowired private ReviewVoteService reviewVoteService;

    @Autowired private ControllerHelper controllerHelper;

    @PostMapping("/vote_review/{id}/{type}")
    public VoteResult voteReview(@PathVariable(name = "id") Integer reviewId,
                                 @PathVariable(name = "type") String type,
                                 HttpServletRequest request) {
        try {
            Customer customer = controllerHelper.getAuthenticatedCustomer(request)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found."));
            VoteType voteType = VoteType.valueOf(type.toUpperCase());

            return reviewVoteService.doVote(reviewId, customer, voteType);
        } catch (CustomerNotFoundException ex) {
            return VoteResult.fail("You must login to vote the review.");
        }
    }
}
