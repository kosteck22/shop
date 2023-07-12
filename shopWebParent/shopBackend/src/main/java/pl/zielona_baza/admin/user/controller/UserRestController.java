package pl.zielona_baza.admin.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.zielona_baza.admin.user.UserService;

@RestController
public class UserRestController {
    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/check_email")
    public ResponseEntity<String> checkDuplicateEmail(@RequestParam(name = "id", required = false) Integer id,
                                                      @RequestParam("email") String email) {
        boolean isEmailUnique = userService.isEmailUnique(id, email);

        return isEmailUnique ? ResponseEntity.ok("OK") : ResponseEntity.ok("Duplicated");
    }
}
