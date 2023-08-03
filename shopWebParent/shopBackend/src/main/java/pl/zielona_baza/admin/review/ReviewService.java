package pl.zielona_baza.admin.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.product.ProductRepository;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.exception.ReviewNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;
import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.validateSortDir;

@Service
@Transactional
public class ReviewService {
    public static final int REVIEWS_PER_PAGE = 10;
    private static final List<String> SORTABLE_FIELDS_AVAILABLE = new ArrayList<>(List.of("id", "product", "customer", "rating", "reviewTime"));
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, REVIEWS_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "name");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper( "listReviews", sortField, sortDir, keyword, limit);

        helper.listEntities(pageNumber, reviewRepository, model);
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
