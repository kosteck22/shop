package pl.zielona_baza.site.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Transient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.order.OrderStatus;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ReviewNotFoundException;
import pl.zielona_baza.site.order.OrderDetailRepository;
import pl.zielona_baza.site.product.ProductRepository;

import java.util.Date;

@Service
@Transactional
public class ReviewService {

    public static final int REVIEWS_PER_PAGE = 10;

    @Autowired private ReviewRepository reviewRepository;

    @Autowired private OrderDetailRepository orderDetailRepository;

    @Autowired private ProductRepository productRepository;

    public Page<Review> listByCustomerByPage(Customer customer, int pageNum, String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNum - 1, REVIEWS_PER_PAGE, sort);

        if (keyword == null) {
            return reviewRepository.findByCustomer(customer.getId(), pageable);
        } else {
            return reviewRepository.findByCustomer(customer.getId(), keyword, pageable);
        }
    }

    public Review getByCustomerAndId(Customer customer, Integer reviewId) {
        return reviewRepository.findByCustomerAndId(customer.getId(), reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Customer does not have any review with ID " + reviewId));
    }

    public Page<Review> list3MostVotedReviewsByProduct(Product product) {
        Sort sort = Sort.by("votes").descending();
        Pageable pageable = PageRequest.of(0, 3, sort);

        return reviewRepository.findByProduct(product, pageable);
    }

    public Page<Review> listByProduct(Product product, int pageNum, String sortField, String sortDir) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();

        Pageable pageable = PageRequest.of(pageNum - 1, REVIEWS_PER_PAGE, sort);

        return reviewRepository.findByProduct(product, pageable);
    }

    public boolean didCustomerReviewProduct(Customer customer, Integer productId) {
        Long count = reviewRepository.countByCustomerAndProduct(customer.getId(), productId);

        return count > 0;
    }

    public boolean canCustomerReviewProduct(Customer customer, Integer productId) {
        Long count = orderDetailRepository.countByProductAndCustomerAndOrderStatus(productId, customer.getId(), OrderStatus.DELIVERED);

        return count > 0;
    }

    public Review save(Review review) {
        review.setReviewTime(new Date());
        Review savedReview = reviewRepository.save(review);

        Integer productId = savedReview.getProduct().getId();
        productRepository.updateReviewCountAndAverageRating(productId);

        return savedReview;
    }
}
