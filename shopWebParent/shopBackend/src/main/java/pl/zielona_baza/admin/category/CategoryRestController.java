package pl.zielona_baza.admin.category;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zielona_baza.admin.category.CategoryService;

@RestController
public class CategoryRestController {
    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/categories/check_unique")
    public ResponseEntity<String> checkUnique(@RequestParam("id") Integer id, @RequestParam("name") String name,
                                              @RequestParam("alias") String alias) {
        String result = categoryService.checkUnique(id, name, alias);

        return ResponseEntity.ok().body(result);
    }

}
