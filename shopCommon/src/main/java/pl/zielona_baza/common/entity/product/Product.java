package pl.zielona_baza.common.entity.product;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.zielona_baza.common.Constants;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;

import javax.persistence.*;
import java.util.*;

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
    private String name;
    @Column(unique = true, length = 256, nullable = false)
    private String alias;
    @Column(length = 512, nullable = false, name = "short_description")
    private String shortDescription;
    @Column(length = 4096, nullable = false, name = "full_description")
    private String fullDescription;

    @Column(name = "created_time")
    private Date createdTime;
    @Column(name = "updated_time")
    private Date updatedTime;

    private boolean enabled;
    @Column(name = "in_stock")
    private boolean inStock;

    private float cost;
    @Column(name = "price")
    private float price;
    @Column(name = "discount_percent")
    private float discountPercent;

    private float length;
    private float width;
    private float height;
    private float weight;

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

    public void addExtraImage(String imageName) {
        this.images.add(ProductImage.builder()
                .name(imageName)
                .product(this)
                .build());
    }

    public void addDetail(String detailName, String detailValue) {
        this.details.add(new ProductDetail(detailName, detailValue, this));
    }

    public void addDetail(Integer id, String name, String value) {
        this.details.add(new ProductDetail(id, name, value, this));
    }

    @Transient
    public String getMainImagePath() {
        if (id == null || this.mainImage == null) return "/images/image-thumbnail.png";

        return Constants.S3_BASE_URI + "/product-images/" + this.id + "/" + this.mainImage;
    }

    public boolean containsImageName(String imageName) {
        Iterator<ProductImage> iterator = images.iterator();

        while (iterator.hasNext()) {
            ProductImage image = iterator.next();
            if (image.getName().equals(imageName)) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public String getShortName() {
        if (this.name.length() > 70) return this.name.substring(0, 70).concat("...");

        return this.name;
    }

    @Transient
    public float getDiscountPrice() {
        if (discountPercent > 0) {
            return this.price * ((100 - this.discountPercent) / 100);
        }
        return this.price;
    }

    @Transient
    public String getURI() {
        return "/c/" + this.category.getAlias() + "/" + this.alias + "/";
    }
}
