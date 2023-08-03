package pl.zielona_baza.admin.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.paging.PagingAndSortingParam;
import pl.zielona_baza.common.entity.Review;
import pl.zielona_baza.common.exception.ReviewNotFoundException;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final String defaultRedirectURL = "redirect:/reviews/page/1?sortField=reviewTime&sortDir=desc";
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public String listFirstPage(Model model) {
        return defaultRedirectURL;
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(value = "sortField", required = false) String sortField,
                             @RequestParam(value = "sortDir", required = false) String sortDir,
                             @RequestParam(value = "limit", required = false) Integer limit,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model) {
        reviewService.listByPage(pageNum, sortField, sortDir, limit, keyword, model);

        return "reviews/reviews";
    }

    @GetMapping("/detail/{id}")
    public String viewReview(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewService.get(id);
            model.addAttribute("review", review);

            return "/reviews/review_detail_modal";
        } catch (ReviewNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return defaultRedirectURL;
        }
    }

    @GetMapping("/edit/{id}")
    public String editReview(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewService.get(id);

            model.addAttribute("review", review);
            model.addAttribute("pageTitle", String.format("Edit Review (ID: %d)", id));

            return "reviews/review_form";
        } catch (ReviewNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return defaultRedirectURL;
        }
    }

    @PostMapping("/save")
    public String saveReview(Review reviewInForm, RedirectAttributes redirectAttributes) {
        reviewService.save(reviewInForm);
        redirectAttributes.addFlashAttribute("message", "The review has been updated successfully");
        return defaultRedirectURL;
    }

    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.delete(id);
            redirectAttributes.addFlashAttribute("message", "The review has been deleted.");
        } catch (ReviewNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return defaultRedirectURL;
    }
}
