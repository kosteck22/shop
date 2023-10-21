package pl.zielona_baza.admin.user.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.exception.CustomValidationException;
import pl.zielona_baza.admin.user.UserDTO;
import pl.zielona_baza.admin.user.UserNotFoundException;
import pl.zielona_baza.admin.user.UserService;
import pl.zielona_baza.admin.user.export.UserCsvExporter;
import pl.zielona_baza.admin.user.export.UserExcelExporter;
import pl.zielona_baza.admin.user.export.UserPdfExporter;
import pl.zielona_baza.common.entity.User;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listFirstPage(Model model) {
        return listByPage(1, "email", "desc", 20, null, model);
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(name = "sortField", required = false) String sortField,
                             @RequestParam(name = "sortDir", required = false) String sortDir,
                             @RequestParam(name = "limit", required = false) Integer limit,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             Model model) {
        userService.listByPage(pageNum, sortField, sortDir, limit, keyword, model);

        return "users/users";
    }

    @GetMapping("/new")
    public String newUser(Model model) {
        model.addAttribute("roles", userService.listRoles());
        model.addAttribute("user", new UserDTO());
        model.addAttribute("pageTitle", "Create new user.");

        return "users/user_form";
    }

    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try
        {
            model.addAttribute("user", userService.getUserById(id));
            model.addAttribute("pageTitle", "Edit User");
            model.addAttribute("roles", userService.listRoles());

            return "users/user_form";
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return "redirect:/users";
        }
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") UserDTO user,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes,
                           @RequestParam("image") MultipartFile multipartFile) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("roles", userService.listRoles());
            model.addAttribute("pageTitle", "Create/Edit user.");

            return "users/user_form";
        }

        try {
            userService.save(user, multipartFile);

            redirectAttributes.addFlashAttribute("message", "The user has been saved successfully.");
        } catch (CustomValidationException ex) {
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("roles", userService.listRoles());
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Create/Edit user.");

            return "users/user_form";
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return "redirect:/users";
        }

        return getRedirectURLtoAffectedUser(user);
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);

            redirectAttributes.addFlashAttribute("message", "User has been deleted successfully");
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/{id}/enabled/{enabled}")
    public String updateUserEnabledStatus(@PathVariable("id") Integer id,
                                          @PathVariable("enabled") Boolean enabled,
                                          RedirectAttributes redirectAttributes) {
        try {
            UserDTO user = userService.updateUserEnabledStatus(id, enabled);
            String status = enabled ? "enabled" : "disabled";
            String message = "The user ID " + id + " has been " + status;
            redirectAttributes.addFlashAttribute("message", message);

            return getRedirectURLtoAffectedUser(user);
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/users";
    }

    private static String getRedirectURLtoAffectedUser(UserDTO user) {
        return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword=" + user.getEmail();
    }

    @GetMapping("/export/csv")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        List<User> users = userService.listAll();
        UserCsvExporter exporter = new UserCsvExporter();
        exporter.export(users, response);
    }

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<User> users = userService.listAll();
        UserExcelExporter exporter = new UserExcelExporter();
        exporter.export(users, response);
    }

    @GetMapping("//export/pdf")
    public void exportToPdf(HttpServletResponse response) throws IOException {
        List<User> users = userService.listAll();
        UserPdfExporter exporter = new UserPdfExporter();
        exporter.export(users, response);
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
