package pl.zielona_baza.admin.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.AmazonS3Util;
import pl.zielona_baza.admin.FileUploadUtil;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.paging.PagingAndSortingParam;
import pl.zielona_baza.admin.user.UserNotFoundException;
import pl.zielona_baza.admin.user.UserService;
import pl.zielona_baza.admin.user.export.UserCsvExporter;
import pl.zielona_baza.admin.user.export.UserExcelExporter;
import pl.zielona_baza.admin.user.export.UserPdfExporter;
import pl.zielona_baza.common.entity.Role;
import pl.zielona_baza.common.entity.User;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String listFirstPage() {
        return "redirect:/users/page/1?sortField=firstName&sortDir=asc";
    }

    @GetMapping("/users/page/{pageNum}")
    public String listByPage(@PagingAndSortingParam(listName = "listUsers") PagingAndSortingHelper helper,
                             @PathVariable("pageNum") Integer pageNum, Model model) {
        userService.listByPage(pageNum, helper);

        return "users/users";
    }

    @GetMapping("/users/new")
    public String newUser(Model model) {
        User user = new User();
        model.addAttribute("roles", userService.listRoles());
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Create new user.");
        return "users/user_form";
    }

    @PostMapping("/users/save")
    public String saveUser(User user, RedirectAttributes redirectAttributes,
                           @RequestParam("image")MultipartFile multipartFile) throws IOException {

        if(!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            user.setPhotos(fileName);
            User savedUser = userService.save(user);

            String uploadDir = "user-photos/" + savedUser.getId();

            //Upload to Amazon S3
            AmazonS3Util.removeFolder(uploadDir);
            AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());

            //Upload to the file system
            //FileUploadUtil.cleanDir(uploadDir);
            //FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
        } else {
            if(user.getPhotos().isEmpty()) user.setPhotos(null);
                userService.save(user);
        }
        redirectAttributes.addFlashAttribute("message", "The user has been saved successfully.");

        return getRedirectURLtoAffectedUser(user);
    }

    private static String getRedirectURLtoAffectedUser(User user) {
        //String firstPartOfEmail = user.getEmail().split("@")[0];
        return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword=" + user.getEmail();
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try
        {
            User user = userService.getUserById(id);
            List<Role> roles = userService.listRoles();
            model.addAttribute("user", user);
            model.addAttribute("pageTitle", "Edit User");
            model.addAttribute("roles", roles);

            return "users/user_form";
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return "redirect:/users";
        }
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            String userPhotosDir = "user-photos/" + id;
            AmazonS3Util.removeFolder(userPhotosDir);

            redirectAttributes.addFlashAttribute("message", "User with id = " + id +
                    " has been deleted successfully");
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/users/{id}/enabled/{enabled}")
    public String updateUserEnabledStatus(@PathVariable("id") Integer id,
                                          @PathVariable("enabled") Boolean enabled,
                                          RedirectAttributes redirectAttributes) {
        try {
            User user = userService.updateUserEnabledStatus(id, enabled);
            String status = enabled ? "enabled" : "disabled";
            String message = "The user ID " + id + " has been " + status;
            redirectAttributes.addFlashAttribute("message", message);

            return getRedirectURLtoAffectedUser(user);
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/users";
    }

    @GetMapping("/users/export/csv")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        List<User> users = userService.listAll();
        UserCsvExporter exporter = new UserCsvExporter();
        exporter.export(users, response);
    }
    
    @GetMapping("/users/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<User> users = userService.listAll();
        UserExcelExporter exporter = new UserExcelExporter();
        exporter.export(users, response);
    }

    @GetMapping("/users/export/pdf")
    public void exportToPdf(HttpServletResponse response) throws IOException {
        List<User> users = userService.listAll();
        UserPdfExporter exporter = new UserPdfExporter();
        exporter.export(users, response);
    }
}
