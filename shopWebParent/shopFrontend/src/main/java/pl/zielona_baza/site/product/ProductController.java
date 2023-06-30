package pl.zielona_baza.site.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.CategoryNotFoundException;
import pl.zielona_baza.common.exception.ProductNotFoundException;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.category.CategoryService;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;
import pl.zielona_baza.site.review.ReviewService;
import pl.zielona_baza.site.review.vote.ReviewVoteService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired private CategoryService categoryService;
    @Autowired private ProductService productService;

    @Autowired private ReviewService reviewService;

    @Autowired private ReviewVoteService reviewVoteService;

    @Autowired private ControllerHelper controllerHelper;

    @GetMapping("/c/{category_alias}")
    public String viewCategoryFirstPage(@PathVariable(name = "category_alias") String alias,
                                     Model model) {
        return viewCategoryByPage(alias, 1, model);
    }

    @GetMapping("/c/{category_alias}/page/{pageNum}")
    public String viewCategoryByPage(@PathVariable(name = "category_alias") String alias,
                               @PathVariable(name = "pageNum") int pageNum,
                               Model model) {
        try{
            Category category = categoryService.getCategory(alias);
            List<Category> categoryParents = categoryService.getCategoryParents(category);

            Page<Product> pageProducts = productService.listByCategory(pageNum, category.getId());
            List<Product> listProducts = pageProducts.getContent();

            int startCount = (pageNum - 1) * ProductService.PRODUCTS_PER_PAGE + 1;
            long endCount = startCount + ProductService.PRODUCTS_PER_PAGE - 1;
            if (endCount > pageProducts.getTotalElements()) {
                endCount = pageProducts.getTotalElements();
            }


            model.addAttribute("currentPage", pageNum);
            model.addAttribute("totalPages", pageProducts.getTotalPages());
            model.addAttribute("startCount", startCount);
            model.addAttribute("endCount", endCount);
            model.addAttribute("totalItems", pageProducts.getTotalElements());
            model.addAttribute("pageTitle", category.getName());
            model.addAttribute("listCategoryParents", categoryParents);
            model.addAttribute("listProducts", listProducts);
            model.addAttribute("category", category);

            return "product/products_by_category";
        } catch (CategoryNotFoundException ex) {
            return "redirect:/";
        }
    }

    @GetMapping("/c/{category_alias}/{product_alias}")
    public String viewProductDetail(@PathVariable(name = "category_alias") String categoryAlias,
                                    @PathVariable(name = "product_alias") String productAlias,
                                    Model model,
                                    HttpServletRequest request) {
        try {
            Product product = productService.getProductByAlias(productAlias);

            if (!product.getCategory().getAlias().equals(categoryAlias)) {
                return "error/404";
            }

            List<Category> listCategoryParents = categoryService.getCategoryParents(product.getCategory());
            Page<Review> listReviews = reviewService.list3MostVotedReviewsByProduct(product);
            Optional<Customer> customer = controllerHelper.getAuthenticatedCustomer(request);

            if (customer.isPresent()) {
                reviewVoteService.markReviewsVotedForProductByCustomer(listReviews.getContent(), product.getId(), customer.get().getId());
                boolean didCustomerReviewProduct = reviewService.didCustomerReviewProduct(customer.get(), product.getId());

                if (didCustomerReviewProduct) {
                    model.addAttribute("customerReviewed", true);
                } else {
                    boolean canCustomerReviewProduct = reviewService.canCustomerReviewProduct(customer.get(), product.getId());
                    model.addAttribute("customerCanReview", canCustomerReviewProduct);
                }
            }

            model.addAttribute("listCategoryParents", listCategoryParents);
            model.addAttribute("product", product);
            model.addAttribute("listReviews", listReviews.getContent());
            model.addAttribute("pageTitle", product.getShortName());

            return "product/product_detail";
        } catch (ProductNotFoundException e) {
            return "error/404";
        }
    }

    @GetMapping("/search")
    public String searchFirstPage(@RequestParam("keyword") String keyword, Model model) {
        return search(keyword, 1, model);
    }

    @GetMapping("/search/page/{pageNum}")
    public String search(@RequestParam("keyword") String keyword,
                         @PathVariable("pageNum") int pageNum,
                         Model model) {
        Page<Product> pageProducts = productService.search(keyword, pageNum);
        List<Product> listResult = pageProducts.getContent();

        long startCount = (pageNum - 1) * productService.SEARCH_RESULTS_PER_PAGE + 1;
        long endCount = startCount + ProductService.SEARCH_RESULTS_PER_PAGE - 1;
        if (endCount > pageProducts.getTotalElements()) {
            endCount = pageProducts.getTotalElements();
        }

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", pageProducts.getTotalPages());
        model.addAttribute("startCount", startCount);
        model.addAttribute("endCount", endCount);
        model.addAttribute("totalItems", pageProducts.getTotalElements());
        model.addAttribute("pageTitle", "Search Result - " + keyword);
        model.addAttribute("keyword", keyword);
        model.addAttribute("listResult", listResult);

        return "product/search_result";
    }
}
