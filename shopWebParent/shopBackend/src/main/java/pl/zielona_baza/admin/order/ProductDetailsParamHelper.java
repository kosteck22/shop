package pl.zielona_baza.admin.order;

import pl.zielona_baza.admin.MethodsUtil;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderDetail;
import pl.zielona_baza.common.entity.product.Product;

import java.util.Set;

public class ProductDetailsParamHelper {
    private String[] detailIds;
    private String[] productIds;
    private String[] productDetailCosts;
    private String[] quantities;
    private String[] productPrices;
    private String[] productSubtotals;
    private String[] productShipCosts;

    public ProductDetailsParamHelper(String[] detailIds,
                                     String[] productIds,
                                     String[] productDetailCosts,
                                     String[] quantities,
                                     String[] productPrices,
                                     String[] productSubtotals,
                                     String[] productShipCosts) {
        this.detailIds = detailIds;
        this.productIds = productIds;
        this.productDetailCosts = productDetailCosts;
        this.quantities = quantities;
        this.productPrices = productPrices;
        this.productSubtotals = productSubtotals;
        this.productShipCosts = productShipCosts;
    }

    public String[] getDetailIds() {
        return detailIds;
    }

    public void setDetailIds(String[] detailIds) {
        this.detailIds = detailIds;
    }

    public String[] getProductIds() {
        return productIds;
    }

    public void setProductIds(String[] productIds) {
        this.productIds = productIds;
    }

    public String[] getProductDetailCosts() {
        return productDetailCosts;
    }

    public void setProductDetailCosts(String[] productDetailCosts) {
        this.productDetailCosts = productDetailCosts;
    }

    public String[] getQuantities() {
        return quantities;
    }

    public void setQuantities(String[] quantities) {
        this.quantities = quantities;
    }

    public String[] getProductPrices() {
        return productPrices;
    }

    public void setProductPrices(String[] productPrices) {
        this.productPrices = productPrices;
    }

    public String[] getProductSubtotals() {
        return productSubtotals;
    }

    public void setProductSubtotals(String[] productSubtotals) {
        this.productSubtotals = productSubtotals;
    }

    public String[] getProductShipCosts() {
        return productShipCosts;
    }

    public void setProductShipCosts(String[] productShipCosts) {
        this.productShipCosts = productShipCosts;
    }

    public void setOrderDetails(Order order) {
        MethodsUtil.checkParallelArrays(detailIds, productIds, productDetailCosts, quantities, productPrices, productSubtotals,
                productShipCosts);

        Set<OrderDetail> orderDetails = order.getOrderDetails();

        for (int i = 0; i < detailIds.length; i++) {
            int detailId = Integer.parseInt(detailIds[i]);

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
