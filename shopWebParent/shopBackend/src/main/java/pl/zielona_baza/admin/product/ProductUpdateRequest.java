package pl.zielona_baza.admin.product;

import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;
import pl.zielona_baza.common.entity.product.ProductDetail;
import pl.zielona_baza.common.entity.product.ProductImage;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public class ProductUpdateRequest {

    private Integer id;

    @Size(min = 3, max = 256)
    @NotBlank
    private String name;

    @Size(min = 3, max = 256)
    private String alias;

    @Size(min = 5, max = 512)
    @NotBlank
    private String shortDescription;

    @Size(min = 5, max = 4096)
    @NotBlank
    private String fullDescription;

    private Date createdTime;
    private Date updatedTime;

    private boolean enabled;
    private boolean inStock;


    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal cost;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax(value = "100.0", inclusive = false)
    @Digits(integer = 2, fraction = 2)
    private BigDecimal discountPercent;

    private float length;
    private float width;
    private float height;
    private float weight;

    private String mainImage;

    private Category category;

    private Brand brand;

    private Set<ProductImage> images = new HashSet<>();

    private List<ProductDetail> details = new ArrayList<>();
}
