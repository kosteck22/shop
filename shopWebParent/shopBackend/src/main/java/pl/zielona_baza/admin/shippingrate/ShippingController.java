package pl.zielona_baza.admin.shippingrate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.paging.PagingAndSortingParam;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.ShippingRate;

import java.util.List;

@Controller
@RequestMapping("/shipping_rates")
public class ShippingController {
    private final String defaultRedirectURL = "redirect:/shipping_rates/page/1?sortField=country&sortDir=asc";
    private final ShippingRateService shippingRateService;

    public ShippingController(ShippingRateService shippingRateService) {
        this.shippingRateService = shippingRateService;
    }

    @GetMapping
    public String listFirstPage() {
        return defaultRedirectURL;
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(name = "sortField", required = false) String sortField,
                             @RequestParam(name = "sortDir", required = false) String sortDir,
                             @RequestParam(name = "limit", required = false) Integer limit,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             Model model) {
        shippingRateService.listByPage(pageNum, sortField, sortDir, limit, keyword, model);

        return "shipping_rates/shipping_rates";
    }

    @GetMapping("/new")
    public String newRate(Model model) {
        List<Country> listCountries = shippingRateService.listAllCountries();

        model.addAttribute("rate", new ShippingRate());
        model.addAttribute("listCountries", listCountries);
        model.addAttribute("pageTitle", "New Rate");

        return "shipping_rates/shipping_rate_form";
    }

    @PostMapping("/save")
    public String saveRate(ShippingRate rate, RedirectAttributes redirectAttributes) {
        try {
            shippingRateService.save(rate);
            redirectAttributes.addFlashAttribute("message", "The shipping rate has been saved successfully.");
        } catch (ShippingRateAlreadyExistsException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return defaultRedirectURL;
    }

    @GetMapping("/edit/{id}")
    public String editRate(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ShippingRate rate = shippingRateService.get(id);
            List<Country> listCountries = shippingRateService.listAllCountries();

            model.addAttribute("rate", rate);
            model.addAttribute("listCountries", listCountries);
            model.addAttribute("pageTitle", "Edit Rate");

            return "shipping_rates/shipping_rate_form";
        } catch (ShippingRateNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return defaultRedirectURL;
        }
    }

    @GetMapping("/cod/{id}/enabled/{supported}")
    public String updateCODSupport(@PathVariable(name = "id") Integer id,
                                   @PathVariable(name = "supported") Boolean supported,
                                   RedirectAttributes redirectAttributes) {
        try {
            shippingRateService.updateCODSupport(id, supported);
            redirectAttributes.addFlashAttribute("message", "COD support has been changed");
        } catch (ShippingRateNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return defaultRedirectURL;
    }

    @GetMapping("/delete/{id}")
    public String deleteRate(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            shippingRateService.delete(id);
            redirectAttributes.addFlashAttribute("message", "The shipping rate has been deleted.");
        } catch (ShippingRateNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return defaultRedirectURL;
    }
}
