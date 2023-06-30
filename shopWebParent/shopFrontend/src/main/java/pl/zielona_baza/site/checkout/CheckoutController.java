package pl.zielona_baza.site.checkout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import pl.zielona_baza.common.entity.Address;
import pl.zielona_baza.common.entity.CartItem;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.ShippingRate;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.PaymentMethod;
import pl.zielona_baza.site.ControllerHelper;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.address.AddressService;
import pl.zielona_baza.site.checkout.paypal.PayPalApiException;
import pl.zielona_baza.site.checkout.paypal.PayPalService;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;
import pl.zielona_baza.site.order.OrderService;
import pl.zielona_baza.site.setting.CurrencySettingBag;
import pl.zielona_baza.site.setting.EmailSettingBag;
import pl.zielona_baza.site.setting.PaymentSettingBag;
import pl.zielona_baza.site.setting.SettingService;
import pl.zielona_baza.site.shipping.ShippingRateService;
import pl.zielona_baza.site.shoppingcart.ShoppingCartService;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;

@Controller
public class CheckoutController {

    @Autowired private CheckoutService checkoutService;

    @Autowired private ControllerHelper controllerHelper;

    @Autowired private AddressService addressService;

    @Autowired private ShippingRateService shippingRateService;

    @Autowired private ShoppingCartService shoppingCartService;

    @Autowired private OrderService orderService;

    @Autowired private SettingService settingService;

    @Autowired private PayPalService payPalService;

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpServletRequest request) {
        Customer customer = controllerHelper.getAuthenticatedCustomer(request).get();

        Address defaultAddress = addressService.getDefaultAddress(customer);
        Optional<ShippingRate> shippingRate;

        if (defaultAddress != null) {
            model.addAttribute("shippingAddress", defaultAddress.toString());
            shippingRate = shippingRateService.getShippingRateForAddress(defaultAddress);
        } else {
            model.addAttribute("shippingAddress", customer.getAddress());
            shippingRate = shippingRateService.getShippingRateForCustomer(customer);
        }

        if (shippingRate.isEmpty()) {
            return "redirect:/cart";
        }

        List<CartItem> cartItems = shoppingCartService.listCartItems(customer);
        CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(cartItems, shippingRate.get());

        String currencyCode = settingService.getCurrencyCode();
        PaymentSettingBag paymentSettings = settingService.getPaymentSettings();
        String paypalClientId = paymentSettings.getClientID();

        model.addAttribute("paypalClientId", paypalClientId);
        model.addAttribute("currencyCode", currencyCode);
        model.addAttribute("customer", customer);
        model.addAttribute("checkoutInfo", checkoutInfo);
        model.addAttribute("cartItems", cartItems);

        return "checkout/checkout";
    }

    @PostMapping("/place_order")
    public String placeOrder(HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        String paymentType = request.getParameter("paymentMethod");
        PaymentMethod paymentMethod = PaymentMethod.valueOf(paymentType);
        Customer customer = controllerHelper.getAuthenticatedCustomer(request).get();

        Address defaultAddress = addressService.getDefaultAddress(customer);
        Optional<ShippingRate> shippingRate;

        if (defaultAddress != null) {
                 shippingRate = shippingRateService.getShippingRateForAddress(defaultAddress);
        } else {
            shippingRate = shippingRateService.getShippingRateForCustomer(customer);
        }

        List<CartItem> cartItems = shoppingCartService.listCartItems(customer);
        CheckoutInfo checkoutInfo = checkoutService.prepareCheckout(cartItems, shippingRate.get());

        Order order = orderService.createOrder(customer, defaultAddress, cartItems, paymentMethod, checkoutInfo);
        shoppingCartService.deleteByCustomer(customer);
        sendOrderConfirmationEmail(request, order);

        return "checkout/order_completed";
    }

    private void sendOrderConfirmationEmail(HttpServletRequest request, Order order) throws MessagingException, UnsupportedEncodingException {
        EmailSettingBag emailSettings = settingService.getEmailSettings();
        JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);
        mailSender.setDefaultEncoding("utf-8");

        String toAddress = order.getCustomer().getEmail();
        String subject = emailSettings.getOrderConfirmationSubject();
        String content = emailSettings.getOrderConfirmationContent();

        subject = subject.replace("[[orderId]]", String.valueOf(order.getId()));

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
        helper.setTo(toAddress);
        helper.setSubject(subject);

        DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss E, dd MMM yyyy");
        String orderTime = dateFormatter.format(order.getOrderTime());

        CurrencySettingBag currencySettings = settingService.getCurrencySettings();
        String totalAmount = Utility.formatCurrency(order.getTotal(), currencySettings);

        content = content.replace("[[name]]", order.getCustomer().getFullName());
        content = content.replace("[[orderId]]", String.valueOf(order.getId()));
        content = content.replace("[[orderTime]]", orderTime);
        content = content.replace("[[shippingAddress]]", order.getShippingAddress());
        content = content.replace("[[total]]", totalAmount);
        content = content.replace("[[paymentMethod]]", order.getPaymentMethod().toString());

        helper.setText(content, true);
        mailSender.send(mimeMessage);
    }

    @PostMapping("/process_paypal_order")
    public String processPayPalOrder(HttpServletRequest request, Model model) throws MessagingException, UnsupportedEncodingException {
        String orderId = request.getParameter("orderId");
        String pageTitle = "Checkout Failure";
        String message = null;

        try {
            if (payPalService.validateOrder(orderId)) {
                return placeOrder(request);
            } else {
                message = "ERROR: Transaction could not be completed because order information is invalid";
            }
        } catch (PayPalApiException e) {
            message = "ERROR: Transaction failed due to error: " + e.getMessage();
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("message", message);

        return "message";
    }
}
