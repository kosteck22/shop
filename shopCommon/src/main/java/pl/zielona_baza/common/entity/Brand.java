package pl.zielona_baza.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.zielona_baza.common.Constants;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "brands")
@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 45, unique = true, nullable = false)
    private String name;

    @Builder.Default
    @Column(length = 128)
    private String logo = "brand-logo.png";

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "brand_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories = new ArrayList<>();

    public Brand(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Brand{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", logo='" + logo + '\'' +
                ", categories=" + categories +
                '}';
    }

    @Transient
    public String getLogoPath() {
        if (this.logo == "brand-logo.png" || this.id == null) return "/images/image-thumbnail.png";
        return Constants.S3_BASE_URI + "/brand-logos/" + this.id + "/" + this.logo;
    }
}

