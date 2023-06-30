package pl.zielona_baza.site.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.exception.ProductNotFoundException;

@Service
public class ProductService {

    public static final int PRODUCTS_PER_PAGE = 20;
    public static final int SEARCH_RESULTS_PER_PAGE = 10;

    @Autowired private ProductRepository productRepository;

    public Page<Product> listByCategory(int pageNum, Integer categoryId) {
        String categoryIdMatch = "-" + String.valueOf(categoryId) + "-";
        Pageable pageable = PageRequest.of(pageNum - 1, PRODUCTS_PER_PAGE);

        return productRepository.listByCategory(categoryId, categoryIdMatch, pageable);
    }

    public Product getProductByAlias(String alias) throws ProductNotFoundException {
        Product product = productRepository.findByAlias(alias);

        if (product == null) {
            throw new ProductNotFoundException("Product not found.");
        }

        return product;
    }

    public Product getProductById(Integer id) throws ProductNotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id %s not found".formatted(id)));
    }

    public Page<Product> search(String keyword, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum - 1, SEARCH_RESULTS_PER_PAGE);

        return productRepository.search(keyword, pageable);
    }
}
