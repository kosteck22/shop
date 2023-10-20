package pl.zielona_baza.admin.order;

import pl.zielona_baza.admin.MethodsUtil;
import pl.zielona_baza.common.entity.order.Order;
import pl.zielona_baza.common.entity.order.OrderStatus;
import pl.zielona_baza.common.entity.order.OrderTrack;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrderTrackParamHelper {
    private String[] trackIds;
    private String[] trackStatuses;
    private String[] trackDates;
    private String[] trackNotes;

    public OrderTrackParamHelper(String[] trackIds, String[] trackStatuses, String[] trackDates, String[] trackNotes) {
        this.trackIds = trackIds;
        this.trackStatuses = trackStatuses;
        this.trackDates = trackDates;
        this.trackNotes = trackNotes;
    }

    public String[] getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(String[] trackIds) {
        this.trackIds = trackIds;
    }

    public String[] getTrackStatuses() {
        return trackStatuses;
    }

    public void setTrackStatuses(String[] trackStatuses) {
        this.trackStatuses = trackStatuses;
    }

    public String[] getTrackDates() {
        return trackDates;
    }

    public void setTrackDates(String[] trackDates) {
        this.trackDates = trackDates;
    }

    public String[] getTrackNotes() {
        return trackNotes;
    }

    public void setTrackNotes(String[] trackNotes) {
        this.trackNotes = trackNotes;
    }

    public void setOrderTracks(Order order) {
        MethodsUtil.checkParallelArrays(trackIds, trackStatuses, trackNotes, trackDates);

        List<OrderTrack> orderTracks = order.getOrderTracks();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");

        for (int i = 0; i < trackIds.length; i++) {
            int trackId = Integer.parseInt(trackIds[i]);

            OrderTrack trackRecord = new OrderTrack();
            trackRecord.setId(trackId > 0 ? trackId : null);
            trackRecord.setOrder(order);
            trackRecord.setStatus(OrderStatus.valueOf(trackStatuses[i]));
            trackRecord.setNotes(trackNotes[i]);
            try {
                trackRecord.setUpdatedTime(dateFormatter.parse(trackDates[i]));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }

            orderTracks.add(trackRecord);
        }
    }
}