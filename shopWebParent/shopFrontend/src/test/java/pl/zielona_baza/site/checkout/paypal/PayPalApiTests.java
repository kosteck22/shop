package pl.zielona_baza.site.checkout.paypal;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;


public class PayPalApiTests {
    public static final String BASE_URL = "https://api.sandbox.paypal.com";
    public static final String GET_ORDER_API = "/v2/checkout/orders/";
    public static final String CLIENT_ID = "ATjA7KmUxGFuIcaOCQ7o7G1P45uPcyL_hdPnC6aQGmvhNaudALhlXKruNrcbSE3MfGKs2lYrQptlXFqN";
    public static final String CLIENT_SECRET = "EAe14Yu8QvJQW2qwaiS3TmcGXtfbpacv-bt67q3Iwx997D6Y2lSqT0sJsA0pEgdVRMMvy8qZotLkp8Rl";

    @Test
    public void testGetOrderDetails() {
        String orderId = "123Abc";
        String requestURL = BASE_URL + GET_ORDER_API + orderId;

        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Accept-Language", "en_US");
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<PayPalOrderResponse> response = restTemplate
                .exchange(requestURL, HttpMethod.GET, request, PayPalOrderResponse.class);

        PayPalOrderResponse orderResponse = response.getBody();
    }
}
