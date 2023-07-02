package pl.zielona_baza.admin.category;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.exception.ValidationException;
import pl.zielona_baza.common.exception.CategoryNotFoundException;
import pl.zielona_baza.admin.category.export.CategoryExcelExporter;
import pl.zielona_baza.admin.category.export.CategoryCsvExport;
import pl.zielona_baza.admin.category.export.CategoryPdfExporter;
import pl.zielona_baza.common.entity.Category;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listFirstPage(Model model) {
        return listByPage(1, "name", "asc", 20, null, model);
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(value = "sortField", required = false) String sortField,
                             @RequestParam(value = "sortDir", required = false) String sortDir,
                             @RequestParam(value = "limit", required = false) Integer limit,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model) {
        categoryService.listByPage(pageNum, sortField, sortDir, limit, keyword, model);

        return "categories/categories";
    }

    @GetMapping("/new")
    public String newCategory(Model model) {
        List<Category> listCategories = categoryService.listCategoriesUsedInForm();

        model.addAttribute("category", new Category());
        model.addAttribute("listCategories", listCategories);
        model.addAttribute("pageTitle", "Create new category");

        return "categories/category_form";
    }

    @PostMapping("/save")
    public String saveCategory(Category category,
                               @RequestParam(name = "fileImage", required = false) MultipartFile multipartFile,
                               RedirectAttributes redirectAttributes,
                               Model model) throws IOException {
        try {
            categoryService.save(category, multipartFile);
            redirectAttributes.addFlashAttribute("message", "The category has been saved successfully.");

            return "redirect:/categories";
        } catch (ValidationException | CategoryNotFoundException ex) {
            List<Category> listCategories = categoryService.listCategoriesUsedInForm();

            model.addAttribute("category", new Category());
            model.addAttribute("listCategories", listCategories);
            model.addAttribute("pageTitle", "Create new category");

            return "categories/category_form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, Model model) {
        try {
            Category category = categoryService.getCategoryById(id);
            List<Category> listCategories = categoryService.listCategoriesUsedInForm();

            model.addAttribute("pageTitle", "Edit category");
            model.addAttribute("category", category);
            model.addAttribute("listCategories", listCategories);

            return "categories/category_form";
        } catch (CategoryNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return "redirect:/categories";
        }
    }

    @GetMapping("/{id}/enabled/{enabled}")
    public String updateCategoryEnabledStatus(@PathVariable("id") Integer id,
                                              @PathVariable("enabled") Boolean enabled,
                                              RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategoryEnabledStatus(id, enabled);

            String status = enabled ? "enabled" : "disabled";
            String message = "Category with ID %d has been %s".formatted(id, status);
            redirectAttributes.addFlashAttribute("message", message);

        } catch (CategoryNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);

            redirectAttributes.addFlashAttribute("message", "Category with ID " + id +
                    " has been deleted successfully");
        } catch (CategoryNotFoundException | CategoryHasChildrenException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/categories";
    }

    @GetMapping("/export/csv")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        List<Category> categories = categoryService.listAll("asc", "  ");
        CategoryCsvExport exporter = new CategoryCsvExport();
        exporter.export(categories, response);
    }

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Category> categories = categoryService.listAll("asc", "  ");
        CategoryExcelExporter exporter = new CategoryExcelExporter();
        exporter.export(categories, response);
    }

    @GetMapping("/export/pdf")
    public void exportToPdf(HttpServletResponse response) throws IOException {
        List<Category> categories = categoryService.listAll("asc", "  ");
        CategoryPdfExporter exporter = new CategoryPdfExporter();
        exporter.export(categories, response);
    }
}
