package pl.zielona_baza.admin.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.admin.paging.PagingAndSortingHelper;
import pl.zielona_baza.admin.paging.PagingAndSortingParam;
import pl.zielona_baza.admin.security.ShopUserDetails;
import pl.zielona_baza.admin.setting.SettingService;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderDetail;
import pl.zielona_baza.common.entity.order.OrderStatus;
import pl.zielona_baza.common.entity.order.OrderTrack;
import pl.zielona_baza.common.entity.product.Product;
import pl.zielona_baza.common.entity.setting.Setting;
import pl.zielona_baza.common.exception.OrderNotFoundException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final String defaultRedirectURL = "redirect:/orders/page/1?sortField=orderTime&sortDir=desc";
    private final OrderService orderService;

    private final SettingService settingService;

    public OrderController(OrderService orderService, SettingService settingService) {
        this.orderService = orderService;
        this.settingService = settingService;
    }

    @GetMapping
    public String listFirstPage(Model model, @AuthenticationPrincipal ShopUserDetails loggedUser, HttpServletRequest request) {
        return listByPage(1, "orderTime", "desc", 10, null, model, loggedUser, request);
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(name = "sortField", required = false) String sortField,
                             @RequestParam(name = "sortDir", required = false) String sortDir,
                             @RequestParam(name = "limit", required = false) Integer limit,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             Model model,
                             @AuthenticationPrincipal ShopUserDetails loggedUser,
                             HttpServletRequest request) {
        orderService.listByPage(pageNum, sortField, sortDir, limit, keyword, model);
        loadCurrencySetting(request);
        if (loggedUser.hasRole("Shipper") &&
           !loggedUser.hasRole("Admin") &&
           !loggedUser.hasRole("Salesperson")) {
            return "orders/orders_shipper";
        }

        return "orders/orders";
    }

    private void loadCurrencySetting(HttpServletRequest request) {
        List<Setting> currencySettings = settingService.getCurrencySettings();

        currencySettings.forEach(s -> request.setAttribute(s.getKey(), s.getValue()));
    }

    @GetMapping("/detail/{id}")
    public String viewOrderDetails(@PathVariable("id") Integer id,
                                   Model model,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request,
                                   @AuthenticationPrincipal ShopUserDetails loggedUser) {
        try {
            Order order = orderService.get(id);
            loadCurrencySetting(request);

            boolean isVisibleForAdminOrSalesperson = loggedUser.hasRole("Admin") || loggedUser.hasRole("Salesperson");

            model.addAttribute("order", order);
            model.addAttribute("isVisibleForAdminOrSalesperson", isVisibleForAdminOrSalesperson);

            return "orders/order_details_modal";
        } catch (OrderNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return defaultRedirectURL;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable("id") Integer id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.delete(id);

            redirectAttributes.addFlashAttribute("message", "The order ID %d has been deleted.".formatted(id));
        } catch (OrderNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return defaultRedirectURL;
    }

    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.get(id);

            List<Country> listCountries = orderService.listAllCountries();

            model.addAttribute("pageTitle", "Edit Order");
            model.addAttribute("order", order);
            model.addAttribute("listCountries", listCountries);

            return "orders/order_form";
        } catch (OrderNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());

            return defaultRedirectURL;
        }
    }

    @PostMapping("/save")
    public String saveOrder(@ProductDetailsParam ProductDetailsParamHelper productDetailsHelper,
                            @OrderTracksParam OrderTrackParamHelper orderTrackParamHelper,
                            @Valid Order order,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest request) {

        if (result.hasErrors()) {
            orderService.restoreProductDetailsAndOrderTracks(order, productDetailsHelper, orderTrackParamHelper);

            List<Country> listCountries = orderService.listAllCountries();

            model.addAttribute("pageTitle", "Edit Order");
            model.addAttribute("order", order);
            model.addAttribute("listCountries", listCountries);

            return "orders/order_form";
        }

        try {
            orderService.save(order, productDetailsHelper, orderTrackParamHelper);

            redirectAttributes.addFlashAttribute("message", "The order ID " + order.getId() +
                    " has been updated successfully");
        } catch (Exception ex) {

        }

        return defaultRedirectURL;
    }
}
