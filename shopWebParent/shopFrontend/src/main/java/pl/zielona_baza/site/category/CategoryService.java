package pl.zielona_baza.site.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.exception.CategoryNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;

    public List<Category> listNoChildrenCategories() {
        List<Category> enabledCategories = categoryRepository.findAllEnabled();

        return enabledCategories.stream().filter(category -> {
            return category.getChildren().size() == 0 || category.getChildren() == null;
        }).toList();
    }

    public Category getCategory(String alias) throws CategoryNotFoundException {
        Category category = categoryRepository.findByAliasEnabled(alias);

        if (category == null) throw new CategoryNotFoundException("Category not found.");

        return category;
    }

    public List<Category> getCategoryParents(Category childCategory) {
        List<Category> listParents = new ArrayList<>();

        Category parent = childCategory.getParent();

        while (parent != null) {
            listParents.add(0, parent);
            parent = parent.getParent();
        }
        listParents.add(childCategory);

        return listParents;
    }
}
