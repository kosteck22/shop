package pl.zielona_baza.common.entity.product;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.zielona_baza.common.Constants;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "products")
@Data
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, length = 256, nullable = false)
    @Size(min = 3, max = 256)
    @NotBlank
    private String name;

    @Column(unique = true, length = 256, nullable = false)
    @Size(min = 3, max = 256)
    private String alias;

    @Column(length = 512, nullable = false, name = "short_description")
    @Size(min = 5, max = 512)
    @NotBlank
    private String shortDescription;

    @Column(length = 4096, nullable = false, name = "full_description")
    @Size(min = 5, max = 4096)
    @NotBlank
    private String fullDescription;

    @Column(name = "created_time")
    private Date createdTime;

    @Column(name = "updated_time")
    private Date updatedTime;

    private boolean enabled;

    @Column(name = "in_stock")
    private boolean inStock;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal cost;

    @Column(name = "price")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal price;

    @Column(name = "discount_percent")
    @DecimalMax(value = "50.0", inclusive = false)
    @Digits(integer = 2, fraction = 2)
    private BigDecimal discountPercent;

    @Min(value = 0)
    private int length;

    @Min(value = 0)
    private int width;

    @Min(value = 0)
    private int height;

    @Min(value = 0)
    private int weight;

    @Column(name = "review_count")
    private int reviewCount;

    @Column(name = "average_rating")
    private float averageRating;

    @Column(name = "main_image", nullable = false)
    private String mainImage;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetail> details = new ArrayList<>();

    @Transient
    private boolean customerCanReview;

    @Transient
    private boolean reviewedByCustomer;

    public Product(Integer id) {
        this.id = id;
    }

    public Product(String name) { this.name = name; }

    public void setDetails(List<ProductDetail> details) {
        this.details.clear();
        this.details.addAll(details);
    }

    public void setImages(Set<ProductImage> images) {
        this.images.clear();
        this.images = images;
    }

    public void addExtraImage(String imageName) {
        images.add(ProductImage.builder()
                            .name(imageName)
                            .product(this)
                            .build());
    }

    public void addDetail(String detailName, String detailValue) {
        details.add(ProductDetail.builder()
                            .name(detailName)
                            .value(detailValue)
                            .product(this)
                            .build());
    }

    public void addDetail(Integer detailId, String detailName, String detailValue) {
        details.add(ProductDetail.builder()
                        .id(detailId)
                        .name(detailName)
                        .value(detailValue)
                        .product(this)
                        .build());
    }

    public void updateDetail(Integer id, String detailName, String detailValue) {
        Optional<ProductDetail> detailOpt = details.stream().filter(d -> d.getId().equals(id)).findFirst();

        if (detailOpt.isPresent()) {
            ProductDetail detail = detailOpt.get();
            detail.setName(detailName);
            detail.setValue(detailValue);
        }
    }

    @Transient
    public String getMainImagePath() {
        if (id == null || mainImage == null) return "/images/image-thumbnail.png";

        return Constants.S3_BASE_URI + "/product-images/" + id + "/" + mainImage;
    }

    public boolean containsImageName(String imageName) {
        return images.stream().anyMatch(img -> img.getName().equals(imageName));
    }

    @Transient
    public String getShortName() {
        if (name.length() > 70) return name.substring(0, 70).concat("...");

        return name;
    }

    @Transient
    public BigDecimal getDiscountPrice() {
        if (discountPercent.doubleValue() > 0) {
            BigDecimal percent = BigDecimal.valueOf(100)
                    .subtract(this.discountPercent)
                    .divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_EVEN);

            return price.multiply(percent);
        }
        return price;
    }

    @Transient
    public String getURI() {
        return "/c/" + category.getAlias() + "/" + alias + "/";
    }
}
