package pl.zielona_baza.admin.user.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.security.ShopUserDetails;
import pl.zielona_baza.admin.user.UserDTO;
import pl.zielona_baza.admin.user.UserNotFoundException;
import pl.zielona_baza.admin.user.UserService;

import javax.validation.Valid;
import java.io.IOException;

@Controller
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account")
    public String viewDetails(@AuthenticationPrincipal ShopUserDetails loggedUser,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            String email = loggedUser.getUsername();
            UserDTO user = userService.getUserByEmail(email);
            model.addAttribute("user", user);

            return "users/account_form";
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return "redirect:/";
        }
    }

    @PostMapping("/account/update")
    public String saveDetails(@Valid @ModelAttribute("user") UserDTO userUpdateRequest,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              @RequestParam("image") MultipartFile multipartFile,
                              @AuthenticationPrincipal ShopUserDetails loggedUser) throws IOException {
        if (result.hasErrors()) {
            return "users/account_form";
        }
        try {
            String email = loggedUser.getUsername();
            userService.updateAccount(userUpdateRequest, multipartFile, email);
            redirectAttributes.addFlashAttribute("message", "The account has been updated successfully.");

            return "redirect:/account";
        } catch (UserNotFoundException ex) {
            return "redirect:/";
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
