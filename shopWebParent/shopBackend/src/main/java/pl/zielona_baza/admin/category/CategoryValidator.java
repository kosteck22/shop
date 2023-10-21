package pl.zielona_baza.admin.category;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.MethodsUtil;
import pl.zielona_baza.admin.brand.BrandValidationError;
import pl.zielona_baza.common.entity.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryValidator {
    private static final String ERROR_DELIMITER = ";";
    private final List<CategoryValidationError> errors = new ArrayList<>();

    public CategoryValidatorResult validate(Category category, CategoryRepository repository, MultipartFile file) {
        if (!isCategoryNameLengthCorrect(category.getName())) {
            errors.add(CategoryValidationError.NAME_SIZE_INCORRECT);
        }
        if (!isCategoryNameUnique(category.getId(), category.getName(), repository)) {
            errors.add(CategoryValidationError.NAME_NOT_UNIQUE);
        }
        if (!isCategoryAliasLengthCorrect(category.getAlias())) {
            errors.add(CategoryValidationError.ALIAS_SIZE_INCORRECT);
        }
        if (!isCategoryAliasUnique(category.getId(), category.getAlias(), repository)) {
            errors.add(CategoryValidationError.ALIAS_NOT_UNIQUE);
        }
        if (!isFileImg(file)) {
            errors.add(CategoryValidationError.FILE_NOT_IMG);
        }
        if (!hasImgForNewCategory(category.getId(), file)) {
            errors.add(CategoryValidationError.EMPTY_IMG_FOR_NEW_BRAND);
        }

        if (errors.isEmpty()) {
            return CategoryValidatorResult.success();
        }
        String failureMessage = errors.stream().map(err -> err.message).collect(Collectors.joining(ERROR_DELIMITER));
        return CategoryValidatorResult.failure(failureMessage);
    }

    private boolean hasImgForNewCategory(Integer id, MultipartFile file) {
        if (id == null || id == 0) {
            return !file.isEmpty();
        }
        return true;
    }

    private boolean isCategoryNameUnique(Integer id, String name, CategoryRepository repository) {
        boolean isCreatingNew = (id == null || id == 0);
        Category category = repository.findByName(name);

        if (isCreatingNew) {
            return category == null;
        } else {
            return category == null || id.equals(category.getId());
        }
    }

    private boolean isCategoryNameLengthCorrect(String name) {
        return name != null && name.length() >= 2 && name.length() <= 128;
    }

    private boolean isCategoryAliasLengthCorrect(String name) {
        return name != null && name.length() >= 2 && name.length() <= 64;
    }

    private boolean isCategoryAliasUnique(Integer id, String alias, CategoryRepository repository) {
        boolean isCreatingNew = (id == null || id == 0);
        Category category = repository.findByAlias(alias);

        if (isCreatingNew) {
            return category == null;
        } else {
            return category == null || id.equals(category.getId());
        }
    }

    private boolean isFileImg(MultipartFile file) {
        return MethodsUtil.isImage(file);
    }
}
