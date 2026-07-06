package com.bob.pay.domain.model.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Order(
        String id,
        String merchantId,
        String merchantAccountId,
        String serviceId,
        String technicianId,
        Address address,
        GeoPoint customerLocation,
        GeoPoint technicianLocation,
        String serviceTime,
        BigDecimal travelFee,
        BigDecimal payableAmount,
        OrderStatus status,
        List<OrderTimelineItem> timeline,
        LocalDateTime createdAt
) {
    public Order moveTo(OrderStatus nextStatus, String message) {
        var nextTimeline = new java.util.ArrayList<>(timeline);
        nextTimeline.add(new OrderTimelineItem(nextStatus, message, LocalDateTime.now()));
        return new Order(
                id,
                merchantId,
                merchantAccountId,
                serviceId,
                technicianId,
                address,
                customerLocation,
                technicianLocation,
                serviceTime,
                travelFee,
                payableAmount,
                nextStatus,
                List.copyOf(nextTimeline),
                createdAt
        );
    }

    public Order updateCustomerLocation(GeoPoint location) {
        return new Order(id, merchantId, merchantAccountId, serviceId, technicianId, address, location,
                technicianLocation, serviceTime, travelFee, payableAmount, status, timeline, createdAt);
    }

    public Order updateTechnicianLocation(GeoPoint location) {
        return new Order(id, merchantId, merchantAccountId, serviceId, technicianId, address, customerLocation,
                location, serviceTime, travelFee, payableAmount, status, timeline, createdAt);
    }

    public record OrderTimelineItem(OrderStatus status, String message, LocalDateTime happenedAt) {
    }
}
