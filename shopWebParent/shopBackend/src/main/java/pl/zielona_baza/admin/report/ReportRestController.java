package pl.zielona_baza.admin.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportRestController {
    private final MasterOrderReportService masterOrderReportService;
    private final OrderDetailReportService orderDetailReportService;

    public ReportRestController(MasterOrderReportService masterOrderReportService, OrderDetailReportService orderDetailReportService) {
        this.masterOrderReportService = masterOrderReportService;
        this.orderDetailReportService = orderDetailReportService;
    }

    @GetMapping("/sales_by_date/{period}")
    public List<ReportItem> getReportDataByDatePeriod(
            @PathVariable(name = "period") String period
    ) {
        return switch (period) {
            case "last_28_days" -> masterOrderReportService.getReportDataLast28Days(ReportType.DAY);
            case "last_6_months" -> masterOrderReportService.getReportDataLast6Months(ReportType.MONTH);
            case "last_year" -> masterOrderReportService.getReportDataLastYear(ReportType.MONTH);
            default -> masterOrderReportService.getReportDataLast7Days(ReportType.DAY);
        };
    }

    @GetMapping("/sales_by_date/{startDate}/{endDate}")
    public List<ReportItem> getReportDataByDatePeriod(
            @PathVariable(name = "startDate") String startDate,
            @PathVariable(name = "endDate") String endDate
    ) throws ParseException {
        return masterOrderReportService.getReportDataByDateRange(startDate, endDate, ReportType.DAY);
    }

    @GetMapping("/{groupBy}/{startDate}/{endDate}")
    public List<ReportItem> getReportDataByCategoryOrProductDatePeriod(
            @PathVariable(name = "groupBy") String groupBy,
            @PathVariable(name = "startDate") String startDate,
            @PathVariable(name = "endDate") String endDate
    ) throws ParseException {
        ReportType reportType = ReportType.valueOf(groupBy.toUpperCase());
        return orderDetailReportService.getReportDataByDateRange(startDate, endDate, reportType);
    }

    @GetMapping("/{groupBy}/{period}")
    public List<ReportItem> getReportDataByCategoryOrProduct(
            @PathVariable(name = "groupBy") String groupBy,
            @PathVariable(name = "period") String period
    ) {
        ReportType reportType = ReportType.valueOf(groupBy.toUpperCase());

        return switch (period) {
            case "last_28_days" -> orderDetailReportService.getReportDataLast28Days(reportType);
            case "last_6_months" -> orderDetailReportService.getReportDataLast6Months(reportType);
            case "last_year" -> orderDetailReportService.getReportDataLastYear(reportType);
            default -> orderDetailReportService.getReportDataLast7Days(reportType);
        };
    }
}
