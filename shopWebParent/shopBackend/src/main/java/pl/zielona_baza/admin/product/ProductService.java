package pl.zielona_baza.admin.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.brand.BrandRepository;
import pl.zielona_baza.admin.category.CategoryRepository;
import pl.zielona_baza.admin.exception.CustomValidationException;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ProductNotFoundException;

import java.io.IOException;
import java.util.*;

import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.*;
import static pl.zielona_baza.admin.paging.PagingAndSortingValidator.validateSortDir;

@Service
@Transactional
public class ProductService {

    private static final int PRODUCTS_PER_PAGE = 20;

    private static final List<String> SORTABLE_FIELDS_AVAILABLE = new ArrayList<>(
            List.of("id", "name", "brand", "category", "enabled"));
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, BrandRepository brandRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    public void listByPage(Integer pageNumber,
                           String sortField,
                           String sortDir,
                           Integer limit,
                           String keyword,
                           Model model,
                           Integer categoryId) {
        //validation
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, PRODUCTS_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "id");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper( "listProducts", sortField, sortDir, keyword, limit);

        Pageable pageable = helper.createPageable(pageNumber);
        Page<Product> page;

        //searching products only in one category
        if (categoryId != null && categoryId > 0) {
            String categoryIdMatch = "-" + categoryId + "-";

            if (keyword != null && !keyword.isEmpty()) {
                page = productRepository.searchInCategory(categoryId, categoryIdMatch, keyword, pageable);
            } else {
                page = productRepository.findAllInCategory(categoryId, categoryIdMatch, pageable);
            }
        }
        //searching products in all categories
        else {
            if (keyword != null && !keyword.isEmpty()) {
                page = productRepository.findAll(keyword, pageable);
            } else {
                page = productRepository.findAll(pageable);
            }
        }

        helper.updateModelAttributes(pageNumber, page, model);

