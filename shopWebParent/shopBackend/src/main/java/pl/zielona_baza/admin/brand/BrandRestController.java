package pl.zielona_baza.admin.brand;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zielona_baza.admin.category.CategoryDTO;
import java.util.List;

@RestController
public class BrandRestController {

    private final BrandService brandService;

    public BrandRestController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/brands/check_unique")
    public ResponseEntity<String> checkUnique(@RequestParam(name = "id", required = false) Integer id,
                                              @RequestParam("name") String name) {
        String result = brandService.checkUnique(id, name);

        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/brands/{id}/categories")
    public List<CategoryDTO> listCategoriesByBrand(@PathVariable(name = "id") Integer id) {
        return brandService.getListCategoriesByBrandId(id);
    }
}
