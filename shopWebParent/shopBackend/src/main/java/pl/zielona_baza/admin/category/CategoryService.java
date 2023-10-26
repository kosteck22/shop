package pl.zielona_baza.admin.category;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.exception.CustomValidationException;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.exception.CategoryNotFoundException;

import java.io.IOException;
import java.util.*;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;
import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.validateSortDir;

@Service
@Transactional
public class CategoryService {

    private static final List<String> SORTABLE_FIELDS_AVAILABLE = new ArrayList<>(List.of("id", "name", "alias", "enabled"));
    private static final int CATEGORIES_PER_PAGE = 20;

    private final CategoryRepository categoryRepository;
    private final CategoryValidator validator;

    public CategoryService(CategoryRepository categoryRepository, CategoryValidator validator) {
        this.categoryRepository = categoryRepository;
        this.validator = validator;
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, CATEGORIES_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "name");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper("listCategories", sortField, sortDir, keyword, limit, pageNumber);

        helper.listEntities(categoryRepository, model);
    }

    public void save(Category category, MultipartFile multipartFile) throws CategoryNotFoundException, CustomValidationException, IOException {
        CategoryValidatorResult result = validator.validate(category, categoryRepository, multipartFile);

        if (result.isNotValid()) {
            throw new CustomValidationException(result.message());
        }

        //set parent
        if (category.getParent() != null) {
            Category parentCategory = categoryRepository.findById(category.getParent().getId()).orElseThrow(() ->
                    new CategoryNotFoundException("Parent category not found."));
            category.setParent(parentCategory);
            String allParentIds = parentCategory.getAllParentIds() == null ? "-" : parentCategory.getAllParentIds();
            allParentIds += parentCategory.getId() + "-";
            category.setAllParentIds(allParentIds);
        }

        //save img
        if (!multipartFile.isEmpty()) {
            String fileName = category.getName() + UUID.randomUUID();
            category.setImage(fileName);

            Category savedCategory = categoryRepository.save(category);
            String uploadDir = "category-images/" + savedCategory.getId();

            AmazonS3Util.removeFolder(uploadDir);
            AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());

        } else {
            categoryRepository.save(category);
        }
    }

    public void delete(Integer id) throws CategoryNotFoundException, CategoryHasChildrenException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID %d not found.".formatted(id)));
        if (category.isHasChildren())
            throw new CategoryHasChildrenException("You cannot delete category with children.");

        categoryRepository.deleteById(id);

        String categoryDir = "category-images/" + id;
        AmazonS3Util.removeFolder(categoryDir);
    }

    public List<Category> listAll(String sortDir, String prefixSubCategory) {
        List<Category> rootCategories = categoryRepository.findRootCategories(Sort.by("name").ascending());

        return listHierarchicalCategories(rootCategories, sortDir, prefixSubCategory);
    }

    public void updateCategoryEnabledStatus(Integer id, Boolean enabled) throws CategoryNotFoundException {
        categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID %d not found.".formatted(id)));
        categoryRepository.updateEnabledStatus(id, enabled);
    }

    public Category getCategoryById(Integer id) throws CategoryNotFoundException {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID %d not found.".formatted(id)));
    }

    private List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir, String prefixSubCategory) {
        List<Category> hierarchicalCategories = new ArrayList<>();

        for (Category rootCategory : rootCategories) {
            hierarchicalCategories.add(Category.copyFull(rootCategory));

            Set<Category> children = sortSubCategories(rootCategory.getChildren(), sortDir);

            for (Category subCategory : children) {
                String name = prefixSubCategory + subCategory.getName();
                hierarchicalCategories.add(Category.copyFull(subCategory, name));

                listSubHierarchicalCategories(hierarchicalCategories, subCategory, 1, sortDir, prefixSubCategory);
            }
        }

        return hierarchicalCategories;
    }

    private void listSubHierarchicalCategories(List<Category> hierarchicalCategories, Category parent,
                                               int level, String sortDir, String prefixSubCategory) {
        Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);
        int newSubLevel = level + 1;
        for (Category subCategory : children) {
            String name = "";
            for (int i = 0; i < newSubLevel; i++) {
                name += prefixSubCategory;
            }
            name += subCategory.getName();

            hierarchicalCategories.add(Category.copyFull(subCategory, name));

            listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel, sortDir, prefixSubCategory);
        }
    }

    public List<Category> listCategoriesUsedInForm() {
        List<Category> parentsCategories = categoryRepository.findRootCategories(Sort.by("name").ascending());

        return parentsCategories.stream().flatMap(Category::streamAll).toList();
    }

    private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
        SortedSet<Category> sortedChildren = new TreeSet<>((o1, o2) -> {
            if (sortDir.equals("asc")) {
                return o1.getName().compareTo(o2.getName());
            } else {
                return o2.getName().compareTo(o1.getName());
            }
        });
        sortedChildren.addAll(children);

        return sortedChildren;
    }

    public String checkUnique(Integer id, String name, String alias) {
        if (name == null || name.length() < 2) return "Bad Name";
        if (alias == null || alias.length() < 2) return "Bad Alias";

        boolean isCreatingNew = (id == null || id == 0);

        Category categoryByName = categoryRepository.findByName(name);
        Category categoryByAlias = categoryRepository.findByAlias(alias);

        if (isCreatingNew) {
            if (categoryByName != null) return "DuplicateName";
            if (categoryByAlias != null) return "DuplicateAlias";
        }
        if (!isCreatingNew) {
            if (categoryByName != null && !Objects.equals(categoryByName.getId(), id)) return "DuplicateName";
            if (categoryByAlias != null && !Objects.equals(categoryByAlias.getId(), id)) return "DuplicateAlias";
        }
        return "OK";
    }
}
