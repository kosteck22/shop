package pl.zielona_baza.admin.customer;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.brand.BrandNotFoundException;
import pl.zielona_baza.admin.exception.CustomValidationException;
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
    public String listByPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "sortField", defaultValue = "id") String sortField,
                             @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
                             @RequestParam(value = "limit", defaultValue = "20") Integer limit,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             Model model) {
        customerService.listByPage(page, sortField, sortDir, limit, keyword, model);

        return "customers/customers";
    }

    @GetMapping("edit/{id}")
    public String editCustomer(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) throws CustomerNotFoundException {
        CustomerDTO customer = customerService.get(id);
        List<Country> countries = customerService.listAllCountries();

        model.addAttribute("customer", customer);
        model.addAttribute("listCountries", countries);
        model.addAttribute("pageTitle", "Edit Customer");

        return "customers/customer_form";
    }

    @PostMapping("save")
    public String saveCustomer(@Valid @ModelAttribute("customer") CustomerDTO customer,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) throws CustomerNotFoundException {
        if (result.hasErrors()) {
            List<Country> countries = customerService.listAllCountries();

            model.addAttribute("listCountries", countries);
            model.addAttribute("pageTitle", "Edit Customer");

            return "customers/customer_form";
        }

        try {
            customerService.save(customer);

            redirectAttributes.addFlashAttribute("message", "Customer saved successfully");
            return "redirect:/customers";
        } catch (CustomValidationException ex) {
            List<Country> countries = customerService.listAllCountries();

            model.addAttribute("customer", customer);
            model.addAttribute("listCountries", countries);
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("pageTitle", "Edit Customer");

            return "customers/customer_form";
        }
    }

    @GetMapping("delete/{id}")
    public String deleteCustomer(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) throws CustomerNotFoundException {
        customerService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Customer deleted successfully");

        return "redirect:/customers";
    }

    @GetMapping("{id}/enabled/{status}")
    public String updateCustomerEnabledStatus(@PathVariable("id") Integer id,
                                              @PathVariable("status") boolean enabled,
                                              RedirectAttributes redirectAttributes) throws CustomerNotFoundException {
        customerService.updateCustomerEnabledStatus(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        String message = "The Customer ID %d has been ".formatted(id) + status;
        redirectAttributes.addFlashAttribute("message", message);

        return "redirect:/customers";
    }

    @GetMapping("detail/{id}")
    public String viewCustomer(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) throws CustomerNotFoundException {
        CustomerDTO customer = customerService.get(id);
        model.addAttribute("customer", customer);

        return "customers/customer_detail_modal";
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public String handleBrandNotFoundException(CustomerNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        return "redirect:/customers";
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        StringTrimmerEditor editor = new StringTrimmerEditor(true);
        webDataBinder.registerCustomEditor(String.class, editor);
    }
}
