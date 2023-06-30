package pl.zielona_baza.common.entity.product;

import lombok.*;
import pl.zielona_baza.common.Constants;

import javax.persistence.*;

@Entity
@Table(name = "product_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Transient
    public String getImagePath() {
        return Constants.S3_BASE_URI + "/product-images/" + product.getId() + "/extras/" + this.name;
    }
}
