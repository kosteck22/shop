package pl.zielona_baza.admin.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.zielona_baza.admin.order.OrderDetailRepository;
import pl.zielona_baza.common.entity.order.OrderDetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderDetailReportService extends AbstractReportService {

    @Autowired private OrderDetailRepository orderDetailRepository;

    @Override
    protected List<ReportItem> getReportDataByDateRangeInternal(Date startDate, Date endDate, ReportType reportType) {
        List<OrderDetail> listOrderDetails = null;

        if (reportType.equals(ReportType.CATEGORY)) {
            listOrderDetails = orderDetailRepository.findWithCategoryAndTimeBetween(startDate, endDate);
        } else if (reportType.equals(ReportType.PRODUCT)) {
            listOrderDetails = orderDetailRepository.findWithProductAndTimeBetween(startDate, endDate);
        }

        List<ReportItem> listReportItems = new ArrayList<>();

        assert listOrderDetails != null;
        listOrderDetails.forEach(orderDetail -> {
            String identifier = "";
            float grossSales = orderDetail.getSubtotal() + orderDetail.getShippingCost();
            float netSales = orderDetail.getSubtotal() - orderDetail.getProductCost();
            int quantity = orderDetail.getQuantity();

            if (reportType.equals(ReportType.CATEGORY)) {
                identifier = orderDetail.getProduct().getCategory().getName();
            } else if (reportType.equals(ReportType.PRODUCT)) {
                identifier = orderDetail.getProduct().getShortName();
            }

            String finalIdentifier = identifier;
            Optional<ReportItem> foundReportItem = listReportItems.stream()
                    .filter(ri -> ri.getIdentifier().equals(finalIdentifier))
                    .findFirst();

            if (foundReportItem.isEmpty()) {
                listReportItems.add(new ReportItem(identifier, grossSales, netSales, quantity));
            } else {
                ReportItem reportItemFromList = foundReportItem.get();
                reportItemFromList.addGrossSales(grossSales);
                reportItemFromList.addNetSales(netSales);
                reportItemFromList.increaseProductCount(quantity);
            }
        });

        return listReportItems;
    }
}
