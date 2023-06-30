package pl.zielona_baza.admin.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.FileUploadUtil;
import pl.zielona_baza.admin.brand.BrandService;
import pl.zielona_baza.admin.category.CategoryService;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.paging.PagingAndSortingParam;
import pl.zielona_baza.admin.security.ShopUserDetails;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ProductNotFoundException;

import java.io.IOException;
import java.util.List;

@Controller
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private BrandService brandService;
    @Autowired private CategoryService categoryService;

    @GetMapping("/products")
    public String listAll(Model model) {
        return "redirect:/products/page/1?sortField=name&sortDir=asc&categoryId=0";
    }

    @GetMapping("/products/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @PagingAndSortingParam(listName = "listProducts") PagingAndSortingHelper helper,
                             @RequestParam(name = "categoryId", required = false) Integer categoryId,
                             Model model){

        productService.listByPage(pageNum, helper, categoryId);
        List<Category> categories = categoryService.listCategoriesUsedInForm();

        if (categoryId != null) model.addAttribute("categoryId", categoryId);

        model.addAttribute("listCategories", categories);

        return "products/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        List<Brand> listBrands = brandService.listAll();

        Product product = new Product();
        Integer numberOfExistingExtraImages = 0;

        model.addAttribute("product", product);
        model.addAttribute("listBrands", listBrands);
        model.addAttribute("pageTitle", "Create new Product");
        model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);
        return "products/product_form";
    }

    @PostMapping("products/save")
    public String saveProduct(Product product,
                              @RequestParam("fileImage") MultipartFile mainImageMultipart,
                              @RequestParam(value = "extraImage", required = false) MultipartFile[] extraImageMultiparts,
                              @RequestParam(name = "detailIds", required = false) String[] detailIds,
                              @RequestParam(name = "detailNames", required = false) String[] detailNames,
                              @RequestParam(name = "detailValues", required = false) String[] detailValues,
                              @RequestParam(name = "imageIds", required = false) String[] imageIds,
                              @RequestParam(name = "imageNames", required = false) String[] imageNames,
                              @AuthenticationPrincipal ShopUserDetails loggedUser,
                              RedirectAttributes redirectAttributes) throws IOException {
        if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Editor")) {
            if (loggedUser.hasRole("Salesperson")) {
                productService.saveProductPrice(product);
                redirectAttributes.addFlashAttribute("message", "The product has been saved successfully.");
                return "redirect:/products";
            }
        }

        ProductSaveHelper.setMainImageName(mainImageMultipart, product);
        ProductSaveHelper.setExistingExtraImageNames(imageIds, imageNames, product);
        ProductSaveHelper.setNewExtraImageNames(extraImageMultiparts, product);
        ProductSaveHelper.setProductDetails(detailIds, detailNames, detailValues, product);

        Product savedProduct = productService.save(product);

        ProductSaveHelper.saveUploadedImage(mainImageMultipart,extraImageMultiparts, savedProduct);
        ProductSaveHelper.deleteExtraImagesWereRemovedOnForm(product);

        redirectAttributes.addFlashAttribute("message", "The product has been saved successfully.");

        return "redirect:/products";
    }

    @GetMapping("/products/{id}/enabled/{enabled}")
    public String updateProductEnabledStatus(@PathVariable(name = "id") Integer id,
                                             @PathVariable(name = "enabled") boolean enabled,
                                             RedirectAttributes redirectAttributes) {
        try {
            productService.updateProductEnabledStatus(id, enabled);
            String status = enabled ? "enabled" : "disabled";
            String message = "Product with ID " + id + " has been " + status;

            redirectAttributes.addFlashAttribute("message", message);
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);

            String productExtraImagesDir = "../product-images/" + id + "/extras";
            String productImagesDir = "../product-images/" + id;

            FileUploadUtil.removeDir(productExtraImagesDir);
            FileUploadUtil.removeDir(productImagesDir);

            redirectAttributes.addFlashAttribute("message", "Product with ID " + id +
                    " has been deleted successfully");
        } catch (ProductNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes,
                              @AuthenticationPrincipal ShopUserDetails loggedUser) {
        try {
            Product product = productService.get(id);
            List<Brand> listBrands = brandService.listAll();
            Integer numberOfExistingExtraImages = product.getImages().size();

            boolean isReadOnlyForSalesperson = false;

            if (!loggedUser.hasRole("Admin") && !loggedUser.hasRole("Editor")) {
                if (loggedUser.hasRole("Salesperson")) {
                    isReadOnlyForSalesperson = true;
                }
            }

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

    @GetMapping("/products/detail/{id}")
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
}
