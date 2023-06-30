package pl.zielona_baza.site.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ProductNotFoundException;
import pl.zielona_baza.common.exception.ReviewNotFoundException;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;
import pl.zielona_baza.site.order.OrderService;
import pl.zielona_baza.site.product.ProductService;
import pl.zielona_baza.site.review.vote.ReviewVoteService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
public class ReviewController {

    private final String defaultRedirectURL = "redirect:/reviews/page/1?sortField=reviewTime&sortDir=desc";

    @Autowired private ReviewService reviewService;

    @Autowired private ControllerHelper controllerHelper;

    @Autowired private ReviewVoteService reviewVoteService;

    @Autowired private ProductService productService;

    @GetMapping("/reviews")
    public String listFirstPage(Model model) {
        return defaultRedirectURL;
    }

    @GetMapping("/reviews/page/{pageNum}")
    public String listReviewsByCustomerByPage(Model model,
                                              HttpServletRequest request,
                                              @PathVariable(name = "pageNum") int pageNum,
                                              @RequestParam(name = "sortField") String sortField,
                                              @RequestParam(name = "sortDir") String sortDir,
                                              @RequestParam(name = "reviewKeyword", required = false) String reviewKeyword) {
        Customer loggedCustomer = controllerHelper.getAuthenticatedCustomer(request).get();

        Page<Review> page = reviewService.listByCustomerByPage(loggedCustomer, pageNum, sortField, sortDir, reviewKeyword);
        List<Review> listReviews = page.getContent();

        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("listReviews", listReviews);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", reviewKeyword);
        model.addAttribute("moduleURL", "/reviews");
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        long startCount = (long) (pageNum - 1) * OrderService.ORDERS_PER_PAGE + 1;
        model.addAttribute("startCount", startCount);

        long endCount = startCount + OrderService.ORDERS_PER_PAGE - 1;
        if (endCount > page.getTotalElements()) {
            endCount = page.getTotalElements();
        }

        model.addAttribute("endCount", endCount);

        return "reviews/reviews_customer";
    }

    @GetMapping("/reviews/detail/{id}")
    public String viewReview(@PathVariable(name = "id") Integer id,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request) {
        try {
            Customer customer = controllerHelper.getAuthenticatedCustomer(request)
                    .orElseThrow(() -> new CustomerNotFoundException("You must log in"));
            Review review = reviewService.getByCustomerAndId(customer, id);
            model.addAttribute("review", review);

            return "reviews/review_detail_modal";
        } catch (CustomerNotFoundException|ReviewNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return defaultRedirectURL;
        }
    }

    @GetMapping("/ratings/{product_alias}/page/{pageNum}")
    public String listByProductByPage(Model model,
                                      @PathVariable(name = "product_alias") String productAlias,
                                      @PathVariable(name = "pageNum") int pageNum,
                                      @RequestParam(name = "sortField") String sortField,
                                      @RequestParam(name = "sortDir") String sortDir,
                                      HttpServletRequest request) {
        try {
            Product product = productService.getProductByAlias(productAlias);
            Page<Review> page = reviewService.listByProduct(product, pageNum, sortField, sortDir);
            List<Review> listReviews = page.getContent();
            Optional<Customer> customer = controllerHelper.getAuthenticatedCustomer(request);

            customer.ifPresent(c -> reviewVoteService.markReviewsVotedForProductByCustomer(listReviews, product.getId(), c.getId()));

            model.addAttribute("totalPages", page.getTotalPages());
            model.addAttribute("totalItems", page.getTotalElements());
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
            model.addAttribute("listReviews", listReviews);
            model.addAttribute("product", product);

            long startCount = (pageNum - 1) * ReviewService.REVIEWS_PER_PAGE + 1;
            model.addAttribute("startCount", startCount);

            long endCount = startCount + ReviewService.REVIEWS_PER_PAGE - 1;
            if (endCount > page.getTotalElements()) {
                endCount = page.getTotalElements();
            }

            model.addAttribute("endCount", endCount);
            model.addAttribute("pageTitle", "Reviews for " + product.getShortName());

            return "reviews/reviews_product";
        } catch (ProductNotFoundException ex) {
            return "error/404";
        }
    }

    @GetMapping("/ratings/{product_alias}")
    public String listByProductFirstPage(@PathVariable(name = "product_alias") String productAlias, Model model, HttpServletRequest request) {
        return listByProductByPage(model, productAlias, 1, "reviewTime", "desc", request);
    }

    @GetMapping("/write_review/product/{productId}")
    public String showViewForm(@PathVariable(name = "productId") Integer productId,
                               Model model,
                               HttpServletRequest request) {

        try {
            Product product = productService.getProductById(productId);
            Review review = new Review();

            Customer customer = controllerHelper.getAuthenticatedCustomer(request)
                    .orElseThrow(() -> new CustomerNotFoundException("You must log in"));
            boolean customerReviewedProduct = reviewService.didCustomerReviewProduct(customer, product.getId());

            if (customerReviewedProduct) {
                model.addAttribute("customerReviewed", true);
            } else {
                boolean customerCanReview = reviewService.canCustomerReviewProduct(customer, product.getId());

                if (customerCanReview) {
                    model.addAttribute("customerCanReview", true);
                } else {
                    model.addAttribute("NoReviewPermission", true);
                }
            }

            model.addAttribute("product", product);
            model.addAttribute("review", review);

            return "reviews/review_form";
        } catch (CustomerNotFoundException|ProductNotFoundException ex) {
            return "error/404";
        }
    }

    @PostMapping("/post_review")
    public String saveReview(Model model,
                             Review review,
                             Integer productId,
                             HttpServletRequest request) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request).get();

        try {
            Product product = productService.getProductById(productId);
            review.setProduct(product);
            review.setCustomer(customer);

            Review savedReview = reviewService.save(review);
            model.addAttribute("review", savedReview);

            return "reviews/review_done";
        } catch (ProductNotFoundException ex) {
            return "error/404";
        }
    }
}
