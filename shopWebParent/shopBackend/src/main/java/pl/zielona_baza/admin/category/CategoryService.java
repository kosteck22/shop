package pl.zielona_baza.admin.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.exception.CategoryNotFoundException;

import java.util.*;

@Service
@Transactional
public class CategoryService {
    public static final int CATEGORIES_PER_PAGE = 20;

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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

    public Page<Category> listByPage(Integer pageNum, String sortField, String sortDir, String keyword) {
        Sort sort = Sort.by(sortField);
        sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
        Pageable pageable = PageRequest.of(pageNum - 1, CATEGORIES_PER_PAGE, sort);

        if(keyword != null) {
            return categoryRepository.findAll(keyword, pageable);
        }
        return categoryRepository.findAll(pageable);
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
                            .id(subCategory.getId()).build());
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
        SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {
            @Override
            public int compare(Category o1, Category o2) {
                if (sortDir.equals("asc")) {
                    return o1.getName().compareTo(o2.getName());
                } else {
                    return o2.getName().compareTo(o1.getName());
                }
            }
        });
        sortedChildren.addAll(children);

        return sortedChildren;
    }

    public Category save(Category category) throws CategoryNotFoundException {
        if (category.getParent() != null) {
            try {
                Category parentCategory = categoryRepository.findById(category.getParent().getId()).orElseThrow(() -> {
                    throw new NoSuchElementException("Parent category not found.");
                });
                category.setParent(parentCategory);
                String allParentIds = parentCategory.getAllParentIds() == null ? "-" : parentCategory.getAllParentIds();
                allParentIds += String.valueOf(parentCategory.getId()) + "-";
                category.setAllParentIds(allParentIds);

                return categoryRepository.save(category);
            } catch (NoSuchElementException ex) {
                throw new CategoryNotFoundException(ex.getMessage());
            }
        }
        return categoryRepository.save(category);
    }

    public Category getCategoryById(Integer id) throws CategoryNotFoundException {
        try {
            return categoryRepository.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new CategoryNotFoundException("Category with ID " + id + " not found.");
        }
    }

    public String checkUnique(Integer id, String name, String alias) {
        boolean isCreatingNew = (id == null || id == 0);

        Category categoryByName = categoryRepository.findByName(name);
        Category categoryByAlias = categoryRepository.findByAlias(alias);

        if (isCreatingNew) {
            if (categoryByName != null) return "DuplicateName";
            if (categoryByAlias != null) return "DuplicateAlias";
        }
        if (!isCreatingNew) {
            if (categoryByName != null && categoryByName.getId() != id) return "DuplicateName";
            if (categoryByAlias != null && categoryByAlias.getId() != id) return "DuplicateAlias";
        }
        return "OK";
    }

    public void updateCategoryEnabledStatus(Integer id, Boolean enabled) throws CategoryNotFoundException {
        try {
            categoryRepository.findById(id).orElseThrow(() -> {
                throw new NoSuchElementException();
            });
            categoryRepository.updateEnabledStatus(id, enabled);
        } catch (NoSuchElementException ex) {
            throw new CategoryNotFoundException("Category with ID " + id + " not found.");
        }
    }

    public void delete(Integer id) throws CategoryNotFoundException, CategoryHasChildrenException {
        try {
            Category category = categoryRepository.findById(id).orElseThrow(() -> {
                throw new NoSuchElementException();
            });
            if (category.isHasChildren()) {
                throw new CategoryHasChildrenException("You cannot delete category with children.");
            }
            categoryRepository.deleteById(id);
        } catch (NoSuchElementException ex) {
            throw new CategoryNotFoundException("Category with ID" + id + " not found.");
        } catch (CategoryHasChildrenException e) {
            throw new CategoryHasChildrenException(e.getMessage());
        }
    }
}
