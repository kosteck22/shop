package pl.zielona_baza.site.customer;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.zielona_baza.common.entity.Country;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.site.Utility;
import pl.zielona_baza.site.security.CustomerUserDetails;
import pl.zielona_baza.site.security.oauth.CustomerOAuth2User;
import pl.zielona_baza.site.setting.EmailSettingBag;
import pl.zielona_baza.site.setting.SettingService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

@Controller
public class CustomerController {
    private final CustomerService customerService;
    private final SettingService settingService;

    public CustomerController(CustomerService customerService, SettingService settingService) {
        this.customerService = customerService;
        this.settingService = settingService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        List<Country> listCountries = customerService.listAllCountries();

        model.addAttribute("listCountries", listCountries);
        model.addAttribute("pageTitle", "Customer Registration");
        model.addAttribute("customer", new Customer());

        return "register/register_form";
    }

    @PostMapping("/create_customer")
    public String createCustomer(Customer customer, Model model, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        Customer savedCustomer = customerService.registerCustomer(customer);
        sendVerificationEmail(request, savedCustomer);

        model.addAttribute("pageTitle", "Registration Succeeded!");

        return "/register/register_success";
    }

    private void sendVerificationEmail(HttpServletRequest request, Customer customer) throws MessagingException, UnsupportedEncodingException {
        EmailSettingBag emailSettings = settingService.getEmailSettings();
        JavaMailSenderImpl mailSender = Utility.prepareMailSender(emailSettings);

        String toAddress = customer.getEmail();
        String subject = emailSettings.getCustomerVerifySubject();
        String content = emailSettings.getCustomerVerifyContent();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailSettings.getFromAddress(), emailSettings.getSenderName());
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", customer.getFullName());
        String verifyURL = Utility.getSiteURL(request) + "/verify?code=" + customer.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("code") String verifyCode) {
        boolean isVerified = customerService.verify(verifyCode);

        return "register/" + (isVerified ? "verify_success" : "verify_fail");
    }

    @GetMapping("/account_details")
    public String viewAccountDetails(Model model, HttpServletRequest request,
                                     @RequestParam(name = "redirect", required = false) String redirect) {
        String email = Utility.getEmailOfAuthenticatedCustomer(request);
        Optional<Customer> customerByEmail = customerService.getCustomerByEmail(email);
        List<Country> countries = customerService.listAllCountries();

        model.addAttribute("customer", customerByEmail.get());
        model.addAttribute("listCountries", countries);
        model.addAttribute("redirect", redirect);

        return "customer/account_form";
    }

    @PostMapping("/update_account_details")
    public String updateAccountDetails(HttpServletRequest request,
                                       Customer customer,
                                       RedirectAttributes redirectAttributes,
                                       @RequestParam(name = "redirect", required = false) String redirect) {
        customerService.update(customer);

        redirectAttributes.addFlashAttribute("message", "Your account details have been updated.");
        updateNameForAuthenticatedCustomer(request, customer);

        String redirectURL = "redirect:/account_details";

        if ("address_book".equals(redirect)) {
            redirectURL = "redirect:/address_book";
        } else if ("cart".equals(redirect)) {
            redirectURL = "redirect:/cart";
        } else if ("checkout".equals(redirect)) {
            redirectURL = "redirect:/address_book?redirect=checkout";
        }

        return redirectURL;
    }

    private void updateNameForAuthenticatedCustomer(HttpServletRequest request, Customer customer) {
        Object userPrincipal = request.getUserPrincipal();
        if (userPrincipal instanceof UsernamePasswordAuthenticationToken ||
                userPrincipal instanceof RememberMeAuthenticationToken) {
            CustomerUserDetails userDetails = getCustomerUserDetailsObject(userPrincipal);
            Customer authenticatedCustomer = userDetails.getCustomer();
            authenticatedCustomer.setFirstName(customer.getFirstName());
            authenticatedCustomer.setLastName(customer.getLastName());

        } else if (userPrincipal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) userPrincipal;
            CustomerOAuth2User oAuth2User = (CustomerOAuth2User) oauth2Token.getPrincipal();
            String fullName = customer.getFullName();

            oAuth2User.setFullName(fullName);
        }
    }

    private CustomerUserDetails getCustomerUserDetailsObject(Object principal) {
        CustomerUserDetails customerUserDetails = null;

        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            customerUserDetails = (CustomerUserDetails) token.getPrincipal();
        } else if (principal instanceof RememberMeAuthenticationToken) {
            RememberMeAuthenticationToken token = (RememberMeAuthenticationToken) principal;
            customerUserDetails = (CustomerUserDetails) token.getPrincipal();
        }

        return customerUserDetails;
    }
}
