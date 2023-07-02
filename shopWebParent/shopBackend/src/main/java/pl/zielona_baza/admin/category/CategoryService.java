package pl.zielona_baza.admin.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.exception.ValidationException;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.common.entity.Brand;
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
    public static final int CATEGORIES_PER_PAGE = 20;

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, CATEGORIES_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "name");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper( "listCategories", sortField, sortDir, keyword, limit);

        helper.listEntities(pageNumber, categoryRepository, model);
    }

    public List<Category> listAll(String sortDir, String prefixSubCategory) {
        List<Category> rootCategories = categoryRepository.findRootCategories(Sort.by("name").ascending());

        return listHierarchicalCategories(rootCategories, sortDir, prefixSubCategory);
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
            for (int i=0; i < newSubLevel; i++) {
                name+=prefixSubCategory;
            }
            name += subCategory.getName();

            hierarchicalCategories.add(Category.copyFull(subCategory, name));

            listSubHierarchicalCategories(hierarchicalCategories, subCategory, newSubLevel, sortDir, prefixSubCategory);
        }
    }

    public List<Category> listCategoriesUsedInForm() {
        List<Category> categoriesUsedInForm = new ArrayList<>();
        Iterable<Category> categoriesInDB = categoryRepository.findRootCategories(Sort.by("name").ascending());

        for (Category category : categoriesInDB) {
            if (category.getParent() == null) {
                categoriesUsedInForm.add(
                        Category.builder()
                                .name(category.getName())
                                .alias(category.getAlias())
                                .id(category.getId())
                                .build()
                );

                Set<Category> children = category.getChildren();

                for (Category subCategory : children) {
                    String name = "--" + subCategory.getName();
                    categoriesUsedInForm.add(
                            Category.builder()
                                .name(name)
                                .alias(subCategory.getAlias())
                                .id(subCategory.getId())
                                .build());
                    listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, 1);
                }
            }
        }

        return categoriesUsedInForm;
    }

    private void listSubCategoriesUsedInForm(List<Category> categoriesUsedInForm, Category parent, int subLevel) {
        int newSubLevel = subLevel + 1;
        Set<Category> children = sortSubCategories(parent.getChildren());

        for (Category subCategory : children) {
            String name = "";
            for(int i = 0; i < newSubLevel; i++) {
                name += "--";
            }
            categoriesUsedInForm.add(
                    Category.builder()
                    .name(name + subCategory.getName())
                    .alias(name + subCategory.getName())
                    .id(subCategory.getId())
                    .build());

            listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, newSubLevel);
        }
    }

    private SortedSet<Category> sortSubCategories(Set<Category> children) {
        return sortSubCategories(children, "asc");
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

    private boolean isNameValid(Integer id, String name) {
        if (name == null || name.length() < 2 || name.length() > 128) return false;

        boolean isCreatingNew = (id == null || id == 0);
        Category category = categoryRepository.findByName(name);

        if (isCreatingNew) {
            return category == null;
        } else {
            return category == null || id.equals(category.getId());
        }
    }

    private boolean isAliasValid(Integer id, String alias) {
        if (alias == null || alias.length() < 2 || alias.length() > 64) return false;

        boolean isCreatingNew = (id == null || id == 0);
        Category category = categoryRepository.findByAlias(alias);

        if (isCreatingNew) {
            return category == null;
        } else {
            return category == null || id.equals(category.getId());
        }
    }

    public void save(Category category, MultipartFile multipartFile) throws CategoryNotFoundException, ValidationException, IOException {
        //Validate category name
        if (category.getName() != null) category.setName(category.getName().trim());
        if (!isNameValid(category.getId(), category.getName())) {
            throw new ValidationException("Category name is not valid try another one");
        }

        //Validate category alias
        if (category.getAlias() != null) category.setAlias(category.getAlias().trim());
        if (!isAliasValid(category.getId(), category.getAlias())) {
            throw new ValidationException("Category alias is not valid try another one");
        }

        //Validate&Set parent
        if (category.getParent() != null) {
            Category parentCategory = categoryRepository.findById(category.getParent().getId()).orElseThrow(() ->
                new CategoryNotFoundException("Parent category not found."));
            category.setParent(parentCategory);
            String allParentIds = parentCategory.getAllParentIds() == null ? "-" : parentCategory.getAllParentIds();
            allParentIds += parentCategory.getId() + "-";
            category.setAllParentIds(allParentIds);
        }

        //New category
        if (category.getId() == null || category.getId() == 0) {
            if (multipartFile.isEmpty()) {
                throw new ValidationException("File image cannot be empty");
            }
        }

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

    public Category getCategoryById(Integer id) throws CategoryNotFoundException {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID %d not found.".formatted(id)));
    }

    public String checkUnique(Integer id, String name, String alias) {
        if (name == null || name.trim().length() < 2) return "Bad Name";
        if (alias == null || alias.trim().length() < 2) return "Bad Alias";

        boolean isCreatingNew = (id == null || id == 0);

        Category categoryByName = categoryRepository.findByName(name.trim());
        Category categoryByAlias = categoryRepository.findByAlias(alias.trim());

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

    public void updateCategoryEnabledStatus(Integer id, Boolean enabled) throws CategoryNotFoundException {
        categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID %d not found.".formatted(id)));
        categoryRepository.updateEnabledStatus(id, enabled);
    }

    public void delete(Integer id) throws CategoryNotFoundException, CategoryHasChildrenException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID %d not found.".formatted(id)));
        if (category.isHasChildren()) throw new CategoryHasChildrenException("You cannot delete category with children.");

        categoryRepository.deleteById(id);

        String categoryDir = "category-images/" + id;
        AmazonS3Util.removeFolder(categoryDir);
    }
}
