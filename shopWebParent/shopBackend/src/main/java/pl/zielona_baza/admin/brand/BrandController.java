package pl.zielona_baza.admin.brand;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.category.CategoryService;
import pl.zielona_baza.admin.exception.CustomValidationException;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/brands")
public class BrandController {
    private final BrandService brandService;
    private final CategoryService categoryService;

    public BrandController(BrandService brandService, CategoryService categoryService) {
        this.brandService = brandService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listByPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "sortField", defaultValue = "id") String sortField,
                             @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                             @RequestParam(value = "limit", defaultValue = "20") Integer limit,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model) {
        brandService.listByPage(page, sortField, sortDir, limit, keyword, model);

        return "brands/brands";
    }

    @GetMapping("/new")
    public String newBrand(Model model) {
        List<Category> listCategories = categoryService.listCategoriesUsedInForm();

        model.addAttribute("brand", new Brand());
        model.addAttribute("listCategories", listCategories);
        model.addAttribute("pageTitle", "Create new brand");

        return "brands/brand_form";
    }

    @PostMapping("/save")
    public String saveBrand(Brand brand,
                            @RequestParam(name = "fileImage", required = false) MultipartFile multipartFile,
                            RedirectAttributes redirectAttributes,
                            Model model) throws IOException {
        try {
            brandService.save(brand, multipartFile);
        } catch (CustomValidationException ex) {
            List<Category> listCategories = categoryService.listCategoriesUsedInForm();

            model.addAttribute("brand", brand);
            model.addAttribute("listCategories", listCategories);
            model.addAttribute("pageTitle", "Brand Form");
            model.addAttribute("message", ex.getMessage());

            return "brands/brand_form";
        }
        redirectAttributes.addFlashAttribute("message", "The brand has been saved successfully.");
        return "redirect:/brands";
    }

    @GetMapping("/edit/{id}")
    public String editBrand(@PathVariable(name = "id") Integer id,
                            Model model) throws BrandNotFoundException {
        Brand brand = brandService.getById(id);
        List<Category> listCategories = categoryService.listCategoriesUsedInForm();

        model.addAttribute("brand", brand);
        model.addAttribute("listCategories", listCategories);
        model.addAttribute("pageTitle", "Create new brand");

        return "brands/brand_form";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable(name = "id") Integer id,
                              Model model,
                              RedirectAttributes redirectAttributes) throws BrandNotFoundException {
        brandService.delete(id);
        redirectAttributes.addFlashAttribute("message", "The brand with ID " + id +
                " has been deleted successfully");

        return "redirect:/brands";
    }

    @ExceptionHandler(BrandNotFoundException.class)
    public String handleBrandNotFoundException(BrandNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        return "redirect:/brands";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        StringTrimmerEditor editor = new StringTrimmerEditor(true);
        webDataBinder.registerCustomEditor(String.class, editor);
    }
}
