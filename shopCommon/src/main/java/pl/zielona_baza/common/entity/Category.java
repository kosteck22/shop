package pl.zielona_baza.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.zielona_baza.common.Constants;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 128, unique = true, nullable = false)
    private String name;

    @Column(length = 64, unique = true, nullable = false)
    private String alias;

    @Column(length = 128, nullable = false)
    private String image;

    private boolean enabled;

    @Column(name = "all_parent_ids", length = 256, nullable = true)
    private String allParentIds;

    @OneToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("name asc")
    private Set<Category> children = new HashSet<>();

    @ManyToMany(mappedBy = "categories")
    private List<Brand> brands = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    public static Category copyFull(Category category) {
        Category copyCategory = Category.builder()
                .id(category.getId())
                .alias(category.getAlias())
                .name(category.getName())
                .image(category.getImage())
                .enabled(category.isEnabled())
                .parent(category.getParent())
                .build();

        return copyCategory;
    }

    public static Category copyFull(Category subCategory, String name) {
        Category copyCategory = copyFull(subCategory);
        copyCategory.setName(name);

        return copyCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id.equals(category.id) && name.equals(category.name) && alias.equals(category.alias) && image.equals(category.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, alias, image);
    }

    @Transient
    public String getImagePath() {
        if (this.id == null) return "/images/image-thumbnail.png";

        return Constants.S3_BASE_URI + "/category-images/" + this.id + "/" + this.image;
    }
    @Transient
    public boolean isHasChildren() {
        if (this.children.size() > 0) return true;
        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }
}