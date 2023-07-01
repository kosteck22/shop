package pl.zielona_baza.admin.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ProductNotFoundException;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ProductService {

    public static final int PRODUCTS_PER_PAGE = 20;
    @Autowired
    private ProductRepository productRepository;

    public List<Product> listALl() {
        return productRepository.findAll(Sort.by("name").ascending());
    }

    public void listByPage(Integer pageNum, PagingAndSortingHelper helper, Integer categoryId) {
        /*Pageable pageable = helper.createPageable(PRODUCTS_PER_PAGE, pageNum);
        String keyword = helper.getKeyword();
        Page<Product> page = null;

        if (keyword != null && !keyword.isEmpty()) {
            if (categoryId != null && categoryId > 0) {
                String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
                page = productRepository.searchInCategory(categoryId, categoryIdMatch, keyword, pageable);
            } else {
                page = productRepository.findAll(keyword, pageable);
            }
        } else {
            if (categoryId != null && categoryId > 0) {
                String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
                page = productRepository.findAllInCategory(categoryId, categoryIdMatch, pageable);
            } else {
                page = productRepository.findAll(pageable);
            }
        }
        helper.updateModelAttributes(pageNum, page);*/
    }

    public void searchProducts(int pageNum, PagingAndSortingHelper helper) {
        /*Pageable pageable = helper.createPageable(PRODUCTS_PER_PAGE, pageNum);
        String keyword = helper.getKeyword();

        Page<Product> page = productRepository.searchProductsByName(keyword, pageable);

        helper.updateModelAttributes(pageNum, page);*/
    }

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setCreatedTime(new Date());
        }

        if(product.getAlias() == null || product.getAlias().isEmpty()) {
            String defaultAlias = product.getName().replaceAll(" ", "-");
            product.setAlias(defaultAlias);
        } else {
            product.setAlias(product.getAlias().replaceAll(" ", "-"));
        }

        product.setUpdatedTime(new Date());

        Product savedProduct = productRepository.save(product);
        productRepository.updateReviewCountAndAverageRating(savedProduct.getId());

        return savedProduct;
    }

    public void saveProductPrice(Product product) {
        Product productInDB = productRepository.findById(product.getId()).get();
        productInDB.setCost(product.getCost());
        productInDB.setPrice(product.getPrice());
        productInDB.setDiscountPercent(product.getDiscountPercent());

        productRepository.save(productInDB);
    }

    public Product get(Integer id) throws ProductNotFoundException {
        try {
            return productRepository.findById(id).get();
        } catch (NoSuchElementException ex) {
            throw new ProductNotFoundException("Could not find any product with ID " + id);
        }
    }

    public String checkUnique(Integer id, String name) {
        boolean isCreatingNew = (id == null || id == 0);
        Product productByName = productRepository.findByName(name);

        if (isCreatingNew) {
            if (productByName != null) return "Duplicated";
        } else {
            if (productByName != null && productByName.getId() != id) return "Duplicated";
        }

        return "OK";
    }

    public void updateProductEnabledStatus(Integer id, boolean enabled) throws ProductNotFoundException {
        try {
            productRepository.findById(id).orElseThrow(() -> {
                throw new NoSuchElementException();
            });
            productRepository.updateEnabledStatus(id, enabled);
        } catch (NoSuchElementException e) {
                throw new ProductNotFoundException("Product with ID " + id + " not found.");
        }
    }

    public void delete(Integer id) throws ProductNotFoundException {
        boolean productExist = productRepository.existsById(id);

        if (!productExist) throw new ProductNotFoundException("Product with ID " + id + " not found.");
        productRepository.deleteById(id);
    }
}
