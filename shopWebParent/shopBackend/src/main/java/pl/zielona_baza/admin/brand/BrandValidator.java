package pl.zielona_baza.admin.brand;

import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.MethodsUtil;
import pl.zielona_baza.admin.exception.CustomValidationException;
import pl.zielona_baza.common.entity.Brand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BrandValidator {
    private static final String ERROR_DELIMITER = ";";
    private final List<BrandValidationError> errors = new ArrayList<>();

    public BrandValidatorResult validate(Brand brand, BrandRepository repository, MultipartFile file) {
        if (!isBrandNameLengthCorrect(brand.getName())) {
            errors.add(BrandValidationError.NAME_SIZE_INCORRECT);
        }
        if (!isBrandNameUnique(brand.getId(), brand.getName(), repository)) {
            errors.add(BrandValidationError.NAME_NOT_UNIQUE);
        }
        if (!isFileImg(file)) {
            errors.add(BrandValidationError.FILE_NOT_IMG);
        }
        if (!hasImgForNewBrand(brand.getId(), file)) {
            errors.add(BrandValidationError.EMPTY_IMG_FOR_NEW_BRAND);
        }

        if (errors.isEmpty()) {
            return BrandValidatorResult.success();
        }
        String failureMessage = errors.stream().map(err -> err.message).collect(Collectors.joining(ERROR_DELIMITER));
        return BrandValidatorResult.failure(failureMessage);
    }

    private boolean hasImgForNewBrand(Integer id, MultipartFile file) {
        if (id == null || id == 0) {
            return !file.isEmpty();
        }
        return true;
    }

    private boolean isFileImg(MultipartFile file) {
        return MethodsUtil.isImage(file);
    }

    private boolean isBrandNameUnique(Integer id, String name, BrandRepository repository) {
        boolean isCreatingNew = (id == null || id == 0);
        Brand brand = repository.findByName(name);

        if (isCreatingNew) {
            return brand == null;
        } else {
            return brand == null || id.equals(brand.getId());
        }
    }

    private boolean isBrandNameLengthCorrect(String name) {
        return name != null && name.length() >= 2 && name.length() <= 45;
    }
}
