package pl.zielona_baza.site.checkout.paypal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pl.zielona_baza.site.setting.PaymentSettingBag;
import pl.zielona_baza.site.setting.SettingService;

import java.util.Arrays;

@Component
public class PayPalService {
    public static final String GET_ORDER_API = "/v2/checkout/orders/";
    private final SettingService settingService;

    public PayPalService(SettingService settingService) {
        this.settingService = settingService;
    }

    public boolean validateOrder(String orderId) throws PayPalApiException {
        PayPalOrderResponse orderResponse = getOrderDetails(orderId);

        return orderResponse.validate(orderId);
    }

    private PayPalOrderResponse getOrderDetails(String orderId) throws PayPalApiException {
        ResponseEntity<PayPalOrderResponse> response = makeRequest(orderId);

        HttpStatus statusCode = response.getStatusCode();

        if (!statusCode.equals(HttpStatus.OK)) {
            throwExceptionForNonOKResponse(statusCode);
        }

        return response.getBody();
    }

    private ResponseEntity<PayPalOrderResponse> makeRequest(String orderId) {
        PaymentSettingBag paymentSettings = settingService.getPaymentSettings();
        String baseURL = paymentSettings.getURL();
        String requestURL = baseURL + GET_ORDER_API + orderId;

        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Accept-Language", "en_US");
        headers.setBasicAuth(paymentSettings.getClientID(), paymentSettings.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.exchange(requestURL, HttpMethod.GET, request, PayPalOrderResponse.class);
    }

    private void throwExceptionForNonOKResponse(HttpStatus statusCode) throws PayPalApiException {
        String msg = null;

        switch (statusCode) {
            case NOT_FOUND:
                msg = "Order ID not found";
            case BAD_REQUEST:
                msg = "Bad Request to PayPal Checkout API";
            case INTERNAL_SERVER_ERROR:
                msg = "PayPal server error";
            default:
                msg = "PayPal returned non-OK status code";
        }

        throw new PayPalApiException(msg);
    }
}
