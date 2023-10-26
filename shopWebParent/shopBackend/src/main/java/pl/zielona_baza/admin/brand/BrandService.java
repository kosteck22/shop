package pl.zielona_baza.admin.brand;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.category.CategoryDTO;
import pl.zielona_baza.admin.exception.CustomValidationException;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.common.entity.Brand;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;

@Service
@Transactional
public class BrandService {
    private static final int BRANDS_PER_PAGE = 10;
    private static final List<String> SORTABLE_FIELDS_AVAILABLE = new ArrayList<>(List.of("id", "name"));
    private final BrandRepository brandRepository;
    private final BrandValidator validator;

    public BrandService(BrandRepository brandRepository, BrandValidator validator) {
        this.brandRepository = brandRepository;
        this.validator = validator;
    }

    public void listByPage(Integer pageNumber, String sortField, String sortDir, Integer limit, String keyword, Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, BRANDS_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "id");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper("listBrands", sortField, sortDir, keyword, limit, pageNumber);

        helper.listEntities(brandRepository, model);
    }

    public void save(Brand brand, MultipartFile file) throws IOException, CustomValidationException {
        BrandValidatorResult result = validator.validate(brand, brandRepository, file);
        if (result.isNotValid()) {
            throw new CustomValidationException(result.message());
        }

        // Edit brand
        if (file.isEmpty()) {
            brandRepository.save(brand);
            return;
        }

        // Saves image to Amazon S3
        String fileName = brand.getName() + UUID.randomUUID();
        brand.setLogo(fileName);

        Brand savedBrand = brandRepository.save(brand);
        String uploadDir = "brand-logos/" + savedBrand.getId();

        AmazonS3Util.removeFolder(uploadDir);
        AmazonS3Util.uploadFile(uploadDir, fileName, file.getInputStream());
    }

    public void delete(Integer id) throws BrandNotFoundException {
        Brand brand = getById(id);

        String brandDir = "brand-logos/" + id;
        AmazonS3Util.removeFolder(brandDir);
        brandRepository.delete(brand);
    }

    public Brand getById(Integer id) throws BrandNotFoundException {
        return brandRepository.findById(id).orElseThrow(() -> new BrandNotFoundException("Brand with ID %d not found.".formatted(id)));
    }

    public String checkUnique(Integer id, String name) {
        if (name == null || name.trim().length() < 2) return "Bad Name";

        boolean isCreatingNew = (id == null || id == 0);
        Brand brand = brandRepository.findByName(name.trim());

        if (isCreatingNew) {
            if (brand != null) return "Duplicate";
        } else {
            if (brand != null && !id.equals(brand.getId())) {
                return "Duplicate";
            }
        }

        return "OK";
    }

    public List<Brand> listAll() {
        return brandRepository.findAllProjection();
    }

    public List<CategoryDTO> getListCategoriesByBrandId(Integer id) {
        return brandRepository.findById(id)
                .orElseThrow(BrandNotFoundRestException::new)
                .getCategories().stream()
                .map(cat -> CategoryDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