        if (categoryId != null) model.addAttribute("categoryId", categoryId);
    }

    public void searchProducts(Integer pageNumber,
                               String sortField,
                               String sortDir,
                               Integer limit,
                               String keyword,
                               Model model) {
        pageNumber = validatePage(pageNumber);
        limit = validateLimit(limit, PRODUCTS_PER_PAGE);
        sortField = validateSortField(sortField, SORTABLE_FIELDS_AVAILABLE, "id");
        sortDir = validateSortDir(sortDir);

        PagingAndSortingHelper helper = new PagingAndSortingHelper( "listCustomers", sortField, sortDir, keyword, limit);

        Pageable pageable = helper.createPageable(pageNumber);
        Page<Product> page = productRepository.searchProductsByName(keyword, pageable);

        helper.updateModelAttributes(pageNumber, page, model);
    }

    public void save(Product product,
                        MultipartFile mainImageMultipart,
                        MultipartFile[] extraImageMultipart,
                        String[] detailIds,
                        String[] detailNames,
                        String[] detailValues,
                        String[] imageIds,
                        String[] imageNames) throws IOException, CustomValidationException, ProductNotFoundException {
        Integer id = product.getId();

        //validation main image (required for new product)
        if (id == null || id == 0) {
            if (mainImageMultipart.isEmpty() || !ProductSaveHelper.isImage(mainImageMultipart)) {
                throw new CustomValidationException("Main image cannot be empty");
            }
        }

        //validation name (check unique)
        Product productByName = productRepository.findByName(product.getName());

        if (productByName != null) {
            if ((id == null || id == 0)) {
                throw new CustomValidationException("Product name must be unique");
            }
            if (!Objects.equals(id, productByName.getId())) {
                throw new CustomValidationException("Product name must be unique");
            }
        }

        //validation alias (check unique)
        Product productByAlias = productRepository.findByAlias(product.getAlias());

        if(product.getAlias() == null || product.getAlias().isEmpty()) {
            String defaultAlias = product.getName().replaceAll(" ", "-");
            product.setAlias(defaultAlias);
        } else {
            product.setAlias(product.getAlias().replaceAll(" ", "-"));
        }

        if (productByAlias != null) {
            if (id == null || id == 0) {
                throw new CustomValidationException("Product alias must be unique");
            }
            if (!Objects.equals(id, productByAlias.getId())) {
                throw new CustomValidationException("Product alias must be unique");
            }
        }

        Product productToSave;
        if (id == null || id == 0) {
            productToSave =  new Product();
        } else {
            productToSave = productRepository.findById(id)
                    .orElseThrow(() -> new ProductNotFoundException("Product with given id not found"));
        }

        //validation brand
        Brand brand = product.getBrand();

        if (brand == null) {
            product.setBrand(productToSave.getBrand());
            throw new CustomValidationException("Chosen brand does not exist");
        }

        Brand brandFromDB = brandRepository.findById(brand.getId())
                .orElseThrow(() -> new CustomValidationException("Chosen brand does not exist"));

        //validation category
        Category category = product.getCategory();

        if (category == null) {
            product.setCategory(productToSave.getCategory());
            throw new CustomValidationException("Chosen category does not exist");
        }

        Category categoryFromDB = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new CustomValidationException("Chosen category does not exist"));

        if (!brandFromDB.getCategories().contains(category)) {
            throw new CustomValidationException("The chosen category is not part of brand");
        }

        productToSave.setName(product.getName());
        productToSave.setAlias(product.getAlias());
        productToSave.setShortDescription(product.getShortDescription());
        productToSave.setFullDescription(product.getFullDescription());
        productToSave.setBrand(brandFromDB);
        productToSave.setCategory(categoryFromDB);

        if (id == null) {
            productToSave.setCreatedTime(new Date());
        }

        productToSave.setUpdatedTime(new Date());
        productToSave.setEnabled(product.isEnabled());
        productToSave.setInStock(product.isInStock());
        productToSave.setCost(product.getCost());
        productToSave.setPrice(product.getPrice());
        productToSave.setDiscountPercent(product.getDiscountPercent());
        productToSave.setLength(product.getLength());
        productToSave.setWidth(product.getWidth());
        productToSave.setHeight(product.getHeight());
        productToSave.setWeight(product.getWeight());

        if (ProductSaveHelper.isImage(mainImageMultipart)) {
            ProductSaveHelper.setMainImageName(mainImageMultipart, productToSave);
        }

        if (id != null && id > 0) {
            ProductSaveHelper.setExistingExtraImageNames(imageIds, imageNames, productToSave);
        }

        List<String> newExtraImgNames = ProductSaveHelper.setNewExtraImageNames(extraImageMultipart, productToSave);
        ProductSaveHelper.updateProductDetails(detailIds, detailNames, detailValues, productToSave);

        Product savedProduct = productRepository.save(productToSave);
        productRepository.updateReviewCountAndAverageRating(savedProduct.getId());

        ProductSaveHelper.saveUploadedImage(mainImageMultipart,extraImageMultipart, savedProduct, newExtraImgNames);
        ProductSaveHelper.deleteExtraImagesWereRemovedOnForm(savedProduct);
    }

    public void saveProductPrice(Product product) throws ProductNotFoundException {
        Product productInDB = productRepository.findById(product.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product with ID %d not found".formatted(product.getId())));

        productInDB.setCost(product.getCost());
        productInDB.setPrice(product.getPrice());
        productInDB.setDiscountPercent(product.getDiscountPercent());

        productRepository.save(productInDB);
    }

    public Product get(Integer id) throws ProductNotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Could not find any product with ID %d".formatted(id)));
    }

    public String checkUnique(Integer id, String name) {
        boolean isCreatingNew = (id == null || id == 0);
        Product productByName = productRepository.findByName(name);

        if (isCreatingNew) {
            if (productByName != null) return "Duplicated";
        } else {
            if (productByName != null && !Objects.equals(productByName.getId(), id)) return "Duplicated";
        }

        return "OK";
    }

    public void updateProductEnabledStatus(Integer id, boolean enabled) throws ProductNotFoundException {
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Could not find any product with ID %d".formatted(id)));
        productRepository.updateEnabledStatus(id, enabled);
    }

    public void delete(Integer id) throws ProductNotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Could not find any product with ID %d".formatted(id)));
        productRepository.delete(product);

        String productExtraImagesDir = "product-images/" + id + "/extras";
        AmazonS3Util.removeFolder(productExtraImagesDir);

        String userPhotosDir = "product-images/" + id;
        AmazonS3Util.removeFolder(userPhotosDir);

    }

    public void restoreProductImagesAndDetails(Product product, String[] detailIds, String[] detailNames, String[] detailValues) throws ProductNotFoundException {
        restoreProductImages(product);
        restoreProductDetails(product, detailIds, detailNames, detailValues);
    }

    private void restoreProductDetails(Product product, String[] detailIds, String[] detailNames, String[] detailValues) {
        ProductSaveHelper.setProductDetails(detailIds, detailNames, detailValues, product);
    }

    private void restoreProductImages(Product product) throws ProductNotFoundException {
        Integer productId = product.getId();

        if (productId != null && productId > 0) {
            Product productFromDB = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Could not find any product with ID %d".formatted(productId)));

            product.setMainImage(productFromDB.getMainImage());
            product.setImages(productFromDB.getImages());
        }
    }
}
