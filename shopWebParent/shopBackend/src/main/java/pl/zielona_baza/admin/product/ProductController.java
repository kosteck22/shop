package pl.zielona_baza.admin.product;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.brand.BrandService;
import pl.zielona_baza.admin.category.CategoryService;
import pl.zielona_baza.admin.exception.ValidationException;
import pl.zielona_baza.admin.security.ShopUserDetails;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ProductNotFoundException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final BrandService brandService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, BrandService brandService, CategoryService categoryService) {
        this.productService = productService;
        this.brandService = brandService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listAll(Model model) {
        return listByPage(1, "id", "asc", 20, null, null, model);
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(value = "sortField", required = false) String sortField,
                             @RequestParam(value = "sortDir", required = false) String sortDir,
                             @RequestParam(value = "limit", required = false) Integer limit,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(name = "categoryId", required = false) Integer categoryId,
                             Model model){
        productService.listByPage(pageNum, sortField, sortDir, limit, keyword, model, categoryId);
        List<Category> categories = categoryService.listCategoriesUsedInForm();

        model.addAttribute("listCategories", categories);

        return "products/products";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        List<Brand> listBrands = brandService.listAll();

        model.addAttribute("product", new Product());
        model.addAttribute("listBrands", listBrands);
        model.addAttribute("pageTitle", "Create new Product");
        model.addAttribute("numberOfExistingExtraImages", 0);

        return "products/product_form";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal ShopUserDetails loggedUser) {
        try {
            Product product = productService.get(id);
            List<Brand> listBrands = brandService.listAll();
            Integer numberOfExistingExtraImages = product.getImages().size();

            boolean isReadOnlyForSalesperson = isLoggedUserSalesperson(loggedUser);

            model.addAttribute("isReadOnlyForSalesperson", isReadOnlyForSalesperson);
            model.addAttribute("listBrands", listBrands);
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);


            return "products/product_form";
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return "redirect:/products";
        }
    }

    @PostMapping("/save")
    public String saveProduct(@Valid Product product,
                              BindingResult result,
                              Model model,
                              @RequestParam("fileImage") MultipartFile mainImageMultipart,
                              @RequestParam(name = "extraImage", required = false) MultipartFile[] extraImageMultipart,
                              @RequestParam(name = "imageIds", required = false) String[] imageIds,
                              @RequestParam(name = "imageNames", required = false) String[] imageNames,
                              @RequestParam(name = "detailIds", required = false) String[] detailIds,
                              @RequestParam(name = "detailNames", required = false) String[] detailNames,
                              @RequestParam(name = "detailValues", required = false) String[] detailValues,
                              @AuthenticationPrincipal ShopUserDetails loggedUser,
                              RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            try {
                productService.restoreProductImagesAndDetails(product, detailIds, detailNames, detailValues);
            } catch (ProductNotFoundException ex) {
                redirectAttributes.addFlashAttribute("message", ex.getMessage());
                return "redirect:/products";
            }

            List<Brand> listBrands = brandService.listAll();
            Integer numberOfExistingExtraImages = product.getImages().size();

            boolean isReadOnlyForSalesperson = isLoggedUserSalesperson(loggedUser);

            model.addAttribute("isReadOnlyForSalesperson", isReadOnlyForSalesperson);
            model.addAttribute("listBrands", listBrands);
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);

            return "products/product_form";
        }

        if (isLoggedUserSalesperson(loggedUser)) {
            try {
                productService.saveProductPrice(product);
                redirectAttributes.addFlashAttribute("message", "The product has been saved successfully.");
            } catch (ProductNotFoundException ex) {
                redirectAttributes.addFlashAttribute("message", ex.getMessage());
            }
            return "redirect:/products";
        }

        try {
            productService.save(product, mainImageMultipart, extraImageMultipart,
                    detailIds, detailNames, detailValues, imageIds, imageNames);
            redirectAttributes.addFlashAttribute("message", "The product has been saved successfully.");

            return "redirect:/products";
        } catch (ValidationException | ProductNotFoundException ex) {
            List<Brand> listBrands = brandService.listAll();
            Integer numberOfExistingExtraImages = product.getImages().size();

            boolean isReadOnlyForSalesperson = isLoggedUserSalesperson(loggedUser);

            model.addAttribute("message",ex.getMessage());
            model.addAttribute("isReadOnlyForSalesperson", isReadOnlyForSalesperson);
            model.addAttribute("listBrands", listBrands);
            model.addAttribute("product", product);
            model.addAttribute("pageTitle", "Edit Product");
            model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);

            return "products/product_form";
        }
    }

    private boolean isLoggedUserSalesperson(ShopUserDetails loggedUser) {
        if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Editor")) {
            return loggedUser.hasRole("Salesperson");
        }
        return false;
    }

    @GetMapping("/{id}/enabled/{enabled}")
    public String updateProductEnabledStatus(@PathVariable(name = "id") Integer id,
                                             @PathVariable(name = "enabled") boolean enabled,
                                             RedirectAttributes redirectAttributes) {
        try {
            productService.updateProductEnabledStatus(id, enabled);
            String status = enabled ? "enabled" : "disabled";
            String message = "Product with ID %d has been %s".formatted(id, status);

            redirectAttributes.addFlashAttribute("message", message);
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);

            redirectAttributes.addFlashAttribute("message", "Product with ID %d has been deleted successfully".formatted(id));
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/detail/{id}")
    public String viewProductDetails(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.get(id);

            model.addAttribute("product", product);

            return "products/product_detail_modal";
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return "redirect:/products";
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
