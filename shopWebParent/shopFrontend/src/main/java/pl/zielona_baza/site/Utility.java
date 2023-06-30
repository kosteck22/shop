package pl.zielona_baza.site;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.site.customer.CustomerNotFoundException;
import pl.zielona_baza.site.customer.CustomerService;
import pl.zielona_baza.site.security.oauth.CustomerOAuth2User;
import pl.zielona_baza.site.setting.CurrencySettingBag;
import pl.zielona_baza.site.setting.EmailSettingBag;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Properties;

public class Utility {
    public static String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();

        return siteURL.replace(request.getServletPath(), "");
    }

    public static JavaMailSenderImpl prepareMailSender(EmailSettingBag emailSettingBag) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(emailSettingBag.getHost());
        mailSender.setPort(emailSettingBag.getPort());
        mailSender.setUsername(emailSettingBag.getUsername());
        mailSender.setPassword(emailSettingBag.getPassword());

        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.smtp.auth", emailSettingBag.getSmtpAuth());
        mailProperties.setProperty("mail.smtp.starttls.enable", emailSettingBag.getSmtpSecured());

        mailSender.setJavaMailProperties(mailProperties);

        return mailSender;
    }

    public static String getEmailOfAuthenticatedCustomer(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal == null) return null;

        String customerEmail = null;

        if (userPrincipal instanceof UsernamePasswordAuthenticationToken ||
                userPrincipal instanceof RememberMeAuthenticationToken) {
            customerEmail = userPrincipal.getName();
        } else if (userPrincipal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) userPrincipal;
            CustomerOAuth2User oAuth2User = (CustomerOAuth2User) oauth2Token.getPrincipal();
            customerEmail = oAuth2User.getEmail();
        }

        return customerEmail;
    }

    public static String formatCurrency(float amount, CurrencySettingBag currencySettingBag) {
        String symbol = currencySettingBag.getSymbol();
        String symbolPosition = currencySettingBag.getSymbolPosition();
        String decimalPointType = currencySettingBag.getDecimalPointType();
        String thousandsPointType = currencySettingBag.getThousandsPointType();
        int decimalDigits = currencySettingBag.getDecimalDigits();

        String pattern = symbolPosition.equals("Before price") ? symbol : "";
        pattern += "###,###";

        if (decimalDigits > 0) {
            pattern += ".";
            for (int count = 1; count <= decimalDigits; count++) {
                pattern += "#";
            }
        }

        pattern += symbolPosition.equals("After price") ? symbol : "";

        char thousandSeparator = thousandsPointType.equals("POINT") ? '.' : ',';
        char decimalSeparator = decimalPointType.equals("POINT") ? '.' : ',';

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setGroupingSeparator(thousandSeparator);
        decimalFormatSymbols.setDecimalSeparator(decimalSeparator);

        DecimalFormat formatter = new DecimalFormat(pattern, decimalFormatSymbols);

        return formatter.format(amount);
    }
}
