package pl.zielona_baza.admin.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String listFirstPage(Model model, @AuthenticationPrincipal ShopUserDetails loggedUser) {
        return listByPage(1, "orderTime", "desc", 10, null, model, loggedUser);
    }

    @GetMapping("/page/{pageNum}")
    public String listByPage(@PathVariable("pageNum") Integer pageNum,
                             @RequestParam(name = "sortField", required = false) String sortField,
                             @RequestParam(name = "sortDir", required = false) String sortDir,
                             @RequestParam(name = "limit", required = false) Integer limit,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             Model model,
                             @AuthenticationPrincipal ShopUserDetails loggedUser) {
        orderService.listByPage(pageNum, sortField, sortDir, limit, keyword, model);
       /* loadCurrencySetting(request);
*/
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

            boolean isVisibleForAdminOrSalesperson = false;

            if (loggedUser.hasRole("Admin") || loggedUser.hasRole("Salesperson")) {
                isVisibleForAdminOrSalesperson = true;
            }

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

            redirectAttributes.addFlashAttribute("message", "The order ID " + id + " has been deleted.");
        } catch (OrderNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return defaultRedirectURL;
    }
    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes,
                            HttpServletRequest request) {
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
    public String saveOrder(Order order, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        updateProductDetails(order, request);
        updateOrderTracks(order, request);

        orderService.save(order);

        redirectAttributes.addFlashAttribute("message", "The order ID " + order.getId() +
                " has been updated successfully");

        return defaultRedirectURL;
    }

    private void updateOrderTracks(Order order, HttpServletRequest request) {
        String[] trackIds = request.getParameterValues("trackId");
        String[] trackStatuses = request.getParameterValues("trackStatus");
        String[] trackDates = request.getParameterValues("trackDate");
        String[] trackNotes = request.getParameterValues("trackNotes");

        List<OrderTrack> orderTracks = order.getOrderTracks();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

        for (int i = 0; i < trackIds.length; i++) {
            Integer trackId = Integer.parseInt(trackIds[i]);

            OrderTrack trackRecord = new OrderTrack();
                trackRecord.setId(trackId > 0 ? trackId : null);
                trackRecord.setOrder(order);
                trackRecord.setStatus(OrderStatus.valueOf(trackStatuses[i]));
                trackRecord.setNotes(trackNotes[i]);
                try {
                trackRecord.setUpdatedTime(dateFormatter.parse(trackDates[i]));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                orderTracks.add(trackRecord);
        }
    }

    private void updateProductDetails(Order order, HttpServletRequest request) {
        String[] detailIds = request.getParameterValues("detailId");
        String[] productIds = request.getParameterValues("productId");
        String[] productDetailCosts = request.getParameterValues("productDetailCost");
        String[] quantities = request.getParameterValues("quantity");
        String[] productPrices = request.getParameterValues("productPrice");
        String[] productSubtotals = request.getParameterValues("productSubtotal");
        String[] productShipCosts = request.getParameterValues("productShipCost");

        Set<OrderDetail> orderDetails = order.getOrderDetails();

        for (int i = 0; i < detailIds.length; i++) {
            Integer detailId = Integer.parseInt(detailIds[i]);

            OrderDetail orderDetail = new OrderDetail();
                orderDetail.setId(detailId > 0 ? detailId : null);
                orderDetail.setOrder(order);
                orderDetail.setProduct(new Product(Integer.parseInt(productIds[i])));
                orderDetail.setProductCost(Float.parseFloat(productDetailCosts[i]));
                orderDetail.setQuantity(Integer.parseInt(quantities[i]));
                orderDetail.setUnitPrice(Float.parseFloat(productPrices[i]));
                orderDetail.setSubtotal(Float.parseFloat(productSubtotals[i]));
                orderDetail.setShippingCost(Float.parseFloat(productShipCosts[i]));

            orderDetails.add(orderDetail);
        }
    }
}
