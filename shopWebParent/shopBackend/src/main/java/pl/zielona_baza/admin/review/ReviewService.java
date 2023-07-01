package pl.zielona_baza.admin.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.product.ProductRepository;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.exception.ReviewNotFoundException;

@Service
@Transactional
public class ReviewService {

    public static final int REVIEWS_PER_PAGE = 10;

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ProductRepository productRepository;

    public void listByPage(int pageNum, PagingAndSortingHelper helper) {
        //helper.listEntities(pageNum, REVIEWS_PER_PAGE, reviewRepository);
    }

    public Review get(Integer id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Could not find any reviews with ID [%s]".formatted(id)));
    }

    public void save(Review reviewInForm) {
        Review reviewInDB = reviewRepository.findById(reviewInForm.getId())
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Could not find any review with ID [%s]".formatted(reviewInForm.getId()
                        )
                ));
        reviewInDB.setHeadline(reviewInForm.getHeadline());
        reviewInDB.setComment(reviewInForm.getComment());

        reviewRepository.save(reviewInDB);
        productRepository.updateReviewCountAndAverageRating(reviewInDB.getProduct().getId());
    }

    public void delete(Integer id) {
        boolean exists = reviewRepository.existsById(id);
        if (!exists) throw new ReviewNotFoundException("Could not find aby reviews with ID " + id);

        reviewRepository.deleteById(id);
    }
}
