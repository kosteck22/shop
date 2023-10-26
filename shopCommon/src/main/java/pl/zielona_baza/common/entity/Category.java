package pl.zielona_baza.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.zielona_baza.common.Constants;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Stream;

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

    @Column(name = "all_parent_ids", length = 256)
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
        return Category.builder()
                .id(category.getId())
                .alias(category.getAlias())
                .name(category.getName())
                .image(category.getImage())
                .enabled(category.isEnabled())
                .parent(category.getParent())
                .build();
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
        return !this.children.isEmpty();
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Stream<Category> streamAll() {
        return Stream.concat(Stream.of(this), getChildren().stream().flatMap(cat -> {
            cat.setName(getModifiedName(cat));
            return cat.streamAll();
        }));
    }

    private String getModifiedName(Category category) {
        String prefix = "";
        Category parent = category.getParent();
        while(parent != null) {
            prefix += "--";
            parent = parent.getParent();
        }
        return prefix + category.getName();
    }
}
