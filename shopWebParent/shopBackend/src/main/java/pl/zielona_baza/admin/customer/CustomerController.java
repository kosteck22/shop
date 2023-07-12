package pl.zielona_baza.admin.customer;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.exception.ValidationException;
import pl.zielona_baza.common.entity.Country;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listFirstPage(Model model) {
        return listByPage(1, "email", "asc", 10, null, model);
    }

    @GetMapping("page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(value = "sortField", required = false) String sortField,
                             @RequestParam(value = "sortDir", required = false) String sortDir,
                             @RequestParam(value = "limit", required = false) Integer limit,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model) {
        customerService.listByPage(pageNum, sortField, sortDir, limit, keyword, model);

        return "customers/customers";
    }

    @GetMapping("edit/{id}")
    public String editCustomer(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CustomerDTO customer = customerService.get(id);
            List<Country> countries = customerService.listAllCountries();

            model.addAttribute("customer", customer);
            model.addAttribute("listCountries", countries);
            model.addAttribute("pageTitle", "Edit Customer");

            return "customers/customer_form";
        } catch (CustomerNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return "redirect:/customers";
        }
    }

    @PostMapping("save")
    public String saveCustomer(@Valid @ModelAttribute("customer") CustomerDTO customer,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Country> countries = customerService.listAllCountries();

            model.addAttribute("listCountries", countries);
            model.addAttribute("pageTitle", "Edit Customer");

            return "customers/customer_form";
        }

        try {
            customerService.save(customer);

            redirectAttributes.addFlashAttribute("message", "Customer saved successfully");
        } catch (CustomerNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        } catch (ValidationException ex) {
            List<Country> countries = customerService.listAllCountries();

            model.addAttribute("customer", customer);
            model.addAttribute("listCountries", countries);
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("pageTitle", "Edit Customer");

            return "customers/customer_form";
        }

        return "redirect:/customers";
    }

    @GetMapping("delete/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            customerService.delete(id);

            redirectAttributes.addFlashAttribute("message", "Customer deleted successfully");
        } catch (CustomerNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/customers";
    }

    @GetMapping("{id}/enabled/{status}")
    public String updateCustomerEnabledStatus(@PathVariable("id") Integer id,
                                              @PathVariable("status") boolean enabled,
                                              RedirectAttributes redirectAttributes) {
        try {
            customerService.updateCustomerEnabledStatus(id, enabled);
            String status = enabled ? "enabled" : "disabled";
            String message = "The Customer ID %d has been ".formatted(id) + status;
            redirectAttributes.addFlashAttribute("message", message);

        } catch (CustomerNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/customers";
    }

    @GetMapping("detail/{id}")
    public String viewCustomer(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CustomerDTO customer = customerService.get(id);
            model.addAttribute("customer", customer);

            return "customers/customer_detail_modal";
        } catch (CustomerNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return "redirect:/customers";
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
