package com.bob.pay.domain.model.technician;

import com.bob.pay.domain.model.order.GeoPoint;

import java.util.List;

public record Technician(
        String id,
        String name,
        String level,
        double distanceKm,
        int servedOrders,
        int likedCount,
        int commentCount,
        String nextAvailableTime,
        boolean newcomer,
        String portraitUrl,
        GeoPoint location,
        List<String> serviceIds
) {
    public boolean canServe(String serviceId) {
        return serviceIds.contains(serviceId);
    }

    public Technician withDistance(double distanceKm) {
        return new Technician(id, name, level, distanceKm, servedOrders, likedCount, commentCount,
                nextAvailableTime, newcomer, portraitUrl, location, serviceIds);
    }

    public Technician withLocation(GeoPoint nextLocation) {
        return new Technician(id, name, level, distanceKm, servedOrders, likedCount, commentCount,
                nextAvailableTime, newcomer, portraitUrl, nextLocation, serviceIds);
    }

    public Technician withPortraitUrl(String nextPortraitUrl) {
        return new Technician(id, name, level, distanceKm, servedOrders, likedCount, commentCount,
                nextAvailableTime, newcomer, nextPortraitUrl, location, serviceIds);
    }
}
