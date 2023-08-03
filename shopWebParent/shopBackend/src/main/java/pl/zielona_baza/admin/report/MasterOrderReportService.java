package pl.zielona_baza.admin.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.zielona_baza.admin.order.OrderRepository;
import pl.zielona_baza.common.entity.order.Order;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class MasterOrderReportService extends AbstractReportService{
    private final OrderRepository orderRepository;

    public MasterOrderReportService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    protected List<ReportItem> getReportDataByDateRangeInternal(Date startTime, Date endTime, ReportType reportType) {
        List<Order> listOrders = orderRepository.findByOrderTimeBetween(startTime, endTime);

        List<ReportItem> listReportItems = createReportData(startTime, endTime, reportType);
        calculateSalesForReportData(listOrders, listReportItems);

        return listReportItems;
    }

    private void calculateSalesForReportData(List<Order> listOrders, List<ReportItem> listReportItems) {
        listReportItems.forEach(reportItem ->
                listOrders.stream()
                    .filter(order -> reportItem.getIdentifier().equals(dateFormatter.format(order.getOrderTime())))
                    .forEach(order -> {
                        reportItem.addGrossSales(order.getTotal());
                        reportItem.addNetSales(order.getSubtotal() - order.getProductCost().floatValue());
                        reportItem.increaseOrdersCount();
                    })
        );
    }

    private List<ReportItem> createReportData(Date startTime, Date endTime, ReportType reportType) {
        List<ReportItem> listReportItems = new ArrayList<>();

        Calendar startDate = Calendar.getInstance();
        startDate.setTime(startTime);

        Calendar endDate = Calendar.getInstance();
        endDate.setTime(endTime);

        Date currentDate = startDate.getTime();
        String dateString = dateFormatter.format(currentDate);
        listReportItems.add(new ReportItem(dateString));

        do {
            if (reportType.equals(ReportType.DAY)) {
                startDate.add(Calendar.DAY_OF_MONTH, 1);
            } else if (reportType.equals(ReportType.MONTH)) {
                startDate.add(Calendar.MONTH, 1);
            }
            currentDate = startDate.getTime();
            dateString = dateFormatter.format(currentDate);

            listReportItems.add(new ReportItem(dateString));
        } while (startDate.before(endDate));

        return listReportItems;
    }
}
