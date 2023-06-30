package pl.zielona_baza.admin.brand.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zielona_baza.admin.brand.BrandNotFoundException;
import pl.zielona_baza.admin.brand.BrandNotFoundRestException;
import pl.zielona_baza.admin.brand.BrandService;
import pl.zielona_baza.admin.brand.CategoryDTO;
import pl.zielona_baza.common.entity.Brand;
import pl.zielona_baza.common.entity.Category;

import java.util.ArrayList;
import java.util.List;

@RestController
public class BrandRestController {

    @Autowired
    private BrandService brandService;

    @PostMapping("/brands/check_unique")
    public ResponseEntity<String> checkUnique(@RequestParam("id") Integer id, @RequestParam("name") String name) {
        String result = brandService.checkUnique(id, name);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/brands/{id}/categories")
    public List<CategoryDTO> listCategoriesByBrand(@PathVariable(name = "id") Integer id) throws BrandNotFoundRestException {
        List<CategoryDTO> listCategories = new ArrayList<>();
        try {
            Brand brand = brandService.get(id);
            List<Category> categories = brand.getCategories();
            categories.forEach(category -> {
                listCategories.add(CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build());
            });
            return listCategories;
        } catch (BrandNotFoundException ex) {
            throw new BrandNotFoundRestException();
        }
    }
}
