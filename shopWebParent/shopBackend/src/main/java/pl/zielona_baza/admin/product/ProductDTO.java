package pl.zielona_baza.admin.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductDTO {
    private String name;
    private String imagePath;
    private float price;
    private float cost;
}
