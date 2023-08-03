package pl.zielona_baza.admin.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.paging.PagingAndSortingParam;
import pl.zielona_baza.admin.product.ProductService;

@Controller
@RequestMapping("/orders/search_product")
public class ProductSearchController {
    private final ProductService productService;

    public ProductSearchController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String showSearchProductPage() {
        return "orders/search_product";
    }

    @PostMapping
    public String searchProducts(String keyword) {
        return "redirect:/orders/search_product/page/1?sortField=name&sortDir=asc&keyword=" + keyword;
    }

    @GetMapping("/page/{pageNum}")
    public String searchProductByPage(@PathVariable(name = "pageNum") int pageNum,
                                      @RequestParam(value = "sortField", required = false) String sortField,
                                      @RequestParam(value = "sortDir", required = false) String sortDir,
                                      @RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "keyword", required = false) String keyword,
                                      Model model) {
        productService.searchProducts(pageNum, sortField, sortDir, limit, keyword, model);

        return "orders/search_product";
    }
}
