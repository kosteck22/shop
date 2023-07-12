package pl.zielona_baza.admin.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRestResponse {
    private Integer orderId;
    private String status;

    public OrderRestResponse(Integer orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }
}
